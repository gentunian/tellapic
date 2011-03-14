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

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.StatusBar;
import ar.com.tellapic.UserManager;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingLocalController extends MouseAdapter {

	private PaintPropertyController controller;
	private AbstractUser            user;
	private java.awt.Point      scrollingPoint;

	
	public DrawingLocalController() {
		controller = null;
		user = UserManager.getInstance().getLocalUser();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent event) {
		Tool tool = user.getToolBoxModel().getLastUsedTool();
		
		if (tool == null)
			return;
		
		if (tool.getName().equals("Zoom")) {
			if (event.getButton() == MouseEvent.BUTTON1)
				DrawingAreaView.getInstance().doZoomIn();
			else
				DrawingAreaView.getInstance().doZoomOut();
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		//Utils.printEventInfo(event);
		
		/* Do nothing if some coordinate is negative */
		if (event.getX() < 0 || event.getY() < 0)
			return;

		/* Do scroll with middle button. Take a point for future references */
		if (event.getButton() == MouseEvent.BUTTON2) {
			scrollingPoint = new java.awt.Point(event.getX(), event.getY());
			return;
		}
		
		
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
		
		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		/* If button is the left one, start using the tool */
		if (event.getButton() == MouseEvent.BUTTON1) {
			if (usedTool.hasAlphaProperties())
				usedTool.setAlpha(toolBoxState.getOpacityProperty());

			if (usedTool.hasColorProperties())
				usedTool.setColor(toolBoxState.getColorProperty());

			if (usedTool.hasStrokeProperties())
				usedTool.setStroke(toolBoxState.getStrokeProperty());

			if (usedTool.hasFontProperties())
				usedTool.setFont(toolBoxState.getFontProperty());
			
			usedTool.onPress(event.getX(), event.getY(), event.getButton(), event.getModifiers());

		} else {
			/* If we press another button, just stop using the tool */
			//TODO: The tool is actually paused. Rename the tool's method onCancel().
			usedTool.onCancel();
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent event) {
//		Utils.printEventInfo(event);
		StatusBar.getInstance().setMouseCoordinates(event.getX(), event.getY());
		/* Do scroll if we are dragging with the middle button. */
		/* Use the point taken as reference in MousePessed.     */
		if ((event.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK) {
			/* Get the visible rectangle from the drawing areas */
			java.awt.Rectangle clipRect = ((DrawingAreaView) event.getSource()).getVisibleRect();
			
			/* Initialize the upper left corner for the rectangle used */
			/* for scrolling to that area.                             */
			/*                                                         */
			/*    scroll to  r            scroll to  r                 */
			/*               ^                       ^                 */
			/*             +--+                    +--+                */
			/*             |  |                    |  |                */
			/*             +--+--------------------+--+                */
			/*                |                    |                   */
			/*                |     visible        |                   */
			/*                |    rectangle       |                   */
			/*                |    (clipRect)      |                   */
			/*                |                    |                   */
			/*             +--+--------------------+--+                */
			/*             |  |                    |  |                */
			/*             +--+                    +--+                */
			/*               ^                       ^                 */
			/*    scroll to  r             scroll to r                 */
			/*                                                         */
			/*                                                         */
			int x = 0;
			int y = 0;
			java.awt.Rectangle r = null;
			
			/* Accommodate r upper-left corner upon this event coordinates */
			if (scrollingPoint.x >= event.getX())
				x = clipRect.x + clipRect.width;
			else
				x = clipRect.x - Math.abs(scrollingPoint.x - event.getX());

			if (scrollingPoint.y >= event.getY())
				y = clipRect.y + clipRect.height;
			else
				y = clipRect.y - Math.abs(scrollingPoint.y - event.getY());
			
			r = new java.awt.Rectangle(
					x,
					y,
					Math.abs(scrollingPoint.x - event.getX()),
					Math.abs(scrollingPoint.y - event.getY())
			);
			
			/* Scroll to r */
			DrawingAreaView.getInstance().scrollRectToVisible(r);

			return;
		}
		
		
		Tool usedTool = user.getToolBoxModel().getLastUsedTool();
		
		
		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		
		if (usedTool.isBeingUsed()) {
			usedTool.onDrag(event.getX(), event.getY(), event.getButton(), event.getModifiersEx());
			
			// This will trigger an update() to the DrawingAreaView
			user.setTemporalDrawing(usedTool.getDrawing());
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		//Utils.printEventInfo(event);
		
		Tool         usedTool = user.getToolBoxModel().getLastUsedTool();

		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		if (usedTool.isBeingUsed() && event.getButton() == MouseEvent.BUTTON1) {
			Drawing drawing = usedTool.onRelease(event.getX(), event.getY(), event.getButton(), event.getModifiersEx());
			
			if (drawing == null) 
				return;
			
			// This will trigger an update() to the DrawingAreaView
			user.addDrawing(drawing);
			
			return;
		}
		

		if (event.getButton() == MouseEvent.BUTTON3 && event.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
			usedTool.onRestore();
			return;
		}
		
		if (event.getButton() == MouseEvent.BUTTON3)
			user.setTemporalDrawing(null);
		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	//TODO: is it possible to think this mouse wheel event be "live" from remote users? Does it make sense?
	@Override
	public void mouseMoved(MouseEvent event) {
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
		StatusBar.getInstance().setMouseCoordinates(event.getX(), event.getY());
		
		if (usedTool == null)
			return;
		
		
		
		if (usedTool.isOnMoveSupported()) {
			Drawing drawing = usedTool.getDrawing();
			if (usedTool.hasAlphaProperties())
				drawing.setAlpha(toolBoxState.getOpacityProperty());
			if (usedTool.hasColorProperties())
				drawing.setColor(toolBoxState.getColorProperty());
			if (usedTool.hasStrokeProperties())
				drawing.setStroke(toolBoxState.getStrokeProperty());
			if (usedTool.hasFontProperties())
				drawing.setFont(toolBoxState.getFontProperty());
			if (usedTool.hasColorProperties())
				drawing.setColor(toolBoxState.getColorProperty());
			
			// TODO: do we really need send every time the drawing? Its a reference, change the value
			// and use it later on the view.
			//view.update(usedTool.onMove(event.getX(), event.getY()), id);

			//solution?
			user.setTemporalDrawing(usedTool.onMove(event.getX(), event.getY()));
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent event) {
		StatusBar.getInstance().showMouseCoordinates(false);
		StatusBar.getInstance().setToolInfo(false);
	}
	
	
	/*
	 *
	 */
	@Override
	public void mouseEntered(MouseEvent event) {
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
	
		if (usedTool == null)
			return;
	
		StatusBar.getInstance().setToolInfo(usedTool.getIconPath(), usedTool.getToolTipText());
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		int step = (event.getWheelRotation() < 0)? 1 : -1;
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
		
		if (usedTool == null)
			return;
		
		if (controller == null)
			return;
		
		if (usedTool.isBeingUsed()) {
			if (event.isShiftDown()) {
				if (usedTool.hasAlphaProperties())
					controller.handleOpacityChange(toolBoxState.getOpacityProperty().alpha + 0.1f * step);

			} else {
				if (usedTool.hasStrokeProperties())
					controller.handleWidthChange((int)toolBoxState.getStrokeProperty().getWidth() + step);

				else if (usedTool.hasFontProperties())
					controller.handleFontSizeChange(toolBoxState.getFontProperty().getSize() + step);
			}
		}
	}
	
	
	/**
	 * 
	 * @param drawing
	 */
	public void updateFromOutside(Drawing drawing) {
		if (drawing != null)
			//view.update(drawing, id);
			user.setTemporalDrawing(drawing);
	}
	
	
	/**
	 * 
	 * @param c
	 */
	public void setController(PaintPropertyController c) {
		controller = c;
	}
}
