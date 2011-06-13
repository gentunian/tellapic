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

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
final public class DrawingToolRectangleNet extends DrawingToolRectangle {
	

	public DrawingToolRectangleNet() {
		super("RectangleNet");
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		super.mouseReleased(event);

		if (isSelected()) {
			DrawingShape drawing = (DrawingShape) getTemporalDrawing();
			if (drawing == null)
				return ;

			if (NetManager.getInstance().isConnected() && !(event instanceof RemoteMouseEvent)) {
				java.awt.Rectangle bounds = drawing.getShape().getBounds();
				tellapic.tellapic_send_fig(
						NetManager.getInstance().getSocket(),
						getToolId(), 
						0,
						SessionUtils.getId(), 
						0,
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
						//					((BasicStroke)drawing.getStroke()).getLineJoin(),
						//					((BasicStroke)drawing.getStroke()).getEndCap(),
						//					((BasicStroke)drawing.getStroke()).getMiterLimit(),
						//					((BasicStroke)drawing.getStroke()).getDashPhase(),
						//					((BasicStroke)drawing.getStroke()).getDashArray()
				);
			}			
			/* This tool has no more temporal drawings */
			setTemporalDrawing(null);
		}
	}
}
