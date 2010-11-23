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

import java.util.AbstractMap;
import java.util.Map;

import com.tellapic.UserManager;
import com.tellapic.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Message {

	private String   messageText;
	private String   messageTo;
	private String   messageFrom;
	
	
	public Message(String from, String to, String text)  {
		messageText = text;
		messageTo   = to;
		messageFrom = from;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isPrivate() {
		return (messageTo != null);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getText() {
		return messageText;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSender() {
		return messageFrom;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getReceiver() {
		return messageTo;
	}
	
	/**
	 * 
	 * @param from
	 * @param to
	 * @param text
	 * @return
	 */
	public static Map.Entry<String,Message> build(String from, String to, String text) {
		if (text == null)
			throw new IllegalArgumentException("text can't be null");
		if (from == null)
			throw new IllegalArgumentException("from can't be null");
		
		AbstractMap.SimpleEntry<String, Message> mapEntry = null;
		if (text.startsWith("/")) {
			// Check which of the available command was issued, if any.
			if (text.startsWith("msg ", 1)) {
				// Syntax: "/msg <to> <text>" The <to> will override 'to' argument.
				String[] processedInput = text.substring(5).split(" ", 2);
				if (processedInput.length < 2) {
					mapEntry = new AbstractMap.SimpleEntry<String, Message>("* "+Utils.msg.getString("usage")+": /msg <to> <message>\n", null);
				} else {
					if (UserManager.getInstance().getUser(processedInput[0]) == null)
						mapEntry = new AbstractMap.SimpleEntry<String, Message>("* "+Utils.msg.getString("nouserfound")+"\n", null);
					else
						mapEntry = new AbstractMap.SimpleEntry<String, Message>("Message", new Message(from, processedInput[0], processedInput[1]));
				}
			} else {
				mapEntry = new AbstractMap.SimpleEntry<String, Message>("* "+Utils.msg.getString("chathelp")+"\n", null);
			}
		} else {
			// Has 'to' been specified? if so, then this is a private message build call.
			mapEntry = new AbstractMap.SimpleEntry<String, Message>("Message", new Message(from, to, text));
		}
		
		return mapEntry;
	}
}
