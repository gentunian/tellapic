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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.DrawingPopupMenu;
import ar.com.tellapic.UserManager;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class SelectorTool extends ControlTool {
	public enum Action {
		ACTION_RESIZE,
		ACTION_MOVE,
		ACTION_NONE
	};
	
	protected AbstractDrawing   temporalDrawing;
	private ControlPoint        controlPoint;
//	private boolean             shouldMove;
//	private boolean             shouldResize;
	private double              firstX;
	private double              firstY;
	private Action              action;


	/**
	 * 
	 */
	public SelectorTool() {
		this("Selector");
	}
	
	/**
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public SelectorTool(int id, String name, String iconPath) {
		super(id, name, iconPath);
//		shouldMove   = false;
//		shouldResize = false;
		action = Action.ACTION_NONE;
	}

	public SelectorTool(String name) {
		this(0, name, "/icons/tools/selector.png");
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.ControlTool#hasZoomCapability()
	 */
	@Override
	public boolean hasZoomCapability() {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		if (isSelected() && !e.isConsumed())
			action = Action.ACTION_NONE;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			
			if (e.isPopupTrigger() & temporalDrawing != null) {
				DrawingPopupMenu popup = new DrawingPopupMenu(null, temporalDrawing);
				popup.show(e.getComponent(), e.getX(), e.getY());
				e.consume();
				return;
			}

			/* Use this tool only while pressing LEFT button. Right button should popup a menu. */
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				AbstractUser user = null;
				if (e instanceof RemoteMouseEvent)
					user = ((RemoteMouseEvent)e).getUser();
				else
					user = UserManager.getInstance().getLocalUser();

				float zoomX = ZoomTool.getInstance().getZoomValue();

				selectDrawingBelow(e);

				/* At this point we have a selected drawing */
				/* If we manage to select a control point, then we need to resize */

				switch(action) {
				case ACTION_RESIZE:
					/* Only move or resize drawings that we have permission to do so */
					if (!temporalDrawing.getUser().equals(user))
						controlPoint.setSelected(false);
					break;

				case ACTION_MOVE:
					if (temporalDrawing.getUser().equals(user)) {
						firstX = e.getX() / zoomX;
						firstY = e.getY() / zoomX;
					}
					break;

				case ACTION_NONE:
					if (temporalDrawing != null)
						temporalDrawing.setSelected(false);
					break;
				}
				setChanged();
				notifyObservers(temporalDrawing);
			}
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			
			if (e.isPopupTrigger() && temporalDrawing != null) {
				DrawingPopupMenu popup = new DrawingPopupMenu(null, temporalDrawing);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
			
			if (e.getButton() == MouseEvent.BUTTON1) {
//				if (temporalDrawing != null && temporalDrawing.isSelected()) {
//					shouldMove = true;
//				} else {
//					selectDrawingBelow(e);
//				}
				setChanged();
				notifyObservers(temporalDrawing);
			}
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				float  zoomX  = ZoomTool.getInstance().getZoomValue();
				double eventX = e.getX()/zoomX;
				double eventY = e.getY()/zoomX;
				
				AbstractUser user = null;
				if (e instanceof RemoteMouseEvent)
					user = ((RemoteMouseEvent)e).getUser();
				else
					user = UserManager.getInstance().getLocalUser();

				/* We can't modify drawing from other users */
				if (temporalDrawing != null  && !temporalDrawing.getUser().equals(user))
					return;
				
				switch(action) {
				case ACTION_RESIZE:
					temporalDrawing.resize(eventX, eventY, controlPoint);
					break;
					
				case ACTION_MOVE:
					temporalDrawing.move(eventX - firstX, eventY - firstY);
					firstX = e.getX()/zoomX;
					firstY = e.getY()/zoomX;
					break;
				}
				setChanged();
				notifyObservers(temporalDrawing);
			}
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			Component component = e.getComponent();
			
			/* While moving the mouse and no drawing was selected, do nothing */
			if (temporalDrawing == null) {
				component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				controlPoint = null;
				return;
			}
			
			/* While moving the mouse and we have a visible and selected drawing, find its control points */
			/* and specify which action should press/click/release/drag event do. */
			if (temporalDrawing.isSelected() && temporalDrawing.isVisible()) {
				float zoomX = ZoomTool.getInstance().getZoomValue();
				
				if (!selectControlPointBelow(e)) {
					if (temporalDrawing.getBounds2D().contains(e.getX()/zoomX, e.getY()/zoomX) && temporalDrawing.isMoveable() && !temporalDrawing.getUser().isRemote()) {
						component.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					} else {
						component.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
				setChanged();
				notifyObservers(temporalDrawing);
			}
			e.consume();
		}
	}
	
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	private boolean selectDrawingBelow(MouseEvent e) {
		float zoomX = ZoomTool.getInstance().getZoomValue();
		
		if (temporalDrawing != null && temporalDrawing.isSelected()) {
			if (temporalDrawing.isResizeable() && selectControlPointBelow(e) ) {
				action = Action.ACTION_RESIZE;
				return true;
			} else if (temporalDrawing.isMoveable() && temporalDrawing.getBounds2D().contains(e.getX()/zoomX, e.getY()/zoomX)) {
				action = Action.ACTION_MOVE;
				return true;
			}
		}
		
		AbstractDrawing[] drawings = DrawingAreaModel.getInstance().getDrawings().toArray(new AbstractDrawing[0]);
		
		for(int i = 0; i < drawings.length; i++) {
			if (drawings[i].getBounds2D().contains(e.getX()/zoomX, e.getY()/zoomX) && drawings[i].isVisible()) {
				drawings[i].setSelected(true);
				temporalDrawing = drawings[i];
				selectControlPointBelow(e);
				/* Stop here, do not overload a mouse event */
				return true;
			}
		}
		if (temporalDrawing != null) {
			temporalDrawing.setSelected(false);
			temporalDrawing = null;
			controlPoint = null;
		}
		action = Action.ACTION_NONE;
		return false;
	}
	
	
	/**
	 * 
	 * @param e
	 * @return
	 */
//	private boolean findControlPoint(MouseEvent e) {
//		if (temporalDrawing == null)
//			return false;
//		
//		if (!temporalDrawing.isResizeable()) {
//			shouldMove   = true;
//			shouldResize = false;
//			return false;
//		}
//		
//		/* Get drawing control points */
//		ControlPoint c[] = temporalDrawing.getControlPoints();
//		
//		/* Retrieve the component (DrawingAreaView) */
//		Component component = e.getComponent();
//		
//		/* The zoom factor will affect current coordinates system */
//		float zoomX = ZoomTool.getInstance().getZoomValue();
//		int i = 0;
//		/* For every drawing that it does not contain the current coordinates, unselect it */
//		/* and set 'shouldResize' only on the first drawing found */
//		for(i = 0; i < c.length && !(shouldResize = c[i].contains(e.getX()/zoomX, e.getY()/zoomX)); i++)
//			c[i].setSelected(false);
//		
//		/* If we found a drawing below this coordinates, change cursor and select its control point. */
//		if (i < c.length) {
//			/* Set and select the current control point to be used later */
//			controlPoint = c[i];
//			controlPoint.setSelected(true);
//			component.setCursor(controlPoint.getCursor());
//		}
//		
//		/* Return true if a control point was found */
//		return (i < c.length);
//	}

	/**
	 * 
	 * @param point
	 */
	private boolean selectControlPointBelow(MouseEvent event) {
		controlPoint = null;
		if (temporalDrawing != null) {
			action = Action.ACTION_MOVE;
			float zoomX = ZoomTool.getInstance().getZoomValue();
			
			if (temporalDrawing.isResizeable()) {
				
				/* Get drawing control points */
				ControlPoint c[] = temporalDrawing.getControlPoints();

				int i = 0;
				/* Select the control point below the point and deselect others.*/
				/* Also, remember the selected control point */
				for(i = 0; i < c.length; i++) {
					if (c[i].contains(event.getX()/zoomX, event.getY()/zoomX)) {
						controlPoint = c[i];
						controlPoint.setSelected(true);
						event.getComponent().setCursor(controlPoint.getCursor());
						action = Action.ACTION_RESIZE;
					} else {
						c[i].setSelected(false);
					}
				}
			}
			
		} else {
			/* No action for no drawing */
			action = Action.ACTION_NONE;
		}
		
		return (controlPoint != null);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.ControlTool#hasMoveCapability()
	 */
	@Override
	public boolean hasMoveCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.ControlTool#hasResizeCapability()
	 */
	@Override
	public boolean hasResizeCapability() {
		return true;
	}
}
