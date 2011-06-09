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
import java.util.Observable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ar.com.tellapic.chat.ChatClientModel;
import ar.com.tellapic.chat.Message;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.graphics.DrawingShape;
import ar.com.tellapic.graphics.IPaintPropertyController;
import ar.com.tellapic.graphics.IToolBoxController;
import ar.com.tellapic.graphics.RemoteMouseEvent;
import ar.com.tellapic.graphics.Tool;
import ar.com.tellapic.graphics.ToolFactory;
import ar.com.tellapic.lib.ddata_t;
import ar.com.tellapic.lib.header_t;
import ar.com.tellapic.lib.message_t;
import ar.com.tellapic.lib.stream_t;
import ar.com.tellapic.lib.stream_t_data;
import ar.com.tellapic.lib.svcontrol_t;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.lib.tellapic_socket_t;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class NetManager extends Observable implements Runnable {
	public static final String CONNECTING_STRING = "Connecting";
	public static final String DISCONNECTED_STRING = "Disconnected";
	public static final String CONNECTED_STRING = "Connected";
	private static final int CONNECTION_STEPS = 3;
	private static final int USER_CANCELLED_ERROR = -1;
	private static final int AUTHENTICATION_ERROR = -2;
	private static final int FILE_ERROR = -3;
	private static final int CONNECTION_ERROR = -4; 
	private static final int AUTHENTICATION_OK = 0;
	private static final int FILE_OK = 1;
	static final int CONNECTION_OK = 1;
	
	
	private int               monitorStep;
	private double            ping;
	private double            pingSentTime;
	private boolean           connected;
	private boolean           isRunning;
	private tellapic_socket_t socket;
	private int               id;
	private boolean           connecting;
	private ProgressMonitor   monitor;
	private Thread            netManager;
	private Thread            pinger;
	final private Lock        lock;
	final private Lock        pingerLock;
	final private Condition   pingCondition;
	final private Condition   connectedCondition;
	protected ProgressDialog  progressDialog;
	
	private static class Holder {
		private final static NetManager INSTANCE = new NetManager();
	}
	
	private NetManager() {
		addObserver(StatusBar.getInstance());
		connected = false;
		isRunning = false;
		connecting = false;
		lock = new ReentrantLock();
		pingerLock = new ReentrantLock();
		connectedCondition  = lock.newCondition();
		pingCondition  = pingerLock.newCondition();
		netManager = new Thread(this);
		netManager.start();
	}
	
	
	/**
	 * 
	 */
	public void run() {
		isRunning = true;
		while(isRunning) {
			try {
				//Utils.logMessage("RUNNING NETMANAGER THREAD");
				lock.lock();
				if (isConnected()) {
					receiveAndProcess();
				} else {
					Utils.logMessage("WAITING NETMANAGER THREAD");
					connectedCondition.await();
				}
			} catch(InterruptedException e) {
				Utils.logMessage("netmanager thread interrupted");
			} finally {
				lock.unlock();
			}
		}
		
		if (pinger != null && pinger.isAlive()) {
			pinger.interrupt();
			try {
				pinger.join(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Utils.logMessage("DYING NETMANAGER THREAD");
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
	 * @param password
	 * @param step
	 * @return
	 * @throws WrongPacketException
	 */
	private int authenticate(String password, String name) throws WrongPacketException {
		stream_t stream;
		short    cbyte;
		
		/* Set the current progress */
		monitor.setCurrent("Authenticating...", monitorStep++);
		
		do {
			/* Send the password to the server */
			tellapic.tellapic_send_ctle(socket, id, tellapicConstants.CTL_CL_PWD, password.length(), password);

			/* Read server response */
			stream = tellapic.tellapic_read_stream_b(socket);
			cbyte  = stream.getHeader().getCbyte();

			/* Check for an invalid sequence/packet */
			if (cbyte == tellapicConstants.CTL_FAIL) {
				/* Dispose the monitor */
				monitor.setCurrent(null,  monitor.getTotal());

				/* Close the connection */
				tellapic.tellapic_close_socket(socket);

				throw new WrongPacketException("Response fail by server. Packet shouln't be CTL_FAIL.");
			}

			/* If we failed the password, re-send it */
			if (cbyte == tellapicConstants.CTL_SV_PWDFAIL) {

				/* Show a retry dialog for the password */ 
				String dialogInput = null;
				do {
					dialogInput = JOptionPane.showInputDialog(progressDialog, Utils.msg.getString("wrongpassword"), null);

					System.out.println("Password was wrong. New pwd: "+dialogInput);

					/* If response was null, user has canceled the Dialog */
					if (dialogInput == null) {
						/* Dispose the monitor */
						monitor.setCurrent(null,  monitor.getTotal());

						/* Close the connection */
						tellapic.tellapic_close_socket(socket);

						/* Return the error */
						return USER_CANCELLED_ERROR;
					}

					password = dialogInput;
				} while(dialogInput.length() <= 0);
			}
		} while (cbyte == tellapicConstants.CTL_SV_PWDFAIL);
		
		/* Check for a correct password */
		if (cbyte != tellapicConstants.CTL_SV_PWDOK) {
			/* Dispose the monitor as we failed the required times to login */
			monitor.setCurrent(null, monitor.getTotal());
			
			/* Close the connection */
			tellapic.tellapic_close_socket(socket);
			
			/* Inform the error */
			return AUTHENTICATION_ERROR;
		}
		
		System.out.println("Password was ok.");
		int id = stream.getData().getControl().getIdfrom();
		
		do {
			System.out.println("Trying new name: "+name+" with length: "+name.length());
			/* Send the user name packet to the server */
			tellapic.tellapic_send_ctle(socket, id, tellapicConstants.CTL_CL_NAME, name.length(), name);

			/* Read server response */
			stream = tellapic.tellapic_read_stream_b(socket);
			cbyte = stream.getHeader().getCbyte();
				
			/* Check for an invalid sequence/packet */
			if (cbyte == tellapicConstants.CTL_FAIL) {
				/* Dispose the monitor */
				monitor.setCurrent(null, monitor.getTotal());

				/* Close the connection */
				tellapic.tellapic_close_socket(socket);
				throw new WrongPacketException("Wrong packet sequence. Shouldn't be CTL_FAIL.");
			}
			
			/* If the name is in use, show a dialog to choose a different user name */
			if(cbyte == tellapicConstants.CTL_SV_NAMEINUSE ) {
				System.out.println("Name "+name+" is in use.");
				String dialogInput = null;
				do {
					dialogInput = JOptionPane.showInputDialog(progressDialog, Utils.msg.getString("nameinuse"), null);
					
					/* If the user has cancelled the dialog... */
					if (dialogInput == null) {
						/* Dispose the monitor */
						monitor.setCurrent(null, monitor.getTotal());

						/* and close the connection */
						tellapic.tellapic_close_socket(socket);

						/* Inform the error. */
						return USER_CANCELLED_ERROR;
					}

					name = dialogInput;
				} while(dialogInput.length() <= 0);
			}
		} while(cbyte == tellapicConstants.CTL_SV_NAMEINUSE);
		
		/* Do we auth ok? */
		if (cbyte != tellapicConstants.CTL_SV_AUTHOK) {
			/* Dispose the monitor */
			monitor.setCurrent(null, monitor.getTotal());
			
			/* Close the connection */
			tellapic.tellapic_close_socket(socket);
			
			/* Inform the error */
			return AUTHENTICATION_ERROR;
		}
		SessionUtils.setUsername(name);
		SessionUtils.setPassword(password);
		SessionUtils.setId(id);
		
		return AUTHENTICATION_OK;
	}
	
	
	/**
	 * 
	 * @return
	 * @throws WrongPacketException
	 */
	private int askForFile() throws WrongPacketException{
		header_t header;
		short    cbyte;
		
		monitor.setCurrent("Asking image file...", monitorStep++);
		
		/* Send the packet */
		tellapic.tellapic_send_ctl(socket, id, tellapicConstants.CTL_CL_FILEASK);
		
		/* Read server response */
		header = tellapic.tellapic_read_header_b(socket);
		cbyte  = header.getCbyte();
		
		/* Check for an invalid sequence/packet */
		if (cbyte != tellapicConstants.CTL_SV_FILE) {
			/* Dispose the dialog */
			monitor.setCurrent(null, monitor.getTotal());
			
			/* Close the connection */
			tellapic.tellapic_close_socket(socket);
			throw new WrongPacketException("Wrong sequence while asking file.");
		}
		
		int dataSize = (int) header.getSsize() - tellapicConstants.HEADER_SIZE;
		int chunkSize =  2048;
		
		byte[]  data = new byte[(int) dataSize];
		int     read = 0;
		int     i = 0;
		int     j = 0;
		int completed = 0;
		
		monitor.changeTotal((int) (Math.ceil((double)dataSize/chunkSize) + CONNECTION_STEPS));
		
//		PrintWriter pm = null;
		//			pm = new PrintWriter("/home/seba/debug2.txt");
//			
		while(read < dataSize) {
			
			int    size = (read + chunkSize < dataSize) ? chunkSize : dataSize - read;
			byte[] temp = new byte[size];
			
			tellapic.custom_wrap(tellapic.tellapic_read_bytes_b(socket, size), temp, size);

			for(j = 0; j < size; j++) {
				data[i * chunkSize + j] = temp[j];
				//System.out.println("data["+(i * chunkSize + j)+"]: "+data[i * chunkSize + j]);
//					pm.println(data[i * chunkSize + j]);
			}
			read += j;
			i++;
			completed = (int) (((float)read/(float)dataSize) * 100);

			monitor.setCurrent("Downloading file: "+completed+"%", monitorStep++);
		}
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		
		try {
			SessionUtils.setSharedImage(ImageIO.read(in));
			in.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			/* Dispose the monitor */
			monitor.setCurrent(null, monitor.getTotal());
			
			/* Close the connection */
			tellapic.tellapic_close_socket(socket);
			
			return FILE_ERROR;
		}
		
		return FILE_OK;
//		tellapic.tellapic_free(stream);
	}
	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @param password
	 * @return
	 * @throws WrongPacketException 
	 */
	public synchronized int connect(String host, String port, String name, String password) throws WrongPacketException {
		/*
		 * A successful connection sequence is:
		 * 
		 * Server ------> CTL_SV_ID     >------> Client
		 * Server <-----< CTL_CL_PWD    <------- Client
		 * Server ------> CTL_SV_PWDOK  >------> Client
		 * Server <-----< CTL_CL_NAME   <------- Client
		 * Server ------> CTL_SV_AUTHOK >------> Client
		 */
		
		int      cbyte;
		int      error;
		stream_t stream;
		System.out.println(SwingUtilities.isEventDispatchThread());
		
		monitor = new ProgressMonitor(CONNECTION_STEPS, false, 200);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				frame.setIconImage(Utils.createIconImage(112, 75, "/icons/system/logo_small.png"));
				progressDialog = new ProgressDialog(frame, monitor);
				progressDialog.pack();
				progressDialog.setLocationRelativeTo(null); 
				progressDialog.setVisible(true);
			}
		});
		
		monitorStep = 0;
		
		/* We are connecting, set this state */
		connecting = true;
		monitor.start("Connecting to '"+host+":"+port);
		socket = tellapic.tellapic_connect_to(host, port);
		if (tellapic.tellapic_valid_socket(socket) == 0) {
			/* Dispose the Dialog */
			monitor.setCurrent(null, monitor.getTotal());
			stop();
			return CONNECTION_ERROR;
		}
		stream = tellapic.tellapic_read_stream_b(socket);
		cbyte  = stream.getHeader().getCbyte();
		if (cbyte == tellapicConstants.CTL_FAIL || cbyte != tellapicConstants.CTL_SV_ID) {
			/* Dispose the Dialog */
			monitor.setCurrent(null, monitor.getTotal());
			/* Close the connection */
			stop();
			throw new WrongPacketException("Wrong response by server upon connection packet.");
		}
		/* Set the session id */
		id = stream.getData().getControl().getIdfrom();
		SessionUtils.setId(id);
		SessionUtils.setServer(host);
		SessionUtils.setPort(port);

		/* Authenticate with the server */
		if ((error = authenticate(password, name)) < 0) {
			stop();
			return error;
		}
		
		/* Ask for file */
		if ((error = askForFile()) < 0) {
			stop();
			return error;
		}
		
		monitor.setCurrent("Downloading done!", monitorStep++);
		setConnected(true);
		
		pinger = new Thread(new Runnable(){
			@Override
			public void run() {
				while(isConnected()) {
					try {
						pingerLock.lock();
						tellapic.tellapic_send_ctl(socket, SessionUtils.getId(), tellapic.CTL_CL_PING);
						pingSentTime = System.nanoTime();
						pingCondition.await();
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						System.out.println("Pinger Thread Interrupted, it will die if netmanager not connected.");
					} finally {
						pingerLock.unlock();
					}
				}
				System.out.println("Pinger thread dying...");
			}
		});
		pinger.start();
		monitor.setCurrent("Starting network thread", monitorStep++);
		
		/* JNI library will return a non-zero to inform True. We use negative numbers */
		/* to inform an error and we can't assure that the function bellow will return */
		/* a positive number. We just know that if it's not zero, then everything went fine */
		if (tellapic.tellapic_valid_socket(socket) != 0)
			return CONNECTION_OK;
		else {
			stop();
			return CONNECTION_ERROR;
		}
	}
	
	
	/**
	 * @return
	 */
	public double getPing() {
		return ping;
	}

	/**
	 * 
	 * @return
	 */
	public String getStatus() {
		if (connected)
			return CONNECTED_STRING;
		else if (connecting)
			return CONNECTING_STRING;
		else
			return DISCONNECTED_STRING;
	}
	
	
	/**
	 * @param pingTime the ping to set
	 */
	public void setPing(double pingTime) {
		this.ping = pingTime;
		setChanged();
		notifyObservers();
	}

	/**
	 * 
	 * @param value
	 */
	private void setConnected(boolean isConnected) {
		if (isConnected != connected) {
			connected = isConnected;
			setChanged();
			notifyObservers();
			if (isConnected) {
				connecting = false;
				try {
					lock.lock();
					connectedCondition.signal();
				} finally {
					lock.unlock();
				}
			}
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
	 * @param socket the socket to set
	 */
	public void setSocket(tellapic_socket_t socket) {
		this.socket = socket;
	}
	
	
	/**
	 * @return the socket
	 */
	public tellapic_socket_t getSocket() {
		return socket;
	}
	
	
	/**
	 * 
	 */
	public synchronized void reconnect() {
		disconnect();
		try {
			Utils.logMessage("Joining pinger...");
			pinger.join(5000);
			Utils.logMessage("Pinger joined.");
			connect(SessionUtils.getServer(), SessionUtils.getPort(), SessionUtils.getUsername(), SessionUtils.getPassword());
		} catch (WrongPacketException e) {
			e.printStackTrace();
			connecting = false;
			setConnected(false);
			stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
			connecting = false;
			setConnected(false);
			stop();
		}
	}
	
	
	/**
	 * 
	 */
	public synchronized void disconnect() {
		tellapic.tellapic_send_ctl(socket, SessionUtils.getId(), tellapicConstants.CTL_CL_DISC);
		tellapic.tellapic_close_socket(socket);
		setConnected(false);
	}
	
	
	/**
	 * 
	 */
	private void receiveAndProcess() {
		stream_t stream = null;
		header_t header = null;
//		Utils.logMessage("receiveAnProcess block..");
		stream = tellapic.tellapic_read_stream_b(socket);
		header = stream.getHeader();
	
		if (header.getCbyte() == tellapicConstants.CTL_FAIL || header.getCbyte() == tellapicConstants.CTL_NOPIPE) {
			setConnected(false);
			
		} else if (tellapic.tellapic_isfile(header) == 1) {
			Utils.logMessage("Was file: "+header.getCbyte());
			stream_t_data d = stream.getData();
			byte[] data = new byte[(int)header.getSsize()];
			tellapic.custom_wrap(d.getFile(), data, header.getSsize());

			ByteArrayInputStream in = new ByteArrayInputStream(data);
			try {
				SessionUtils.setSharedImage(ImageIO.read(in));
			} catch (IOException e) {
				e.printStackTrace();
				disconnect();
			}
			tellapic.tellapic_free(stream);

		} else if (tellapic.tellapic_isctl(header) == 1) {
			System.out.println("Was ctl");
			switch(header.getCbyte()) {
			case tellapicConstants.CTL_SV_CLRM:
				int id = stream.getData().getControl().getIdfrom();
				UserManager.getInstance().delUser(id);
				break;

			case tellapicConstants.CTL_CL_DISC:
				setConnected(false);
				break;
			}
			
		} else if (tellapic.tellapic_isctle(header) == 1) {
			System.out.println("Was ctl extended");
			AbstractUser user = null;
			svcontrol_t ctlExtended = stream.getData().getControl();
			int         userId      = ctlExtended.getIdfrom();
			short[]     userInfo    = ctlExtended.getInfo();
			long        userInfoLen = stream.getHeader().getSsize() - tellapicConstants.HEADER_SIZE - 1;
			String      info = "";
			for(int i =0 ; i < userInfoLen; i++)
				info += (char)userInfo[i];

			switch(header.getCbyte()) {
			case tellapicConstants.CTL_SV_CLADD:
				if (userId != SessionUtils.getId()) {
					UserManager.getInstance().createUser(userId, info, true);
				}
				break;

			case tellapicConstants.CTL_CL_RMFIG:
				user = UserManager.getInstance().getUser(userId);
				if (user != null && user.isRemote())
					user.removeDrawing(info);
				break;

			case tellapicConstants.CTL_SV_FIGID:
				Utils.logMessage("RECEIVEING NUMBER ID");
				user = UserManager.getInstance().getLocalUser();
				user.setDrawingNumber(info);
				break;
			}

		} else if (tellapic.tellapic_ischatb(header) == 1) {
			message_t chatmsg = stream.getData().getChat();
			int       idFrom  = chatmsg.getIdfrom();
			AbstractUser userFrom = UserManager.getInstance().getUser(idFrom);
			String text    = chatmsg.getType().getBroadmsg();
			Message message = new Message(userFrom.getName(), null, text);
			ChatClientModel.getInstance().addMessage(message);
			System.out.println("Was broadcast chat");

		} else if  (tellapic.tellapic_ischatp(header) == 1) {
			message_t chatmsg = stream.getData().getChat();
			String    text    = chatmsg.getType().getPrivmsg().getText();
			int       idFrom  = chatmsg.getIdfrom();
			int       idTo    = chatmsg.getType().getPrivmsg().getIdto();
			AbstractUser userFrom = UserManager.getInstance().getUser(idFrom);
			AbstractUser userTo   = UserManager.getInstance().getUser(idTo);
			Message message = new Message(
					userFrom.getName(),
					userTo.getName(),
					text
			);
			ChatClientModel.getInstance().addMessage(message);
			System.out.println("Was private chat");

		} else if (tellapic.tellapic_isdrw(header) == 1 ) {
			final ddata_t    drawing    = stream.getData().getDrawing();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
//					createAndAddDrawing(drawing);
					configureUserToolBox(drawing);
				}
			});

		} else if (tellapic.tellapic_isfig(header) == 1) {
			System.out.println("Was fig");
			final ddata_t    drawing    = stream.getData().getDrawing();
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
//					createAndAddFigure(drawing);
					configureUserToolBox(drawing);
				}
			});

		} else if (tellapic.tellapic_isfigtxt(stream) == 1) {
			System.out.println("Was text");

		} else if (tellapic.tellapic_ispong(header) == 1) {
			setPing((double) ((System.nanoTime() - pingSentTime) / 1000000));
			try {
				pingerLock.lock();
				pingCondition.signal();
			} finally {
				pingerLock.unlock();
			}
		}
	}
	
	/**
	 * 
	 * @param drawingData
	 */
	private void configureUserToolBox(ddata_t drawingData) {
		/* Get the remote user who has drawn this figure */
		AbstractUser remoteUser = UserManager.getInstance().getUser(drawingData.getIdfrom());

		if (remoteUser == null) {
			Utils.logMessage("Warning: Received packet from an unexisting user yet.");
			return;
		}
		/* Get the drawing control byte from protocol */
		int dcbyte = drawingData.getDcbyte();

		/* Get the remote tool id he/she has used */
		int remoteTool = dcbyte & tellapicConstants.TOOL_MASK;
		
		/* Get the tool class name for this local user */
		String toolClassName = ToolFactory.getRegisteredToolsClassNames().get(remoteTool);

		IToolBoxController       toolControl  = remoteUser.getToolboxController();

		toolControl.selectToolByName(toolClassName.split("[a-z].*\\.")[1]);

		configureUserEvents(drawingData);
	}
	
	/**
	 * @param drawingData
	 */
	private void configureUserEvents(ddata_t drawingData) {
		/* Get the remote user who has drawn this figure */
		AbstractUser remoteUser = UserManager.getInstance().getUser(drawingData.getIdfrom());
	
		/* Get the mouse and event protocol data used in this figure */
		int button   = drawingData.getDcbyte() & tellapicConstants.EVENT_MASK;
		int eventExt = drawingData.getDcbyte_ext();

		/* Convert protocol button values to swing button and mask values */
		int swingButton = convertToSwingButtonValue(button);
		int swingMask   = getSwingMask(swingButton, eventExt);
		
		/* Get the event protocol data from the stream */
		int event  = drawingData.getDcbyte() & tellapicConstants.EVENT_MASK;
		
		Tool usedTool = remoteUser.getToolBoxModel().getLastUsedTool();
		IPaintPropertyController c = remoteUser.getPaintController();
		Color color = null;
		
		switch(event) {
		case tellapicConstants.EVENT_PRESS:
			color = new Color(
					drawingData.getColor().getRed(),
					drawingData.getColor().getGreen(),
					drawingData.getColor().getBlue()
			);
			c.handleEndCapsChange(drawingData.getType().getFigure().getEndcaps());
			c.handleLineJoinsChange(drawingData.getType().getFigure().getLinejoin());
			c.handleOpacityChange(drawingData.getOpacity());
			c.handleWidthChange(drawingData.getWidth());
			c.handleColorChange(color);
			RemoteMouseEvent pressEvent = new RemoteMouseEvent(
					remoteUser,
					DrawingAreaView.getInstance(),
					MouseEvent.MOUSE_PRESSED,
					System.currentTimeMillis(),
					swingMask,
					(int)drawingData.getPoint1().getX(),
					(int)drawingData.getPoint1().getY(),
					0,
					false,
					swingButton);
			usedTool.mousePressed(pressEvent);
			if (!usedTool.getName().equals("SelectorNet"))
				remoteUser.getDrawing().setNumber(drawingData.getNumber());
			break;
			
		case tellapicConstants.EVENT_DRAG:
			RemoteMouseEvent dragEvent = new RemoteMouseEvent(
					remoteUser,
					DrawingAreaView.getInstance(),
					MouseEvent.MOUSE_DRAGGED,
					System.currentTimeMillis(),
					swingMask,
					(int)drawingData.getPoint1().getX(),
					(int)drawingData.getPoint1().getY(),
					0,
					false,
					swingButton);
			usedTool.mouseDragged(dragEvent);
			break;
			
		case tellapicConstants.EVENT_RELEASE:
			RemoteMouseEvent releaseEvent = new RemoteMouseEvent(
					remoteUser,
					DrawingAreaView.getInstance(),
					MouseEvent.MOUSE_RELEASED,
					System.currentTimeMillis(),
					swingMask,
					(int)drawingData.getPoint1().getX(),
					(int)drawingData.getPoint1().getY(),
					0,
					false,
					swingButton);
			usedTool.mouseReleased(releaseEvent);
			break;
			
		case tellapicConstants.EVENT_NULL:
			/* Create a color instance upon the remote color used */
			color = new Color(
					drawingData.getColor().getRed(),
					drawingData.getColor().getGreen(),
					drawingData.getColor().getBlue()
			);
			int x1 = (int)drawingData.getPoint1().getX();
			int x2 = x1;
			int y1 = (int)drawingData.getPoint1().getY();
			int y2 = y1;
			
			/* Handle text properties if the used tool was TEXT. Otherwise, handle stroke properties */
			if ((drawingData.getDcbyte() & tellapicConstants.TOOL_TEXT) == tellapicConstants.TOOL_TEXT) {
				c.handleFontSizeChange(drawingData.getWidth());
				c.handleFontStyleChange(drawingData.getType().getText().getStyle());
				c.handleTextChange(drawingData.getType().getText().getInfo());
				c.handleFontFaceChange(drawingData.getType().getText().getFace());

			} else {
				c.handleEndCapsChange(drawingData.getType().getFigure().getEndcaps());
				c.handleLineJoinsChange(drawingData.getType().getFigure().getLinejoin());
				c.handleOpacityChange(drawingData.getOpacity());
				c.handleWidthChange(drawingData.getWidth());
				c.handleDashChange(drawingData.getType().getFigure().getDash_array(), drawingData.getType().getFigure().getDash_phase());
				y2 = (int)drawingData.getType().getFigure().getPoint2().getY();
				x2 = (int)drawingData.getType().getFigure().getPoint2().getX();
			}
			
			/* Both text and stroke has color properties */
			c.handleColorChange(color);
			RemoteMouseEvent pressEvent1 = new RemoteMouseEvent(
					remoteUser,
					DrawingAreaView.getInstance(),
					MouseEvent.MOUSE_PRESSED,
					System.currentTimeMillis(),
					swingMask,
					x1,
					y1,
					0,
					false,
					swingButton);

			RemoteMouseEvent dragEvent1 = new RemoteMouseEvent(
					remoteUser,
					DrawingAreaView.getInstance(),
					MouseEvent.MOUSE_DRAGGED,
					System.currentTimeMillis(),
					swingMask,
					x2,
					y2,
					0,
					false,
					swingButton);

			RemoteMouseEvent releaseEvent1 = new RemoteMouseEvent(
					remoteUser,
					DrawingAreaView.getInstance(),
					MouseEvent.MOUSE_RELEASED,
					System.currentTimeMillis(),
					swingMask,
					x2,
					y2,
					0,
					false,
					swingButton);

			if (usedTool.getName().equals("SelectorNet")){
				DrawingShape d = (DrawingShape) remoteUser.findDrawing(drawingData.getNumber());
				d.setSelected(true);
				double xOffset = (drawingData.getPoint1().getX() - d.getBounds2D().getX());
				double yOffset = drawingData.getPoint1().getY() - d.getBounds2D().getY() ;
				d.move(xOffset, yOffset);
			} else {
				usedTool.mousePressed(pressEvent1);
				remoteUser.getDrawing().setNumber(drawingData.getNumber());
				usedTool.mouseDragged(dragEvent1);
				usedTool.mouseReleased(releaseEvent1);
			}
			break;
		}
	}


	/**
	 * @param drawing
	 */
