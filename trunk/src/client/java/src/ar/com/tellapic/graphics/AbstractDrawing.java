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

import java.awt.Shape;

import ar.com.tellapic.AbstractUser;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class AbstractDrawing {

	
	private boolean             isVisible;
	private AbstractUser        user;
	
	/**
	 * @return the text
	 */
	public abstract String getText();
	
	/**
	 * @param shape the shape to set
	 */
	public abstract void setShape(Shape shape);
	
	/**
	 * @return the shape
	 */
	public abstract Shape getShape();
	
	/**
	 * @return the stroke
	 */
	public abstract PaintPropertyStroke getPaintPropertyStroke();
	
	/**
	 * @return the color
	 */
	public abstract PaintPropertyColor getPaintPropertyColor();

	/**
	 * @return the alpha
	 */
	public abstract PaintPropertyAlpha getPaintPropertyAlpha();

	/**
	 * @return the font
	 */
	public abstract PaintPropertyFont getPaintPropertyFont();
	
	public boolean isVisible() {
		return isVisible;
	}
	
	public void setVisible(boolean visible) {
		isVisible = visible;
	}
	/**
	 * 
	 * @return
	 */
//	public abstract PaintPropertyZoom getPaintPropertyZoom();
	
	public abstract boolean hasFontProperty();
	
	public abstract boolean hasColorProperty();
	
	public abstract boolean hasStrokeProperty();
	
	public abstract boolean hasAlphaProperty();
	
//	public abstract boolean hasZoomProperty();

	/**
	 * @param user the user to set
	 */
	public void setUser(AbstractUser user) {
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public AbstractUser getUser() {
		return user;
	}
	
	
}
