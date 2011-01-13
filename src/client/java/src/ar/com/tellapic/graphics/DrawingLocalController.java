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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.LocalUser;
import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.Utils;
import ar.com.tellapic.lib.tellapic;

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
	private int id;

	
	public DrawingLocalController() { //IToolBoxState toolBoxState) { //, IDrawingAreaManager drawingAreaModel, DrawingAreaView view) {
		//this.toolBoxState     = toolBoxState;
		//this.drawingAreaModel = drawingAreaModel;
		//this.view  = view;
		controller = null;
		//id = view.addPainter();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent event) {
		printEventInfo(event);
//		Utils.logMessage("Mouse dragged: "+event.getButton());
		int eventX = event.getX();             // This event X coordinate
		int eventY = event.getY();             // This event Y coordinate
		AbstractUser user = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		Tool usedTool = user.getToolBoxModel().getLastUsedTool();
		
		Utils.logMessage("lastUsedTool is: "+usedTool);
		
		if (usedTool.isBeingUsed()) {
			usedTool.onDraw(eventX, eventY, event.isControlDown());
			
			//Drawing drawing = usedTool.getDrawing();
			//view.update(drawing, id);
			
			// This will trigger an update() to the DrawingAreaView
			user.setTemporalDrawing(usedTool.getDrawing());
			Drawing drawing = user.getTemporalDrawing();
			if (usedTool.isLiveModeSupported() && !(event instanceof RemoteMouseEvent)) {
				int wrappedButton = 0; 
				if (event.getButton() == MouseEvent.BUTTON1)
					wrappedButton = tellapic.BUTTON_LEFT;
				else if (event.getButton() == MouseEvent.BUTTON2)
					wrappedButton = tellapic.BUTTON_RIGHT;
				else
					wrappedButton = tellapic.BUTTON_LEFT;
				
				int toolEvent = usedTool.getToolId() | tellapic.EVENT_DRAG | wrappedButton;
				System.out.println("tellapic_send_drw_using");
				tellapic.tellapic_send_drw_using(
						NetManager.getInstance().getFd(),
						toolEvent,
						SessionUtils.getId(),
						1,
						//((BasicStroke)drawing.getStroke()).getLineWidth(),
						1.0f,
						//((AlphaComposite)drawing.getComposite()).getAlpha(),
						1.0f,
						drawing.getColor().getRed(),
						drawing.getColor().getGreen(),
						drawing.getColor().getBlue(),
						eventX,
						eventY
				);
			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		printEventInfo(event);
		AbstractUser user = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		
		Tool usedTool = user.getToolBoxModel().getLastUsedTool();

		if (usedTool == null)
			return;
		
		if (usedTool.isBeingUsed() && event.getButton() == MouseEvent.BUTTON1) {
			Drawing drawing = usedTool.onFinishDraw();
			
			if (drawing == null) 
				return;
			
			// This will trigger an update() to the DrawingAreaView
			user.addDrawing(drawing);
			
			/* Avoid loopbacking a remote event and send it again to the server */
			if (!(event instanceof RemoteMouseEvent)) {
				
				/* If this is not a Remote Event check the direct or deferred tool mode */
				/* and send data accordingly to the server.  */ 
				if (!usedTool.isLiveModeSupported()) {
					/* No live mode implies a deferred mode */
					
					if (!usedTool.hasFontProperties()) {
						
						java.awt.Rectangle bounds = drawing.getShape().getBounds();
						tellapic.tellapic_send_fig(
								NetManager.getInstance().getFd(),
								usedTool.getToolId(), 
								SessionUtils.getId(), 
								1,
								((BasicStroke)drawing.getStroke()).getLineWidth(),
								((AlphaComposite)drawing.getComposite()).getAlpha(),
								drawing.getColor().getRed(),
								drawing.getColor().getGreen(),
								drawing.getColor().getBlue(),
								(int)bounds.getX(),
								(int)bounds.getY(),
								(int)(bounds.getX() + bounds.getWidth()),
								(int)(bounds.getY() + bounds.getHeight()),
								((BasicStroke)drawing.getStroke()).getLineJoin(),
								((BasicStroke)drawing.getStroke()).getEndCap(),
								((BasicStroke)drawing.getStroke()).getMiterLimit(),
								((BasicStroke)drawing.getStroke()).getDashPhase(),
								((BasicStroke)drawing.getStroke()).getDashArray()
						);
						System.out.println("SENT COORD: ("+(int)bounds.getX()+","+(int)bounds.getY()+") ("+(int)(bounds.getX() + bounds.getWidth())+","+(int)(bounds.getY() + bounds.getHeight())+")");
						
					} else {
						tellapic.tellapic_send_text(
								NetManager.getInstance().getFd(),
								SessionUtils.getId(),
								1,
								((BasicStroke)drawing.getStroke()).getLineWidth(),
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
				} else {
					int wrappedButton = 0; 
					if (event.getButton() == MouseEvent.BUTTON1)
						wrappedButton = tellapic.BUTTON_LEFT;
					else if (event.getButton() == MouseEvent.BUTTON2)
						wrappedButton = tellapic.BUTTON_RIGHT;
					else
						wrappedButton = tellapic.BUTTON_LEFT;
					
					int toolEvent = usedTool.getToolId() | tellapic.EVENT_RELEASE | wrappedButton;
					System.out.println("tellapic_send_drw_using");
					tellapic.tellapic_send_drw_using(
							NetManager.getInstance().getFd(),
							toolEvent,
							SessionUtils.getId(),
							1,
							((BasicStroke)drawing.getStroke()).getLineWidth(),
							((AlphaComposite)drawing.getComposite()).getAlpha(),
							drawing.getColor().getRed(),
							drawing.getColor().getGreen(),
							drawing.getColor().getBlue(),
							(int)event.getX(),
							(int)event.getY()
					);
				
				}
			} else {
				System.out.println("Remote Release Event Processed");
			}
		}
	

		
		if (event.getButton() == MouseEvent.BUTTON3 && event.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
			usedTool.onRestore();
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		printEventInfo(event);
		//Utils.logMessage("Mouse pressed: "+event.getButton());
		
		AbstractUser user = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		
		//DrawingAreaView.getInstance();
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
		
		if (usedTool == null)
			return;
		
		double x = event.getX();
		double y = event.getY();
		
		if (event.getButton() == MouseEvent.BUTTON1) {
			usedTool.init(x, y);
			Drawing drawing = usedTool.getDrawing();

			if (usedTool.hasAlphaProperties())
				drawing.setAlpha(toolBoxState.getOpacityProperty());

			if (usedTool.hasColorProperties())
				drawing.setColor(toolBoxState.getColorProperty());

			if (usedTool.hasStrokeProperties())
				drawing.setStroke(toolBoxState.getStrokeProperty());

			if (usedTool.hasFontProperties())
				drawing.setFont(toolBoxState.getFontProperty());

			if (usedTool.isLiveModeSupported() && !(event instanceof RemoteMouseEvent)) {
				int wrappedButton = 0; 
				if (event.getButton() == MouseEvent.BUTTON1)
					wrappedButton = tellapic.BUTTON_LEFT;
				else if (event.getButton() == MouseEvent.BUTTON2)
					wrappedButton = tellapic.BUTTON_RIGHT;
				else
					wrappedButton = tellapic.BUTTON_MIDDLE;
				
				int toolEvent = usedTool.getToolId() | tellapic.EVENT_PRESS | wrappedButton;
				System.out.println("tellapic_send_drw_init() with toolEvent: "+toolEvent);
				tellapic.tellapic_send_drw_init(
						NetManager.getInstance().getFd(),
						toolEvent,
						SessionUtils.getId(),
						1,
						((BasicStroke)drawing.getStroke()).getLineWidth(),
						((AlphaComposite)drawing.getComposite()).getAlpha(),
						drawing.getColor().getRed(),
						drawing.getColor().getGreen(),
						drawing.getColor().getBlue(),
						(int)x,
						(int)y,
						0,
						0,
						((BasicStroke)drawing.getStroke()).getLineJoin(),
						((BasicStroke)drawing.getStroke()).getEndCap(),
						((BasicStroke)drawing.getStroke()).getMiterLimit(),
						((BasicStroke)drawing.getStroke()).getDashPhase(),
						((BasicStroke)drawing.getStroke()).getDashArray()
				);

			}
		} else if (usedTool != null) {
			//TODO: The tool is actually paused. Rename the tool's method onCancel().
			usedTool.onCancel();
		}
		//view.repaint();
	}


	public static void printEventInfo(MouseEvent event) {
		Utils.logMessage("Mouse event:");
		Utils.logMessage("\tbutton: "+event.getButton());
		Utils.logMessage("\tclick count: "+event.getClickCount());
		Utils.logMessage("\tid: "+event.getID());
		Utils.logMessage("\tmodifiers: "+event.getModifiers());
		Utils.logMessage("\tmodifiers ext: "+event.getModifiersEx());
		Utils.logMessage("\twhen: "+event.getWhen());
		Utils.logMessage("\tx: "+event.getX());
		Utils.logMessage("\ty: "+event.getY());
		Utils.logMessage("\tsource: "+event.getSource());
		Utils.logMessage("\tinstanceof RemoteMouseEvent?: "+ (event instanceof RemoteMouseEvent));
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	//TODO: is it possible to think this mouse wheel event be "live" from remote users? Does it make sense?
	@Override
	public void mouseMoved(MouseEvent event) {
		AbstractUser user = (event instanceof RemoteMouseEvent)? ((RemoteMouseEvent) event).getUser() : UserManager.getInstance().getLocalUser();
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();

		if (usedTool != null)
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
		LocalUser user = UserManager.getInstance().getLocalUser();
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();

		if (controller != null && usedTool != null && usedTool.isBeingUsed())
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
