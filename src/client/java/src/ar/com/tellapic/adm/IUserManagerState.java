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

import java.util.List;


/**
 * An interface that defines methods for querying the users model.
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IUserManagerState {

	/**
	 * Retrieves an user.
	 * 
	 * @param userName The user name of the user to be retrieved.
	 * @return The user with name userName or null if no such user exists.
	 */
	public AbstractUser                      getUser(String userName);
	
	/**
	 * Retrieves an user.
	 * 
	 * @param id The user id of the user to be retrieved.
	 * @returnThe user with id or null if no such user exists.
	 */
	public AbstractUser                      getUser(int id);
	
	/**
	 * Get the list of users.
	 * 
	 * @return the list of users.
	 */
	public List<AbstractUser>           getUsers();
}
