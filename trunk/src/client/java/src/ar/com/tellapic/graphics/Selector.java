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
import java.awt.geom.RectangularShape;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Selector extends ControlTool {
	
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
	public Selector(int id, String name, String iconPath) {
		super(id, name, iconPath);
	}

	public Selector() {
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
			AbstractUser user = UserManager.getInstance().getLocalUser();
			for(AbstractDrawing drawing : DrawingAreaModel.getInstance().getDrawings()) {
				if (drawing.getBounds2D().contains(e.getPoint())) {
					user.setDrawingSelected(drawing);
					temporalDrawing = drawing;
					break;
				}
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
			shouldMove = (temporalDrawing != null) ? temporalDrawing.getBounds2D().contains(e.getPoint()): false;
			if (shouldMove) {
				firstX = e.getX() - temporalDrawing.getBounds2D().getX();
				firstY = e.getY() - temporalDrawing.getBounds2D().getY();
			}
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			double eventX = e.getX();
			double eventY = e.getY();
			if (shouldResize) {
				temporalDrawing.resize(eventX, eventY, controlPoint);
				setChanged();
				notifyObservers(temporalDrawing);
			} else if (shouldMove){
				temporalDrawing.move(firstX, firstY, eventX, eventY);
				setChanged();
				notifyObservers(temporalDrawing);
				firstX = e.getX() - temporalDrawing.getBounds2D().getX();
				firstY = e.getY() - temporalDrawing.getBounds2D().getY();
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
			if (temporalDrawing != null) {
				int i = 0;
				ControlPoint c[] = temporalDrawing.getControlPoints();
				for(i = 0; i < c.length && !(shouldResize = c[i].contains(e.getPoint())); i++);
				if (i < c.length) {
					((Component)e.getSource()).setCursor(c[i].getCursor());
					controlPoint = c[i];
					shouldMove = false;
				} else if (i == c.length) {
					controlPoint = null;
					if (temporalDrawing.getBounds2D().contains(e.getPoint())) {
						((Component)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					}else {
						((Component)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			}
			e.consume();
		}
	}
}
