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
package ar.com.tellapic.adm;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class that basically manages user list.
 * 
 * It implements two interfaces to provide flexible access, modification and queries to
 * the user list structure.
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class UserManager implements IUserManager, IUserManagerState {
	
	/* The users list */
	private ArrayList<AbstractUser> users;
	
	/**
	 * Constructor: Creates an empty uses list.
	 */
	public UserManager() {
		users = new ArrayList<AbstractUser>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#addUser(ar.com.tellapic.AbstractUser)
	 */
	@Override
	public boolean addUser(AbstractUser user) {
		return users.add(user);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#delUser(java.lang.String)
	 */
	@Override
	public boolean delUser(String name) {
		AbstractUser user = getUser(name);
	
		return delUser(user);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#delUser(int)
	 */
	@Override
	public boolean delUser(int id) {
		AbstractUser user = getUser(id);

		return delUser(user);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#delUser(ar.com.tellapic.AbstractUser)
	 */
	@Override
	public boolean delUser(AbstractUser user) {
		return users.remove(user);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getUsers()
	 */
	@Override
	public List<AbstractUser> getUsers() {
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
	 * @see com.tellapic.IUserManagerState#getUser(int)
	 */
	@Override
	public AbstractUser getUser(String userName) {
		AbstractUser user  = null;
		int i;
		
		for(i = 0; i < users.size() && !users.get(i).getName().equals(userName); i++);

		if (i < users.size())
			user = users.get(i);
		
		return user;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.IUserManagerState#getUserName(int)
	 */
	@Override
	public AbstractUser getUser(int id) {
		AbstractUser user = null;
		int i;
		for(i = 0; i < users.size() && users.get(i).getUserId() != id; i++);
		
		if (i < users.size())
			user = users.get(i);
		
		return user;
	}
}
