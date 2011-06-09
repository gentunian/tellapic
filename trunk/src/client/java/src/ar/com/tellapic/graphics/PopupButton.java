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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class PopupButton extends JButton {

	private static final int LEFT_MARGIN   = 3;
	private static final int BOTTOM_MARGIN = 3;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param imageIcon
	 */
	public PopupButton(ImageIcon imageIcon) {
		super(imageIcon);
	}

	/**
	 * 
	 */
	public PopupButton() {
		super();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		((Graphics2D) g).addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		
		g.fillPolygon(
				new int[] { 
						getWidth() - LEFT_MARGIN - 6,
						getWidth() - LEFT_MARGIN,
						getWidth() - LEFT_MARGIN - 3
				},
				new int[] { 
						getHeight()- BOTTOM_MARGIN - 4,
						getHeight() - BOTTOM_MARGIN - 4,
						getHeight() - BOTTOM_MARGIN
				},
				3);
	}
}
