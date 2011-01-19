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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingLocalController extends MouseAdapter {

	//private IToolBoxState           toolBoxState;
	//private final IDrawingAreaManager     drawingAreaModel = DrawingAreaModel.getInstance();
	//private final DrawingAreaView         view = DrawingAreaView.getInstance();
	private PaintPropertyController controller;
	private AbstractUser            user;
	//private Tool                    currentTool;
	//private boolean                 isLocal;
	//private int id;

	
	public DrawingLocalController() { //IToolBoxState toolBoxState) { //, IDrawingAreaManager drawingAreaModel, DrawingAreaView view) {
		//this.toolBoxState     = toolBoxState;
		//this.drawingAreaModel = drawingAreaModel;
		//this.view  = view;
		controller = null;
		//id = view.addPainter();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		//Utils.printEventInfo(event);
		
		user = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		
		//DrawingAreaView.getInstance();
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
		
		if (usedTool == null)
			return;
		
		if (event.getButton() == MouseEvent.BUTTON1) {
			if (usedTool.hasAlphaProperties())
				usedTool.setAlpha(toolBoxState.getOpacityProperty());

			if (usedTool.hasColorProperties())
				usedTool.setColor(toolBoxState.getColorProperty());

			if (usedTool.hasStrokeProperties())
				usedTool.setStroke(toolBoxState.getStrokeProperty());

			if (usedTool.hasFontProperties())
				usedTool.setFont(toolBoxState.getFontProperty());
			
			avoidLoopback();
			usedTool.onPress(event.getX(), event.getY(), event.getButton(), event.getModifiers());

		} else if (usedTool != null) {
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
		//Utils.printEventInfo(event);

		user = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		Tool usedTool = user.getToolBoxModel().getLastUsedTool();
		
		if (usedTool == null)
			return;
		
		if (usedTool.isBeingUsed()) {
			avoidLoopback();
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
		
		user     = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		Tool         usedTool = user.getToolBoxModel().getLastUsedTool();

		if (usedTool == null)
			return;
		
		if (usedTool.isBeingUsed() && event.getButton() == MouseEvent.BUTTON1) {
			avoidLoopback();
			Drawing drawing = usedTool.onRelease(event.getX(), event.getY(), event.getButton(), event.getModifiersEx());
			
			if (drawing == null) 
				return;
			
			// This will trigger an update() to the DrawingAreaView
			user.addDrawing(drawing);
		}
		
		if (event.getButton() == MouseEvent.BUTTON3 && event.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
			usedTool.onRestore();
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	//TODO: is it possible to think this mouse wheel event be "live" from remote users? Does it make sense?
	@Override
	public void mouseMoved(MouseEvent event) {
		user = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();

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

			// TODO: do we really need send every time the drawing? Its a reference, change the value
			// and use it later on the view.
			//view.update(usedTool.onMove(event.getX(), event.getY()), id);

			//solution?
			user.setTemporalDrawing(usedTool.onMove(event.getX(), event.getY()));
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		int step = (event.getWheelRotation() < 0)? 1 : -1;
		user = UserManager.getInstance().getLocalUser();
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
	 * @param user
	 */
	private void avoidLoopback() {
		Tool usedTool = user.getToolBoxModel().getLastUsedTool();
		if (user.isRemote()) {
			try {
				Method avoidLoopback = usedTool.getClass().getMethod("setAvoidLoopback", boolean.class);
				try {
					avoidLoopback.invoke(usedTool, false);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
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
