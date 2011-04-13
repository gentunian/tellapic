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

import java.util.HashMap;

import ar.com.tellapic.graphics.DrawingAreaView;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UserManager implements IUserManager, IUserManagerState {

	private HashMap<String, AbstractUser> users;
	
	private static class Holder {
		private final static UserManager INSTANCE = new UserManager();
	}
	
	private UserManager() {
		//userList = new ArrayList<AbstractUser>();
		users = new HashMap<String, AbstractUser>();
	}
	

	/**
	 * 
	 * @return
	 */
	public static UserManager getInstance() {
		return Holder.INSTANCE;
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManager#createLocalUser(int)
	 */
	@Override
	public LocalUser createLocalUser(int id, String name) {
		//AbstractUser user = new LocalUser(id, "local");
		//TODO: use constant
		AbstractUser user = LocalUser.getInstance();
		user.setName(name);
		user.setUserId(id);
		users.put(LocalUser.LOCAL_NAME, user);
//		user.addObserver(DrawingAreaView.getInstance());
		user.addObserver(UserView.getInstance());
		user.notifyObservers(user);
		return (LocalUser) user;
		//new UserGUIBuilder((LocalUser)user);
		//notifyObservers(new ActionData(ActionData.ACTION_ADD, user));
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getLocalUser()
	 */
	@Override
	public LocalUser getLocalUser() {
		//TODO: use constant
		return (LocalUser)users.get(LocalUser.LOCAL_NAME);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManager#addUser(int, java.lang.String)
	 */
	@Override
	public void addUser(int id, String name) {
		AbstractUser user = new RemoteUser(id, name);
		users.put(name, user);
		user.addObserver(DrawingAreaView.getInstance());
		user.addObserver(UserView.getInstance());
		user.notifyObservers(user);
		
		//notifyObservers(new ActionData(ActionData.ACTION_ADD, user));
	}

	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManager#delUser(java.lang.String)
	 */
	@Override
	public void delUser(String name) {
		AbstractUser userRemoved = users.remove(name);
		userRemoved.cleanUp();
		//TODO: A USER HAS A LOT OF MEMORY LOADED. FREE IT. E.G: TOOLBOX, DRAWINGCONTROLLERS, ETC
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getUsers()
	 */
	@Override
	public HashMap<String, AbstractUser> getUsers() {
		return users;
	}
	


	/* (non-Javadoc)
	 * @see com.tellapic.IUserManager#requireDisconnection(int)
	 */
	@Override
	public boolean requireDisconnection(String name) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.IUserManager#setUserVisible(int, boolean)
	 */
	@Override
	public void setUserVisible(String name, boolean visible) {
		users.get(name).setVisible(visible);
	}


	/* (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getRemoteUser(int)
	 */
	@Override
	public RemoteUser getRemoteUser(String name) {
		AbstractUser user = users.get(name); 
		if (user instanceof RemoteUser)
			return (RemoteUser) user;
		else
			return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getRemoteUsers()
	 */
	@Override
	public HashMap<String, RemoteUser> getRemoteUsers() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getUser(int)
	 */
	@Override
	public AbstractUser getUser(String name) {
		return users.get(name);
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManager#changeUserVisibility(java.lang.String)
	 */
	@Override
	public void changeUserVisibility(String name) {
		AbstractUser user = users.get(name);
		if (user != null)
			user.setVisible(!user.isVisible());
	}
	
	
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	public class ActionData {
		public static final int ACTION_REMOVE = 0;
		public static final int ACTION_ADD    = 1;

		private int action;
		private AbstractUser user;

		public ActionData(int action, AbstractUser user) {
			this.action = action;
			this.user = user;
		}

		/**
		 * @param user the user to set
		 */
		public void setUser(AbstractUser user) {
			this.user = user;
		}

		/**
		 * @return the user
		 */
		public AbstractUser getUser() {
			return user;
		}

		/**
		 * @param action the action to set
		 */
		public void setAction(int action) {
			this.action = action;
		}

		/**
		 * @return the action
		 */
		public int getAction() {
			return action;
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.IUserManagerState#getUserName(int)
	
	@Override
	public String getUserName(int id) {
		for(AbstractUser user : users.values()) {
			if (user.getUserId() == id)
				return user.getName();
		}
		return null;
	}
	*/

	/* (non-Javadoc)
	 * @see ar.com.tellapic.IUserManagerState#getUserName(int)
	 */
	@Override
	public AbstractUser getUser(int id) {
		for(AbstractUser user : users.values()) {
			if (user.getUserId() == id)
				return user;
		}
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#delUser(int)
	 */
	@Override
	public void delUser(int id) {
		for(AbstractUser user : users.values()) {
			if (user.getUserId() == id) {
				this.delUser(user.getName());
				return;
			}
		}
	}
}
