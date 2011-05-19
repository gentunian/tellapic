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
final public class PenNet extends Pen {

	
	private boolean avoidLoopback = true;
	

	public PenNet() {
		super("PenNet");
	}
	
	
	/**
	 * 
	 * @param v
	 */
	public void setAvoidLoopback(boolean v) {
		avoidLoopback  = v;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void onPress(int x, int y, int button, int mask) {
		super.onPress(x, y, button, mask);
		Drawing drawing = super.getTemporalDrawing();
		if (NetManager.getInstance().isConnected() && avoidLoopback) {
			int wrappedEvent = getToolId();
			if (button == MouseEvent.BUTTON1)
				wrappedEvent |= tellapicConstants.EVENT_PLEFT;
			else if (button == MouseEvent.BUTTON2)
				wrappedEvent |= tellapicConstants.EVENT_PRESS;
			else
				wrappedEvent |= tellapicConstants.EVENT_PMIDDLE;
			tellapic.tellapic_send_drw_init(
					NetManager.getInstance().getSocket(),
					wrappedEvent,
					0,
					SessionUtils.getId(),
					1,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().alpha,
					drawing.getPaintPropertyColor().getRed(),
					drawing.getPaintPropertyColor().getGreen(),
					drawing.getPaintPropertyColor().getBlue(),
					x,
					y,
					x,
					y,
					drawing.getPaintPropertyStroke().getLineJoins(),
					drawing.getPaintPropertyStroke().getEndCaps(),
					drawing.getPaintPropertyStroke().getMiterLimit(),
					drawing.getPaintPropertyStroke().getDash_phase(),
					drawing.getPaintPropertyStroke().getDash()
			);
		}
		avoidLoopback = true;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDrag(int x, int y, int button, int mask) {
		super.onDrag(x, y, button, mask);
		if (isBeingUsed()) {
			if (NetManager.getInstance().isConnected() && avoidLoopback) {
				int eventExtMod  = 0;
				int wrappedEvent = getToolId();
				if ((mask & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
					wrappedEvent |= tellapicConstants.EVENT_DLEFT;
				else if ((mask & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK)
					wrappedEvent |= tellapicConstants.EVENT_DRIGHT;
				else if ((mask & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK)
					wrappedEvent |= tellapicConstants.EVENT_DMIDDLE;
				
				if ((mask & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
					eventExtMod = tellapicConstants.EVENT_CTL_DOWN;
					
				Drawing drawing = super.getTemporalDrawing();
				tellapic.tellapic_send_drw_using(
						NetManager.getInstance().getSocket(),
						wrappedEvent,
						eventExtMod,
						SessionUtils.getId(), 
						1,
						(float) drawing.getPaintPropertyStroke().getWidth(),
						drawing.getPaintPropertyAlpha().alpha,
						drawing.getPaintPropertyColor().getRed(),
						drawing.getPaintPropertyColor().getGreen(),
						drawing.getPaintPropertyColor().getBlue(),
						x,
						y
				);
			}
			avoidLoopback = true;
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void onRelease(int x, int y, int button, int mask) {
		super.onRelease(x, y, button, mask);
		
		Drawing drawing = super.getTemporalDrawing();
		if (drawing == null)
			return;
		
		if (NetManager.getInstance().isConnected() && avoidLoopback) {
			int wrappedEvent = getToolId();
			if (button == MouseEvent.BUTTON1)
				wrappedEvent |= tellapicConstants.EVENT_RLEFT;
			else if (button == MouseEvent.BUTTON2)
				wrappedEvent |= tellapicConstants.EVENT_RRIGHT;
			else
				wrappedEvent |= tellapicConstants.EVENT_RMIDDLE;

			tellapic.tellapic_send_drw_using(
					NetManager.getInstance().getSocket(),
					wrappedEvent,
					0,
					SessionUtils.getId(), 
					1,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().alpha,
					drawing.getPaintPropertyColor().getRed(),
					drawing.getPaintPropertyColor().getGreen(),
					drawing.getPaintPropertyColor().getBlue(),
					x,
					y
			);
		}
		avoidLoopback = true;
	}
}
