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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import ar.com.tellapic.utils.Utils;
/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class DrawingTool extends Tool {
	protected AbstractDrawing        temporalDrawing;
	protected boolean                symmetricMode;
	
	/**
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public DrawingTool(int id, String name, String iconPath) {
		this(id, name, iconPath, null);
	}


	/**
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public DrawingTool(int id, String name, String iconPath, String description) {
		this(id, name, iconPath, description, null);
	}


	/**
	 * 
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public DrawingTool(int id, String name, String iconPath, String description, Cursor cursor) {
		super(id, name, iconPath, description, cursor);
		symmetricMode = false;
	}
	
	/**
	 * 
	 * @param cursorPath
	 * @param xOffset
	 * @param yOffset
	 */
	public void setToolCursor(String cursorPath, int xOffset, int yOffset) {
		Image image = Utils.createIconImage(12, 12, cursorPath);
		setCursor(Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(xOffset, yOffset), getName()));
	}

	/**
	 * 
	 * @return
	 */
	public AbstractDrawing getTemporalDrawing() {
		return temporalDrawing;
	}
	
	/**
	 * 
	 * @param drawing
	 */
	public void setTemporalDrawing(AbstractDrawing drawing) {
		temporalDrawing = drawing;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSymmetricModeEnabled() {
		return symmetricMode;
	}
	
	/**
	 * 
	 * @param enabled
	 */
	public void setSymmetricMode(boolean enabled) {
		symmetricMode = enabled;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean hasAlphaCapability();
	public abstract boolean hasColorCapability();
	public abstract boolean hasFontCapability();
	public abstract boolean hasStrokeCapability();
	public abstract boolean hasFillCapability();
	public abstract double getDefaultAlpha();
	public abstract Color  getDefaultColor();
	public abstract String getDefaultFontFace();
	public abstract double getDefaultFontSize();
	public abstract int    getDefaultFontStyle();
	public abstract double getDefaultWidth();
	public abstract float  getDefaultMiterLimit();
	public abstract int    getDefaultCaps();
	public abstract int    getDefaultJoins();
}
