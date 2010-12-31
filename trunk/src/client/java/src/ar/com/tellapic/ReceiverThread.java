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

import ar.com.tellapic.chat.ChatController;
import ar.com.tellapic.chat.IChatController;
import ar.com.tellapic.chat.Message;
import ar.com.tellapic.lib.message_t;
import ar.com.tellapic.lib.stream_t;
import ar.com.tellapic.lib.svcontrol_t;
import ar.com.tellapic.lib.tellapic;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ReceiverThread extends Thread {
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
		stream_t stream;
		running = true;
		while(running) {
			stream = tellapic.tellapic_read_stream_b(fd);
			
			if (stream.getHeader().getCbyte() != tellapic.CTL_FAIL) {
				
				if (tellapic.tellapic_isctl(stream.getHeader()) == 1) {
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
					
				} else if (tellapic.tellapic_isfigtxt(stream) == 1) {
					System.out.println("Was text");
					
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
				
			case tellapic.CTL_SV_ID:
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
				String name = tellapic.tellapic_bytetp2charp(ctle.getInfo());
				UserManager.getInstance().addUser(id, name);
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
			int       idTo;
			
			switch(cbyte) {
			
			case tellapic.CTL_CL_BMSG:
				text    = chatmsg.getType().getBroadmsg();
				message = new Message(UserManager.getInstance().getUserName(idFrom), null, text);
				break;
				
			case tellapic.CTL_CL_PMSG:
				idTo = chatmsg.getType().getPrivmsg().getIdto();
				text = chatmsg.getType().getPrivmsg().getText();
				message = new Message(
						UserManager.getInstance().getUserName(idFrom),
						UserManager.getInstance().getUserName(idTo),
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
	}
}
