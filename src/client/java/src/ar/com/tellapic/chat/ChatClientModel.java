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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import ar.com.tellapic.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ChatClientModel extends Observable implements IChatModelManager, IChatModelState {

	private ArrayList<Message> messages;
	private ArrayList<User>    users;
	
	private static class Holder {
		private static final ChatClientModel INSTANCE = new ChatClientModel();
	}
	
	private ChatClientModel() {
		messages = new ArrayList<Message>();
		users    = new ArrayList<User>();
	}
	
	public static ChatClientModel getInstance() {
		return Holder.INSTANCE;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelManager#addMessage(com.tellapic.chat.Message)
	 */
	@Override
	public void addMessage(Message message) {
		if (message == null)
			return;
		
		messages.add(message);
		Utils.logMessage("Adding message. Notifying observers.");
		setChanged();
		notifyObservers(message);
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelManager#addUser(com.tellapic.chat.User)
	 */
	@Override
	public void addUser(User user) {
		if (user == null)
			return;
		
		users.add(user);
		setChanged();
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelManager#removeFirstMessage()
	 */
	@Override
	public boolean removeFirstMessage() {
		if (!messages.isEmpty())
			return false;
		
		messages.remove(0);
		setChanged();
		notifyObservers();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelManager#removeLastMessage()
	 */
	@Override
	public boolean removeLastMessage() {
		if (!messages.isEmpty())
			return false;
		
		messages.remove(messages.size() - 1);
		setChanged();
		notifyObservers();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelManager#removeMessage(int)
	 */
	@Override
	public boolean removeMessage(int i) {
		if (i < 0 || i >= messages.size())
			return false;
		
		messages.remove(i);
		setChanged();
		notifyObservers();
		return true;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelManager#removeMessageFrom(com.tellapic.chat.User)
	 */
	@Override
	public boolean removeMessageFrom(User user) {
		if (user == null)
			return false;
		
		setChanged();
		notifyObservers();
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelManager#removeMessageFrom(int)
	 */
	@Override
	public boolean removeMessageFrom(int userId) {
		setChanged();
		notifyObservers();
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelState#getLastMessage()
	 */
	@Override
	public Message getLastMessage() {
		if (!messages.isEmpty())
			return messages.get(messages.size() - 1);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelState#getMessage(int)
	 */
	@Override
	public Message getMessage(int i) {
		if (i < 0 || i >= messages.size())
			return null;
		
		return messages.get(i);
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelState#getMessages()
	 */
	@Override
	public List<Message> getMessages() {
		return messages;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelState#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String name) {
		for(User user : users) {
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelState#getUser(int)
	 */
	@Override
	public User getUser(int userId) {
		for(User user : users) {
			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.chat.IChatModelState#getUsers()
	 */
	@Override
	public List<User> getUsers() {
		return users;
	}
}
