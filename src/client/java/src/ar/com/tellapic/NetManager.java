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

import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ar.com.tellapic.chat.ChatController;
import ar.com.tellapic.chat.IChatController;
import ar.com.tellapic.chat.Message;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.graphics.IPaintPropertyController;
import ar.com.tellapic.graphics.RemoteMouseEvent;
import ar.com.tellapic.graphics.ToolFactory;
import ar.com.tellapic.lib.ddata_t;
import ar.com.tellapic.lib.message_t;
import ar.com.tellapic.lib.stream_t;
import ar.com.tellapic.lib.stream_t_data;
import ar.com.tellapic.lib.svcontrol_t;
import ar.com.tellapic.lib.tellapic;

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
		this.addObserver(SimpleStatusBar.getInstance());
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
		
		if (cbyte == tellapic.CTL_FAIL || cbyte != tellapic.CTL_SV_ID) {
			System.out.println("Something went wrong. Closing link.");
			tellapic.tellapic_close_fd(fd);
			return -1;
		}
		
		int id = stream.getData().getControl().getIdfrom();

		System.out.println("Sending password: "+password);
		tellapic.tellapic_send_ctle(fd, id, tellapic.CTL_CL_PWD, password.length(), password);

		stream = tellapic.tellapic_read_stream_b(fd);
		cbyte = stream.getHeader().getCbyte();
		
		if (cbyte == tellapic.CTL_FAIL)
			return -1;
		
		while(cbyte == tellapic.CTL_SV_PWDFAIL) {
			//Retry password dialog here
			String response = JOptionPane.showInputDialog(Utils.msg.getString("wrongpassword"), null);
			System.out.println("Password was wrong. New pwd: "+response);
			if (response == null) {
				tellapic.tellapic_close_fd(fd);
				return -1;
			}
			
			tellapic.tellapic_send_ctle(fd, id, tellapic.CTL_CL_PWD, response.length(), response);
			stream = tellapic.tellapic_read_stream_b(fd);
			cbyte = stream.getHeader().getCbyte();
		}

		if (cbyte != tellapic.CTL_SV_PWDOK) {
			tellapic.tellapic_close_fd(fd);
			return -1;
		}
		
		System.out.println("Password was ok. Sending name: "+name);
		
		tellapic.tellapic_send_ctle(fd, stream.getData().getControl().getIdfrom(), tellapic.CTL_CL_NAME, name.length(), name);
		stream = tellapic.tellapic_read_stream_b(fd);
		cbyte = stream.getHeader().getCbyte();
		
		if (cbyte == tellapic.CTL_FAIL)
			return -1;
		
		while(cbyte == tellapic.CTL_SV_NAMEINUSE) {
			//Retry name dialog here
			String response = JOptionPane.showInputDialog(Utils.msg.getString("nameinuse"), null);
			name = response;
			System.out.println("Name in use: "+response);
			if (response == null) {
				tellapic.tellapic_close_fd(fd);
				return -1;
			}
			
			tellapic.tellapic_send_ctle(fd, id, tellapic.CTL_CL_NAME, response.length(), response);
			stream = tellapic.tellapic_read_stream_b(fd);
			cbyte = stream.getHeader().getCbyte();
		}
	
		if (cbyte != tellapic.CTL_SV_AUTHOK)
			return -1;
		
		tellapic.tellapic_send_ctl(fd, id, tellapic.CTL_CL_FILEASK);
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
			while(running) {
				stream = tellapic.tellapic_read_stream_b(fd);
				System.out.println("something read");

				if (stream.getHeader().getCbyte() != tellapic.CTL_FAIL) {
					
					if (tellapic.tellapic_isfile(stream.getHeader()) == 1) {
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
						System.out.println("Was drawing");

					} else if (tellapic.tellapic_isfig(stream.getHeader()) == 1) {
						System.out.println("Was fig");
						ddata_t drawing = stream.getData().getDrawing();
						RemoteUser remoteUser = (RemoteUser) UserManager.getInstance().getUser(stream.getData().getDrawing().getIdfrom());
						int remoteTool = drawing.getDcbyte() & tellapic.TOOL_MASK;
						String toolClassName = ToolFactory.getRegisteredToolNames().get(remoteTool);
						remoteUser.getToolboxController().selectToolByName(toolClassName.split("[a-z].*\\.")[1]);
						IPaintPropertyController c = remoteUser.getPaintController();
						c.handleEndCapsChange(drawing.getType().getFigure().getEndcaps());
						c.handleLineJoinsChange(drawing.getType().getFigure().getLinejoin());
						c.handleOpacityChange(drawing.getOpacity());
						c.handleWidthChange((int)drawing.getWidth());
						
						RemoteMouseEvent event = new RemoteMouseEvent(
								remoteUser,
								DrawingAreaView.getInstance(),
								501,
								System.currentTimeMillis(),
								MouseEvent.BUTTON1_DOWN_MASK,
								(int)stream.getData().getDrawing().getPoint1().getX(),
								(int)stream.getData().getDrawing().getPoint1().getY(),
								1,
								false,
								MouseEvent.BUTTON1
						);
						DrawingAreaView.getInstance().dispatchEvent(event);
						event = new RemoteMouseEvent(
								remoteUser,
								DrawingAreaView.getInstance(),
								506,
								System.currentTimeMillis(),
								MouseEvent.BUTTON1_DOWN_MASK,
								(int)stream.getData().getDrawing().getType().getFigure().getPoint2().getX(),
								(int)stream.getData().getDrawing().getType().getFigure().getPoint2().getY(),
								0,
								false,
								MouseEvent.NOBUTTON);
						DrawingAreaView.getInstance().dispatchEvent(event);
						event = new RemoteMouseEvent(
								remoteUser,
								DrawingAreaView.getInstance(),
								502,
								System.currentTimeMillis(),
								MouseEvent.NOBUTTON,
								(int)stream.getData().getDrawing().getType().getFigure().getPoint2().getX(),
								(int)stream.getData().getDrawing().getType().getFigure().getPoint2().getY(),
								0,
								false,
								MouseEvent.BUTTON1);
						DrawingAreaView.getInstance().dispatchEvent(event);
						int x1 = (int)stream.getData().getDrawing().getType().getFigure().getPoint2().getX();
						int x2 = (int)stream.getData().getDrawing().getType().getFigure().getPoint2().getY();
						int y1 = (int)stream.getData().getDrawing().getType().getFigure().getPoint2().getX();
						int y2 = (int)stream.getData().getDrawing().getType().getFigure().getPoint2().getY();
						System.out.println("RECEIVED COORDS: ("+x1+","+y1+") ("+x2+","+y2+")");
						
					} else if (tellapic.tellapic_isfigtxt(stream) == 1) {
						System.out.println("Was text");

					}
				} else {
					setConnected(false);
					running = false;
				}
			}
		}
	}
	
	
	private class ManageCtlThread extends Thread {
		private stream_t stream;
		
		public ManageCtlThread(stream_t stream) {
			this.stream = stream;
		}
		
		public void run() {
			int cbyte = stream.getHeader().getCbyte();
			switch(cbyte) {
			case tellapic.CTL_SV_CLRM:
				int id = stream.getData().getControl().getIdfrom();
				UserManager.getInstance().delUser(id);
				break;
				
			case tellapic.CTL_CL_DISC:
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
		
		public void run() {
			int cbyte = stream.getHeader().getCbyte();
			switch(cbyte) {
				
			case tellapic.CTL_SV_CLADD:
				svcontrol_t ctle = stream.getData().getControl();
				int id = ctle.getIdfrom();
				//String name = tellapic.tellapic_bytetp2charp(ctle.getInfo());
				String name = ctle.getInfo().toString();
				UserManager.getInstance().addUser(id, name);
				
			case tellapic.CTL_SV_FILE:
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

		public void run() {
			Message   message = null;
			String    text    = null;
			message_t chatmsg = stream.getData().getChat();
			int       idFrom  = chatmsg.getIdfrom();
			int       cbyte   = stream.getHeader().getCbyte();
			int       idTo    = 0;
			AbstractUser userFrom = UserManager.getInstance().getUser(idFrom);
			
			switch(cbyte) {
			
			case tellapic.CTL_CL_BMSG:
				text    = chatmsg.getType().getBroadmsg();
				
				message = new Message(userFrom.getName(), null, text);
				break;
				
			case tellapic.CTL_CL_PMSG:
				AbstractUser userTo = UserManager.getInstance().getUser(idTo);
				idTo = chatmsg.getType().getPrivmsg().getIdto();
				text = chatmsg.getType().getPrivmsg().getText();
				message = new Message(
						userFrom.getName(),
						userTo.getName(),
						text
				);
			}
			chatController.handleInput(message, false);
		}
	}
	
	private class ManageDrawingThread extends Thread {
		private stream_t stream;
		
		public ManageDrawingThread(stream_t stream) {
			this.stream = stream;
		}
		
		public void run() {
			int cbyte = stream.getHeader().getCbyte();
			
			switch(cbyte) {
			
			case tellapic.CTL_CL_FIG:
				
				break;
				
			case tellapic.CTL_CL_DRW:
				break;
			}
		}
	}

	/**
	 * 
	 */
	public void reconnect() {
		this.connect(SessionUtils.getServer(), SessionUtils.getPort(), SessionUtils.getUsername(), SessionUtils.getPassword());
	}
}
