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

import java.awt.geom.Rectangle2D;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingShapeRectangle extends DrawingShape {
private Rectangle2D rectangle;
	
	public DrawingShapeRectangle(String name, double x, double y, double w, double h) {
		super(name, true, true);
		rectangle = new Rectangle2D.Double(x, y, w, h);
		setShape(rectangle);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(int, int, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double eventX, double eventY, ControlPoint controlPoint) {
		switch(controlPoint.getType()) {
		case BOTTOM_CONTROL_POINT:
			rectangle.setFrame(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), (eventY < rectangle.getY())? 0 : eventY - rectangle.getY());
			break;
		case TOP_CONTROL_POINT:
			rectangle.setFrame(rectangle.getX(), (eventY > rectangle.getMaxY())? rectangle.getY() : eventY, rectangle.getWidth(), (eventY > rectangle.getMaxY())? 0 : rectangle.getMaxY() - eventY);
			break;
		case TOP_LEFT_CONTROL_POINT:
			rectangle.setFrame(
					((eventX > rectangle.getMaxX())? rectangle.getMaxX() : eventX), 
					((eventY > rectangle.getMaxY())? rectangle.getY() : eventY), 
					((eventX > rectangle.getMaxX())? 0 : rectangle.getMaxX() - eventX),
					((eventY > rectangle.getMaxY())? 0 : rectangle.getMaxY() - eventY)
			);
			break;
		case TOP_RIGHT_CONTROL_POINT:
			rectangle.setFrame(rectangle.getX(), ((eventY > rectangle.getMaxY())? rectangle.getMaxY() : eventY), ((eventX < rectangle.getX())? 0 : eventX - rectangle.getX()), ((eventY > rectangle.getMaxY())? 0 : rectangle.getMaxY() - eventY));
			break;

		case BOTTOM_RIGHT_CONTROL_POINT:
			rectangle.setFrame(rectangle.getX(), rectangle.getY(), ((eventX < rectangle.getX())? 0 : eventX - rectangle.getX()), ((eventY < rectangle.getY())? 0 : eventY - rectangle.getY()));
			break;

		case BOTTOM_LEFT_CONTROL_POINT:
			rectangle.setFrame(((eventX > rectangle.getMaxX())? rectangle.getMaxX() : eventX), rectangle.getY(), ((eventX > rectangle.getMaxX())? 0 : rectangle.getMaxX() - eventX), ((eventY < rectangle.getY())? 0 : eventY - rectangle.getY()));
			break;

		case LEFT_CONTROL_POINT:
			rectangle.setFrame(((eventX > rectangle.getMaxX())? rectangle.getMaxX() : eventX), rectangle.getY(), ((eventX > rectangle.getMaxX())? 0 : rectangle.getMaxX() - eventX), rectangle.getHeight());
			break;

		case RIGHT_CONTROL_POINT:
			rectangle.setFrame(rectangle.getX(), rectangle.getY(), ((eventX < rectangle.getX())? 0 : eventX - rectangle.getX()), rectangle.getHeight());
			break;
		}
		updateControlPoints();
		properties[PropertyType.X1COORD.ordinal()][VALUE_COLUMN] = rectangle.getX();
		properties[PropertyType.Y1COORD.ordinal()][VALUE_COLUMN] = rectangle.getY();
		properties[PropertyType.X2COORD.ordinal()][VALUE_COLUMN] = rectangle.getMaxX();
		properties[PropertyType.Y2COORD.ordinal()][VALUE_COLUMN] = rectangle.getMaxY();
		setChanged();
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		rectangle.setFrame(rectangle.getX() + xOffset, rectangle.getY() + yOffset, rectangle.getWidth(), rectangle.getHeight());
		properties[PropertyType.X1COORD.ordinal()][VALUE_COLUMN] = rectangle.getX();
		properties[PropertyType.Y1COORD.ordinal()][VALUE_COLUMN] = rectangle.getY();
		properties[PropertyType.X2COORD.ordinal()][VALUE_COLUMN] = rectangle.getMaxX();
		properties[PropertyType.Y2COORD.ordinal()][VALUE_COLUMN] = rectangle.getMaxY();
		updateControlPoints();
//		setChanged();
//		notifyObservers();
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return rectangle.isEmpty();
	}
}
