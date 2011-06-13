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
final public class DrawingToolTextNet extends DrawingToolText {

	public DrawingToolTextNet() {
		super("TextNet");
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

			if (NetManager.getInstance().isConnected() && !(event instanceof RemoteMouseEvent)) {

				tellapic.tellapic_send_text(
						NetManager.getInstance().getSocket(),
						SessionUtils.getId(),
						0,
						drawing.getPaintPropertyFont().getFont().getSize2D(),
						drawing.getPaintPropertyAlpha().alpha,
						drawing.getPaintPropertyColor().getRed(),
						drawing.getPaintPropertyColor().getGreen(),
						drawing.getPaintPropertyColor().getBlue(),
						drawing.getTextX(),
						drawing.getTextY(),
						drawing.getPaintPropertyFont().getStyle(),
						drawing.getPaintPropertyFont().getFace().length(),
						drawing.getPaintPropertyFont().getFace(),
						drawing.getText().length(),
						drawing.getText()
				);
			}			
			/* This tool has no more temporal drawings */
			setTemporalDrawing(null);
		}
	}
}
