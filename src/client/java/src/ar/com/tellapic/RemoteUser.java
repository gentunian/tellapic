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

import ar.com.tellapic.graphics.IToolBoxController;
import ar.com.tellapic.graphics.ToolBoxController;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class RemoteUser extends AbstractUser {

	private IToolBoxController c;
	
	/**
	 * @param id
	 */
	public RemoteUser(int id, String name) {
		super(id, name);
		c = new ToolBoxController(getToolBoxModel());
	}

	public void selectTool(String name) {
		c.selectToolEvent(name);
	}
	
	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isRemote()
	 */
	@Override
	public boolean isRemote() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isSelected()
	 */
	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isSpecial()
	 */
	@Override
	public boolean isSpecial() {
		// TODO Auto-generated method stub
		return false;
	}
}
