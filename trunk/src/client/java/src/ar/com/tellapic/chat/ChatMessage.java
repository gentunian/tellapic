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
public class ChatMessage {

	private String   messageText;
	private String   messageTo;
	private String   messageFrom;
	private boolean  isRemote;
	
	/**
	 * Constructs a local message as default.
	 * @param from
	 * @param to
	 * @param text
	 */
	public ChatMessage(String from, String to, String text)  {
		this(from, to, text, false);
	}
	
	/**
	 * Constructs a message specifying if it's local or not.
	 * @param from
	 * @param to
	 * @param text
	 * @param remote
	 */
	public ChatMessage(String from, String to, String text, boolean remote)  {
		messageText = text;
		messageTo   = to;
		messageFrom = from;
		isRemote    = remote;
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
	 * @param isRemote the isRemote to set
	 */
	public void setRemote(boolean isRemote) {
		this.isRemote = isRemote;
	}

	/**
	 * @return the isRemote
	 */
	public boolean isRemote() {
		return isRemote;
	}
}
