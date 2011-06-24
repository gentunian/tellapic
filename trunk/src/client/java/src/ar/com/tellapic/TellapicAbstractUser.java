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
import java.util.Observable;

import ar.com.tellapic.adm.AbstractUser;
import ar.com.tellapic.console.ConsoleModel;
import ar.com.tellapic.console.IConsoleModelController;
import ar.com.tellapic.graphics.AbstractDrawing;
import ar.com.tellapic.graphics.DrawingAreaModel;
import ar.com.tellapic.graphics.IPaintPropertyController;
import ar.com.tellapic.graphics.IToolBoxController;
import ar.com.tellapic.graphics.PaintProperty;
import ar.com.tellapic.graphics.PaintPropertyAlpha;
import ar.com.tellapic.graphics.PaintPropertyColor;
import ar.com.tellapic.graphics.PaintPropertyController;
import ar.com.tellapic.graphics.PaintPropertyFont;
import ar.com.tellapic.graphics.PaintPropertyStroke;
import ar.com.tellapic.graphics.Tool;
import ar.com.tellapic.graphics.ToolBoxController;
import ar.com.tellapic.graphics.ToolBoxModel;
import ar.com.tellapic.utils.Utils;

/**
 * This class encapsulates user information. An User owns a toolbox model with 2 controllers over it.
 * Each user has a drawing list, that could be not sorted. This drawings are of their own, and only
 * this user can modify them. The only way drawings can be modified externally, is by setting this
 * user custom override properties. Although, no real modification is made to the actual drawings.
 * 
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class TellapicAbstractUser extends AbstractUser {
	
	public static final int CUSTOM_PAINT_PROPERTY_COLOR  = 0;
	public static final int CUSTOM_PAINT_PROPERTY_STROKE = 1;
	public static final int CUSTOM_PAINT_PROPERTY_FONT   = 2;
	public static final int CUSTOM_PAINT_PROPERTY_ALPHA  = 3;
	public static final int DRAWING_NUMBER_SET           = 4;
	public static final int REMOVE_DRAWING               = 5;
	public static final int PROPERTY_REMOVE              = 6;
	public static final int PROPERTY_SET                 = 7;
	public static final int CLEANUP                      = 8;
	public static final int ADD_DRAWING                  = 9;
	public static final int DRAWING_CHANGED              = 10;

	//Each user will have control over its own tool box. The tool box model, for remote users
	//will be updated from wrapped remote events dispatched to the actual model controller.
	//Local user will update its model from mouse events.
	private ToolBoxModel                     toolBox;
	private IToolBoxController               toolboxController;
	private IPaintPropertyController         paintController;
	
	//Each user, will also have a list of drawing objects. Each of this objects holds the
	//complete information to be drawn on the screen. This objects will live as long as the
	//local user wants, that is, if a user gets disconnected, the user will appear as disconnected
	//and it won't be removed unless the local user wants to.
	protected ArrayList<AbstractDrawing>     drawingList;
	
	//This is the actual drawing object. That is, the object that the user *is* drawing. This is used
	//to create a drawing animation effect. Commonly, this drawing is updated while dragging the mouse (if local)
	//and creating the desired form, size, transparency, etc. This is a candidate to be added to the drawing
	//list and to be a 'firstNotDrawn' object accordingly, if the user desires to complete the drawing. Else,
	//this object will be discarded and not drawn.
	private AbstractDrawing                  temporalDrawing;
	private PaintProperty[]                  customProperties;
	private boolean                          removed;
	
	
	
	/**
	 * Create an user instance with a toolbox model, a toolbox controller, a paint property controller
	 * and an empty drawing list. 
	 */
	public TellapicAbstractUser() {
		setVisible(true);
		removed     = false;
		toolBox     = new ToolBoxModel();
		drawingList = new ArrayList<AbstractDrawing>();
		
		//TODO: the idea was that local user can set how to paint all paintings of a user
		customProperties  = new PaintProperty[4];
		
		toolboxController = new ToolBoxController(toolBox);
		
		paintController   = new PaintPropertyController(toolBox);
		
		for(Tool tool : toolBox.getTools().values())
			tool.setUser(this);
	}
	
	/**
	 * Create an user instance with a toolbox model, a toolbox controller, a paint property controller
	 * and an empty drawing list with the specified name and id.
	 * @param id the id of this user.
	 * @param name the name of this user.
	 */ 
	public TellapicAbstractUser(int id, String name) {
		this();
		setName(name);
		setUserId(id);
	}
		
	/**
	 * Adds a drawing to this user drawing list. If the drawing has been numbered (f.i. a server) 
	 * @see {ar.com.tellapic.AbstractUser#setDrawingNumber(java.lang.String info))}, then
	 * the drawing is also added to the DrawingAreaModel that mantains an ordered list of drawings.
	 * 
	 * If the drawing added is the same object as the user temporal drawing, then the temporal drawing is
	 * set to null. Temporal drawings are drawings that the user is actually drawing.
	 *
	 * It's not possible to add a null drawing.
	 * 
	 * The user will notify its observer if is visible.
	 * 
	 * @param drawing the drawing to be added.
	 * @throws NullPointerException if the drawing to be added is null.
	 */
	public synchronized void addDrawing(AbstractDrawing drawing) throws NullPointerException {
		if (drawing == null)
			throw new NullPointerException("Drawing should not be null");
		
		// If the drawing we are added is a temporal, then, it should no more be one.
		if (temporalDrawing != null && temporalDrawing.equals(drawing))
			temporalDrawing = null;
		
		drawingList.add(drawing);
		
		/* Shouldn't we set this line outside this object??? */
		drawing.setUser(this);
		
		if (drawing.getNumber() != 0)
			DrawingAreaModel.getInstance().addDrawing(drawing);
		
		drawing.addObserver(this);
		
		setChanged();
		if (isVisible())
			notifyObservers(new Object[] {ADD_DRAWING, drawing});
	}

	
	/**
	 * Gets the model of this user toolbox.
	 * @return the model of this user toolbox.
	 */
	public ToolBoxModel getToolBoxModel() {
		return toolBox;
	}
	
	/**
	 * A string representation of this user is it's name.
	 */
	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Gets the drawings this user owns.
	 * @return the list of drawings this user has.
	 */
	public ArrayList<AbstractDrawing> getDrawings() {
		return drawingList;
	}
	
	/**
	 * 
	 */
	public void cleanUp() {
		removed = true;
		setChanged();
		notifyObservers(new Object[] {CLEANUP});
		deleteObservers();
	}
	

	/**
	 * Returns the removed state of this user.
	 * @return removed true if the user has been removed or disconnected. False otherwise.
	 */
	public boolean isRemoved() {
		return removed;
	}

	/**
	 * Sets custom properties for all drawings of this user. Overrides each drawing properties and sets
	 * each drawgin property to property. No actual drawing property modification is made. All drawings
	 * conserve its properties, but if any custom property is set, the drawing will be drawn with that
	 * custom property.
	 * 
	 * Commonly, other users will set custom properties for drawing this user drawings. It could be useful
	 * to see all drawings black, for example, and quickly identify all the drawings this user have.
	 * 
	 * @param property The property to override
	 * @param type The type of the property to override.
	 * @throws NoSuchPropertyType if no type does not specify a valid property
	 * @throws WrongPropertyType if type property is valid, but does not match with the property 'property'.
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
		notifyObservers(new Object[] {PROPERTY_SET, property});
	
	}
	
	/**
	 * Gets a custom property, if any.
	 * @param type The type of the custom property we want to retrieve.
	 * @return The custom property if any, or nulll.
	 * @throws NoSuchPropertyType if no type does not specify a valid property
	 */
	public PaintProperty getCustomProperty(int type) throws NoSuchPropertyTypeException {
		if (type != CUSTOM_PAINT_PROPERTY_COLOR && type != CUSTOM_PAINT_PROPERTY_STROKE && type != CUSTOM_PAINT_PROPERTY_FONT && type != CUSTOM_PAINT_PROPERTY_ALPHA )
			throw new NoSuchPropertyTypeException("Cannot find type "+type);
		
		return customProperties[type];
	}
	
	/**
	 * Removes a custom property, if set.
	 * @param type The type of the property to be removed
	 * @throws NoSuchPropertyTypeException if no type does not specify a valid property
	 */
	public void removeCustomProperty(int type) throws NoSuchPropertyTypeException {
		if (type != CUSTOM_PAINT_PROPERTY_COLOR && type != CUSTOM_PAINT_PROPERTY_STROKE && type != CUSTOM_PAINT_PROPERTY_FONT && type != CUSTOM_PAINT_PROPERTY_ALPHA )
			throw new NoSuchPropertyTypeException("Cannot find type "+type);
		
		customProperties[type] = null;
		setChanged();
		notifyObservers(new Object[] {PROPERTY_REMOVE});
	}
	
	/**
	 * Gets the array of custom properties.
	 * 
	 * @return the customProperties
	 */
	public PaintProperty[] getCustomProperties() {
		return customProperties;
	}


	/**
	 * Removes a drawing from the drawing list. It will also remove this drawing from the DrawingAreaModel if
	 * remove procedure was successful.
	 * 
	 * If this user is a local user, it then sends information about the removal to the server.
	 * 
	 * @param drawing The drawing to be removed.
	 * @return true if the drawing was successfully removed. False otherwise.
	 */
	public synchronized boolean removeDrawing(AbstractDrawing drawing) {
		if (drawing == null)
			return false;
		
		int index = drawingList.indexOf(drawing);
		boolean removed = drawingList.remove(drawing);
		
		if (removed) {
			DrawingAreaModel.getInstance().removeDrawing(drawing);
			setChanged();
			notifyObservers(new Object[]{REMOVE_DRAWING, drawing, index});
		}
		
		return removed;
	}
	
	/**
	 * Removes last drawing from the drawing list.
	 *  
	 * @return true if removal was successfull. False otherwise.
	 */
	public synchronized boolean removeLastDrawing() {
		if (drawingList.isEmpty())
			return false;
		
		AbstractDrawing drawing = drawingList.get(drawingList.size() - 1);
		
		return removeDrawing(drawing);
	}
	
	/**
	 * Gets information about this user action.
	 * 
	 * @return true if the user is drawing. False otherwise.
	 */
	public boolean isDrawing() {
		return temporalDrawing != null;
	}

	/**
	 * Returns the temporal drawing this user is drawing.
	 * 
	 * @return the temporal drawing. Could be null.
	 */
	public AbstractDrawing getDrawing() {
		return temporalDrawing;
	}

	/**
	 * Sets this user temporal drawing. This is commonly set by this user tool.
	 * Tools will create drawings as they are used. While a tool is in use by
	 * this user, the tool will set this user temporal drawing.
	 * 
	 * @param temporalDrawing the drawing the current user tool is generating.
	 */
	public void setTemporalDrawing(AbstractDrawing temporalDrawing) {
		this.temporalDrawing = temporalDrawing; 
	}

	/**
	 * Removes a drawing by its number represented by a String.
	 * @param number the number from the drawing to be removed.
	 */
	public synchronized boolean removeDrawing(String number) {
		return removeDrawing(Long.parseLong(number));
	}
	
	/**
	 * Removes a drawing by its number.
	 * @param number the number from the drawing to be removed.
	 */
	public synchronized boolean removeDrawing(long number) {
		AbstractDrawing drawing = getDrawing(number);
		
		if (drawing == null)
			return false;
		
		return removeDrawing(drawing);
	}
	
	/**
	 * Gets a drawing from this user drawing list with the specified number.
	 * 
	 * @param number the number from the drawing we want to gather.
	 * @return the drawing found, or null if no drawing was found.
	 */
	public AbstractDrawing getDrawing(long number) {
		boolean found   = false;
		AbstractDrawing drawing = null;
		
		for(int i = 0; i < drawingList.size() && !found; i++) {
			if (found = (drawingList.get(i).getNumber() == number))
				drawing = drawingList.get(i);
		}
		
		return drawing;
	}
	
	/**
	 * Sets this user toolbox controller.
	 * 
	 * @param toolboxController the toolbox controller to be set.
	 */
	public void setToolboxController(IToolBoxController toolboxController) {
		this.toolboxController = toolboxController;
	}

	/**
	 * Gets this user toolbox controller.
	 * @return the toolbox controller this user has.
	 */
	public IToolBoxController getToolboxController() {
		return toolboxController;
	}

	/**
	 * Sets this user paint property controller.
	 * @param paintController the paint property controller this user has.
	 */
	public void setPaintController(IPaintPropertyController paintController) {
		this.paintController = paintController;
	}

	/**
	 * Gets the paint controller this user has.
	 * @return
	 */
	public IPaintPropertyController getPaintController() {
		return paintController;
	}

	/**
	 * Sets the drawing number from the first drawing in the drawing list numbered with 0.
	 * 
	 * When a local user draws a drawing, it can't generate a drawing with a correct number. The
	 * drawing must reach the server first as it's the only who can specify drawing order. The 
	 * server will respond each user with a number for the drawing the user is drawing. Before that,
	 * local users could have finish drawing. So they add the drawing to their drawing list, and they
	 * set the drawing number to 0. When each user receives a number packet, they set this number
	 * to the first drawing unnumbered in their drawing list, that is, the first drawing with number 0.
	 * 
	 * Numbered drawings starts at 1. Any number less than 1 is considered an unnumbered drawing.
	 * 
	 * @param info The number to set as a string representation.
	 */
	public void setDrawingNumber(String info) {
		Utils.logMessage("setDrawingNumber() call");
		AbstractDrawing drawing = getDrawing(0);
		Long number = Long.parseLong(info);
		
		/* If we find a drawin 'unnumbered' (that is, with number 0), then set the number the server provides us */
		if (drawing != null) {
			drawing.setNumber(number);
			/* Drawings with number 0 are not added to the model, so add it now */
			DrawingAreaModel.getInstance().addDrawing(drawing);
			setChanged();
			notifyObservers(new Object[] {DRAWING_NUMBER_SET});
			/* If no drawing with no number is found, then it must be the temporalDrawing */
		} else if (temporalDrawing != null){
			temporalDrawing.setNumber(number);
		} else {
			throw new IllegalStateException("Something is probably wrong with drawing numbers.");
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
//		if (o instanceof DrawingShape) {
//			DrawingShape drawingShape = (DrawingShape) o;
//			int action = (Integer)((Object[]) arg)[0];
//			
//			switch(action) {
//			case DrawingShape.ALPHA_PROPERTY_SET:
//				break;
//			case DrawingShape.BOTTOM_EDGE_SELECTION_CHANGED:
//				break;
//			case DrawingShape.COLOR_PROPERTY_SET:
//				break;
//			case DrawingShape.LEFT_EDGE_SELECTION_CHANGED:
//				break;
//			case DrawingShape.RIGHT_EDGE_SELECTION_CHANGED:
//				break;
//			case DrawingShape.STROKE_PROPERTY_SET:
//				break;
//			case DrawingShape.TOP_EDGE_SELECTION_CHANGED:
//				break;
//			case DrawingShape.MOVED:
//				break;
//			case DrawingShape.RESIZED:
//				break;
//			case DrawingShape.SELECTION_CHANGED:
//				break;
//			case DrawingShape.VISIBILITY_CHANGED:
//				break;
//			}
//		} else if (o instanceof DrawingText) {
//			DrawingText drawingText = (DrawingText) o;
//			
//		}
		AbstractDrawing drawing = (AbstractDrawing) o;
		setChanged();
		notifyObservers(new Object[]{DRAWING_CHANGED, drawing, drawingList.indexOf(drawing)});
	}
}
