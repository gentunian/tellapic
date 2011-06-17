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

/**
 * An interface that provide access and modification to the chat model.
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IChatModelManager {

	/**
	 * Removes the ith message.
	 * @param i the position of the message to remove.
	 * @return true if the message existed and was removed. False otherwise.
	 */
	public boolean removeChatMessage(int i);
	
	/**
	 * Removes a message from an user
	 * @param userId
	 * @return
	 */
	public boolean removeChatMessageFrom(int userId);
	
//	public abstract boolean removeMessageFrom(User user);
	
	/**
	 * Removes the last message of the list of messages. 
	 * @return True if removal was successfull. False otherwise.
	 */
	public boolean removeLastChatMessage();
	
	/**
	 * Removes the first message of the list of messages.
	 * @return true if the message was removed. False if not or the list is empty.
	 */
	public boolean removeFirstChatMessage();
	
	/**
	 * Adds a message to the model.
	 * @param message the message to be added.
	 */
	public void addChatMessage(ChatMessage message);
	
//	public abstract void addUser(User user) throws NullPointerException;
}
