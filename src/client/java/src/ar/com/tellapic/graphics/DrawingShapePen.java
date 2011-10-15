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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

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
public class DrawingShapePen extends DrawingShape {
	private GeneralPath pen;
	
	public DrawingShapePen(TellapicAbstractUser user, String name, double x, double y) {
		super(name, false, true, false);
		pen = new GeneralPath();
		pen.moveTo(x, y);
		setShape(pen);
		setPaintPropertyAlpha((PaintPropertyAlpha) user.getToolBoxModel().getOpacityProperty().clone());
		setPaintPropertyStroke((PaintPropertyStroke) user.getToolBoxModel().getStrokeProperty().clone());
		setRenderingHints((RenderingHints) user.getToolBoxModel().getRenderingHints().clone());
		setPaintPropertyFill( new PaintPropertyFill());
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		if (getBounds2D().getX() + xOffset < 0 || getBounds2D().getY() + yOffset < 0)
			return;
		pen = (GeneralPath) pen.createTransformedShape(AffineTransform.getTranslateInstance(xOffset, yOffset));
		setShape(pen);
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

	/**
	 * 
	 * @param arg0
	 * @param arg1
	 */
	public void lineTo(float arg0, float arg1) {
		if (arg0<0 || arg1<0)
			return;
		pen.lineTo(arg0, arg1);
		setShape(pen);
	}
	
	/**
	 * 
	 * @param arg0
	 * @param arg1
	 */
	public void lineTo(double arg0, double arg1) {
		pen.lineTo(arg0, arg1);
		setShape(pen);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstX()
	 */
	@Override
	public int getFirstX() {
		int firstX = 0;
		PathIterator pi = pen.getPathIterator(null);
		double[] coords = new double[6];
		int result = pi.currentSegment(coords);
		
//		switch(result) {
//		case PathIterator.SEG_CLOSE:
//			break;
//			
//		case PathIterator.SEG_CUBICTO:
//			break;
//			
//		case PathIterator.SEG_LINETO:
//			break;
//			
//		case PathIterator.SEG_MOVETO:
//			break;
//			
//		case PathIterator.SEG_QUADTO:
//			break;
//		}
		firstX = (int) coords[0];
		return firstX;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstY()
	 */
	@Override
	public int getFirstY() {
		int firstY = 0;
		PathIterator pi = pen.getPathIterator(null);
		double[] coords = new double[6];
		int result = pi.currentSegment(coords);
		
//		switch(result) {
//		case PathIterator.SEG_CLOSE:
//			break;
//			
//		case PathIterator.SEG_CUBICTO:
//			break;
//			
//		case PathIterator.SEG_LINETO:
//			break;
//			
//		case PathIterator.SEG_MOVETO:
//			break;
//			
//		case PathIterator.SEG_QUADTO:
//			break;
//		}
		firstY = (int) coords[1];
		return firstY;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastX()
	 */
	@Override
	public int getLastX() {
		int lastX = 0;
		PathIterator pi     = pen.getPathIterator(null);
		double[]     coords = new double[6];
		int          result = 0;
		
		while(!pi.isDone()) {
			result = pi.currentSegment(coords);
			if (result != PathIterator.SEG_CLOSE)
				lastX = (int) coords[0];
			pi.next();
		}
		
		return lastX;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastY()
	 */
	@Override
	public int getLastY() {
		int lastY = 0;
		PathIterator pi = pen.getPathIterator(null);
		double[]     coords = new double[6];
		int          result = 0;
		
		while(!pi.isDone()) {
			result = pi.currentSegment(coords);
			if (result != PathIterator.SEG_CLOSE)
				lastY = (int) coords[0];
			pi.next();
		}
		
		return lastY;
//		while(!pi.isDone())
//			pi.next();
//		
//		double[] coords = new double[6];
//		int result = pi.currentSegment(coords);
//		
//		switch(result) {
//		case PathIterator.SEG_CLOSE:
//			break;
//			
//		case PathIterator.SEG_CUBICTO:
//			break;
//			
//		case PathIterator.SEG_LINETO:
//			break;
//			
//		case PathIterator.SEG_MOVETO:
//			break;
//			
//		case PathIterator.SEG_QUADTO:
//			break;
//		}
//		lastY = (int) coords[1];
//		return lastY;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDeferred()
	 */
	@Override
	public void sendDeferred() {

	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDragged()
	 */
	@Override
	public void sendReleased(MouseEvent event) {
		if (NetManager.getInstance().isConnected()) {
			
			int wrappedEvent = tellapicConstants.TOOL_PATH;
			
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
					event.getX(),
					event.getY()
			);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendPressed()
	 */
	@Override
	public void sendPressed(MouseEvent event) {
		if (NetManager.getInstance().isConnected()) {
			int wrappedEvent = tellapicConstants.TOOL_PATH;
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
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendReleased()
	 */
	@Override
	public void sendDragged(MouseEvent event) {
		if (NetManager.getInstance().isConnected()) {
			int eventExtMod  = 0;
			int wrappedEvent = tellapicConstants.TOOL_PATH;
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
					event.getX(),
					event.getY()
			);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendChanged()
	 */
	@Override
	public void sendChanged() {
		if (NetManager.getInstance().isConnected()) {
			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_EDIT_FIG,
					0,
					SessionUtils.getId(), 
					getNumber(),
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					((Color) getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) getPaintPropertyFill().getFillPaint()).getAlpha(),
					(int)getBounds2D().getMaxX(),
					(int)getBounds2D().getMaxY(),
					(int)getBounds2D().getX(),
					(int)getBounds2D().getY(),
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
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int x1, int y1, int x2, int y2) {
		move(x1 - getBounds2D().getX(), y1 - getBounds2D().getY());
	}
}
