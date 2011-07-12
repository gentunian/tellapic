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
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

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
public class DrawingShapeEllipse extends DrawingShape {
	private Ellipse2D ellipse;
	
	public DrawingShapeEllipse(TellapicAbstractUser user, String name, double x, double y, double w, double h) {
		super(name, true, true, true);
		ellipse = new Ellipse2D.Double(x, y, w, h);
		setShape(ellipse);
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
			setFrame(ellipse.getX(), ellipse.getY(), ellipse.getWidth(), (eventY < ellipse.getY())? 0 : eventY - ellipse.getY());
			break;
			
		case TOP_CONTROL_POINT:
			setFrame(ellipse.getX(), (eventY > ellipse.getMaxY())? ellipse.getY() : eventY, ellipse.getWidth(), (eventY > ellipse.getMaxY())? 0 : ellipse.getMaxY() - eventY);
			break;
			
		case TOP_LEFT_CONTROL_POINT:
			setFrame(
					((eventX > ellipse.getMaxX())? ellipse.getMaxX() : eventX), 
					((eventY > ellipse.getMaxY())? ellipse.getY() : eventY), 
					((eventX > ellipse.getMaxX())? 0 : ellipse.getMaxX() - eventX),
					((eventY > ellipse.getMaxY())? 0 : ellipse.getMaxY() - eventY)
			);
			break;
			
		case TOP_RIGHT_CONTROL_POINT:
			setFrame(ellipse.getX(), ((eventY > ellipse.getMaxY())? ellipse.getMaxY() : eventY), ((eventX < ellipse.getX())? 0 : eventX - ellipse.getX()), ((eventY > ellipse.getMaxY())? 0 : ellipse.getMaxY() - eventY));
			break;

		case BOTTOM_RIGHT_CONTROL_POINT:
			setFrame(ellipse.getX(), ellipse.getY(), ((eventX < ellipse.getX())? 0 : eventX - ellipse.getX()), ((eventY < ellipse.getY())? 0 : eventY - ellipse.getY()));
			break;

		case BOTTOM_LEFT_CONTROL_POINT:
			setFrame(((eventX > ellipse.getMaxX())? ellipse.getMaxX() : eventX), ellipse.getY(), ((eventX > ellipse.getMaxX())? 0 : ellipse.getMaxX() - eventX), ((eventY < ellipse.getY())? 0 : eventY - ellipse.getY()));
			break;

		case LEFT_CONTROL_POINT:
			setFrame(((eventX > ellipse.getMaxX())? ellipse.getMaxX() : eventX), ellipse.getY(), ((eventX > ellipse.getMaxX())? 0 : ellipse.getMaxX() - eventX), ellipse.getHeight());
			break;

		case RIGHT_CONTROL_POINT:
			setFrame(ellipse.getX(), ellipse.getY(), ((eventX < ellipse.getX())? 0 : eventX - ellipse.getX()), ellipse.getHeight());
			break;
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		ellipse.setFrame(ellipse.getX() + xOffset, ellipse.getY() + yOffset, ellipse.getWidth(), ellipse.getHeight());
		setShape(ellipse);
	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return ellipse.isEmpty();
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public void setFrame(double x, double y, double w, double h) {
		ellipse.setFrame(x, y, w, h);
		setShape(ellipse);
	}

	/**
	 * @param point
	 * @param size
	 */
	public void setFrame(Point2D point, Dimension size) {
		ellipse.setFrame(point, size);
		setShape(ellipse);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstX()
	 */
	@Override
	public int getFirstX() {
		return (int) ellipse.getX();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstY()
	 */
	@Override
	public int getFirstY() {
		return (int) ellipse.getY();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastX()
	 */
	@Override
	public int getLastX() {
		return (int) ellipse.getMaxX();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastY()
	 */
	@Override
	public int getLastY() {
		return (int) ellipse.getMaxY();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDeferred()
	 */
	@Override
	public void sendDeferred() {
		if (NetManager.getInstance().isConnected() ) {

			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_ELLIPSE,
					0,
					SessionUtils.getId(), 
					0,
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					((Color) getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) getPaintPropertyFill().getFillPaint()).getAlpha(),
					(int)ellipse.getX(),
					(int)ellipse.getY(),
					getPaintPropertyStroke().getColor().getRed(),
					getPaintPropertyStroke().getColor().getGreen(),
					getPaintPropertyStroke().getColor().getBlue(),
					getPaintPropertyStroke().getColor().getAlpha(),
					(int)ellipse.getMaxX(),
					(int)ellipse.getMaxY(),
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
		ellipse.setFrameFromDiagonal(x1, y1, x2, y2);
		setShape(ellipse);
	}
}
