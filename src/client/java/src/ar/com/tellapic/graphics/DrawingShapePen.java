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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingShapePen extends DrawingShape {
	private GeneralPath pen;
	
	public DrawingShapePen(String name, double x, double y) {
		super(name, false, true);
		pen = new GeneralPath();
		pen.moveTo(x, y);
		setShape(pen);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		pen = (GeneralPath) pen.createTransformedShape(AffineTransform.getTranslateInstance(xOffset, yOffset));
		setShape(pen);
		updateControlPoints();
		setChanged();
		notifyObservers(new Object[] {MOVED});
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(double, double, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double eventX, double eventY, ControlPoint controlPoint) {
	}

	/**
	 * @return
	 */
	public boolean hasPoints() {
		return (pen.getCurrentPoint() != null);
	}
}
