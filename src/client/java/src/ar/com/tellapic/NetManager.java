/**
 *   Copyright (c) 2010 Sebastián Treu.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 * @author
 *         Sebastian Treu 
 *         sebastian.treu(at)gmail.com
 *
 */  
package ar.com.tellapic;

import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ar.com.tellapic.chat.ChatController;
import ar.com.tellapic.chat.IChatController;
import ar.com.tellapic.chat.Message;
import ar.com.tellapic.graphics.Drawing;
import ar.com.tellapic.graphics.IPaintPropertyController;
import ar.com.tellapic.graphics.Tool;
import ar.com.tellapic.graphics.ToolBoxModel;
import ar.com.tellapic.graphics.ToolFactory;
import ar.com.tellapic.lib.ddata_t;
import ar.com.tellapic.lib.message_t;
import ar.com.tellapic.lib.stream_t;
import ar.com.tellapic.lib.stream_t_data;
import ar.com.tellapic.lib.svcontrol_t;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class NetManager extends Observable {
	
	private boolean connected;
	private int     fd;
	
	private static class Holder {
		private final static NetManager INSTANCE = new NetManager();
	}
	
	private NetManager() {
		this.addObserver(StatusBar.getInstance());
		connected = false;
		setFd(0);
	}
	
	/**
	 * 
	 * @return
	 */
	public static NetManager getInstance() {
		return Holder.INSTANCE;
	}

	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @param password
	 * @return
	 */
	public int connect(String host, int port, String name, String password) {
		/*
		 * A successful connection sequence is:
		 * 
		 * Server ------> CTL_SV_ID     >------> Client
		 * Server <-----< CTL_CL_PWD    <------- Client
		 * Server ------> CTL_SV_PWDOK  >------> Client
		 * Server <-----< CTL_CL_NAME   <------- Client
		 * Server ------> CTL_SV_AUTHOK >------> Client
		 */
		System.out.println("Connecting to "+host+":"+port);
		fd = tellapic.tellapic_connect_to(host, port);
		if (fd <= 0)
			return fd;
		
		stream_t stream = tellapic.tellapic_read_stream_b(fd);
		int cbyte = stream.getHeader().getCbyte();
		
		if (cbyte == tellapicConstants.CTL_FAIL || cbyte != tellapicConstants.CTL_SV_ID) {
			System.out.println("Something went wrong. Closing link.");
			tellapic.tellapic_close_fd(fd);
			return -1;
		}
		
		int id = stream.getData().getControl().getIdfrom();

		System.out.println("Sending password: "+password);
		tellapic.tellapic_send_ctle(fd, id, tellapicConstants.CTL_CL_PWD, password.length(), password);

		stream = tellapic.tellapic_read_stream_b(fd);
		cbyte = stream.getHeader().getCbyte();
		
		if (cbyte == tellapicConstants.CTL_FAIL)
			return -1;
		
		while(cbyte == tellapicConstants.CTL_SV_PWDFAIL) {
			//Retry password dialog here
			String response = JOptionPane.showInputDialog(Utils.msg.getString("wrongpassword"), null);
			System.out.println("Password was wrong. New pwd: "+response);
			if (response == null) {
				tellapic.tellapic_close_fd(fd);
				return -1;
			}
			
			tellapic.tellapic_send_ctle(fd, id, tellapicConstants.CTL_CL_PWD, response.length(), response);
			stream = tellapic.tellapic_read_stream_b(fd);
			cbyte = stream.getHeader().getCbyte();
		}

		if (cbyte != tellapicConstants.CTL_SV_PWDOK) {
			tellapic.tellapic_close_fd(fd);
			return -1;
		}
		
		System.out.println("Password was ok. Sending name: "+name);
		
		tellapic.tellapic_send_ctle(fd, stream.getData().getControl().getIdfrom(), tellapicConstants.CTL_CL_NAME, name.length(), name);
		stream = tellapic.tellapic_read_stream_b(fd);
		cbyte = stream.getHeader().getCbyte();
		
		if (cbyte == tellapicConstants.CTL_FAIL)
			return -1;
		
		while(cbyte == tellapicConstants.CTL_SV_NAMEINUSE) {
			//Retry name dialog here
			String response = JOptionPane.showInputDialog(Utils.msg.getString("nameinuse"), null);
			name = response;
			System.out.println("Name in use: "+response);
			if (response == null) {
				tellapic.tellapic_close_fd(fd);
				return -1;
			}
			
			tellapic.tellapic_send_ctle(fd, id, tellapicConstants.CTL_CL_NAME, response.length(), response);
			stream = tellapic.tellapic_read_stream_b(fd);
			cbyte = stream.getHeader().getCbyte();
		}
	
		if (cbyte != tellapicConstants.CTL_SV_AUTHOK)
			return -1;
		
		tellapic.tellapic_send_ctl(fd, id, tellapicConstants.CTL_CL_FILEASK);
		setConnected(true);
		
		SessionUtils.setUsername(name);
		SessionUtils.setId(id);
		SessionUtils.setServer(host);
		SessionUtils.setPort(port);
		SessionUtils.setPassword(password);
		ReceiverThread r = new ReceiverThread(fd);
		r.start();
		
		return fd;
	}
	

	private void setConnected(boolean value) {
		if (value != connected) {
			connected = value;
			this.setChanged();
			this.notifyObservers();
		}
	}
	
	
	/**
	 * 
	 * @param port
	 * @param filename
	 * @param name
	 * @param password
	 */
	public void startServer(int port, String filename, String name, String password) {
		
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @param fd the fd to set
	 */
	public void setFd(int fd) {
		this.fd = fd;
	}

	/**
	 * @return the fd
	 */
	public int getFd() {
		return fd;
	}
	
	
	/**
	 * 
	 */
	public void reconnect() {
		this.disconnect();
		this.connect(SessionUtils.getServer(), SessionUtils.getPort(), SessionUtils.getUsername(), SessionUtils.getPassword());
	}
	
	
	/**
	 * 
	 */
	private void disconnect() {
		tellapic.tellapic_send_ctl(fd, SessionUtils.getId(), tellapicConstants.CTL_CL_DISC);
		tellapic.tellapic_close_fd(fd);
		setConnected(false);
	}
	
	
	private class ReceiverThread extends Thread {
		private boolean running;
		private int     fd;
		
		public ReceiverThread(int fd) {
			running = false;
			this.fd = fd;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			stream_t stream = null;
			running = true;
			while(running && isConnected()) {
				stream = tellapic.tellapic_read_stream_b(fd);

				if (stream.getHeader().getCbyte() == tellapicConstants.CTL_FAIL || stream.getHeader().getCbyte() == tellapicConstants.CTL_NOPIPE) {
					setConnected(false);
					running = false;
					
				} else if (tellapic.tellapic_isfile(stream.getHeader()) == 1) {
					System.out.println("Was file: "+stream.getHeader().getCbyte());
					stream_t_data d = stream.getData();
					System.out.println("Was file1: "+stream.getHeader().getSsize());
					byte[] data = new byte[(int)stream.getHeader().getSsize()];
					tellapic.custom_wrap(d.getFile(), data, stream.getHeader().getSsize());

					ByteArrayInputStream in = new ByteArrayInputStream(data);
					try {
						SessionUtils.setSharedImage(ImageIO.read(in));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					tellapic.tellapic_free(stream);

				} else if (tellapic.tellapic_isctl(stream.getHeader()) == 1) {
					System.out.println("Was ctl");
					ManageCtlThread t = new ManageCtlThread(stream);
					t.start();

				} else if (tellapic.tellapic_isctle(stream.getHeader()) == 1) {
					System.out.println("Was ctl extended");
					ManageCtlExtendedThread t = new ManageCtlExtendedThread(stream);
					t.start();

				} else if ((tellapic.tellapic_ischatb(stream.getHeader()) == 1) || (tellapic.tellapic_ischatp(stream.getHeader()) == 1)) {
					System.out.println("Was chat");
					ManageChatThread t = new ManageChatThread(stream);
					t.start();

				} else if (tellapic.tellapic_isdrw(stream.getHeader()) == 1 ) {
					ddata_t    drawing    = stream.getData().getDrawing();
					createAndAddDrawing(drawing);

				} else if (tellapic.tellapic_isfig(stream.getHeader()) == 1) {
					System.out.println("Was fig");
					ddata_t    drawing    = stream.getData().getDrawing();
					createAndAddFigure(drawing);

				} else if (tellapic.tellapic_isfigtxt(stream) == 1) {
					System.out.println("Was text");

				}
			}
		}
		
		
		/**
		 * @param drawing
		 */
		private void createAndAddFigure(ddata_t drawingData) {
			/* Get the remote user who has drawn this figure */
			RemoteUser remoteUser = (RemoteUser) UserManager.getInstance().getUser(drawingData.getIdfrom());
			
			/* Get the drawing control byte from protocol */
			int dcbyte = drawingData.getDcbyte();
			
			/* Get the remote tool id he/she has used */
			int remoteTool = dcbyte & tellapicConstants.TOOL_MASK;
			
			/* Get the mouse and event protocol data used in this figure */
			int button   = dcbyte & tellapicConstants.EVENT_MASK;
			int eventExt = drawingData.getDcbyte_ext();
			
			/* Get the tool class name for this local user */
			String toolClassName = ToolFactory.getRegisteredToolsClassNames().get(remoteTool);
			
			/* Get the remote user paint controller */
			IPaintPropertyController c = remoteUser.getPaintController();
			ToolBoxModel toolBoxState  = remoteUser.getToolBoxModel();
			
			remoteUser.getToolboxController().selectToolByName(toolClassName.split("[a-z].*\\.")[1]);
			
			/* Create a color instance upon the remote color used */
			Color color = new Color(
					drawingData.getColor().getRed(),
					drawingData.getColor().getGreen(),
					drawingData.getColor().getBlue()
			);
			
			if ((remoteTool & tellapicConstants.TOOL_TEXT) == tellapicConstants.TOOL_TEXT) {
				c.handleFontSizeChange((int)drawingData.getWidth());
				c.handleFontStyleChange(drawingData.getType().getText().getStyle());
				c.handleTextChange(drawingData.getType().getText().getInfo());
				c.handleFontFaceChange(drawingData.getType().getText().getFace());

			} else {
				c.handleEndCapsChange(drawingData.getType().getFigure().getEndcaps());
				c.handleLineJoinsChange(drawingData.getType().getFigure().getLinejoin());
				c.handleOpacityChange(drawingData.getOpacity());
				c.handleWidthChange((int)drawingData.getWidth());
			}
			c.handleColorChange(color);
			
			int swingButton = convertToSwingButtonValue(button);
			int swingMask   = getSwingMask(swingButton, eventExt);
			
			Tool usedTool = toolBoxState.getLastUsedTool();
			avoidLoopback(usedTool);
			if (usedTool.hasAlphaProperties())
				usedTool.setAlpha(toolBoxState.getOpacityProperty());

			if (usedTool.hasColorProperties())
				usedTool.setColor(toolBoxState.getColorProperty());

			if (usedTool.hasStrokeProperties())
				usedTool.setStroke(toolBoxState.getStrokeProperty());

			if (usedTool.hasFontProperties())
				usedTool.setFont(toolBoxState.getFontProperty());

			usedTool.onPress(
					(int)drawingData.getPoint1().getX(),
					(int)drawingData.getPoint1().getY(),
					swingButton,
					swingMask
			);
			usedTool.onDrag(
					(int)drawingData.getType().getFigure().getPoint2().getX(),
					(int)drawingData.getType().getFigure().getPoint2().getY(),
					swingButton,
					swingMask
			);
			remoteUser.setTemporalDrawing(usedTool.getDrawing());

			Drawing drawing = usedTool.onRelease(
					(int)drawingData.getType().getFigure().getPoint2().getX(),
					(int)drawingData.getType().getFigure().getPoint2().getY(),
					swingButton,
					swingMask
			);
			
			if (drawing == null) 
				return;
			
			remoteUser.addDrawing(drawing);
			
//			createAndDispatchPressEvent(remoteUser, drawing, eventAndButton, eventExt);
//			createAndDispatchDragEvent(remoteUser, drawing, eventAndButton, eventExt);
//			createAndDispatchReleaseEvent(remoteUser, drawing, eventAndButton, eventExt);
			
			int x1 = (int)drawingData.getPoint1().getX();
			int y1 = (int)drawingData.getPoint1().getY();
			int x2 = (int)drawingData.getType().getFigure().getPoint2().getX();
			int y2 = (int)drawingData.getType().getFigure().getPoint2().getY();
			System.out.println("RECEIVED COORDS: ("+x1+","+y1+") ("+x2+","+y2+")");
			
		}

		/**
		 * @param user
		 */
		private void avoidLoopback(Tool usedTool) {
			try {
				Method avoidLoopback = usedTool.getClass().getMethod("setAvoidLoopback", boolean.class);
				try {
					avoidLoopback.invoke(usedTool, false);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}

		
		/**
		 * @param drawing
		 */
		private void createAndAddDrawing(ddata_t drawingData) {
			/* Get the remote user who is drawing */
			RemoteUser remoteUser = (RemoteUser) UserManager.getInstance().getUser(drawingData.getIdfrom());
			
			/* Get the Drawing Control Byte from the stream */
			int dcbyte = drawingData.getDcbyte();
			
			/* Get the tool used by the remote user */
			int remoteTool = dcbyte & tellapicConstants.TOOL_MASK;
			
			/* Get the event protocol data from the stream */
			int event  = dcbyte & tellapicConstants.EVENT_MASK;
			
			/* Get the button protocol data from the stream */
			int button = dcbyte & tellapicConstants.BUTTON_MASK;
			
			/* Get extra information about the event protocol data from the stream */
			int extDcbyte = drawingData.getDcbyte_ext();
			
			/* Get all the registered tools in this local user */
			Map<Integer, String> toolsClassNames = ToolFactory.getRegisteredToolsClassNames();
			
			/* With the tool used from the remote user, get the same tool in this local user */
			String     toolClassName   = toolsClassNames.get(remoteTool);

			/* Convert protocol mouse data to swing mouse data */
			int swingButton = convertToSwingButtonValue(button);
			int swingMask   = getSwingMask(swingButton, extDcbyte);
			
			/* Get the remote user paint controller to set paint properties */
			IPaintPropertyController c = remoteUser.getPaintController();
			
			/* Get the remote user tool box model */
			ToolBoxModel  toolBoxState = remoteUser.getToolBoxModel();
			
			/* Select the tool the remote user has used for this drawing and set its properties */
			remoteUser.getToolboxController().selectToolByName(toolClassName.split("[a-z].*\\.")[1]);
						
			/* Get an instance of the used tool */
			Tool usedTool = toolBoxState.getLastUsedTool();
			
			/* Avoid loopback information through the network. Each time a net tool is used */
			/* it sends data through the network if loopback is set to true.                */
			avoidLoopback(usedTool);
			
			switch(event) {

			case tellapicConstants.EVENT_PRESS:
				Color color = new Color(
						drawingData.getColor().getRed(),
						drawingData.getColor().getGreen(),
						drawingData.getColor().getBlue()
				);
				c.handleEndCapsChange(drawingData.getType().getFigure().getEndcaps());
				c.handleLineJoinsChange(drawingData.getType().getFigure().getLinejoin());
				c.handleOpacityChange(drawingData.getOpacity());
				c.handleWidthChange((int)drawingData.getWidth());
				c.handleColorChange(color);
				if (usedTool.hasAlphaProperties())
					usedTool.setAlpha(toolBoxState.getOpacityProperty());

				if (usedTool.hasColorProperties())
					usedTool.setColor(toolBoxState.getColorProperty());

				if (usedTool.hasStrokeProperties())
					usedTool.setStroke(toolBoxState.getStrokeProperty());

				if (usedTool.hasFontProperties())
					usedTool.setFont(toolBoxState.getFontProperty());
				
				if (usedTool.hasColorProperties())
					usedTool.setColor(toolBoxState.getColorProperty());
				
				usedTool.onPress(
						(int)drawingData.getPoint1().getX(),
						(int)drawingData.getPoint1().getY(),
						swingButton,
						swingMask
				);
				
				//createAndDispatchPressEvent(remoteUser, drawingData, eventAndButton, eventExtMod);
				break;

			case tellapicConstants.EVENT_DRAG:
				usedTool.onDrag(
						(int)drawingData.getPoint1().getX(), 
						(int)drawingData.getPoint1().getY(), 
						swingButton,
						swingMask
				);
				remoteUser.setTemporalDrawing(usedTool.getDrawing());
				//createAndDispatchDragEvent(remoteUser, drawingData, eventAndButton, eventExtMod);
				break;

			case tellapicConstants.EVENT_RELEASE:
				Drawing drawing = usedTool.onRelease(
						(int)drawingData.getPoint1().getX(),
						(int)drawingData.getPoint1().getY(),
						swingButton,
						swingMask
				);
				
				if (drawing == null) 
					return;
				
				remoteUser.addDrawing(drawing);
				
				//createAndDispatchReleaseEvent(remoteUser, drawingData, eventAndButton, eventExtMod);
				break;
			}
		}

//		/**
//		 * @param remoteUser
//		 * @param drawing
//		 * @param button1
//		 */
//		private void createAndDispatchReleaseEvent(RemoteUser remoteUser, ddata_t drawing, int eventAndButton, int mod) {
//			int x = 0;
//			int y = 0;
//			if ((eventAndButton & tellapicConstants.EVENT_MASK) == tellapicConstants.EVENT_NULL) {
//				x = (int) drawing.getType().getFigure().getPoint2().getX();
//				y = (int) drawing.getType().getFigure().getPoint2().getY();
//			} else {
//				x = (int)drawing.getPoint1().getX();
//				y = (int)drawing.getPoint1().getY();
//			}
//			
//			int button = getButtonFromEvent(eventAndButton);
//			//int mask   = getMaskForButton(button, mod);
//			
//			RemoteMouseEvent event = new RemoteMouseEvent(
//					remoteUser,
//					DrawingAreaView.getInstance(),
//					502,
//					System.currentTimeMillis(),
//					MouseEvent.NOBUTTON,
//					x,
//					y,
//					0,
//					false,
//					button
//			);
//			DrawingAreaView.getInstance().dispatchEvent(event);
//			
//		}
//
//		/**
//		 * @param remoteUser 
//		 * @param drawing
//		 * @param buttonFromEvent
//		 */
//		private void createAndDispatchDragEvent(RemoteUser remoteUser, ddata_t drawing, int eventAndButton, int mod) {
//			int x = 0;
//			int y = 0;
//			int button = getButtonFromEvent(eventAndButton);
//			int mask = getMaskForButton(button, mod);
//
//			if ((eventAndButton & tellapicConstants.EVENT_MASK) == tellapicConstants.EVENT_NULL) {
//				x = (int) drawing.getType().getFigure().getPoint2().getX();
//				y = (int) drawing.getType().getFigure().getPoint2().getY();
//			} else {
//				x = (int)drawing.getPoint1().getX();
//				y = (int)drawing.getPoint1().getY();
//			}
//			
//			RemoteMouseEvent event = new RemoteMouseEvent(
//					remoteUser,
//					DrawingAreaView.getInstance(),
//					506,
//					System.currentTimeMillis(),
//					mask,
//					x,
//					y,
//					0,
//					false,
//					MouseEvent.NOBUTTON
//			);
//			Utils.printEventInfo(event);
//			DrawingAreaView.getInstance().dispatchEvent(event);
//		}
//
//		
//		/**
//		 * @param remoteUser 
//		 * @param drawing
//		 */
//		private void createAndDispatchPressEvent(RemoteUser remoteUser, ddata_t drawing, int eventAndButton, int mod) {
//			int button = getButtonFromEvent(eventAndButton);
//			int mask   = getMaskForButton(button, mod);
//			
//			RemoteMouseEvent event = new RemoteMouseEvent(
//					remoteUser,
//					DrawingAreaView.getInstance(),
//					501,
//					System.currentTimeMillis(),
//					mask,
//					(int)drawing.getPoint1().getX(),
//					(int)drawing.getPoint1().getY(),
//					1,
//					false,
//					button
//			);
//			DrawingAreaView.getInstance().dispatchEvent(event);
//		}
		
		
		/**
		 * @param button
		 * @param mod
		 * @return
		 */
		private int getSwingMask(int swingButton, int dcbyte_ext) {
			int mask   = 0;
			int modext = 0;
			
			if (swingButton == MouseEvent.BUTTON1)
				mask = InputEvent.BUTTON1_DOWN_MASK;
			else if (swingButton == MouseEvent.BUTTON2)
				mask = InputEvent.BUTTON2_DOWN_MASK;
			else
				mask = InputEvent.BUTTON3_DOWN_MASK;

			if (dcbyte_ext == tellapicConstants.EVENT_CTL_DOWN)
				modext = InputEvent.CTRL_DOWN_MASK;
			else if (dcbyte_ext == tellapicConstants.EVENT_ALT_DOWN)
				modext = InputEvent.ALT_DOWN_MASK;
			else if (dcbyte_ext == tellapicConstants.EVENT_SHIFT_DOWN)
				modext = InputEvent.SHIFT_DOWN_MASK;
			else
				modext = 0;

			return mask | modext;
		}

		
		/*
		 * 
		 */
		private int convertToSwingButtonValue(int button) {
			if (button == tellapicConstants.BUTTON_LEFT)
				return MouseEvent.BUTTON1;
			else if (button == tellapicConstants.BUTTON_RIGHT)
				return MouseEvent.BUTTON2;
			else 
				return MouseEvent.BUTTON3;
		}
	}
	
	
	private class ManageCtlThread extends Thread {
		private stream_t stream;
		
		public ManageCtlThread(stream_t stream) {
			this.stream = stream;
		}
		
		@Override
		public void run() {
			int cbyte = stream.getHeader().getCbyte();
			switch(cbyte) {
			case tellapicConstants.CTL_SV_CLRM:
				int id = stream.getData().getControl().getIdfrom();
				UserManager.getInstance().delUser(id);
				break;
				
			case tellapicConstants.CTL_CL_DISC:
				setConnected(false);
				break;
			}
		}
	}
	
	private class ManageCtlExtendedThread extends Thread {
		private stream_t stream;
		
		public ManageCtlExtendedThread(stream_t stream) {
			this.stream = stream;
		}
		
		@Override
		public void run() {
			int cbyte = stream.getHeader().getCbyte();
			switch(cbyte) {
				
			case tellapicConstants.CTL_SV_CLADD:
				svcontrol_t ctle = stream.getData().getControl();
				int id = ctle.getIdfrom();
				//String name = tellapic.tellapic_bytetp2charp(ctle.getInfo());
				short[] s = ctle.getInfo();
				String name = "";
				for(int i =0 ; s[i] != 0; i++)
					name += (char)s[i];
				UserManager.getInstance().addUser(id, name);
				
			case tellapicConstants.CTL_SV_FILE:
				break;
			}
		}
	}
	
	private class ManageChatThread extends Thread {
		private stream_t stream;
		private IChatController chatController; 
		
		public ManageChatThread(stream_t stream) {
			this.stream = stream;
			chatController = new ChatController();
		}

		@Override
		public void run() {
			Message   message = null;
			String    text    = null;
			message_t chatmsg = stream.getData().getChat();
			int       idFrom  = chatmsg.getIdfrom();
			int       cbyte   = stream.getHeader().getCbyte();
			int       idTo    = 0;
			AbstractUser userFrom = UserManager.getInstance().getUser(idFrom);
			
			switch(cbyte) {
			
			case tellapicConstants.CTL_CL_BMSG:
				text    = chatmsg.getType().getBroadmsg();
				
				message = new Message(userFrom.getName(), null, text);
				break;
				
			case tellapicConstants.CTL_CL_PMSG:
				idTo = chatmsg.getType().getPrivmsg().getIdto();
				text = chatmsg.getType().getPrivmsg().getText();
				AbstractUser userTo = UserManager.getInstance().getUser(idTo);
				message = new Message(
						userFrom.getName(),
						userTo.getName(),
						text
				);
			}
			chatController.handleInput(message, false);
		}
	}
	
	@SuppressWarnings("unused")
	private class ManageDrawingThread extends Thread {
		private stream_t stream;
		
		public ManageDrawingThread(stream_t stream) {
			this.stream = stream;
		}
		
		@Override
		public void run() {
			int cbyte = stream.getHeader().getCbyte();
			
			switch(cbyte) {
			
			case tellapicConstants.CTL_CL_FIG:
				
				break;
				
			case tellapicConstants.CTL_CL_DRW:
				break;
			}
		}
	}

	/**
	 * @return
	 */
	public int getPing() {
		return 0;
	}
}
