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

import java.util.Map;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IChatViewProtocol {

	/**
	 * Constructs a Message instance.
	 * 
	 * @param from The user owner of the message
	 * @param to The user the message is directed to
	 * @param text The protocol-dependent text to be procesed
	 * @return Returns a map that contains a Message if creating was ok. If Message is null, contains a String
	 * indicating the error.
	 */
	public  Map.Entry<String,ChatMessage> buildChatMessage(String from, String to, String text);
}
