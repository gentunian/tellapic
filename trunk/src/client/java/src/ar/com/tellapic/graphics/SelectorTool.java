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
import java.awt.event.MouseEvent;

import ar.com.tellapic.DrawingPopupMenu;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class SelectorTool extends ControlTool {
	
	private AbstractDrawing temporalDrawing;
	private ControlPoint    controlPoint;
	private boolean         shouldMove;
	private boolean shouldResize;
	private double firstX;
	private double firstY;
	


	/**
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public SelectorTool(int id, String name, String iconPath) {
		super(id, name, iconPath);
	}

	public SelectorTool() {
		this(0, "Selector", "/icons/tools/selector.png");
		shouldMove   = false;
		shouldResize = false;
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
		if (isSelected() && !e.isConsumed()) {
			if (temporalDrawing != null && temporalDrawing.isSelected() && temporalDrawing.ownsControlPoint(controlPoint)) {
				shouldMove = true;
			} else {
				selectDrawingBelow(e);
			}
			e.consume();
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (temporalDrawing != null && temporalDrawing.isSelected() && temporalDrawing.ownsControlPoint(controlPoint)) {
				shouldMove = true;
			} else {
				selectDrawingBelow(e);
			}
			findControlPoint(e);
			if (shouldMove && !temporalDrawing.getUser().isRemote()) {
				firstX = e.getX();// - temporalDrawing.getBounds2D().getX();
				firstY = e.getY();// - temporalDrawing.getBounds2D().getY();
				if (e.isPopupTrigger()) {
					DrawingPopupMenu popup = new DrawingPopupMenu(null, temporalDrawing);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			} else if (shouldResize && !temporalDrawing.getUser().isRemote()) {
				controlPoint.setSelected(true);
			}
			setChanged();
			notifyObservers(temporalDrawing);
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (e.isPopupTrigger()) {
				DrawingPopupMenu popup = new DrawingPopupMenu(null, temporalDrawing);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
			setChanged();
			notifyObservers(temporalDrawing);
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			double eventX = e.getX();
			double eventY = e.getY();
			if (shouldResize && !temporalDrawing.getUser().isRemote()) {
				controlPoint.setSelected(true);
				if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
					temporalDrawing.resize(eventX, eventY, controlPoint);

			} else if (shouldMove && !temporalDrawing.getUser().isRemote()){
				if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
					temporalDrawing.move(eventX - firstX, eventY - firstY);
				firstX = e.getX();
				firstY = e.getY();
			}
			setChanged();
			notifyObservers(temporalDrawing);
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (temporalDrawing != null && temporalDrawing.isSelected() && temporalDrawing.isVisible()) {
				if (!findControlPoint(e)) { 
					controlPoint = null;
					if (temporalDrawing.getBounds2D().contains(e.getPoint()) && temporalDrawing.isMoveable() && !temporalDrawing.getUser().isRemote()) {
						((Component)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
						shouldMove = true;
					} else {
						((Component)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						shouldMove = false;
					}
				}
				setChanged();
				notifyObservers(temporalDrawing);
			} else {
				controlPoint = null;
			}
			e.consume();
		}
	}
	
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	private void selectDrawingBelow(MouseEvent e) {
		AbstractDrawing[] drawings = DrawingAreaModel.getInstance().getDrawings().toArray(new AbstractDrawing[0]);
		for(int i = 0; i < drawings.length; i++) {
			if (drawings[i].getBounds2D().contains(e.getPoint()) && drawings[i].isVisible()) {
				DrawingAreaModel.getInstance().selectDrawing(drawings[i]);
				temporalDrawing = drawings[i];
				/* Stop here, do not overload a mouse event */
				break;
			}
		}
	}
	
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	private boolean findControlPoint(MouseEvent e) {
		ControlPoint c[] = (temporalDrawing != null) ?temporalDrawing.getControlPoints() : null;
		if (c == null)
			return false;
		else {
			int i = 0;
			for(i = 0; i < c.length && !(shouldResize = c[i].contains(e.getPoint())); i++)
				c[i].setSelected(false);

			if (i < c.length) {
				controlPoint = c[i];
				if (temporalDrawing.isResizeable()) {
					((Component)e.getSource()).setCursor(controlPoint.getCursor());
					controlPoint.setSelected(true);
				}else {
					((Component)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
					shouldResize = false;
				}
			} else if (temporalDrawing.getBounds2D().contains(e.getPoint())) {
				shouldMove = true;
			}
			return (i < c.length);
		}
	}
}
