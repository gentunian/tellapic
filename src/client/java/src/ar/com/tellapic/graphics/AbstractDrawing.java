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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.TellapicAbstractUser;
import ar.com.tellapic.console.IConsoleCommand;
import ar.com.tellapic.graphics.ControlPoint.ControlPointType;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class AbstractDrawing implements TableModel, ListSelectionListener, IConsoleCommand {
	
	/* Key constant that indicates the property of being selected */
	public static final String   PROPERTY_SELECTION  = "Selected";
	
	/* Key constant that indicates the property of being visible */
	public static final String   PROPERTY_VISIBILITY = "Visible";
	
	/* Key constant that indicates the property of being resizeable */
	public static final String   PROPERTY_RESIZEABLE = "Resizeable";
	
	/* Key constant that indicates the property of being moveable */
	public static final String   PROPERTY_MOVEABLE   = "Moveable";

	/* Key constant that indicates the name property */
	public static final String   PROPERTY_NAME       = "Name";
	
	/* Key constant that indicates the number property */
	public static final String   PROPERTY_NUMBER     = "Number";
	
	/* Key constant that indicates the user name property */
	public static final String   PROPERTY_USER_NAME  = "User Owner";
	
	/* Key constant that indicates the property of being fillable */
	public static final String   PROPERTY_FILLABLE   = "Fillable";
	
	/* Key constant that indicates the property of having edges selected */
	public static final String   PROPERTY_VERTICAL_EDGES_SELECTED      = "Edge Selected (Vertically)";
	
	/* Key constant that indicates the property of having edges selected */
	public static final String   PROPERTY_HORIZONTAL_EDGES_SELECTED    = "Edge Selected (Horizontally)";
	
	/* Value for the left edge being selected */
	public static final String   VALUE_VERTICAL_LEFT_EDGE_SELECTED     = "Left";
	
	/* Value for the rigth edge being selected */
	public static final String   VALUE_VERTICAL_RIGHT_EDGE_SELECTED    = "Right";
	
	/* Value for the top edge being selected */
	public static final String   VALUE_HORIZONTAL_TOP_EDGE_SELECTED    = "Top";
	
	/* Value for the bottom edge being selected */
	public static final String   VALUE_HORIZONTAL_BOTTOM_EDGE_SELECTED = "Bottom";
	
	/* Value for the two edges being selected */
	public static final String   VALUE_BOTH_EDGES_SELECTED             = "Both";
	
	/* Value for the none edge being selected */
	public static final String   VALUE_NONE_EDGE_SELECTED              = "None";
	
	/* The rendering hints to be used while drawing this shape */
	protected RenderingHints           renderingHints;
	
	/* A helper class to report changes on properties */
	protected PropertyChangeSupport    pcs;
	
	/* The set of properties this shape has */
	protected Map<String, Object>      properties;
	
	/* The user owner of this shape */
	private   TellapicAbstractUser     user;
	
	/* Each shape, if resizeable, has 8 control points where they can be grabbed */
	/* for resizing the shape. If the shape isn't resizeable, it won't have any. */
	protected ControlPoint             controlPoints[];
	
	/* How this drawing should be drawn (stroke) while is being selected */
	/* This will affect how this drawing boundaries are drawn. */
	protected BasicStroke              selectedShapeStroke;
	
	/* How this drawing edges straight lines should be drawn when they are selected */
	/* This will affect the straight lines the edges have when selected */
	protected BasicStroke              selectedEdgesStroke;
	
	/* The composite value for the drawing when its selected */
	protected AlphaComposite           selectedAlphaComposite;
	
	/**
	 * Public Constructor.
	 * @param name The name this drawing will have. Commonly, is the name of the tool used to draw this drawing.
	 * @param resizeable If this drawing could be resizeable or not. This will affect controlPoints feature.
	 * @param moveable If this drawing supports being moved arround.
	 * @param fillable If this drawing is fillable. For example, lines aren't filleable as they don't enclose an area.
	 */
	public AbstractDrawing(String name, boolean resizeable, boolean moveable, boolean fillable) {
		pcs                    = new PropertyChangeSupport(this);
		properties             = new TreeMap<String, Object>();
		renderingHints         = new RenderingHints(null);
		selectedShapeStroke    = new BasicStroke(2, 0, 0, 1, new float[] { 5, 5}, 0);
		selectedEdgesStroke    = new BasicStroke(2, 0, 0); //, new float[] { 5, 5}, 0);
		selectedAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
		
		/* Set this drawing name */
		setName(name);
		
		/* Set its initial visible value as true */
		setVisible(true);
		
		/* Set the resize ability for this drawing */
		setResizeable(resizeable);
		
		/* Set the move ability for this drawing */
		setMoveable(moveable);
		
		/* Set the fill ability for this drawing */
		setFillable(fillable);
		
		/* Set the selected state for this drawing. Initially, a drawing isn't selected */
		setSelected(false);
		
		/* Create its control points if applicable */
		createControlPoints();
	}
	
	/**
	 * Creates this drawing control points.
	 */
	private void createControlPoints() {
		if (isResizeable()) {
			/* Instantiates the control points this drawing will have */
			controlPoints  = new ControlPoint[8];
			int i = 0;
			for(ControlPointType type : ControlPoint.ControlPointType.values()) {
				try {
					controlPoints[i++] = new ControlPoint(type, Color.white);
				} catch (IllegalControlPointTypeException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param l
	 */
	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}
	
	/**
	 * 
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(propertyName, listener);
	}
	
	/**
	 * 
	 * @param l
	 */
	public void removePropertyChangeListener(PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}
	
	/**
	 * Call this methos after a resize or move on subclasses. This will update control points location.
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
		Long v = (Long) properties.get(PROPERTY_NUMBER);
		return (v != null)? v.longValue() : 0;
	}
	
	/**
	 * 
	 * @param n
	 */
	public void setNumber(long n) {
		properties.put(PROPERTY_NUMBER, n);
		pcs.firePropertyChange(PROPERTY_NUMBER, 0, n);
		fireTableUpdate();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isVisible() {
		Boolean v = (Boolean) properties.get(PROPERTY_VISIBILITY);
		return (user.isVisible())? v.booleanValue() : user.isVisible();
	}
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		Object oldVisible =  properties.get(PROPERTY_VISIBILITY);
		properties.put(PROPERTY_VISIBILITY, visible);
		pcs.firePropertyChange(PROPERTY_VISIBILITY, oldVisible, visible);
		fireTableUpdate();
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(TellapicAbstractUser user) {
		properties.put(PROPERTY_USER_NAME, user.getName());
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public TellapicAbstractUser getUser() {
		return user;
	}
	
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		properties.put(PROPERTY_NAME, name);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return (String) properties.get(PROPERTY_NAME);
	}
	
	/**
	 * @return the controlPoints
	 */
	public ControlPoint[] getControlPoints() {
		return this.controlPoints;
	}
	
	/**
	 * Gets the control point specified by a type. For instance, you can get the left control point
	 * using {@code getControlPointByType(ControlPointType.LEFT_CONTROL_POINT);}
	 * @param type
	 * @return
	 */
	public ControlPoint getControlPointByType(ControlPointType type) {
		if (controlPoints == null)
			return null;

		int i = 0;
		for (i = 0; i < controlPoints.length; i++)
			if (controlPoints[i].getType().equals(type))
				return controlPoints[i];
			
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isResizeable() {
		Boolean v = (Boolean) properties.get(PROPERTY_RESIZEABLE);
		return v.booleanValue();
	}
	
	/**
	 * 
	 * @param v
	 */
	public void setResizeable(boolean v) {
		Object oldValue =  properties.get(PROPERTY_RESIZEABLE);
		properties.put(PROPERTY_RESIZEABLE, v);
		pcs.firePropertyChange(PROPERTY_RESIZEABLE, oldValue, v);
	}
	
	/**
	 * 
	 * @param v
	 */
	public void setMoveable(boolean v) {
		Object oldValue = properties.get(PROPERTY_MOVEABLE);
		properties.put(PROPERTY_MOVEABLE, v);
		pcs.firePropertyChange(PROPERTY_MOVEABLE, oldValue, v);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isMoveable() {
		Boolean v = (Boolean) properties.get(PROPERTY_MOVEABLE);
		return v.booleanValue();
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
		Object oldValue = properties.get(PROPERTY_SELECTION);
		properties.put(PROPERTY_SELECTION, isSelected);
		if (isSelected)
			updateControlPoints();
		pcs.firePropertyChange(PROPERTY_SELECTION, oldValue, isSelected);
		fireTableUpdate();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSelected() {
		Boolean v = (Boolean) properties.get(PROPERTY_SELECTION);
		return v.booleanValue();
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setFillable(boolean value) {
		properties.put(PROPERTY_FILLABLE, value);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isFillable() {
		Boolean v = (Boolean) properties.get(PROPERTY_FILLABLE);
		return v.booleanValue();
	}
	
	/**
	 * 
	 * @param pValue
	 * @param value
	 */
	protected void setVerticalEdgePropertyValue(String value) {
		Object oldValue = properties.get(PROPERTY_VERTICAL_EDGES_SELECTED);
		properties.put(PROPERTY_VERTICAL_EDGES_SELECTED, value);
		pcs.firePropertyChange(PROPERTY_VERTICAL_EDGES_SELECTED, oldValue, value);
		fireTableUpdate();
	}
	
	/**
	 * 
	 * @param pValue
	 * @param value
	 */
	protected void setHorizontalEdgePropertyValue(String value) {
		Object oldValue = properties.get(PROPERTY_HORIZONTAL_EDGES_SELECTED);
		properties.put(PROPERTY_HORIZONTAL_EDGES_SELECTED, value);
		pcs.firePropertyChange(PROPERTY_HORIZONTAL_EDGES_SELECTED, oldValue, value);
		fireTableUpdate();
	}
	
	/**
	 * @param isLeftEdgeSelected the isLeftEdgeSelected to set
	 */
	public void setLeftEdgeSelected(boolean isLeftEdgeSelected) {
		if (isLeftEdgeSelected && isRightEdgeSelected())
			setVerticalEdgePropertyValue(VALUE_BOTH_EDGES_SELECTED);
		else if (isLeftEdgeSelected)
			setVerticalEdgePropertyValue(VALUE_VERTICAL_LEFT_EDGE_SELECTED);
		else if (isRightEdgeSelected())
			setVerticalEdgePropertyValue(VALUE_VERTICAL_RIGHT_EDGE_SELECTED);
		else
			setVerticalEdgePropertyValue(VALUE_NONE_EDGE_SELECTED);
	}

	/**
	 * @return the isLeftEdgeSelected
	 */
	public boolean isLeftEdgeSelected() {
		Object value = properties.get(PROPERTY_VERTICAL_EDGES_SELECTED);
		return (value != null)? value.equals(VALUE_VERTICAL_LEFT_EDGE_SELECTED) || value.equals(VALUE_BOTH_EDGES_SELECTED) : false;
	}

	/**
	 * @param isRightEdgeSelected the isRightEdgeSelected to set
	 */
	public void setRightEdgeSelected(boolean isRightEdgeSelected) {
		if (isRightEdgeSelected && isLeftEdgeSelected())
			setVerticalEdgePropertyValue(VALUE_BOTH_EDGES_SELECTED);
		else if (isRightEdgeSelected)
			setVerticalEdgePropertyValue(VALUE_VERTICAL_LEFT_EDGE_SELECTED);
		else if (isLeftEdgeSelected())
			setVerticalEdgePropertyValue(VALUE_VERTICAL_RIGHT_EDGE_SELECTED);
		else
			setVerticalEdgePropertyValue(VALUE_NONE_EDGE_SELECTED);
	}

	/**
	 * @return the isRightEdgeSelected
	 */
	public boolean isRightEdgeSelected() {
		Object value = properties.get(PROPERTY_VERTICAL_EDGES_SELECTED);
		return (value != null)? value.equals(VALUE_VERTICAL_RIGHT_EDGE_SELECTED) || value.equals(VALUE_BOTH_EDGES_SELECTED) : false;
	}

	/**
	 * @param isTopEdgeSelected the isTopEdgeSelected to set
	 */
	public void setTopEdgeSelected(boolean isTopEdgeSelected) {
		if (isTopEdgeSelected && isBottomEdgeSelected())
			setHorizontalEdgePropertyValue(VALUE_BOTH_EDGES_SELECTED);
		else if (isTopEdgeSelected)
			setHorizontalEdgePropertyValue(VALUE_HORIZONTAL_TOP_EDGE_SELECTED);
		else if (isBottomEdgeSelected())
			setHorizontalEdgePropertyValue(VALUE_HORIZONTAL_BOTTOM_EDGE_SELECTED);
		else
			setHorizontalEdgePropertyValue(VALUE_NONE_EDGE_SELECTED);
	}

	/**
	 * @return the isTopEdgeSelected
	 */
	public boolean isTopEdgeSelected() {
		Object value = properties.get(PROPERTY_HORIZONTAL_EDGES_SELECTED);
		return (value != null)? value.equals(VALUE_HORIZONTAL_TOP_EDGE_SELECTED) || value.equals(VALUE_BOTH_EDGES_SELECTED) : false;
	}

	/**
	 * @param isBottomEdgeSelected the isBottomEdgeSelected to set
	 */
	public void setBottomEdgeSelected(boolean isBottomEdgeSelected) {
		if (isTopEdgeSelected() && isBottomEdgeSelected)
			setHorizontalEdgePropertyValue(VALUE_BOTH_EDGES_SELECTED);
		else if (isTopEdgeSelected())
			setHorizontalEdgePropertyValue(VALUE_HORIZONTAL_TOP_EDGE_SELECTED);
		else if (isBottomEdgeSelected)
			setHorizontalEdgePropertyValue(VALUE_HORIZONTAL_BOTTOM_EDGE_SELECTED);
		else
			setHorizontalEdgePropertyValue(VALUE_NONE_EDGE_SELECTED);
	}

	/**
	 * @return the isBottomEdgeSelected
	 */
	public boolean isBottomEdgeSelected() {
		Object value = properties.get(PROPERTY_HORIZONTAL_EDGES_SELECTED);
		return (value != null)? value.equals(VALUE_HORIZONTAL_BOTTOM_EDGE_SELECTED) || value.equals(VALUE_BOTH_EDGES_SELECTED) : false;
	}

	/**
	 * 
	 * @param graphics
	 */
	public void draw(Graphics g) {
		Graphics2D graphics = (Graphics2D) g;
		
		graphics.setStroke(selectedEdgesStroke);
		graphics.setColor(Color.orange);
//		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1f));
		
		if (isLeftEdgeSelected())
			graphics.drawLine((int)getBounds2D().getX(), 0, (int)getBounds2D().getX(), Integer.MAX_VALUE);
		
		if (isRightEdgeSelected())
			graphics.drawLine((int)getBounds2D().getMaxX(), 0, (int)getBounds2D().getMaxX(), Integer.MAX_VALUE);
		
		if (isTopEdgeSelected())
			graphics.drawLine(0, (int)getBounds2D().getY(), Integer.MAX_VALUE, (int)getBounds2D().getY());
		
		if (isBottomEdgeSelected())
			graphics.drawLine(0, (int)getBounds2D().getMaxY(), Integer.MAX_VALUE, (int)getBounds2D().getMaxY());
		
		if (isSelected()) {
			graphics.setRenderingHints(renderingHints);
//			g.setComposite(selectedAlphaComposite);
			graphics.setColor(Color.yellow);
			graphics.setStroke(selectedShapeStroke);
			graphics.draw(getBounds2D());
			ControlPoint[] points = getControlPoints();
			
			/* Dont draw control points for remote users */
			if (points != null && !getUser().isRemote()) {
				ControlPoint selectedPoint = null;
				for(ControlPoint p : points)
					/* If a control point is selected, draw it in the last order */
					if (p.isSelected())
						selectedPoint = p;
					else
						p.draw(graphics);
				/* If a control point is selected, draw it in the last order */
				if (selectedPoint != null)
					selectedPoint.draw(graphics);
			}
		}
	}

	/**
	 * 
	 */
	public void sendRemoved() {
		if (NetManager.getInstance().isConnected()) {

			String number = String.valueOf(getNumber());
			tellapic.tellapic_send_ctle(
					NetManager.getInstance().getSocket(),
					SessionUtils.getId(),
					tellapicConstants.CTL_CL_RMFIG,
					number.length(),
					number
			);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract Rectangle2D getBounds2D();
	
	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public abstract void setBounds(int x1, int y1, int x2, int y2);
	
	/**
	 * 
	 * @param eventX
	 * @param eventY
	 * @param controlPoint
	 */
	public abstract void resize(double eventX, double eventY, ControlPoint controlPoint);
	
	/**
	 * 
	 * @param xOffset
	 * @param yOffset
	 */
	public abstract void move(double xOffset, double yOffset);
	
	/**
	 * 
	 * @return
	 */
	public abstract int getFirstX();
	
	/**
	 * 
	 * @return
	 */
	public abstract int getFirstY();
	
	/**
	 * 
	 * @return
	 */
	public abstract int getLastX();
	
	/**
	 * 
	 * @return
	 */
	public abstract int getLastY();
	
	/**
	 * 
	 */
	protected abstract void fireTableUpdate();
	
	/**
	 * 
	 * @return
	 */
	public abstract Shape getFillableShape();
	
	/**
	 * 
	 * @return
	 */
	public abstract Paint getFillableShapePaint();
	
	/**
	 * 
	 */
	public abstract void sendPressed(MouseEvent event);
	
	/**
	 * 
	 */
	public abstract void sendDragged(MouseEvent event);
	
	/**
	 * 
	 */
	public abstract void sendReleased(MouseEvent event);
	
	/**
	 * 
	 */
	public abstract void sendDeferred();
	
	/**
	 * 
	 */
	public abstract void sendChanged();
}
