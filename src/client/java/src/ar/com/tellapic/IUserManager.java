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
	
	public boolean addUser(AbstractUser user);
	
	public AbstractUser delUser(String userName);
	
	public AbstractUser delUser(int id);
	
//	public AbstractUser delUser(AbstractUser user);
	
	public boolean requireDisconnection(String userName);
	
//	public void setUserVisible(String userName, boolean visible);
	
	public AbstractUser createUser(int userId, String userName, boolean remote);
	
//	public void toggleUserVisibility(String name);
}