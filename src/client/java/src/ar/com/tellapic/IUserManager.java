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

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IUserManager {
	
	/**
	 * Adds an existing user to the users list. 
	 * 
	 * @param user the user to be added to the users list.
	 * @return true if the user was succesfully added.
	 */
	public boolean addUser(AbstractUser user);
	
	/**
	 * Removes the user with the specified userName.
	 * 
	 * @param userName The user name of the user to be removed.
	 * @return true if the user was removed. False otherwise.
	 */
	public boolean delUser(String userName);
	
	/**
	 * Removes the user with the specified id.
	 * 
	 * @param id the id of the user to be removed.
	 * @return true if the user was removed. False otherwise.
	 */
	public boolean delUser(int id);
	
	/**
	 * Removes the user from the users list.
	 * 
	 * @param user the user to be removed from the users list.
	 * @return true if the user is succcessfully removed, false otherwise.
	 */
	public boolean delUser(AbstractUser user);
	
	/**
	 * 
	 * @param userName
	 * @return
	 */
	public boolean requireDisconnection(String userName);
	
	/**
	 * Creates a remote user instance with the specified userId and userName.
	 * 
	 * @param userId
	 * @param userName
	 * @return the remote user instance.
	 */
	public RemoteUser createRemoteUser(int userId, String userName);
	
	/**
	 * Creates a local user instance with the specified userId and userName.
	 * 
	 * @param userId
	 * @param userName
	 * @return the local user instance.
	 */
	public LocalUser createLocalUser(int userId, String userName);
}
