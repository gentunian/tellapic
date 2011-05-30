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

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import ar.com.tellapic.AbstractUser;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class AbstractDrawing implements Cloneable {

	private String              name;
	private long                number;
	private boolean             isVisible;
	private AbstractUser        user;
	protected ControlPoint      controlPoints[];
	protected boolean           selected;
	
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
	 * 
	 * @param g
	 */
	public abstract void draw(Graphics2D g);

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
	
	public abstract void cloneProperties();
	
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
	 * @param controlPoints the controlPoints to set
	 */
	public void setControlPoints(ControlPoint[] controlPoints) {
		this.controlPoints = controlPoints;
	}
	public abstract void setSelected(boolean value);
	public abstract boolean isSelected();
	public abstract Rectangle2D getBounds2D();
	public abstract boolean isResizeable();
	public abstract void resize(double eventX, double eventY, ControlPoint controlPoint);
	public abstract void move(double firstX, double firstY, double eventX, double eventY);

}
