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

import ar.com.tellapic.chat.ChatClientModel;
import ar.com.tellapic.chat.ChatMessage;
import ar.com.tellapic.chat.IChatConnection;
import ar.com.tellapic.chat.IChatController;
import ar.com.tellapic.chat.IChatModelManager;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapic_socket_t;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class TellapicChatModelController implements IChatController, IChatConnection {
	
	private IChatModelManager model;
	
	/**
	 * 
	 */
	public TellapicChatModelController() {
		model = ChatClientModel.getInstance();
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatController#handleInput(java.lang.String, boolean)
	 */
	@Override
	public void handleInput(ChatMessage message) {
		model.addChatMessage(message);

		if (!message.isRemote())
			sendMessage(message);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.chat.IChatConnection#receiveMessage()
	 */
	@Override
	public ChatMessage receiveMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.chat.IChatConnection#sendMessage(ar.com.tellapic.chat.Message)
	 */
	@Override
	public void sendMessage(ChatMessage message) {
		NetManager        netManager = NetManager.getInstance();
		tellapic_socket_t socket     = netManager.getSocket();
		
		int    idFrom = TellapicUserManager.getInstance().getUser(SessionUtils.getUsername()).getUserId();
		String text   = message.getText();
		
		if (message.isPrivate()) {
			int idTo = TellapicUserManager.getInstance().getUser(message.getReceiver()).getUserId();
			tellapic.tellapic_send_chatp(socket, idFrom, idTo, text.length(), text);
		} else {
			tellapic.tellapic_send_chatb(socket, idFrom, text.length(), text);
		}
	}
}
