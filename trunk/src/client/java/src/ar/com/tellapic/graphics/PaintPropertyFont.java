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

import java.awt.Font;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class PaintPropertyFont extends PaintProperty {
	private String text;
	private String face;
	private float    size;
	private int    style;
	
	public PaintPropertyFont() {
		super(PaintPropertyType.FONT);
		text  = "";
		face  = Font.SERIF;
		size  = 12;
		style = Font.PLAIN;
	}
	/*
	private static class PaintPropertyFontHolder {
		private static final PaintPropertyFont INSTANCE = new PaintPropertyFont();
	}
	
	
	public static PaintPropertyFont getInstance() {
		return PaintPropertyFontHolder.INSTANCE;
	}
	 */

	/**
	 * @param face the face to set
	 */
	public void setFace(String face) {
		this.face = face;
	}


	/**
	 * @return the face
	 */
	public String getFace() {
		return face;
	}


	/**
	 * @param size the size to set
	 */
	public void setSize(float size) {
		this.size = size;
	}


	/**
	 * @return the size
	 */
	public float getSize() {
		return size;
	}


	/**
	 * @param style the style to set
	 */
	public void setStyle(int style) {
		this.style = style;
	}


	/**
	 * @return the style
	 */
	public int getStyle() {
		return style;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Font getFont() {
		return Font.decode(face).deriveFont(style, size);
	}


	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}


	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
}
