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
package ar.com.tellapic;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Observable;

import ar.com.tellapic.graphics.Drawing;
import ar.com.tellapic.graphics.IPaintPropertyController;
import ar.com.tellapic.graphics.IToolBoxController;
import ar.com.tellapic.graphics.PaintProperty;
import ar.com.tellapic.graphics.PaintPropertyAlpha;
import ar.com.tellapic.graphics.PaintPropertyColor;
import ar.com.tellapic.graphics.PaintPropertyFont;
import ar.com.tellapic.graphics.PaintPropertyStroke;
import ar.com.tellapic.graphics.ToolBoxModel;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class AbstractUser extends Observable  {
	
	public static final int CUSTOM_PAINT_PROPERTY_COLOR  = 0;
	public static final int CUSTOM_PAINT_PROPERTY_STROKE = 1;
	public static final int CUSTOM_PAINT_PROPERTY_FONT   = 2;
	public static final int CUSTOM_PAINT_PROPERTY_ALPHA  = 3;

	//Each user will have control over its own tool box. The tool box model, for remote users
	//will be updated from wrapped remote events dispatched to the actual model controller.
	//Local user will update its model from mouse events.
	private ToolBoxModel             toolBox;
	private IToolBoxController       toolboxController;
	private IPaintPropertyController paintController;
	
	//Each user, will also have a list of drawing objects. Each of this objects holds the
	//complete information to be drawn on the screen. This objects will live as long as the
	//local user wants, that is, if a user gets disconnected, the user will appear as disconnected
	//and it won't be removed unless the local user wants to.
	private ArrayList<Drawing>      drawingList;
	
	
	// This is the first drawing, added to the list of drawings, that has not been drawn yet on screen.
	// And, that divides the drawing list in two as follows:
	//
	//                            firstNotDrawn
	//                                  ^
	//                                 not   not
	//  drawn drawn drawn drawn       drawn drawn
	//    ^     ^     ^     ^           ^     ^
	// +-----+-----+-----+-----+ ... +-----+-----+
	// |     |     |     |     |     |     |     |
	// +-----+-----+-----+-----+ ... +-----+-----+
	//
	// That means that updating 'firstNotDrawn' is just selecting the next not drawn object if applicable.
//	private Drawing                 firstNotDrawn;

	
	// The firstNotDrawn is a candidate to be the lastDrawn when it gets drawn. This is useful to know
	// which drawing was the last being drawn, and views can get this field instead of searching the whole
	// list.
//	private Drawing                 lastDrawn;
	
	
	//This is the actual drawing object. That is, the object that the user *is* drawing. This is used
	//to create a drawing animation effect. Commonly, this drawing is updated while dragging the mouse (if local)
	//and creating the desired form, size, transparency, etc. This is a candidate to be added to the drawing
	//list and to be a 'firstNotDrawn' object accordingly, if the user desires to complete the drawing. Else,
	//this object will be discarded and not drawn.
	private Drawing                 temporalDrawing;
	
	
	// User properties
	//private int     state;
	private int              userId;
	private String           name;
	private boolean          visible;
	private boolean          removed;
	private PaintProperty[]  customProperties;
	
	
	/**
	 * 
	 */
	public AbstractUser() {
		visible = true;
		removed = false;
		toolBox   = new ToolBoxModel();
		drawingList = new ArrayList<Drawing>();
		//TODO: the idea was that local user can set how to paint all paintings of a user
		customProperties = new PaintProperty[4];
//		addObserver(DrawingAreaView.getInstance());
//		addObserver(UserManager.getInstance());
	}
	
	
	/**
	 * 
	 * @param id
	 * @param name
	 */
	public AbstractUser(int id, String name) {
		this();
		this.name = name;
		userId    = id;
	}
	
	
	/**
	 * 
	 * @param drawing
	 * @throws NullPointerException
	 */
	public synchronized void addDrawing(Drawing drawing) throws NullPointerException {
		if (drawing == null)
			throw new NullPointerException("Drawing should not be null");
		
		// If the drawing we are added is a temporal, then, it should no more be one.
		if (temporalDrawing != null && temporalDrawing.equals(drawing))
			temporalDrawing = null;
		
		drawingList.add(drawing);
		
		/* Shouldn't we set this line outside this object??? */
		drawing.setUser(this);
		
		/* Report the view only if we are visible */
		setChanged();
		notifyObservers(drawing);
	}
	
	
	/**
	 * 
	 * @param id
	 */
	public AbstractUser(int id) {
		this(id, null);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public ToolBoxModel getToolBoxModel() {
		return toolBox;
	}
	

	/**
	 * 
	 * @return
	 */
	public int getUserId() {
		return userId;
	}
	
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		return name;
	}
	
	
	/**
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		setChanged();
		notifyObservers(visible);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return visible;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean isSpecial();
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean isSelected();
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean isRemote();
	
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}
	

	public void setName(String name) {
		this.name = name;
		setChanged();
		notifyObservers();
	}
	
	public void setUserId(int id) {
		this.userId = id;
		setChanged();
		notifyObservers();
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<Drawing> getDrawings() {
		return drawingList;
	}
	
	
	/**
	 * 
	 * @param drawing
	 */
	public void changeDrawingVisibility(Drawing drawing) {
		if (!drawingList.contains(drawing))
			throw new NoSuchElementException("Drawing is not a member of this user drawing list");
		
		boolean oldValue = drawing.isVisible();
		drawing.setVisible(!oldValue);
		setChanged();
		notifyObservers(drawingList.indexOf(drawing));
	}
	
	
	/**
	 * 
	 * @param drawing
	 */
	public void setDrawingVisible(Drawing drawing, boolean visible) {
		if (!drawingList.contains(drawing))
			throw new NoSuchElementException("Drawing is not a member of this user drawing list");
		
		drawing.setVisible(visible);
		setChanged();
		notifyObservers(drawingList.indexOf(drawing));
	}
	
	
	/**
	 * 
	 */
	public void cleanUp() {
		removed = true;
		setChanged();
		notifyObservers();
		deleteObservers();
	}
	

	/**
	 * @return the removed
	 */
	public boolean isRemoved() {
		return removed;
	}


	/**
	 * 
	 * @param property
	 * @param type
	 * @throws NoSuchPropertyType
	 * @throws WrongPropertyType
	 */
	public void setCustomProperty(PaintProperty property, int type) throws NoSuchPropertyTypeException, WrongPropertyTypeException {
		if (type != CUSTOM_PAINT_PROPERTY_COLOR && type != CUSTOM_PAINT_PROPERTY_STROKE && type != CUSTOM_PAINT_PROPERTY_FONT && type != CUSTOM_PAINT_PROPERTY_ALPHA )
			throw new NoSuchPropertyTypeException("Cannot find type "+type);
		
		
		if ( property != null &&
				(type == CUSTOM_PAINT_PROPERTY_COLOR && !(property instanceof PaintPropertyColor)) ||
				(type == CUSTOM_PAINT_PROPERTY_STROKE &&  !(property instanceof PaintPropertyStroke)) ||
				(type == CUSTOM_PAINT_PROPERTY_FONT && !(property instanceof PaintPropertyFont)) ||
				(type == CUSTOM_PAINT_PROPERTY_ALPHA &&!(property instanceof PaintPropertyAlpha)))
			throw new WrongPropertyTypeException("type and property does not match.");
	
		
		customProperties[type] = property;
		setChanged();
		notifyObservers(property);
	
	}
	
	
	/**
	 * 
	 * @param type
	 * @throws NoSuchPropertyTypeException
	 * @throws WrongPropertyTypeException
	 */
	public void removeCustomProperty(int type) throws NoSuchPropertyTypeException {
		if (type != CUSTOM_PAINT_PROPERTY_COLOR && type != CUSTOM_PAINT_PROPERTY_STROKE && type != CUSTOM_PAINT_PROPERTY_FONT && type != CUSTOM_PAINT_PROPERTY_ALPHA )
			throw new NoSuchPropertyTypeException("Cannot find type "+type);
		
		customProperties[type] = null;
		setChanged();
		notifyObservers();
	}
	
	
	/**
	 * 
	 * @param type
	 * @return
	 * @throws NoSuchPropertyType
	 */
	public PaintProperty getCustomProperty(int type) throws NoSuchPropertyTypeException {
		if (type != CUSTOM_PAINT_PROPERTY_COLOR && type != CUSTOM_PAINT_PROPERTY_STROKE && type != CUSTOM_PAINT_PROPERTY_FONT && type != CUSTOM_PAINT_PROPERTY_ALPHA )
			throw new NoSuchPropertyTypeException("Cannot find type "+type);
		
		return customProperties[type];
	}
	
	
	/**
	 * @return the customProperties
	 */
	public PaintProperty[] getCustomProperties() {
		return customProperties;
	}


	/**
	 * @param drawing
	 */
	public synchronized boolean removeDrawing(Drawing drawing) {
		if (drawing == null)
			return false;
		
		int index = drawingList.indexOf(drawing);
		boolean removed = drawingList.remove(drawing);
		
		if (removed) {
			setChanged();
			notifyObservers(new Object[]{drawing, index});
			String number = String.valueOf(drawing.getNumber());
			if (!isRemote())
				tellapic.tellapic_send_ctle(NetManager.getInstance().getSocket(), SessionUtils.getId(), tellapicConstants.CTL_CL_RMFIG, number.length(), number);
		}
		
		return removed;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public synchronized boolean removeLastDrawing() {
		if (drawingList.isEmpty())
			return false;
		
		Drawing drawing = drawingList.get(drawingList.size() - 1);
		
		return removeDrawing(drawing);
	}


	/**
	 * 
	 */
	public void changeVisibility() {
		boolean oldValue = isVisible();
		setVisible(!oldValue);
	}


	/**
	 * @return
	 */
	public boolean isDrawing() {
		return temporalDrawing != null;
	}


	/**
	 * @return
	 */
	public Drawing getDrawing() {
		return temporalDrawing;
	}


	/**
	 * @param temporalDrawing2
	 */
	public void setTemporalDrawing(Drawing temporalDrawing) {
		this.temporalDrawing = temporalDrawing; 
	}


	/**
	 * @param number
	 */
	public synchronized boolean removeDrawing(String number) {
		return removeDrawing(Long.parseLong(number));
	}
	
	/**
	 * 
	 * @param number
	 */
	public synchronized boolean removeDrawing(long number) {
		Drawing drawing = findDrawing(number);
		
		if (drawing == null)
			return false;
		
		return removeDrawing(drawing);
	}
	
	/**
	 * 
	 * @param number
	 * @return
	 */
	private Drawing findDrawing(long number) {
		boolean found   = false;
		Drawing drawing = null;
		
		for(int i = 0; i < drawingList.size() && !found; i++) {
			if (found = (drawingList.get(i).getNumber() == number))
				drawing = drawingList.get(i);
		}
		
		return drawing;
	}
	
	/**
	 * @param toolboxController the toolboxController to set
	 */
	public void setToolboxController(IToolBoxController toolboxController) {
		this.toolboxController = toolboxController;
	}

	/**
	 * @return the toolboxController
	 */
	public IToolBoxController getToolboxController() {
		return toolboxController;
	}

	/**
	 * @param paintController the paintController to set
	 */
	public void setPaintController(IPaintPropertyController paintController) {
		this.paintController = paintController;
	}

	/**
	 * @return the paintController
	 */
	public IPaintPropertyController getPaintController() {
		return paintController;
	}
}
