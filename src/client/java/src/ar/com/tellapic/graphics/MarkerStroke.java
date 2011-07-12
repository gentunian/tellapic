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

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class MarkerStroke implements StrokeProvider, Stroke {
	
	private double angle;
	private int   offset;
	private float halfWidth; 
	
	public MarkerStroke(double angle) {
		this.angle = angle;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.StrokeProvider#buildStroke(float, int, int, float, float[], float)
	 */
	@Override
	public Stroke buildStroke(float width, int endCaps, int lineJoins, float miterLimit, float[] newdash, float dashPhase) {
		this.halfWidth = width / 2;
		offset = (int) (Math.atan(angle) * halfWidth);
		return this;
	}

	/* (non-Javadoc)
	 * @see java.awt.Stroke#createStrokedShape(java.awt.Shape)
	 */
	@Override
	public Shape createStrokedShape(Shape shape) {
		GeneralPath result = new GeneralPath();
		Line2D line = (Line2D) shape;
		if (line.getY1() == line.getY2()) {
			/* Horizontal line */
			result.moveTo(line.getX1() - offset, line.getY1() + halfWidth);
			result.lineTo(line.getX2() - offset, line.getY1() + halfWidth);
			result.lineTo(line.getX2() + offset, line.getY1() - halfWidth);
			result.lineTo(line.getX1() + offset, line.getY1() - halfWidth);
			result.closePath();	
		} else { 
			/* Vertical line */
			result.moveTo(line.getX1() - offset, line.getY1() - halfWidth);
			result.lineTo(line.getX1() - offset, line.getY2() - halfWidth);
			result.lineTo(line.getX1() + offset, line.getY2() + halfWidth);
			result.lineTo(line.getX1() + offset, line.getY1() + halfWidth);
			result.closePath();
		}

		return result;
	}

}
