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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Grid extends BufferedImage {

	public static Color DEFAULT_COLOR = Color.gray;
	private boolean gridEnabled;
	private int     gridSize = 1;
	private Color   gridColor;
	private float   gridTransparency;
	private Dimension dimension;
	
	
	/**
	 * 
	 */
	public Grid(int w, int h) {
		this(w, h, true, 1, Color.gray, 1.0f);
	}
	
	
	/**
	 * 
	 * @param enabled
	 * @param size
	 * @param color
	 * @param alpha
	 */
	public Grid(int w, int h, boolean enabled, int size, Color color, float alpha) {
		super(w, h, BufferedImage.TYPE_INT_ARGB);
		
		gridEnabled = enabled;
		gridSize = size;
		gridColor = color;
		gridTransparency = alpha;
		dimension = new Dimension(w,h);
		
		drawGrid();
	}


	/**
	 * 
	 */
	private void drawGrid() {
		Graphics2D g = createGraphics();
		
		g.setBackground(new Color(0,0,0,0.0f));
		g.setColor(gridColor);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, gridTransparency));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.clearRect(0, 0, dimension.width, dimension.height);
		
		/* How many dots (pixels) are in a cm? */
		int dpcm = (int)((double) RuleHeader.INCH / (double)2.546);

		/* How many lines will be in a cm? */
		int linesInCm =  gridSize;

		/* How long will be the space between lines */
		double divisionSize =  ((double)dpcm / (double)(gridSize));

		/* How many vertical unit lines do we need to draw? */
		int vLines = (int) Math.round(getWidth() / dpcm);

		/* How many horizontal unit lines do we need to draw? */
		int hLines = (int) Math.round(getHeight() / dpcm);

		/* Take the problem as divide and conquer in the sense that */
		/* treat it as drawing lines between a centimeter. Repeat it */
		/* until we draw all centimeters. */
		for(int i = 0; i < vLines; i++) {
			int x = dpcm * i;;
			for (int j = 0; j < linesInCm; j++) {
				x += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
				g.drawLine(x, 0, x, getHeight());
			}
		}

		for(int i = 0; i < hLines; i++) {
			int y = dpcm * i;;
			for (int j = 0; j < linesInCm; j++) {
				y += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
				g.drawLine(0, y, getWidth(), y);
			}
		}	
	}


	/**
	 * @return the gridEnabled
	 */
	public boolean isGridEnabled() {
		return this.gridEnabled;
	}


	/**
	 * @return the gridSize
	 */
	public int getGridSize() {
		return this.gridSize;
	}


	/**
	 * @return the gridColor
	 */
	public Color getGridColor() {
		return this.gridColor;
	}


	/**
	 * @return the gridTransparency
	 */
	public float getGridTransparency() {
		return this.gridTransparency;
	}


	/**
	 * @param gridEnabled the gridEnabled to set
	 */
	public void setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
	}


	/**
	 * @param gridSize the gridSize to set
	 */
	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
		drawGrid();
	}


	/**
	 * @param gridColor the gridColor to set
	 */
	public void setGridColor(Color gridColor) {
		this.gridColor = gridColor;
		drawGrid();
	}


	/**
	 * @param gridTransparency the gridTransparency to set
	 */
	public void setGridTransparency(float gridTransparency) {
		this.gridTransparency = gridTransparency;
		drawGrid();
	}

}
