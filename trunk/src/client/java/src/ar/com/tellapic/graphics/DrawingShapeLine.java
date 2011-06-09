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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingShapeLine extends DrawingShape {
	private Line2D line;
	
	/**
	 * 
	 * @param name
	 * @param p1
	 * @param p2
	 */
	public DrawingShapeLine(String name, Point2D p1, Point2D p2) {
		this(name, p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	/**
	 * @param name
	 * @param resizeable
	 * @param moveable
	 */
	public DrawingShapeLine(String name, double x1, double y1, double x2, double y2) {
		super(name, true, true);
		line = new Line2D.Double(x1, y1, x2, y2);
		setShape(line);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(int, int, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double eventX, double eventY, ControlPoint controlPoint) {
		Point2D p1 = line.getP1();
		Point2D p2 = line.getP2();
		switch(controlPoint.getType()) {
		case BOTTOM_CONTROL_POINT:
			if (p1.getY() > p2.getY())
				p1.setLocation(p1.getX(), eventY > p2.getY()? eventY : p2.getY()+1);
			else
				p2.setLocation(p2.getX(), eventY > p1.getY()? eventY : p1.getY()+1);
			break;
			
		case TOP_CONTROL_POINT:
			if (p1.getY() < p2.getY())
				p1.setLocation(p1.getX(), eventY < p2.getY()? eventY : p2.getY()-1);
			else
				p2.setLocation(p2.getX(), eventY < p1.getY()? eventY : p1.getY()-1);
			break;
			
		case TOP_LEFT_CONTROL_POINT:
			if (p1.getX() < p2.getX())
				p1.setLocation(eventX < p2.getX()? eventX : p2.getX()-1, p1.getY());
			else
				p2.setLocation(eventX < p1.getX()? eventX : p1.getX()-1, p2.getY());
			if (p1.getY() < p2.getY())
				p1.setLocation(p1.getX(), eventY < p2.getY()? eventY : p2.getY()-1);
			else
				p2.setLocation(p2.getX(), eventY < p1.getY()? eventY : p1.getY()-1);
			break;
		case TOP_RIGHT_CONTROL_POINT:
			if (p1.getX() > p2.getX())
				p1.setLocation(eventX > p2.getX()? eventX : p2.getX()+1, p1.getY());
			else
				p2.setLocation(eventX > p1.getX()? eventX : p1.getX()+1, p2.getY());
			if (p1.getY() < p2.getY())
				p1.setLocation(p1.getX(), eventY < p2.getY()? eventY : p2.getY()-1);
			else
				p2.setLocation(p2.getX(), eventY < p1.getY()? eventY : p1.getY()-1);
			break;

		case BOTTOM_RIGHT_CONTROL_POINT:
			if (p1.getX() > p2.getX())
				p1.setLocation(eventX > p2.getX()? eventX : p2.getX()+1, p1.getY());
			else
				p2.setLocation(eventX > p1.getX()? eventX : p1.getX()+1, p2.getY());
			if (p1.getY() > p2.getY())
				p1.setLocation(p1.getX(), eventY > p2.getY()? eventY : p2.getY()+1);
			else
				p2.setLocation(p2.getX(), eventY > p1.getY()? eventY : p1.getY()+1);
			break;

		case BOTTOM_LEFT_CONTROL_POINT:
			if (p1.getX() < p2.getX())
				p1.setLocation(eventX < p2.getX()? eventX : p2.getX()-1, p1.getY());
			else
				p2.setLocation(eventX < p1.getX()? eventX : p1.getX()-1, p2.getY());
			if (p1.getY() > p2.getY())
				p1.setLocation(p1.getX(), eventY > p2.getY()? eventY : p2.getY()+1);
			else
				p2.setLocation(p2.getX(), eventY > p1.getY()? eventY : p1.getY()+1);
			break;

		case LEFT_CONTROL_POINT:
			if (p1.getX() < p2.getX())
				p1.setLocation(eventX < p2.getX()? eventX : p2.getX()-1, p1.getY());
			else
				p2.setLocation(eventX < p1.getX()? eventX : p1.getX()-1, p2.getY());
			break;

		case RIGHT_CONTROL_POINT:
			if (p1.getX() > p2.getX())
				p1.setLocation(eventX > p2.getX()? eventX : p2.getX()+1, p1.getY());
			else
				p2.setLocation(eventX > p1.getX()? eventX : p1.getX()+1, p2.getY());
			break;
		}
		line.setLine(p1, p2);
		updateControlPoints();
		properties[PropertyType.X1COORD.ordinal()][VALUE_COLUMN] = line.getX1();
		properties[PropertyType.Y1COORD.ordinal()][VALUE_COLUMN] = line.getY1();
		properties[PropertyType.X2COORD.ordinal()][VALUE_COLUMN] = line.getX2();
		properties[PropertyType.Y2COORD.ordinal()][VALUE_COLUMN] = line.getY2();
		setChanged();
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		line.setLine(line.getX1() + xOffset, line.getY1() + yOffset, line.getX2() + xOffset, line.getY2() + yOffset);
		properties[PropertyType.X1COORD.ordinal()][VALUE_COLUMN] = line.getX1();
		properties[PropertyType.Y1COORD.ordinal()][VALUE_COLUMN] = line.getY1();
		properties[PropertyType.X2COORD.ordinal()][VALUE_COLUMN] = line.getX2();
		properties[PropertyType.Y2COORD.ordinal()][VALUE_COLUMN] = line.getY2();
		updateControlPoints();
		setChanged();
		notifyObservers();
	}
}
