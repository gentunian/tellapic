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
package com.tellapic.chat;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ChatController implements IChatController {
	private IChatModelManager model;
	private IChatConnection   connection;
	private ReceiverThread    receiver = new ReceiverThread();
	
	public ChatController() {
		this.model = ChatClientModel.getInstance();
		receiver.start();
	}
	
	
	public void cleanUp() {
		if (receiver.isAlive())
			receiver.kill();
	}
	
	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatController#handleInput(java.lang.String, boolean)
	 */
	@Override
	public void handleInput(Message message, boolean fromView) {
		model.addMessage(message);
		if (!fromView)
			connection.sendMessage(message);
	}
	
	
	private class ReceiverThread extends Thread {
		private boolean alive;
		
		public ReceiverThread() {
			
		}
		
		public void run() {
			alive = true;
			while(alive) {
				try {
					handleInput(connection.receiveMessage(), false);
				} catch(Exception e) {}
			}
		}
		
		public void kill() {
			this.interrupt();
			alive = false;
		}
	}
}
