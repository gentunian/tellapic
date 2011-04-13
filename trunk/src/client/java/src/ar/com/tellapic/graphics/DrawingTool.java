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
public abstract class DrawingTool extends Tool {

	
	/**
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public DrawingTool(int id, String name, String iconPath) {
		super(id, name, iconPath);

	}

	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public DrawingTool(int id, String name, String iconPath, String description, Cursor cursor) {
		super(id, name, iconPath, description, cursor);
	}
	
	public abstract Drawing getDrawing();
	
	public abstract boolean hasAlphaCapability();
	public abstract boolean hasColorCapability();
	public abstract boolean hasFontCapability();
	public abstract boolean hasStrokeCapability();
	

	
	public abstract boolean isFilleable();

	
	public abstract boolean isLiveModeSupported();
	public abstract boolean isOnDragSupported();
	public abstract boolean isOnPressSupported();
	public abstract boolean isOnReleaseSupported();

	public abstract void setAlpha(PaintPropertyAlpha alpha);
	public abstract void setColor(PaintPropertyColor color);
	public abstract void setFont(PaintPropertyFont font);
	public abstract void setStroke(PaintPropertyStroke stroke);
}
