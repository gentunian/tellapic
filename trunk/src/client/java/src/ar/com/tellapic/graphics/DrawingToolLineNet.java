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
import java.awt.geom.Line2D;

import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.lib.tellapic;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
final public class DrawingToolLineNet extends DrawingToolLine {
	
	
	public DrawingToolLineNet() {
		super("DrawingToolLineNet");
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		super.mouseReleased(event);
		
		/* The model guarentees that no 2 tools are selected */
		if (isSelected() && event.getButton() == MouseEvent.BUTTON1) {
			DrawingShape drawing = (DrawingShape) getTemporalDrawing();

			if (drawing == null)
				return ;

			sendGeneratedDrawing(drawing);

			/* This tool has no more temporal drawings */
			setTemporalDrawing(null);
		}
	}
	
	@Override
	public DrawingShape line(String x1, String y1, String x2, String y2) {
		DrawingShape drawing = super.line(x1, y1, x2, y2);
		
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
			Line2D line = (Line2D) drawing.getShape();
			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					getToolId(), 
					0,
					SessionUtils.getId(), 
					0,
					(float) drawing.getPaintPropertyStroke().getWidth(),
					drawing.getPaintPropertyAlpha().getAlpha(),
					0,
					0,
					0,
					0,
					(int)line.getX1(),
					(int)line.getY1(),
					drawing.getPaintPropertyStroke().getColor().getRed(),
					drawing.getPaintPropertyStroke().getColor().getGreen(),
					drawing.getPaintPropertyStroke().getColor().getBlue(),
					drawing.getPaintPropertyStroke().getColor().getAlpha(),
					(int)line.getX2(),
					(int)line.getY2(),
					drawing.getPaintPropertyStroke().getLineJoins().ordinal(),
					drawing.getPaintPropertyStroke().getEndCaps().ordinal(),
					drawing.getPaintPropertyStroke().getMiterLimit(),
					drawing.getPaintPropertyStroke().getDash_phase(),
					drawing.getPaintPropertyStroke().getDash()
			);
		}
	}
}
