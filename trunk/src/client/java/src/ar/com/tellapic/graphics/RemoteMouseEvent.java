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
package ar.com.tellapic.graphics;

import java.awt.Component;
import java.awt.event.MouseEvent;

import ar.com.tellapic.AbstractUser;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class RemoteMouseEvent extends MouseEvent {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AbstractUser user;
	
	/**
	 * @param source
	 * @param id
	 * @param when
	 * @param modifiers
	 * @param x
	 * @param y
	 * @param clickCount
	 * @param popupTrigger
	 */
	public RemoteMouseEvent(Component source, int id, long when, int modifiers,	int x, int y, int clickCount, boolean popupTrigger) {
		super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
	}

	
	/**
	 * 
	 * @param user
	 * @param source
	 * @param id
	 * @param when
	 * @param modifiers
	 * @param x
	 * @param y
	 * @param clickCount
	 * @param popupTrigger
	 * @param button
	 */
	public RemoteMouseEvent(AbstractUser user, Component source, int id, long when, int modifiers, int x, int y, int clickCount, boolean popupTrigger, int button) {
		super(source, id, when, modifiers, x, y, clickCount, popupTrigger, button);
		this.user = user;
	}

	
	/**
	 * @param source
	 * @param id
	 * @param when
	 * @param modifiers
	 * @param x
	 * @param y
	 * @param xAbs
	 * @param yAbs
	 * @param clickCount
	 * @param popupTrigger
	 * @param button
	 */
	public RemoteMouseEvent(Component source, int id, long when, int modifiers,	int x, int y, int xAbs, int yAbs, int clickCount, boolean popupTrigger, int button) {
		super(source, id, when, modifiers, x, y, xAbs, yAbs, clickCount, popupTrigger, button);
	}


	/**
	 * @param user the user to set
	 */
//	public void setUser(RemoteUser user) {
//		this.user = user;
//	}


	/**
	 * @return the user
	 */
	public AbstractUser getUser() {
		return user;
	}

}
