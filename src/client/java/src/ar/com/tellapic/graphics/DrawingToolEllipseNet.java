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
final public class DrawingToolEllipseNet extends DrawingToolEllipse {

	/**
	 * 
	 */
	public DrawingToolEllipseNet() {
		super("DrawingToolEllipseNet");
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		
		/* The model guarentees that no 2 tools are selected */
		if (isSelected() && e.getButton() == MouseEvent.BUTTON1) {
			DrawingShape drawing = (DrawingShape) getTemporalDrawing();

			if (drawing == null)
				return ;
			
			sendGeneratedDrawing(drawing);

			/* This tool has no more temporal drawings */
			setTemporalDrawing(null);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingToolEllipse#ellipse(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public DrawingShape ellipse(String left, String top, String width, String height) {
		DrawingShape drawing = super.ellipse(left, top, width, height);
		
		if (drawing != null)
			sendGeneratedDrawing(drawing);
			
		return drawing;
	}
	
	/**
	 * 
	 * @param drawing
	 */
	private void sendGeneratedDrawing(DrawingShape drawing) {
		if (NetManager.getInstance().isConnected() && !getUser().isRemote()) {
			java.awt.Rectangle bounds = drawing.getShape().getBounds();
			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					getToolId(), 
					0,
					SessionUtils.getId(), 
					0,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().getAlpha(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getAlpha(),
					(int)bounds.getX(),
					(int)bounds.getY(),
					drawing.getPaintPropertyStroke().getColor().getRed(),
					drawing.getPaintPropertyStroke().getColor().getGreen(),
					drawing.getPaintPropertyStroke().getColor().getBlue(),
					drawing.getPaintPropertyStroke().getColor().getAlpha(),
					(int)(bounds.getX() + bounds.getWidth()),
					(int)(bounds.getY() + bounds.getHeight()),
					drawing.getPaintPropertyStroke().getLineJoins().ordinal(),
					drawing.getPaintPropertyStroke().getEndCaps().ordinal(),
					drawing.getPaintPropertyStroke().getMiterLimit(),
					drawing.getPaintPropertyStroke().getDash_phase(),
					drawing.getPaintPropertyStroke().getDash()
			);
		}
	}
}
