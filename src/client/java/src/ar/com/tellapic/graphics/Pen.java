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
public class Pen extends Tool {
	private Point2D             firstPoint;
	private boolean             inUse;
	private GeneralPath         pen;
	private Drawing             temporalDrawing;

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
		super(tellapicConstants.TOOL_PATH, name, "/icons/marker.png", Utils.msg.getString("pentooltip"), null);
		firstPoint = new Point2D.Double();
		inUse = false;
		temporalDrawing = new Drawing(getName());
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		if (inUse) {
			return temporalDrawing;
		}
		return null;
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
	public boolean hasAlphaProperties() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasColorProperties()
	 */
	@Override
	public boolean hasColorProperties() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasFontProperties()
	 */
	@Override
	public boolean hasFontProperties() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeProperties()
	 */
	@Override
	public boolean hasStrokeProperties() {
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
	 * @see ar.com.tellapic.graphics.Tool#moveTo(double, double)
	 */
	@Override
	public void moveTo(double x, double y) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	public void onCancel() {
		inUse = false;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onDrag(int, int, boolean, int)
	 */
	@Override
	public void onDrag(int x, int y, int button, int mask) {
		if (inUse) {
			pen.lineTo(x, y);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onMove(int, int)
	 */
	@Override
	public Drawing onMove(int x, int y) {
		return null;
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
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRelease(int, int, int)
	 */
	@Override
	public Drawing onRelease(int x, int y, int button, int mask) {
		if (inUse) {
			pen.lineTo(x, y);
			temporalDrawing.cloneProperties();
			inUse = false;
			return temporalDrawing;
		}
		return null;
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
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setColor(ar.com.tellapic.graphics.PaintPropertyColor)
	 */
	@Override
	public void setColor(PaintPropertyColor color) {
		temporalDrawing.setColor(color);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setFont(ar.com.tellapic.graphics.PaintPropertyFont)
	 */
	@Override
	public void setFont(PaintPropertyFont font) {
		temporalDrawing.setFont(font);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setStroke(ar.com.tellapic.graphics.PaintPropertyStroke)
	 */
	@Override
	public void setStroke(PaintPropertyStroke stroke) {
		temporalDrawing.setStroke(stroke);
	}
}
