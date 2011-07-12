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

import java.awt.event.MouseEvent;

import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
final public class DrawingToolMarkerNet extends DrawingToolMarker {
	
	
	public DrawingToolMarkerNet() {
		super("DrawingToolMarkerNet");
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		super.mousePressed(event);
		if (isSelected()) {
			if ((event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				DrawingShape drawing = (DrawingShape) super.getTemporalDrawing();

				int wrappedEvent = getToolId();
				if (event.getButton() == MouseEvent.BUTTON1)
					wrappedEvent |= tellapicConstants.EVENT_PLEFT;
				else if (event.getButton() == MouseEvent.BUTTON2)
					wrappedEvent |= tellapicConstants.EVENT_PRESS;
				else
					wrappedEvent |= tellapicConstants.EVENT_PMIDDLE;

				sendStartingDrawing(drawing, wrappedEvent, event.getX(), event.getY());
			}
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void mouseDragged(MouseEvent event) {
		super.mouseDragged(event);
		if (isSelected() && isBeingUsed() && (event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			
			int eventExtMod  = 0;
			int wrappedEvent = getToolId();
			if ((event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
				wrappedEvent |= tellapicConstants.EVENT_DLEFT;
			else if ((event.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK)
				wrappedEvent |= tellapicConstants.EVENT_DRIGHT;
			else if ((event.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK)
				wrappedEvent |= tellapicConstants.EVENT_DMIDDLE;

			if ((event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
				eventExtMod = tellapicConstants.EVENT_CTL_DOWN;

			DrawingShape drawing = (DrawingShape) getTemporalDrawing();

			if (drawing == null) 
				return;

			sendWhileDrawing(drawing, wrappedEvent, eventExtMod, event.getX(), event.getY());

		}
	}

	
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		super.mouseReleased(event);
		
		if (isSelected() && event.getButton() == MouseEvent.BUTTON1) {
			
			DrawingShape drawing = (DrawingShape) getTemporalDrawing();
			
			if (drawing == null)
				return ;

			int wrappedEvent = getToolId();
			if (event.getButton() == MouseEvent.BUTTON1)
				wrappedEvent |= tellapicConstants.EVENT_RLEFT;
			else if (event.getButton() == MouseEvent.BUTTON2)
				wrappedEvent |= tellapicConstants.EVENT_RRIGHT;
			else
				wrappedEvent |= tellapicConstants.EVENT_RMIDDLE;

			sendWhileDrawing(drawing, wrappedEvent, 0, event.getX(), event.getY());
			
			/* This tool has no more temporal drawings */
			setTemporalDrawing(null);
		}
	}
	
	/**
	 * 
	 * @param drawing
	 * @param wrappedEvent
	 * @param x
	 * @param y
	 */
	private void sendWhileDrawing(DrawingShape drawing, int wrappedEvent, int extra, int x, int y) {
		if (NetManager.getInstance().isConnected() && !getUser().isRemote()) {
			tellapic.tellapic_send_drw_using(
					NetManager.getInstance().getSocket(),
					wrappedEvent,
					extra,
					SessionUtils.getId(),
					0,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().getAlpha(),
					0,
					0,
					0,
					0,
					x,
					y
			);
		}
	}
	
	/**
	 * 
	 * @param drawing
	 * @param x
	 * @param y
	 */
	private void sendStartingDrawing(DrawingShape drawing, int wrappedEvent, int x, int y) {
		if (NetManager.getInstance().isConnected() && !getUser().isRemote()) {
			tellapic.tellapic_send_drw_init(
					NetManager.getInstance().getSocket(),
					wrappedEvent,
					0,
					SessionUtils.getId(), 
					0,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().getAlpha(),
					0,
					0,
					0,
					0,
					x,
					y,
					drawing.getPaintPropertyStroke().getColor().getRed(),
					drawing.getPaintPropertyStroke().getColor().getGreen(),
					drawing.getPaintPropertyStroke().getColor().getBlue(),
					drawing.getPaintPropertyStroke().getColor().getAlpha(),
					x,
					y,
					drawing.getPaintPropertyStroke().getLineJoins().ordinal(),
					drawing.getPaintPropertyStroke().getEndCaps().ordinal(),
					drawing.getPaintPropertyStroke().getMiterLimit(),
					drawing.getPaintPropertyStroke().getDash_phase(),
					drawing.getPaintPropertyStroke().getDash()
			);
		}
	}
}
