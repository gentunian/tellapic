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
import java.awt.Paint;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class PaintPropertyFill extends PaintProperty {

	private Paint fillPaint;

	public static final String[] CLI_CMDS = new String[] {
		"ar.com.tellapic.graphics.AbstractDrawing setFillColor({String_color_The_filling_color}) Sets this shape fill  color if applicable (lines wont be filled).",
	};
	
	/**
	 * @param type
	 * @throws IllegalArgumentException
	 */
	public PaintPropertyFill(PaintPropertyType type) {
		super(type);
		fillPaint = new Color(0,0,0,0);
	}

	
	/**
	 * 
	 */
	public PaintPropertyFill() {
		this(PaintPropertyType.FILL);
	}

	/**
	 * 
	 * @param color
	 */
	public void setFillColor(String color) {
		setFillColor(Color.decode(color));
	}
	
	/**
	 * 
	 * @param fillColor
	 */
	public void setFillColor(Color fillColor) {
		fillPaint = fillColor;
	}
	
	/**
	 * 
	 * @return
	 */
	public Paint getFillPaint() {
		return fillPaint;
	}
	
	/**
	 * 
	 */
	public String toString() {
		Color color = ((Color)fillPaint);
		return "0x" + Integer.toHexString(color.getRed()) + Integer.toHexString(color.getGreen()) + Integer.toHexString(color.getBlue());
	}
}
