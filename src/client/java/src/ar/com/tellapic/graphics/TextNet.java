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
	public Drawing onRelease(int x, int y, int button) {
		Drawing drawing = super.onRelease(x, y, button);
		if (drawing == null)
			return drawing;
		
		if (NetManager.getInstance().isConnected() && avoidLoopback) {
			
			tellapic.tellapic_send_text(
					NetManager.getInstance().getFd(),
					SessionUtils.getId(),
					1,
					drawing.getFont().getSize2D(),
					((AlphaComposite)drawing.getComposite()).getAlpha(),
					drawing.getColor().getRed(),
					drawing.getColor().getGreen(),
					drawing.getColor().getBlue(),
					drawing.getTextX(),
					drawing.getTextY(),
					drawing.getFont().getStyle(),
					drawing.getFont().getName().length(),
					drawing.getFont().getName(),
					drawing.getText().length(),
					drawing.getText()
			);
		}
		avoidLoopback = true;
		return drawing;
	}
}
