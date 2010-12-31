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
import java.awt.Composite;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class AbstractDrawing {

	
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
	public abstract Stroke getStroke();
	
	/**
	 * @return the color
	 */
	public abstract Color getColor();

	/**
	 * @return the alpha
	 */
	public abstract Composite getAlpha();

	/**
	 * @return the font
	 */
	public abstract Font getFont();
	
	public abstract boolean hasFontProperty();
	
	public abstract boolean hasColorProperty();
	
	public abstract boolean hasStrokeProperty();
	
	public abstract boolean hasAlphaProperty();
}
