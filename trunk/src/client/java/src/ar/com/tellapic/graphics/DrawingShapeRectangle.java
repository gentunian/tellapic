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
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.TellapicAbstractUser;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingShapeRectangle extends DrawingShape {
	
	/* The back end structure used to implement this drawing */
	private Rectangle2D rectangle;
	
	/**
	 * 
	 * @param name
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public DrawingShapeRectangle(TellapicAbstractUser user, String name, double x, double y, double w, double h) {
		super(name, true, true, true);
		if (user == null)
			throw new IllegalArgumentException("user cannot be null.");
		rectangle = new Rectangle2D.Double(x, y, w, h);
		setShape(rectangle);
		setUser(user);
		setPaintPropertyAlpha((PaintPropertyAlpha) user.getToolBoxModel().getOpacityProperty().clone());
		setPaintPropertyStroke((PaintPropertyStroke) user.getToolBoxModel().getStrokeProperty().clone());
		setPaintPropertyFill((PaintPropertyFill) user.getToolBoxModel().getFillProperty().clone());
		setRenderingHints((RenderingHints) user.getToolBoxModel().getRenderingHints().clone());
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(int, int, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double eventX, double eventY, ControlPoint controlPoint) {
		switch(controlPoint.getType()) {
		
		case BOTTOM_CONTROL_POINT:
			setFrame(rectangle.getX(), rectangle.getY(), rectangle.getWidth(), (eventY < rectangle.getY())? 0 : eventY - rectangle.getY());
			break;
			
		case TOP_CONTROL_POINT:
			setFrame(rectangle.getX(), (eventY > rectangle.getMaxY())? rectangle.getY() : eventY, rectangle.getWidth(), (eventY > rectangle.getMaxY())? 0 : rectangle.getMaxY() - eventY);
			break;
			
		case TOP_LEFT_CONTROL_POINT:
			setFrame(
					((eventX > rectangle.getMaxX())? rectangle.getMaxX() : eventX), 
					((eventY > rectangle.getMaxY())? rectangle.getY() : eventY), 
					((eventX > rectangle.getMaxX())? 0 : rectangle.getMaxX() - eventX),
					((eventY > rectangle.getMaxY())? 0 : rectangle.getMaxY() - eventY)
			);
			break;
			
		case TOP_RIGHT_CONTROL_POINT:
			setFrame(rectangle.getX(), ((eventY > rectangle.getMaxY())? rectangle.getMaxY() : eventY), ((eventX < rectangle.getX())? 0 : eventX - rectangle.getX()), ((eventY > rectangle.getMaxY())? 0 : rectangle.getMaxY() - eventY));
			break;

		case BOTTOM_RIGHT_CONTROL_POINT:
			setFrame(rectangle.getX(), rectangle.getY(), ((eventX < rectangle.getX())? 0 : eventX - rectangle.getX()), ((eventY < rectangle.getY())? 0 : eventY - rectangle.getY()));
			break;

		case BOTTOM_LEFT_CONTROL_POINT:
			setFrame(((eventX > rectangle.getMaxX())? rectangle.getMaxX() : eventX), rectangle.getY(), ((eventX > rectangle.getMaxX())? 0 : rectangle.getMaxX() - eventX), ((eventY < rectangle.getY())? 0 : eventY - rectangle.getY()));
			break;

		case LEFT_CONTROL_POINT:
			setFrame(((eventX > rectangle.getMaxX())? rectangle.getMaxX() : eventX), rectangle.getY(), ((eventX > rectangle.getMaxX())? 0 : rectangle.getMaxX() - eventX), rectangle.getHeight());
			break;

		case RIGHT_CONTROL_POINT:
			setFrame(rectangle.getX(), rectangle.getY(), ((eventX < rectangle.getX())? 0 : eventX - rectangle.getX()), rectangle.getHeight());
			break;
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		setFrame(rectangle.getX() + xOffset, rectangle.getY() + yOffset, rectangle.getWidth(), rectangle.getHeight());
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return rectangle.isEmpty();
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void setFrame(double x, double y, double w, double h) {
		rectangle.setFrame(x, y, w, h);
		setShape(rectangle);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void setRect(double x, double y, double w, double h) {
		rectangle.setRect(x, y, w, h);
		setShape(rectangle);
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 */
	public void setRect(Point2D p1, Point2D p2) {
		rectangle.setFrameFromDiagonal(p1, p2);
		setShape(rectangle);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstX()
	 */
	@Override
	public int getFirstX() {
		return (int) rectangle.getX();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstY()
	 */
	@Override
	public int getFirstY() {
		return (int) rectangle.getY();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastX()
	 */
	@Override
	public int getLastX() {
		return (int) rectangle.getMaxX();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastY()
	 */
	@Override
	public int getLastY() {
		return (int) rectangle.getMaxY();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDeferred()
	 */
	@Override
	public void sendDeferred() {
		if (NetManager.getInstance().isConnected() ) {

			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_RECT,
					0,
					SessionUtils.getId(), 
					0,
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					((Color) getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) getPaintPropertyFill().getFillPaint()).getAlpha(),
					(int)rectangle.getX(),
					(int)rectangle.getY(),
					getPaintPropertyStroke().getColor().getRed(),
					getPaintPropertyStroke().getColor().getGreen(),
					getPaintPropertyStroke().getColor().getBlue(),
					getPaintPropertyStroke().getColor().getAlpha(),
					(int)rectangle.getMaxX(),
					(int)rectangle.getMaxY(),
					getPaintPropertyStroke().getLineJoins().ordinal(),
					getPaintPropertyStroke().getEndCaps().ordinal(),
					getPaintPropertyStroke().getMiterLimit(),
					getPaintPropertyStroke().getDash_phase(),
					getPaintPropertyStroke().getDash()
			);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendDragged(MouseEvent event) {
		
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendPressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendPressed(MouseEvent event) {
		
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendReleased(MouseEvent event) {
		
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int x1, int y1, int x2, int y2) {
		rectangle.setFrameFromDiagonal(x1, y1, x2, y2);
		setShape(rectangle);
	}
}
