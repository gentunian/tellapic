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
import ar.com.tellapic.lib.tellapicConstants;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
final public class DrawingToolTextNet extends DrawingToolText {

	public DrawingToolTextNet() {
		super("DrawingToolTextNet");
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		super.mouseReleased(event);
		 
		if (isSelected() && event.getButton() == MouseEvent.BUTTON1) {
			DrawingText drawing = (DrawingText) getTemporalDrawing();
			
			if (drawing == null)
				return ;
			
			sendGeneratedDrawing(drawing);

			/* This tool has no more temporal drawings */
			setTemporalDrawing(null);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public DrawingText setLocation(String x, String y) {
		DrawingText drawing = super.setLocation(x, y);
		
		sendGeneratedDrawing(drawing);
		
		return drawing;
	}
	
		/**
	 * 
	 * @param drawing
	 */
	private void sendGeneratedDrawing(DrawingText drawing) {
		if (NetManager.getInstance().isConnected() && !getUser().isRemote()) {
			tellapic.tellapic_send_text(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_TEXT,
					SessionUtils.getId(), 
					0,
					drawing.getPaintPropertyFont().getFont().getSize2D(),
					drawing.getPaintPropertyAlpha().getAlpha(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) drawing.getPaintPropertyFill().getFillPaint()).getAlpha(),
					drawing.getFirstX(),
					drawing.getFirstY(),
					drawing.getPaintPropertyFont().getColor().getRed(),
					drawing.getPaintPropertyFont().getColor().getGreen(),
					drawing.getPaintPropertyFont().getColor().getBlue(),
					drawing.getPaintPropertyFont().getColor().getAlpha(),
					drawing.getPaintPropertyFont().getStyle().ordinal(),
					drawing.getPaintPropertyFont().getFace().length(),
					drawing.getPaintPropertyFont().getFace(),
					drawing.getText().length(),
					drawing.getText()
			);
		}
	}
}
