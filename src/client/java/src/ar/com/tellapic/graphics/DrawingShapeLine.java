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
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
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
public class DrawingShapeLine extends DrawingShape {
	private boolean notDeferred;
	private Line2D line;
	
	/**
	 * 
	 * @param name
	 * @param p1
	 * @param p2
	 */
	public DrawingShapeLine(TellapicAbstractUser user, String name, Point2D p1, Point2D p2) {
		this(user, name, p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	/**
	 * @param name
	 * @param resizeable
	 * @param moveable
	 */
	public DrawingShapeLine(TellapicAbstractUser user, String name, double x1, double y1, double x2, double y2) {
		super(name, true, true, false);
		if (x1 < 0) x1 = 0; 
		if (x2 < 0) x2 = 0; 
		if (y1 < 0) y1 = 10; 
		if (y2 < 0) y2 = 10; 
		line = new Line2D.Double(x1, y1, x2, y2);
		setShape(line);
		setPaintPropertyAlpha((PaintPropertyAlpha) user.getToolBoxModel().getOpacityProperty().clone());
		setPaintPropertyStroke((PaintPropertyStroke) user.getToolBoxModel().getStrokeProperty().clone());
		setRenderingHints((RenderingHints) user.getToolBoxModel().getRenderingHints().clone());
		setPaintPropertyFill(new PaintPropertyFill());
		notDeferred = false;
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
		setLine(p1, p2);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		setLine(line.getX1() + xOffset, line.getY1() + yOffset, line.getX2() + xOffset, line.getY2() + yOffset);
	}

	/**
	 * @return
	 */
	public double length() {
		return line.getP1().distance(line.getP2());
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFillableShape()
	 */
	@Override
	public Shape getFillableShape() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFillableShapePaint()
	 */
	@Override
	public Paint getFillableShapePaint() {
		return null;
	}
	
	/**
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void setLine(double x1, double y1, double x2, double y2) {
		if (x1 < 0 || x2 < 0 || y1 < 0 ||  y2 < 0)
			return;
		line.setLine(x1, y1, x2, y2);
		setShape(line);
	}
	
	/**
	 * 
	 * @param l
	 */
	public void setLine(Line2D l) {
		line.setLine(l);
		setShape(line);
	}
	
	/**
	 * 
	 * @param p1
	 * @param p2
	 */
	public void setLine(Point2D p1, Point2D p2) {
		if (p1.getX() < 0 || p2.getX() < 0 || p1.getY() < 0 ||  p2.getY() < 0)
			return;
		line.setLine(p1, p2);
		setShape(line);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstX()
	 */
	@Override
	public int getFirstX() {
		return (int) line.getX1();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstY()
	 */
	@Override
	public int getFirstY() {
		return (int) line.getY1();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastX()
	 */
	@Override
	public int getLastX() {
		return (int) line.getX2();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastY()
	 */
	@Override
	public int getLastY() {
		return (int) line.getY2();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDeferred()
	 */
	@Override
	public void sendDeferred() {
		if (NetManager.getInstance().isConnected() && !notDeferred) {
			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_LINE,
					0,
					SessionUtils.getId(), 
					0,
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					((Color) getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) getPaintPropertyFill().getFillPaint()).getAlpha(),
					(int)line.getX2(),
					(int)line.getY2(),
					(int)line.getX1(),
					(int)line.getY1(),
					getPaintPropertyStroke().getColor().getRed(),
					getPaintPropertyStroke().getColor().getGreen(),
					getPaintPropertyStroke().getColor().getBlue(),
					getPaintPropertyStroke().getColor().getAlpha(),
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
		if (NetManager.getInstance().isConnected()) {
			int eventExtMod  = 0;
			int wrappedEvent = tellapicConstants.TOOL_LINE;
			if ((event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
				wrappedEvent |= tellapicConstants.EVENT_DLEFT;
			else if ((event.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK)
				wrappedEvent |= tellapicConstants.EVENT_DRIGHT;
			else if ((event.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK)
				wrappedEvent |= tellapicConstants.EVENT_DMIDDLE;

			if ((event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
				eventExtMod = tellapicConstants.EVENT_CTL_DOWN;
			
			tellapic.tellapic_send_drw_using(
					NetManager.getInstance().getSocket(),
					wrappedEvent,
					eventExtMod,
					SessionUtils.getId(),
					0,
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					0,
					0,
					0,
					0,
					(int)line.getX2(),
					(int)line.getY2()
			);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendPressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendPressed(MouseEvent event) {
		if (NetManager.getInstance().isConnected()) {
			notDeferred = true;
			int wrappedEvent = tellapicConstants.TOOL_LINE;
			if (event.getButton() == MouseEvent.BUTTON1)
				wrappedEvent |= tellapicConstants.EVENT_PLEFT;
			else if (event.getButton() == MouseEvent.BUTTON2)
				wrappedEvent |= tellapicConstants.EVENT_PRESS;
			else
				wrappedEvent |= tellapicConstants.EVENT_PMIDDLE;
			tellapic.tellapic_send_drw_init(
					NetManager.getInstance().getSocket(),
					wrappedEvent,
					0,
					SessionUtils.getId(), 
					0,
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					0,
					0,
					0,
					0,
					event.getX(),
					event.getY(),
					event.getX(),
					event.getY(),
					getPaintPropertyStroke().getColor().getRed(),
					getPaintPropertyStroke().getColor().getGreen(),
					getPaintPropertyStroke().getColor().getBlue(),
					getPaintPropertyStroke().getColor().getAlpha(),
					getPaintPropertyStroke().getLineJoins().ordinal(),
					getPaintPropertyStroke().getEndCaps().ordinal(),
					getPaintPropertyStroke().getMiterLimit(),
					getPaintPropertyStroke().getDash_phase(),
					getPaintPropertyStroke().getDash()
			);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendReleased(MouseEvent event) {
		if (NetManager.getInstance().isConnected()) {
			
			int wrappedEvent = tellapicConstants.TOOL_LINE;

			if (event.getButton() == MouseEvent.BUTTON1)
				wrappedEvent |= tellapicConstants.EVENT_RLEFT;
			else if (event.getButton() == MouseEvent.BUTTON2)
				wrappedEvent |= tellapicConstants.EVENT_RRIGHT;
			else
				wrappedEvent |= tellapicConstants.EVENT_RMIDDLE;

			tellapic.tellapic_send_drw_using(
					NetManager.getInstance().getSocket(),
					wrappedEvent,
					0,
					SessionUtils.getId(),
					0,
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					0,
					0,
					0,
					0,
					(int)line.getX2(),
					(int)line.getY2()
			);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int x1, int y1, int x2, int y2) {
		line.setLine(x1, y1, x2, y2);
		setShape(line);
	}

	/**
	 * 
	 */
	public void closeLine() {
		
	}
}
