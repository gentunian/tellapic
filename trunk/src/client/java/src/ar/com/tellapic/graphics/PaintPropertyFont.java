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
import java.awt.Font;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class PaintPropertyFont extends PaintProperty {
	public enum FontStyle {
		Plain,
		Bold,
		Italic,
		Bold_Italic;
		
		@Override
		public String toString() {
			return super.toString().replaceAll("_", " ");
		};
	};
	public static final String[] CLI_CMDS = new String[] { 
		"ar.com.tellapic.graphics.DrawingText setText({String_text_The_text_to_display}) Sets a text to be displayed in the specified coordinates.",
		"ar.com.tellapic.graphics.DrawingText setFace({String_face_The_font_face}) Sets the font face.",
		"ar.com.tellapic.graphics.DrawingText setStyle({String_style_The_font_style}) Sets the font style: bold, plain, italic or italic+bold.",
		"ar.com.tellapic.graphics.DrawingText setSize({float_size_The_font_size}) Sets the font size.",
		"ar.com.tellapic.graphics.DrawingText setColor({Strin_color_The_color_of_this_text}) Sets the color for the text."
	};
	private Color        color;
	private String       text;
	private String       face;
	private float        size;
	private FontStyle    style;
	
	/**
	 * 
	 * @param font
	 */
	public PaintPropertyFont(Font font) {
		super(PaintPropertyType.FONT);
		face = font.getName();
		size = font.getSize2D();
		style = FontStyle.values()[ font.getStyle() ];
	}
	
	/**
	 * 
	 */
	public PaintPropertyFont() {
		super(PaintPropertyType.FONT);
		text  = "";
		face  = Font.SERIF;
		size  = 12;
		style = FontStyle.Plain;
		color = Color.black;
	}
	
	/**
	 * 
	 * @param font
	 */
	public PaintPropertyFont(String font) {
		super(PaintPropertyType.FONT);
		Font f = Font.decode(font);
		setFont(f);
	}
	
	/**
	 * @param face the face to set
	 */
	public void setFace(String face) {
		//TODO: face should be a valid face
		this.face = face;
	}

	/**
	 * @return the face
	 */
	public String getFace() {
		return face;
	}

	/**
	 * 
	 * @param size
	 */
	public void setSize(String size) {
		try {
			setSize(Float.valueOf(size));
		} catch(Exception e) {
			//TODO:
		}
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
	 * 
	 * @param style
	 */
	public void setStyle(String style) {
		if (style.toLowerCase().equals("plain"))
			setStyle(FontStyle.Plain);
		
		else if (style.toLowerCase().equals("bold"))
			setStyle(FontStyle.Bold);
		
		else if (style.toLowerCase().equals("italic"))
			setStyle(FontStyle.Italic);
		
		else if (style.toLowerCase().matches("italic[ ]*\\+[ ]*bold|bold[ ]*\\+[ ]*italic"))
			setStyle(FontStyle.Bold_Italic);
	}
	
	/**
	 * @param style the style to set
	 */
	public void setStyle(FontStyle style) {
		this.style = style;
	}

	/**
	 * @return the style
	 */
	public FontStyle getStyle() {
		return style;
	}
	
	/**
	 * 
	 * @return
	 */
	public Font getFont() {
		return Font.decode(face).deriveFont(style.ordinal(), size);
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
	
	/**
	 * 
	 */
	public String toString() {
		return face+" "+style+" "+(int)size;
	}

	/**
	 * 
	 * @param color
	 */
	public void setColor(String color) {
		this.color = Color.decode(color);
	}
	
	/**
	 * 
	 * @param c
	 */
	public void setColor(Color c) {
		color = c;
	}
	
	/**
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param selectedFont
	 */
	public void setFont(Font f) {
		face = f.getName();
		size = f.getSize2D();
		style = FontStyle.values()[ f.getStyle()];
	}
}
