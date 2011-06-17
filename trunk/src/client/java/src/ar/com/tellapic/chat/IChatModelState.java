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

import java.util.List;

/**
 * An interface that provides methods for query the chat model.
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IChatModelState {

	/**
	 * Gets the ith message from the list of messages.
	 * @param i the desired numbered message to retrieve.
	 * @return The ith message if exist. Null otherwise.
	 */
	public abstract ChatMessage getChatMessage(int i);
	
	/**
	 * Gets the list of messages.
	 * @return the list of messages.
	 */
	public abstract List<ChatMessage> getChatMessages();
	
	/**
	 * Gets the last message in the list of messages.
	 * @return the last message in the list of messages or null if no there are no messages.
	 */
	public abstract ChatMessage getLastChatMessage();
	
//	public abstract User getUser(String name);
//	
//	public abstract List<User> getUsers();
//	
//	public abstract User getUser(int userId);
}