//	private void createAndAddFigure(ddata_t drawingData) {
//		/* Get the remote user who has drawn this figure */
//		AbstractUser remoteUser = UserManager.getInstance().getUser(drawingData.getIdfrom());
//
//		if (remoteUser == null) {
//			Utils.logMessage("Warning: Received packet from an unexisting user yet.");
//			return;
//		}
//		/* Get the drawing control byte from protocol */
//		int dcbyte = drawingData.getDcbyte();
//
//		/* Get the remote tool id he/she has used */
//		int remoteTool = dcbyte & tellapicConstants.TOOL_MASK;
//
//		/* Get the mouse and event protocol data used in this figure */
//		int button   = dcbyte & tellapicConstants.EVENT_MASK;
//		int eventExt = drawingData.getDcbyte_ext();
//
//		/* Convert protocol button values to swing button and mask values */
//		int swingButton = convertToSwingButtonValue(button);
//		int swingMask   = getSwingMask(swingButton, eventExt);
//
//		/* Get the tool class name for this local user */
//		String toolClassName = ToolFactory.getRegisteredToolsClassNames().get(remoteTool);
//
//		/* Get the remote user paint controller and tool box model */
//		IPaintPropertyController paintControl = remoteUser.getPaintController();
//		IToolBoxController       toolControl  = remoteUser.getToolboxController();
//
//		toolControl.selectToolByName(toolClassName.split("[a-z].*\\.")[1]);
//
//		/* Create a color instance upon the remote color used */
//		Color color = new Color(
//				drawingData.getColor().getRed(),
//				drawingData.getColor().getGreen(),
//				drawingData.getColor().getBlue()
//		);
//
//		/* Handle text properties if the used tool was TEXT. Otherwise, handle stroke properties */
//		if ((remoteTool & tellapicConstants.TOOL_TEXT) == tellapicConstants.TOOL_TEXT) {
//			paintControl.handleFontSizeChange(drawingData.getWidth());
//			paintControl.handleFontStyleChange(drawingData.getType().getText().getStyle());
//			paintControl.handleTextChange(drawingData.getType().getText().getInfo());
//			paintControl.handleFontFaceChange(drawingData.getType().getText().getFace());
//
//		} else {
//			paintControl.handleEndCapsChange(drawingData.getType().getFigure().getEndcaps());
//			paintControl.handleLineJoinsChange(drawingData.getType().getFigure().getLinejoin());
//			paintControl.handleOpacityChange(drawingData.getOpacity());
//			paintControl.handleWidthChange(drawingData.getWidth());
//			paintControl.handleDashChange(drawingData.getType().getFigure().getDash_array(), drawingData.getType().getFigure().getDash_phase());
//		}
//		
//		/* Both text and stroke has color properties */
//		paintControl.handleColorChange(color);
//
//		RemoteMouseEvent pressEvent = new RemoteMouseEvent(
//				remoteUser,
//				DrawingAreaView.getInstance(),
//				MouseEvent.MOUSE_PRESSED,
//				System.currentTimeMillis(),
//				swingMask,
//				(int)drawingData.getPoint1().getX(),
//				(int)drawingData.getPoint1().getY(),
//				0,
//				false,
//				swingButton);
//		
//		RemoteMouseEvent dragEvent = new RemoteMouseEvent(
//				remoteUser,
//				DrawingAreaView.getInstance(),
//				MouseEvent.MOUSE_DRAGGED,
//				System.currentTimeMillis(),
//				swingMask,
//				(int)drawingData.getType().getFigure().getPoint2().getX(),
//				(int)drawingData.getType().getFigure().getPoint2().getY(),
//				0,
//				false,
//				swingButton);
//
//		RemoteMouseEvent releaseEvent = new RemoteMouseEvent(
//				remoteUser,
//				DrawingAreaView.getInstance(),
//				MouseEvent.MOUSE_RELEASED,
//				System.currentTimeMillis(),
//				swingMask,
//				(int)drawingData.getType().getFigure().getPoint2().getX(),
//				(int)drawingData.getType().getFigure().getPoint2().getY(),
//				0,
//				false,
//				swingButton);
//
//		Tool usedTool = remoteUser.getToolBoxModel().getLastUsedTool();
//		if (usedTool.getName().equals("SelectorNet")){
//			DrawingShape d = (DrawingShape) remoteUser.findDrawing(drawingData.getNumber());
//			d.setSelected(true);
//			double xOffset = (drawingData.getPoint1().getX() - d.getBounds2D().getX());
//			double yOffset = drawingData.getPoint1().getY() - d.getBounds2D().getY() ;
//			d.move(xOffset, yOffset);
//		} else {
//			usedTool.mousePressed(pressEvent);
//			remoteUser.getDrawing().setNumber(drawingData.getNumber());
//			usedTool.mouseDragged(dragEvent);
//			usedTool.mouseReleased(releaseEvent);
//		}
//		/* TODO: JUST FOR DEBUG */
//		int x1 = (int)drawingData.getPoint1().getX();
//		int y1 = (int)drawingData.getPoint1().getY();
//		int x2 = (int)drawingData.getType().getFigure().getPoint2().getX();
//		int y2 = (int)drawingData.getType().getFigure().getPoint2().getY();
//		System.out.println("RECEIVED COORDS: ("+x1+","+y1+") ("+x2+","+y2+")");
//	}
//
//
//	/**
//	 * @param drawing
//	 */
//	private void createAndAddDrawing(ddata_t drawingData) {
//		/* Get the remote user who is drawing */
//		AbstractUser remoteUser = UserManager.getInstance().getUser(drawingData.getIdfrom());
//		
//		if (remoteUser == null) {
//			Utils.logMessage("Warning: Received packet from an unexisting user yet.");
//			return;
//		}
//		
//		/* Get the Drawing Control Byte from the stream */
//		int dcbyte = drawingData.getDcbyte();
//
//		/* Get the tool used by the remote user */
//		int remoteTool = dcbyte & tellapicConstants.TOOL_MASK;
//
//		/* Get the event protocol data from the stream */
//		int event  = dcbyte & tellapicConstants.EVENT_MASK;
//
//		/* Get the button protocol data from the stream */
//		int button = dcbyte & tellapicConstants.BUTTON_MASK;
//
//		/* Get extra information about the event protocol data from the stream */
//		int extDcbyte = drawingData.getDcbyte_ext();
//
//		/* Get all the registered tools in this local user */
//		//			Map<Integer, String> toolsClassNames = ToolFactory.getRegisteredToolsClassNames();
//
//		/* With the tool used from the remote user, get the same tool in this local user */
//		String     toolClassName   = ToolFactory.getRegisteredToolsClassNames().get(remoteTool);
//
//		/* Convert protocol mouse data to swing mouse data */
//		int swingButton = convertToSwingButtonValue(button);
//		int swingMask   = getSwingMask(swingButton, extDcbyte);
//
//		/* Get the remote user paint controller to set paint properties */
//		IPaintPropertyController c = remoteUser.getPaintController();
//		IToolBoxController toolControl = remoteUser.getToolboxController();
//		
//		/* Select the tool the remote user has used for this drawing and set its properties */
//		remoteUser.getToolboxController().selectToolByName(toolClassName.split("[a-z].*\\.")[1]);
//		Tool usedTool = remoteUser.getToolBoxModel().getLastUsedTool();
//		switch(event) {
//		
//		case tellapicConstants.EVENT_PRESS:
//			Color color = new Color(
//					drawingData.getColor().getRed(),
//					drawingData.getColor().getGreen(),
//					drawingData.getColor().getBlue()
//			);
//
//			c.handleEndCapsChange(drawingData.getType().getFigure().getEndcaps());
//			c.handleLineJoinsChange(drawingData.getType().getFigure().getLinejoin());
//			c.handleOpacityChange(drawingData.getOpacity());
//			c.handleWidthChange(drawingData.getWidth());
//			c.handleColorChange(color);
//			
//			RemoteMouseEvent pressEvent = new RemoteMouseEvent(
//					remoteUser,
//					DrawingAreaView.getInstance(),
//					MouseEvent.MOUSE_PRESSED,
//					System.currentTimeMillis(),
//					swingMask,
//					(int)drawingData.getPoint1().getX(),
//					(int)drawingData.getPoint1().getY(),
//					0,
//					false,
//					swingButton);
//			usedTool.mousePressed(pressEvent);
//			if (!usedTool.getName().equals("SelectorNet"))
//				remoteUser.getDrawing().setNumber(drawingData.getNumber());
//			break;
//
//		case tellapicConstants.EVENT_DRAG:
//			RemoteMouseEvent dragEvent = new RemoteMouseEvent(
//					remoteUser,
//					DrawingAreaView.getInstance(),
//					MouseEvent.MOUSE_DRAGGED,
//					System.currentTimeMillis(),
//					MouseEvent.BUTTON1_DOWN_MASK,
//					(int)drawingData.getPoint1().getX(),
//					(int)drawingData.getPoint1().getY(),
//					0,
//					false,
//					MouseEvent.NOBUTTON);
//			usedTool.mouseDragged(dragEvent);
//			break;
//
//		case tellapicConstants.EVENT_RELEASE:
//			RemoteMouseEvent releaseEvent = new RemoteMouseEvent(
//					remoteUser,
//					DrawingAreaView.getInstance(),
//					MouseEvent.MOUSE_RELEASED,
//					System.currentTimeMillis(),
//					swingMask,
//					(int)drawingData.getPoint1().getX(),
//					(int)drawingData.getPoint1().getY(),
//					0,
//					false,
//					swingButton);
//			usedTool.mouseReleased(releaseEvent);
//			break;
//		}
//	}


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
		/* If no button is set, then is a null event. Treat a null event as an event with button1 */
		if (button == tellapicConstants.BUTTON_LEFT)
			return MouseEvent.BUTTON1;
		else if (button == tellapicConstants.BUTTON_RIGHT)
			return MouseEvent.BUTTON2;
		else if (button == tellapicConstants.BUTTON_MIDDLE)
			return MouseEvent.BUTTON3;
		else
			return MouseEvent.BUTTON1;
	}


	public class WrongPacketException extends Exception {
		/**
		 * @param string
		 */
		public WrongPacketException(String string) {
			super(string);
		}

		private static final long serialVersionUID = 1L;
	}

	/**
	 * 
	 */
	public void stop() {
		this.isRunning = false;
		if (isConnected()) {
			setConnected(false);
			tellapic.tellapic_close_socket(socket);
		}
		try {
			netManager.interrupt();
			netManager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}