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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class PenTool extends DrawingTool {
	private static final String PEN_ICON_PATH = "/icons/tools/pencil.png";
	private static final String PEN_CURSOR_PATH = "/icons/tools/pencil-cursor.png";
	private static final double DEFAULT_WIDTH = 4;
	private static final double DEFAULT_ALPHA = 1;
	private static final int DEFAULT_CAPS = 0;
	private static final Color DEFAULT_COLOR = Color.blue;
	private static final int DEFAULT_JOINS = 0;
	private static final float DEFAULT_MITER_LIMIT = 1;
	private Point2D             firstPoint;
	private boolean             inUse;
//	private GeneralPath         pen;
	private DrawingShape             temporalDrawing;

	/**
	 * 
	 */
	public PenTool() {
		this("Pen");
	}
	
	
	/**
	 * 
	 * @param name
	 */
	public PenTool(String name) {
		super(tellapicConstants.TOOL_PATH, name, PEN_ICON_PATH, Utils.msg.getString("pentooltip") ,Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse = false;
		temporalDrawing = new DrawingShapePen(getName(), 0, 0);
//		setToolCursor(PEN_ICON_PATH, 0, 15);
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasColorCapability()
	 */
	@Override
	public boolean hasColorCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasFontCapability()
	 */
	@Override
	public boolean hasFontCapability() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeCapability()
	 */
	@Override
	public boolean hasStrokeCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		return inUse;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultWidth()
	 */
	@Override
	public double getDefaultWidth() {
		return DEFAULT_WIDTH;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultAlpha()
	 */
	@Override
	public double getDefaultAlpha() {
		return DEFAULT_ALPHA;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultCaps()
	 */
	@Override
	public int getDefaultCaps() {
		return DEFAULT_CAPS;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultColor()
	 */
	@Override
	public Color getDefaultColor() {
		return DEFAULT_COLOR;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontFace()
	 */
	@Override
	public String getDefaultFontFace() {
		return null;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontSize()
	 */
	@Override
	public double getDefaultFontSize() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontStyle()
	 */
	@Override
	public int getDefaultFontStyle() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultJoins()
	 */
	@Override
	public int getDefaultJoins() {
		return DEFAULT_JOINS;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultMiterLimit()
	 */
	@Override
	public float getDefaultMiterLimit() {
		return DEFAULT_MITER_LIMIT;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getTemporalDrawing()
	 */
	@Override
	public AbstractDrawing getTemporalDrawing() {
		return temporalDrawing;
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
			AbstractUser user = null;
			if (e instanceof RemoteMouseEvent) {
				user = ((RemoteMouseEvent)e).getUser();
			} else {
				user = UserManager.getInstance().getLocalUser();
			}
			IToolBoxState toolBoxState = user.getToolBoxModel();
			firstPoint.setLocation(e.getX(), e.getY());
//			pen = new GeneralPath();
//			pen.moveTo(e.getX(), e.getY());
			inUse = true;
			temporalDrawing = new DrawingShapePen(getName(), e.getX(), e.getY());
//			temporalDrawing.setShape(pen);
			temporalDrawing.setAlpha(toolBoxState.getOpacityProperty());
			temporalDrawing.setColor(toolBoxState.getColorProperty());
			temporalDrawing.setStroke(toolBoxState.getStrokeProperty());
			temporalDrawing.setNumber(toolBoxState.getAssignedNumber());
			temporalDrawing.setUser(user);
			user.setTemporalDrawing(temporalDrawing);
			e.consume();
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (isBeingUsed()) {
				AbstractUser user = null;
				if (e instanceof RemoteMouseEvent) {
					user = ((RemoteMouseEvent)e).getUser();
				} else {
					user = UserManager.getInstance().getLocalUser();
				}
				((GeneralPath)temporalDrawing.getShape()).lineTo(e.getX(), e.getY());
				inUse = false;
				temporalDrawing.cloneProperties();
				user.addDrawing(temporalDrawing);
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
			if (inUse) {
				((GeneralPath)temporalDrawing.getShape()).lineTo(e.getX(), e.getY());
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
		// TODO Auto-generated method stub
		
	}
}
