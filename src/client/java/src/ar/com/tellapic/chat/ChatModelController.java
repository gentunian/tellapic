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

import ar.com.tellapic.NetManager;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapic_socket_t;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ChatModelController implements IChatController {
	private IChatModelManager model;
	private NetManager netManager;
	//private IChatConnection   connection;

	public ChatModelController() {
		model = ChatClientModel.getInstance();
		netManager = NetManager.getInstance();
	}


	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatController#handleInput(java.lang.String, boolean)
	 */
	@Override
	public void handleInput(Message message, boolean fromView) {//TODO: REVIEW!
		
		model.addMessage(message);
		if (fromView) {
			//connection.sendMessage(message);
			tellapic_socket_t socket = netManager.getSocket();
			int idFrom = UserManager.getInstance().getLocalUser().getUserId();
			String text = message.getText();
			if (message.isPrivate()) {
				int idTo = UserManager.getInstance().getUser(message.getReceiver()).getUserId();
				tellapic.tellapic_send_chatp(socket, idFrom, idTo, text.length(), text);
			} else {
				tellapic.tellapic_send_chatb(socket, idFrom, text.length(), text);
			}
		}
	}
}
