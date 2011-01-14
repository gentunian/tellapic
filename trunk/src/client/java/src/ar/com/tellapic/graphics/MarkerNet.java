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
final public class MarkerNet extends Marker {
	
	
	private boolean avoidLoopback = true;
	

	public MarkerNet() {
		super("MarkerNet");
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
		Drawing drawing = getDrawing();
		if (NetManager.getInstance().isConnected() && avoidLoopback) {
			int wrappedEvent = getToolId();
			if (button == MouseEvent.BUTTON1)
				wrappedEvent |= tellapicConstants.EVENT_PLEFT;
			else if (button == MouseEvent.BUTTON2)
				wrappedEvent |= tellapicConstants.EVENT_PRESS;
			else
				wrappedEvent |= tellapicConstants.EVENT_PMIDDLE;
			tellapic.tellapic_send_drw_init(
					NetManager.getInstance().getFd(),
					wrappedEvent,
					0,
					SessionUtils.getId(), 
					1,
					((BasicStroke)drawing.getStroke()).getLineWidth(),
					((AlphaComposite)drawing.getComposite()).getAlpha(),
					drawing.getColor().getRed(),
					drawing.getColor().getGreen(),
					drawing.getColor().getBlue(),
					x,
					y,
					x,
					y,
					((BasicStroke)drawing.getStroke()).getLineJoin(),
					((BasicStroke)drawing.getStroke()).getEndCap(),
					((BasicStroke)drawing.getStroke()).getMiterLimit(),
					((BasicStroke)drawing.getStroke()).getDashPhase(),
					((BasicStroke)drawing.getStroke()).getDashArray()
			);
		}
		avoidLoopback = true;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDrag(int x, int y, boolean symmetric, int button) {
		super.onDrag(x, y, symmetric, button);
		if (isBeingUsed()) {
			if (NetManager.getInstance().isConnected() && avoidLoopback) {
				int eventExtMod  = 0;
				int wrappedEvent = getToolId();
				if (button == MouseEvent.BUTTON1)
					wrappedEvent |= tellapicConstants.EVENT_DLEFT;
				else if (button == MouseEvent.BUTTON2)
					wrappedEvent |= tellapicConstants.EVENT_DRIGHT;
				else
					wrappedEvent |= tellapicConstants.EVENT_DMIDDLE;
				if (symmetric)
					eventExtMod = tellapicConstants.EVENT_CTL_DOWN;

					
				Drawing drawing = getDrawing();
				tellapic.tellapic_send_drw_using(
						NetManager.getInstance().getFd(),
						wrappedEvent,
						eventExtMod,
						SessionUtils.getId(), 
						1,
						((BasicStroke)drawing.getStroke()).getLineWidth(),
						((AlphaComposite)drawing.getComposite()).getAlpha(),
						drawing.getColor().getRed(),
						drawing.getColor().getGreen(),
						drawing.getColor().getBlue(),
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
	public Drawing onRelease(int x, int y, int button) {
		Drawing drawing = super.onRelease(x, y, button);
		if (drawing == null)
			return drawing;
		
		if (NetManager.getInstance().isConnected() && avoidLoopback) {
			int wrappedEvent = getToolId();
			if (button == MouseEvent.BUTTON1)
				wrappedEvent |= tellapicConstants.EVENT_RLEFT;
			else if (button == MouseEvent.BUTTON2)
				wrappedEvent |= tellapicConstants.EVENT_RRIGHT;
			else
				wrappedEvent |= tellapicConstants.EVENT_RMIDDLE;

			tellapic.tellapic_send_drw_using(
					NetManager.getInstance().getFd(),
					wrappedEvent,
					0,
					SessionUtils.getId(), 
					1,
					((BasicStroke)drawing.getStroke()).getLineWidth(),
					((AlphaComposite)drawing.getComposite()).getAlpha(),
					drawing.getColor().getRed(),
					drawing.getColor().getGreen(),
					drawing.getColor().getBlue(),
					x,
					y
			);
		}
		avoidLoopback = true;
		return drawing;
	}
}
