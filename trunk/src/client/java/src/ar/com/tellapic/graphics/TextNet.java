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
final public class TextNet extends Text {

	private boolean avoidLoopback = true;


	public TextNet() {
		super("TextNet");
	}
	
	
	public void setAvoidLoopback(boolean v) {
		avoidLoopback  = v;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void onRelease(int x, int y, int button, int mask) {
		super.onRelease(x, y, button, mask);
		 
		DrawingText drawing = (DrawingText) super.getTemporalDrawing();
		if (drawing == null)
			return ;
		
		if (NetManager.getInstance().isConnected() && avoidLoopback) {
			
			tellapic.tellapic_send_text(
					NetManager.getInstance().getSocket(),
					SessionUtils.getId(),
					1,
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
		avoidLoopback = true;
	}
}
