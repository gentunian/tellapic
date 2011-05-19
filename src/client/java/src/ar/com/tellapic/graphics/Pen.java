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
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Pen extends DrawingTool {
	private static final String PEN_ICON_PATH = "/icons/tools/pencil.png";
	private static final String PEN_CURSOR_PATH = "/icons/tools/pencil-cursor.png";
	private static final double DEFAULT_WIDTH = 4;
	private static final double DEFAULT_ALPHA = 1;
	private static final int DEFAULT_CAPS = 0;
	private static final Color DEFAULT_COLOR = Color.blue;
	private static final int DEFAULT_JOINS = 0;
	private static final float DEFAULT_MITER_LIMIT = 1;
	private Point2D             firstPoint;
	private boolean             inUse;
	private GeneralPath         pen;
//	private Drawing             temporalDrawing;

	/**
	 * 
	 */
	public Pen() {
		this("Pen");
	}
	
	
	/**
	 * 
	 * @param name
	 */
	public Pen(String name) {
		super(tellapicConstants.TOOL_PATH, name, PEN_ICON_PATH, Utils.msg.getString("pentooltip") ,Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse = false;
		temporalDrawing = new Drawing(getName());
//		setToolCursor(PEN_ICON_PATH, 0, 15);
	}
	

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getInit()
	 */
	@Override
	public Point2D getInit() {
		return firstPoint;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasColorCapability()
	 */
	@Override
	public boolean hasColorCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasFontCapability()
	 */
	@Override
	public boolean hasFontCapability() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeCapability()
	 */
	@Override
	public boolean hasStrokeCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		return inUse;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isLiveModeSupported()
	 */
	@Override
	public boolean isLiveModeSupported() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isOnMoveSupported()
	 */
	@Override
	public boolean isOnMoveSupported() {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	public void onPause() {
		inUse = false;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onDrag(int, int, boolean, int)
	 */
	@Override
	public void onDrag(int x, int y, int button, int mask) {
		if (inUse) {
			pen.lineTo(x, y);
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onPress(int, int, int, int)
	 */
	@Override
	public void onPress(int x, int y, int button, int mask) {
		firstPoint.setLocation(x, y);
		pen = new GeneralPath();
		pen.moveTo(x, y);
		inUse = true;
		temporalDrawing.setShape(pen);
		setChanged();
		notifyObservers(temporalDrawing);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRelease(int, int, int)
	 */
	@Override
	public void onRelease(int x, int y, int button, int mask) {
		if (inUse) {
			pen.lineTo(x, y);
//			temporalDrawing.cloneProperties();
			inUse = false;
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRestore()
	 */
	@Override
	public void onRestore() {
		inUse = true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setAlpha(ar.com.tellapic.graphics.PaintPropertyAlpha)
	 */
	@Override
	public void setAlpha(PaintPropertyAlpha alpha) {
		temporalDrawing.setAlpha(alpha);
		setChanged();
		notifyObservers(temporalDrawing);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setColor(ar.com.tellapic.graphics.PaintPropertyColor)
	 */
	@Override
	public void setColor(PaintPropertyColor color) {
		temporalDrawing.setColor(color);
		setChanged();
		notifyObservers(temporalDrawing);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setFont(ar.com.tellapic.graphics.PaintPropertyFont)
	 */
	@Override
	public void setFont(PaintPropertyFont font) {
		temporalDrawing.setFont(font);
		setChanged();
		notifyObservers(temporalDrawing);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setStroke(ar.com.tellapic.graphics.PaintPropertyStroke)
	 */
	@Override
	public void setStroke(PaintPropertyStroke stroke) {
		temporalDrawing.setStroke(stroke);
		setChanged();
		notifyObservers(temporalDrawing);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#isOnDragSupported()
	 */
	@Override
	public boolean isOnDragSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#isOnPressSupported()
	 */
	@Override
	public boolean isOnPressSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#isOnReleaseSupported()
	 */
	@Override
	public boolean isOnReleaseSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onMove(int, int)
	 */
	@Override
	public void onMove(int x, int y) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultWidth()
	 */
	@Override
	public double getDefaultWidth() {
		return DEFAULT_WIDTH;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultAlpha()
	 */
	@Override
	public double getDefaultAlpha() {
		return DEFAULT_ALPHA;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultCaps()
	 */
	@Override
	public int getDefaultCaps() {
		return DEFAULT_CAPS;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultColor()
	 */
	@Override
	public Color getDefaultColor() {
		return DEFAULT_COLOR;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontFace()
	 */
	@Override
	public String getDefaultFontFace() {
		return null;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontSize()
	 */
	@Override
	public double getDefaultFontSize() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontStyle()
	 */
	@Override
	public int getDefaultFontStyle() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultJoins()
	 */
	@Override
	public int getDefaultJoins() {
		return DEFAULT_JOINS;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultMiterLimit()
	 */
	@Override
	public float getDefaultMiterLimit() {
		return DEFAULT_MITER_LIMIT;
	}
}
