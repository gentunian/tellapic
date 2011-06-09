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
public class SelectorNet extends SelectorTool {

	public SelectorNet() {
		super(tellapicConstants.TOOL_SELECTOR, "SelectorNet", "/icons/tools/selector.png");
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		super.mousePressed(event);
		if (isSelected()) {
			if (!(temporalDrawing instanceof DrawingShape))
				return;
			
			DrawingShape drawing = (DrawingShape) temporalDrawing;
			
			if (NetManager.getInstance().isConnected() && !(event instanceof RemoteMouseEvent)) {
				int wrappedEvent = getToolId();
				if (event.getButton() == MouseEvent.BUTTON1)
					wrappedEvent |= tellapicConstants.EVENT_PLEFT;
				else if (event.getButton() == MouseEvent.BUTTON2)
					wrappedEvent |= tellapicConstants.EVENT_PRESS;
				else
					wrappedEvent |= tellapicConstants.EVENT_PMIDDLE;
				tellapic.tellapic_send_drw_init(
						NetManager.getInstance().getSocket(),
						wrappedEvent,
						0,
						SessionUtils.getId(), 
						drawing.getNumber(),
						(float) drawing.getPaintPropertyStroke().getWidth(),
						drawing.getPaintPropertyAlpha().alpha,
						drawing.getPaintPropertyColor().getRed(),
						drawing.getPaintPropertyColor().getGreen(),
						drawing.getPaintPropertyColor().getBlue(),
						event.getX(),
						event.getY(),
						event.getX(),
						event.getY(),
						drawing.getPaintPropertyStroke().getLineJoins(),
						drawing.getPaintPropertyStroke().getEndCaps(),
						drawing.getPaintPropertyStroke().getMiterLimit(),
						drawing.getPaintPropertyStroke().getDash_phase(),
						drawing.getPaintPropertyStroke().getDash()
				);
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void mouseDragged(MouseEvent event) {
		super.mouseDragged(event);
		if (isSelected()) {
			if (!(temporalDrawing instanceof DrawingShape))
				return;
				if (NetManager.getInstance().isConnected() && !(event instanceof RemoteMouseEvent)) {
					int eventExtMod  = 0;
					int wrappedEvent = getToolId();
					if ((event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
						wrappedEvent |= tellapicConstants.EVENT_DLEFT;
					else if ((event.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK)
						wrappedEvent |= tellapicConstants.EVENT_DRIGHT;
					else if ((event.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK)
						wrappedEvent |= tellapicConstants.EVENT_DMIDDLE;

					if ((event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)
						eventExtMod = tellapicConstants.EVENT_CTL_DOWN;

					DrawingShape drawing = (DrawingShape) temporalDrawing;
					tellapic.tellapic_send_drw_using(
							NetManager.getInstance().getSocket(),
							wrappedEvent,
							eventExtMod,
							SessionUtils.getId(), 
							drawing.getNumber(),
							(float) drawing.getPaintPropertyStroke().getWidth(),
							drawing.getPaintPropertyAlpha().alpha,
							drawing.getPaintPropertyColor().getRed(),
							drawing.getPaintPropertyColor().getGreen(),
							drawing.getPaintPropertyColor().getBlue(),
							event.getX(),
							event.getY()
					);
				}
			
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Ellipse#onRelease(int)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		super.mouseReleased(event);
		
		if (isSelected()) {
			if (!(temporalDrawing instanceof DrawingShape))
				return;
			DrawingShape drawing = (DrawingShape) temporalDrawing;
			if (drawing == null)
				return ;

			if (NetManager.getInstance().isConnected() && !(event instanceof RemoteMouseEvent)) {
				int wrappedEvent = getToolId();
				if (event.getButton() == MouseEvent.BUTTON1)
					wrappedEvent |= tellapicConstants.EVENT_RLEFT;
				else if (event.getButton() == MouseEvent.BUTTON2)
					wrappedEvent |= tellapicConstants.EVENT_RRIGHT;
				else
					wrappedEvent |= tellapicConstants.EVENT_RMIDDLE;

				tellapic.tellapic_send_drw_using(
						NetManager.getInstance().getSocket(),
						wrappedEvent,
						0,
						SessionUtils.getId(), 
						drawing.getNumber(),
						(float) drawing.getPaintPropertyStroke().getWidth(),
						drawing.getPaintPropertyAlpha().alpha,
						drawing.getPaintPropertyColor().getRed(),
						drawing.getPaintPropertyColor().getGreen(),
						drawing.getPaintPropertyColor().getBlue(),
						event.getX(),
						event.getY()
				);
			}
		}
	}
//	/**
//	 * 
//	 */
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		super.mouseReleased(e);
//		System.out.println("LOLO: "+temporalDrawing);
//		if (isSelected() && temporalDrawing != null) {
//			if (NetManager.getInstance().isConnected() && !(e instanceof RemoteMouseEvent)) {
//				if (temporalDrawing instanceof DrawingShape) {
//					DrawingShape drawing = (DrawingShape) temporalDrawing;
//					java.awt.Rectangle bounds = drawing.getShape().getBounds();
//
//					tellapic.tellapic_send_fig(
//							NetManager.getInstance().getSocket(),
//							getToolId(), 
//							0,
//							SessionUtils.getId(), 
//							drawing.getNumber(),
//							(float) drawing.getPaintPropertyStroke().getWidth(),
//							drawing.getPaintPropertyAlpha().alpha,
//							drawing.getPaintPropertyColor().getRed(),
//							drawing.getPaintPropertyColor().getGreen(),
//							drawing.getPaintPropertyColor().getBlue(),
//							(int)bounds.getX(),
//							(int)bounds.getY(),
//							e.getX(),
//							e.getY(),
//							drawing.getPaintPropertyStroke().getLineJoins(),
//							drawing.getPaintPropertyStroke().getEndCaps(),
//							drawing.getPaintPropertyStroke().getMiterLimit(),
//							drawing.getPaintPropertyStroke().getDash_phase(),
//							drawing.getPaintPropertyStroke().getDash()
//					);
//				}
//			}
//		}
//	}
}
