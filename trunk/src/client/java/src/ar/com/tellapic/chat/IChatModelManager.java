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
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IChatModelManager {

	public abstract boolean removeMessage(int i);
	
	public abstract boolean removeMessageFrom(User user);
	
	public abstract boolean removeMessageFrom(int userId);
	
	public abstract boolean removeLastMessage();
	
	public abstract boolean removeFirstMessage();
	
	public abstract void addMessage(Message message) throws NullPointerException;
	
	public abstract void addUser(User user) throws NullPointerException;
	
	
}
