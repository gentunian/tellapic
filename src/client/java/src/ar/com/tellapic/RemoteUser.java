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

import ar.com.tellapic.graphics.PaintPropertyController;
import ar.com.tellapic.graphics.ToolBoxController;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class RemoteUser extends AbstractUser {
	
	public RemoteUser() {
		super();
		setToolboxController(new ToolBoxController(getToolBoxModel()));
		setPaintController(new PaintPropertyController(getToolBoxModel()));
	}
	
	/**
	 * @param id
	 */
	public RemoteUser(int id, String name) {
		super(id, name);
		setToolboxController(new ToolBoxController(getToolBoxModel()));
		setPaintController(new PaintPropertyController(getToolBoxModel()));
	}

	/*
	public void selectTool(String name) {
		toolboxController.selectToolByName(name);
	}
	*/
	
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
