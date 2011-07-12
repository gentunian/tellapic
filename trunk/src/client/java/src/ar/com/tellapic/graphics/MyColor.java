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

import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class MyColor extends Color {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 * @param r
	 * @param g
	 * @param b
	 */
	public MyColor(int r, int g, int b) {
		super(r,g,b);
	}
	
	/**
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public MyColor(int r, int g, int b, int a) {
		super(r, g, b, a);
	}
	
	/**
	 * @param rgb
	 */
	public MyColor(int rgb) {
		super(rgb);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.Color#toString()
	 */
	@Override
	public String toString() {
		return Utils.colorToHexa(this);
	}
}
