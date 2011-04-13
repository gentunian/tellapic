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

import java.awt.Cursor;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class ControlTool extends Tool {
	
	/**
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public ControlTool(int id, String name, String iconPath) {
		super(id, name, iconPath);

	}

	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public ControlTool(int id, String name, String iconPath, String description, Cursor cursor) {
		super(id, name, iconPath, description, cursor);
	}
	
	public abstract boolean hasZoomCapability();
}
