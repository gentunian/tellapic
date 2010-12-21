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
package com.tellapic;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UserManagerController implements IUserManagerController {
	public static final int CHANGE_VISIBILITY = 0;
	private final UserManager userManager = UserManager.getInstance();
	
	private static class Holder {
		private final static UserManagerController INSTANCE = new UserManagerController();
	}
	
	private UserManagerController() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static UserManagerController getInstance() {
		return Holder.INSTANCE;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.IUserManagerController#handleEvent()
	 */
	@Override
	public void handleEvent(int event, Object data) {
		// TODO Auto-generated method stub
		switch(event){
		case CHANGE_VISIBILITY:
			userManager.changeUserVisibility((String) data);
			break;
		}
	}
	
	
}
