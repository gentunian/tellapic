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

import java.awt.geom.Ellipse2D;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingShapeEllipse extends DrawingShape {
	private Ellipse2D ellipse;
	
	public DrawingShapeEllipse(String name, double x, double y, double w, double h) {
		super(name, true, true);
		ellipse = new Ellipse2D.Double(x, y, w, h);
		setShape(ellipse);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(int, int, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double eventX, double eventY, ControlPoint controlPoint) {
		switch(controlPoint.getType()) {
		case BOTTOM_CONTROL_POINT:
			ellipse.setFrame(ellipse.getX(), ellipse.getY(), ellipse.getWidth(), (eventY < ellipse.getY())? 0 : eventY - ellipse.getY());
			break;
		case TOP_CONTROL_POINT:
			ellipse.setFrame(ellipse.getX(), (eventY > ellipse.getMaxY())? ellipse.getY() : eventY, ellipse.getWidth(), (eventY > ellipse.getMaxY())? 0 : ellipse.getMaxY() - eventY);
			break;
		case TOP_LEFT_CONTROL_POINT:
			ellipse.setFrame(
					((eventX > ellipse.getMaxX())? ellipse.getMaxX() : eventX), 
					((eventY > ellipse.getMaxY())? ellipse.getY() : eventY), 
					((eventX > ellipse.getMaxX())? 0 : ellipse.getMaxX() - eventX),
					((eventY > ellipse.getMaxY())? 0 : ellipse.getMaxY() - eventY)
			);
			break;
		case TOP_RIGHT_CONTROL_POINT:
			ellipse.setFrame(ellipse.getX(), ((eventY > ellipse.getMaxY())? ellipse.getMaxY() : eventY), ((eventX < ellipse.getX())? 0 : eventX - ellipse.getX()), ((eventY > ellipse.getMaxY())? 0 : ellipse.getMaxY() - eventY));
			break;

		case BOTTOM_RIGHT_CONTROL_POINT:
			ellipse.setFrame(ellipse.getX(), ellipse.getY(), ((eventX < ellipse.getX())? 0 : eventX - ellipse.getX()), ((eventY < ellipse.getY())? 0 : eventY - ellipse.getY()));
			break;

		case BOTTOM_LEFT_CONTROL_POINT:
			ellipse.setFrame(((eventX > ellipse.getMaxX())? ellipse.getMaxX() : eventX), ellipse.getY(), ((eventX > ellipse.getMaxX())? 0 : ellipse.getMaxX() - eventX), ((eventY < ellipse.getY())? 0 : eventY - ellipse.getY()));
			break;

		case LEFT_CONTROL_POINT:
			ellipse.setFrame(((eventX > ellipse.getMaxX())? ellipse.getMaxX() : eventX), ellipse.getY(), ((eventX > ellipse.getMaxX())? 0 : ellipse.getMaxX() - eventX), ellipse.getHeight());
			break;

		case RIGHT_CONTROL_POINT:
			ellipse.setFrame(ellipse.getX(), ellipse.getY(), ((eventX < ellipse.getX())? 0 : eventX - ellipse.getX()), ellipse.getHeight());
			break;
		}
		updateControlPoints();
		properties[PropertyType.X1COORD.ordinal()][VALUE_COLUMN] = ellipse.getX();
		properties[PropertyType.Y1COORD.ordinal()][VALUE_COLUMN] = ellipse.getY();
		properties[PropertyType.X2COORD.ordinal()][VALUE_COLUMN] = ellipse.getMaxX();
		properties[PropertyType.Y2COORD.ordinal()][VALUE_COLUMN] = ellipse.getMaxY();
		setChanged();
		notifyObservers(new Object[]{RESIZED});
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		ellipse.setFrame(ellipse.getX() + xOffset, ellipse.getY() + yOffset, ellipse.getWidth(), ellipse.getHeight());
		properties[PropertyType.X1COORD.ordinal()][VALUE_COLUMN] = ellipse.getX();
		properties[PropertyType.Y1COORD.ordinal()][VALUE_COLUMN] = ellipse.getY();
		properties[PropertyType.X2COORD.ordinal()][VALUE_COLUMN] = ellipse.getMaxX();
		properties[PropertyType.Y2COORD.ordinal()][VALUE_COLUMN] = ellipse.getMaxY();
		updateControlPoints();
		setChanged();
		notifyObservers(new Object[]{MOVED});
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return ellipse.isEmpty();
	}
}
