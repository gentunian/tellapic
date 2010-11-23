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
public interface IChatConnection {

	/**
	 * Uses a connection to send a message over the net.
	 * @param message The message to be sent.
	 */
	public abstract void sendMessage(Message message);
	
	/**
	 * Receives a message from the net. 
	 * @return The message as a String based on the Chat internal protocol.
	 */
	public abstract Message receiveMessage();
}
