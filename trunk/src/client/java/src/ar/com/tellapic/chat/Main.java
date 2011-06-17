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
package ar.com.tellapic.chat;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.SwingUtilities;

import ar.com.tellapic.TellapicChatViewCustomProtocol;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Main {
	public static final String HOST = "localhost";
	public static int          PORT = 3344;
	public static String       user;
	public static ChatServer   chatServer;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1)
			System.exit(-1);
		
		user = args[0];
		
		if (args.length > 1) {
			PORT = Integer.parseInt(args[1]);
			chatServer = new ChatServer(PORT);
			chatServer.start();
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGui();
			}
		});
		
	}

	/**
	 * 
	 */
	private static void createAndShowGui() {
		try {
			IChatConnection  connection = new MyChatConnection(HOST, PORT, user);
			IChatController  controller = new MyChatController(connection);
			ChatMessagesView chatView   = new ChatMessagesView(controller, user, new TellapicChatViewCustomProtocol(ChatUserManager.getInstance()));
			final ChatWindow mainFrame  = new ChatWindow();
			
			mainFrame.addMessagesView(chatView);
			mainFrame.addUsersView(new ChatUsersView());
			
			mainFrame.addComponentListener(new ComponentListener(){
				public void componentHidden(ComponentEvent e) {
					mainFrame.setVisible(false);
					mainFrame.dispose();
					chatServer.end();
				}
				public void componentMoved(ComponentEvent e) {}
				public void componentResized(ComponentEvent e) {}
				public void componentShown(ComponentEvent e) {}
			});
			mainFrame.pack();
			mainFrame.setVisible(true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	public static class MyChatController implements IChatController {
		private ChatClientModel  chatModel;
		private IChatConnection  connection;
		
		public MyChatController(IChatConnection c) {
			chatModel  = ChatClientModel.getInstance();
			connection = c;
			MessageListener l = new MessageListener();
			new Thread(l).start();
		}
		
		@Override
		public void handleInput(ChatMessage message) {
			if (message == null)
				return;
			
			chatModel.addChatMessage(message);
			
			if (!message.isRemote())
				connection.sendMessage(message);
		}
		
		private class MessageListener implements Runnable {
			private boolean running;

			public MessageListener() {
				running = false;
			}
			
			public void run() {
				running = true;
				while(running)
					handleInput(connection.receiveMessage());
			}
		}
	}

	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	public static class MyChatConnection implements IChatConnection {
		private Socket           socket;
		private PrintWriter      out;
		private BufferedReader   in;
		private String name;
		
		public MyChatConnection(String host, int port, String name) throws UnknownHostException, IOException {
			socket = new Socket(host, port);
			out    = new PrintWriter(socket.getOutputStream(), true);
			in     = new BufferedReader(
					new InputStreamReader(
							socket.getInputStream()
					)
			);
			this.name = name;
			out.println("/iam "+name);
			ChatUserManager.getInstance().addUser(new ChatUser(name));
		}
		
		@Override
		public ChatMessage receiveMessage() {
			ChatMessage  message = null; 
			String   info    = null;
			
			try {
				info  = in.readLine();
				System.out.println("RECEIVE "+info);
				if (info.startsWith("/new")) {
					ChatUserManager.getInstance().addUser(new ChatUser(info.substring(5)));
					return null;
				}
				IChatViewProtocol protocol = new TellapicChatViewCustomProtocol(ChatUserManager.getInstance());
				message = protocol.buildChatMessage(name, null, info).getValue();
				message.setRemote(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return message;
		}
		
		@Override
		public void sendMessage(ChatMessage message) {
			String text = message.getText();
			String from = message.getSender();
			String info = from;
			
			if (message.isPrivate())
				info += ": "+message.getReceiver();

			info += ": "+text;
			
			out.println(info);
		}
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	public static class ChatServer extends Thread {
		private ServerSocket         serverSocket;
		private Vector<ClientThread> clients;
		private boolean              isRunning;
		private int                  port;
		
		public ChatServer(int port) {
			clients   = new Vector<ClientThread>();
			isRunning = false;
			this.port = port;
		}
		
		public void run() {
			try {
				serverSocket = new ServerSocket(port);
				isRunning    = true;
				
				while(isRunning) {
					Socket clientSocket = serverSocket.accept();
					ClientThread client = new ClientThread(clientSocket); 
					clients.add(client);
					client.start();
				}
				
			} catch (IOException e) {
				System.exit(-1);
			}
		}

		
		private void end() {
			isRunning = false;
			
			for(ClientThread client : clients)
				client.close();

			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		private class ClientThread extends Thread {
			private boolean        running;
			private Socket         clientSocket;
			private String         clientName;
			private PrintWriter    out;
			private BufferedReader in;
			
			public ClientThread(Socket clientSocket) {
				this.clientSocket = clientSocket;
				this.running = false;
				try {
					out = new PrintWriter(clientSocket.getOutputStream(), true);
					in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			public void run() {
				running = true;
				String inputLine;

				try {
					inputLine = in.readLine();
					if (inputLine.startsWith("/iam")) {
						setClientName(inputLine.substring(5));
						for(int i = 0; i < clients.size(); i++) {
							ClientThread t = clients.get(i);
							if (!t.equals(this)) {
								t.getOutput().println("/new "+getClientName());
								out.println("/new "+t.getClientName());
							}
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				while(running) {
					try {
						while ((inputLine = in.readLine()) != null) {
							
							for(int i = 0; i < clients.size(); i++) {
								ClientThread t = clients.get(i);
								if (!t.equals(this))
									t.getOutput().println(inputLine);
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			public void close() {
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				running = false;
			}
			
			public PrintWriter getOutput() {
				return out;
			}

			/**
			 * @param clientName the clientName to set
			 */
			public void setClientName(String clientName) {
				this.clientName = clientName;
			}

			/**
			 * @return the clientName
			 */
			public String getClientName() {
				return clientName;
			}
		}
	}
}
