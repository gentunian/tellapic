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

import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.lib.tellapic;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
final public class EllipseNet extends Ellipse {

	private boolean avoidLoopback = true;
	
	
	
	public EllipseNet() {
		super("EllipseNet");
	}
	
	
	/**
	 * 
	 * @param v
	 */
	public void setAvoidLoopback(boolean v) {
		avoidLoopback = v;
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
			return ;
		
		if (NetManager.getInstance().isConnected() && avoidLoopback) {
			java.awt.Rectangle bounds = drawing.getShape().getBounds();
			tellapic.tellapic_send_fig(
					NetManager.getInstance().getFd(),
					getToolId(), 
					0,
					SessionUtils.getId(), 
					1,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().alpha,
					drawing.getPaintPropertyColor().getRed(),
					drawing.getPaintPropertyColor().getGreen(),
					drawing.getPaintPropertyColor().getBlue(),
					(int)bounds.getX(),
					(int)bounds.getY(),
					(int)(bounds.getX() + bounds.getWidth()),
					(int)(bounds.getY() + bounds.getHeight()),
					drawing.getPaintPropertyStroke().getLineJoins(),
					drawing.getPaintPropertyStroke().getEndCaps(),
					drawing.getPaintPropertyStroke().getMiterLimit(),
					drawing.getPaintPropertyStroke().getDash_phase(),
					drawing.getPaintPropertyStroke().getDash()
			);
		}
		avoidLoopback = true;
	}
}
