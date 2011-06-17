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

import java.util.AbstractMap;
import java.util.Map.Entry;

import ar.com.tellapic.adm.IUserManagerState;
import ar.com.tellapic.chat.ChatMessage;
import ar.com.tellapic.chat.IChatViewProtocol;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class TellapicChatViewCustomProtocol implements IChatViewProtocol {

	private IUserManagerState userManagerState;

	/**
	 * 
	 * @param userManagerState
	 */
	public TellapicChatViewCustomProtocol(IUserManagerState userManagerState) {
		this.userManagerState = userManagerState; 
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.chat.IChatViewProtocol#buildChatMessage(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Entry<String, ChatMessage> buildChatMessage(String from, String to, String text) {
		if (text == null)
			throw new IllegalArgumentException("text can't be null");
		if (from == null)
			throw new IllegalArgumentException("from can't be null");
		
		AbstractMap.SimpleEntry<String, ChatMessage> mapEntry = null;
		
		if (text.startsWith("/"))
			
			// Check which of the available command was issued, if any.
			if (text.startsWith("msg ", 1)) {
				
				// Syntax: "/msg <to> <text>" The <to> command option will override 'to' argument.
				String[] processedInput = text.substring(5).split(" ", 2);
				
				if (processedInput.length < 2)
					mapEntry = new AbstractMap.SimpleEntry<String, ChatMessage>("* "+Utils.msg.getString("usage")+": /msg <to> <message>\n", null);
				else
					if (userManagerState.getUser(processedInput[0]) == null)
						mapEntry = new AbstractMap.SimpleEntry<String, ChatMessage>("* "+Utils.msg.getString("nouserfound")+"\n", null);
					else
						mapEntry = new AbstractMap.SimpleEntry<String, ChatMessage>("ChatMessage", new ChatMessage(from, processedInput[0], processedInput[1]));
			}	
			else
				mapEntry = new AbstractMap.SimpleEntry<String, ChatMessage>("* "+Utils.msg.getString("chathelp")+"\n", null);
			
		 else 
			// Has 'to' been specified? if so, then this is a private message build call.
			mapEntry = new AbstractMap.SimpleEntry<String, ChatMessage>("ChatMessage", new ChatMessage(from, to, text));
		
		return mapEntry;
	}
}
