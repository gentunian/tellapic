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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ControlPoint extends Ellipse2D{
	private static final String HORIZONTAL_RESIZE_ICON     = "/icons/tools/arrow-resize.png";
	private static final String VERTICAL_RESIZE_ICON       = "/icons/tools/arrow-resize-090.png";
	private static final String RIGHT_DIAGONAL_RESIZE_ICON = "/icons/tools/arrow-resize-045.png";
	private static final String LEFT_DIAGONAL_RESIZE_ICON  = "/icons/tools/arrow-resize-135.png";
	
	public static enum ControlType {
		LEFT_CONTROL_POINT,
		RIGHT_CONTROL_POINT,
		TOP_LEFT_CONTROL_POINT,
		BOTTOM_LEFT_CONTROL_POINT,
		TOP_RIGHT_CONTROL_POINT,
		BOTTOM_RIGHT_CONTROL_POINT,
		TOP_CONTROL_POINT,
		BOTTOM_CONTROL_POINT
	};
	public static final int    CONTROL_POINT_WIDTH  = 6;
	public static final int    CONTROL_POINT_HEIGHT = CONTROL_POINT_WIDTH;
	public static final double CORRECTION_FACTOR    = CONTROL_POINT_WIDTH / 2;
	
	
	private Ellipse2D    controlPoint;
	private ControlType  controlType;
	private Color        controlPointColor;
	private BasicStroke  stroke;
	private Cursor       cursor;
	
	/**
	 * 
	 * @param type
	 * @param shape
	 * @param color
	 * @throws IllegalControlPointTypeException
	 */
	public ControlPoint(ControlType type, Shape shape, Color color) throws IllegalControlPointTypeException{
		if (shape == null)
			throw new IllegalArgumentException("Shape cannot be null.");
		
		Rectangle2D bounds = shape.getBounds2D();
		Point2D point = null;
		ImageIcon imageIcon = null;
		switch(type) {
		case LEFT_CONTROL_POINT:
			point = new Point2D.Double(bounds.getX(), bounds.getCenterY());
			setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
			break;
			
		case RIGHT_CONTROL_POINT:
			point = new Point2D.Double(bounds.getX() + bounds.getWidth(), bounds.getCenterY());
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			break;
			
		case  TOP_LEFT_CONTROL_POINT:
			point = new Point2D.Double(bounds.getX(), bounds.getY());
			setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
			break;
			
		case  TOP_RIGHT_CONTROL_POINT:
			point = new Point2D.Double(bounds.getX() + bounds.getWidth(), bounds.getY());
			setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
			break;
			
		case  BOTTOM_LEFT_CONTROL_POINT:
			point = new Point2D.Double(bounds.getX(), bounds.getY() + bounds.getHeight());
			setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
			break;
			
		case  BOTTOM_RIGHT_CONTROL_POINT:
			point = new Point2D.Double(bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight());
			setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			break;
			
		case  BOTTOM_CONTROL_POINT:
			point = new Point2D.Double(bounds.getCenterX(), bounds.getY() + bounds.getHeight());
			setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
			break;
			
		case  TOP_CONTROL_POINT:
			point = new Point2D.Double(bounds.getCenterX(), bounds.getY());
			setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			break;
			
		default:
			throw new IllegalControlPointTypeException("No such control point type.");
		}
		controlType = type;
		controlPoint = new Ellipse2D.Double(point.getX() - CORRECTION_FACTOR, point.getY() - CORRECTION_FACTOR, CONTROL_POINT_WIDTH, CONTROL_POINT_HEIGHT);
		stroke = new BasicStroke(2, 0, 0);
		setControlPointColor(color);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.geom.RectangularShape#getHeight()
	 */
	@Override
	public double getHeight() {
		return CONTROL_POINT_HEIGHT;
	}

	/* (non-Javadoc)
	 * @see java.awt.geom.RectangularShape#getWidth()
	 */
	@Override
	public double getWidth() {
		return CONTROL_POINT_WIDTH;
	}

	/* (non-Javadoc)
	 * @see java.awt.geom.RectangularShape#getX()
	 */
	@Override
	public double getX() {
		return controlPoint.getX();
	}

	/* (non-Javadoc)
	 * @see java.awt.geom.RectangularShape#getY()
	 */
	@Override
	public double getY() {
		return controlPoint.getY();
	}

	/* (non-Javadoc)
	 * @see java.awt.geom.RectangularShape#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return controlPoint.isEmpty();
	}

	/* (non-Javadoc)
	 * w and h not used
	 * @see java.awt.geom.RectangularShape#setFrame(double, double, double, double)
	 */
	@Override
	public void setFrame(double x, double y, double w, double h) {
		controlPoint.setFrame(x, y, CONTROL_POINT_WIDTH, CONTROL_POINT_HEIGHT);
	}

	/* (non-Javadoc)
	 * @see java.awt.Shape#getBounds2D()
	 */
	@Override
	public Rectangle2D getBounds2D() {
		return controlPoint.getBounds2D();
	}

	/**
	 * @param controlType the controlType to set
	 */
	public void setType(ControlType controlType) {
		this.controlType = controlType;
	}

	/**
	 * @return the controlType
	 */
	public ControlType getType() {
		return controlType;
	}

	/**
	 * @param controlPointColor the controlPointColor to set
	 */
	public void setControlPointColor(Color controlPointColor) {
		this.controlPointColor = controlPointColor;
	}

	/**
	 * @return the controlPointColor
	 */
	public Color getControlPointColor() {
		return controlPointColor;
	}

	/**
	 * @return the controlPoint
	 */
	public Ellipse2D getControlPoint() {
		return this.controlPoint;
	}

	/**
	 * @param controlPoint the controlPoint to set
	 */
	public void setControlPoint(Ellipse2D controlPoint) {
		this.controlPoint = controlPoint;
	}

	
	/**
	 * @param g
	 */
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setStroke(stroke);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		g.setColor(Color.black);
		g.draw(controlPoint);
		g.setColor(controlPointColor);
		g.fill(controlPoint);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	}

	/**
	 * @param cursor the cursor to set
	 */
	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	/**
	 * @return the cursor
	 */
	public Cursor getCursor() {
		return cursor;
	}
}
