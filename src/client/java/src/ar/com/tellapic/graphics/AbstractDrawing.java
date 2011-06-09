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
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Observable;

import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.graphics.ControlPoint.ControlType;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class AbstractDrawing extends Observable implements Cloneable, TableModel, ListSelectionListener {
	
	protected RenderingHints    renderingHints;
	protected Object[][]        properties;
	private String              name;
	private long                number;
	private boolean             isVisible;
	private AbstractUser        user;
	protected ControlPoint      controlPoints[];
	protected boolean           selected;
	protected boolean           resizeable;
	protected boolean           moveable;
	protected BasicStroke       selectedShapeStroke;
	protected BasicStroke       selectedEdgesStroke;
	protected AlphaComposite    selectedAlphaComposite;
	
	/**
	 * 
	 * @param name
	 * @param resizeable
	 * @param moveable
	 */
	public AbstractDrawing(String name, boolean resizeable, boolean moveable) {
		renderingHints = new RenderingHints(null);
		selectedAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
		selectedShapeStroke = new BasicStroke(2, 0, 0, 1, new float[] { 5, 5}, 0);
		if (resizeable) {
			/* Instantiates the control points this drawing will have */
			controlPoints  = new ControlPoint[8];
			int i = 0;
			for(ControlType type : ControlPoint.ControlType.values()) {
				try {
					controlPoints[i++] = new ControlPoint(type, Color.white);
				} catch (IllegalControlPointTypeException e) {
					e.printStackTrace();
				}
			}
		}
		setResizeable(resizeable);
		setMoveable(moveable);
	}
	
	/**
	 * 
	 */
	public void updateControlPoints() {
		if (controlPoints != null) {
			for(ControlPoint point : controlPoints){
				try {
					point.setControlPoint(getBounds2D());
				} catch (IllegalControlPointTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public long getNumber() {
		return number;
	}
	
	/**
	 * 
	 * @param n
	 */
	public void setNumber(long n) {
		number = n;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return (user.isVisible())? isVisible : user.isVisible();
	}
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		isVisible = visible;
		setChanged();
		notifyObservers();
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(AbstractUser user) {
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public AbstractUser getUser() {
		return user;
	}
	
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 */
	public Object clone() {
		try {
			cloneProperties();
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			// This should never happen
			throw new InternalError(e.toString());
		}
	}
	
	/**
	 * @return the controlPoints
	 */
	public ControlPoint[] getControlPoints() {
		return this.controlPoints;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public ControlPoint getControlPointByType(ControlType type) {
		if (controlPoints == null)
			return null;

		int i = 0;
		for (i = 0; i < controlPoints.length; i++) {
			if (controlPoints[i].getType().equals(type))
				return controlPoints[i];
		}
		return null;
	}

	/**
	 * @param controlPoints the controlPoints to set
	 */
	public void setControlPoints(ControlPoint[] controlPoints) {
		this.controlPoints = controlPoints;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isResizeable() {
		return resizeable;
	}
	
	/**
	 * 
	 * @param v
	 */
	public void setResizeable(boolean v) {
		resizeable = v;
	}
	
	/**
	 * 
	 * @param v
	 */
	public void setMoveable(boolean v) {
		moveable = v;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isMoveable() {
		return moveable;
	}
	
	/**
	 * @param controlPoint
	 * @return
	 */
	public boolean ownsControlPoint(ControlPoint controlPoint) {
		if (controlPoints == null)
			return false;
		int i = 0;
		for (i = 0; i < controlPoints.length && !controlPoints[i].equals(controlPoint); i++);
		return (i < controlPoints.length);
	}
	
	/**
	 * 
	 * @param hints
	 */
	public void setRenderingHints(RenderingHints hints) {
		renderingHints.add(hints);
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putRenderingHint(RenderingHints.Key key, Object value) {
		renderingHints.put(key, value);
	}
	
	/**
	 * 
	 * @param key
	 */
	public void removeRenderingHint(RenderingHints.Key key) {
		renderingHints.remove(key);
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setSelected(boolean isSelected) {
		selected = isSelected;
		if (isSelected) {
			for(AbstractDrawing drawing : getUser().getDrawings())
				if (!drawing.equals(this))
					drawing.setSelected(false);
			updateControlPoints();
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}
	
	/**
	 * 
	 * @param g
	 */
	public void draw(Graphics2D g) {
		if (isSelected()) {
			g.setRenderingHints(renderingHints);
//			g.setComposite(selectedAlphaComposite);
			g.setColor(Color.yellow);
			g.setStroke(selectedShapeStroke);
			g.draw(getBounds2D());
			ControlPoint[] points = getControlPoints();
			if (points != null) {
				ControlPoint selectedPoint = null;
				for(ControlPoint p : points)
					/* If a control point is selected, draw it in the last order */
					if (p.isSelected())
						selectedPoint = p;
					else
						p.draw(g);
				/* If a control point is selected, draw it in the last order */
				if (selectedPoint != null)
					selectedPoint.draw(g);
			}
		}
	}
	public abstract void cloneProperties();
//	public abstract void setSelected(boolean value);
//	public abstract boolean isSelected();
	public abstract Rectangle2D getBounds2D();
	public abstract void resize(double eventX, double eventY, ControlPoint controlPoint);
	public abstract void move(double xOffset, double yOffset);
	
}
