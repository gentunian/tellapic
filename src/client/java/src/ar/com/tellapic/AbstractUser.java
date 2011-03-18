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

import ar.com.tellapic.graphics.Drawing;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.graphics.PaintProperty;
import ar.com.tellapic.graphics.ToolBoxModel;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public abstract class AbstractUser extends Observable {
	//Each user will have control over its own tool box. The tool box model, for remote users
	//will be updated from wrapped remote events dispatched to the actual model controller.
	//Local user will update its model from mouse events.
	private ToolBoxModel            toolBox;
	
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
	
	public AbstractUser(int id, String name) {
		visible = true;
		removed = false;
		this.name = name;
		userId    = id;
		toolBox   = new ToolBoxModel();
		drawingList = new ArrayList<Drawing>();
		//TODO: the idea was that local user can set how to paint all paintings of a user
		customProperties = null;
//		addObserver(DrawingAreaView.getInstance());
//		addObserver(UserView.getInstance());
		setChanged();
	}
	
	
	public void addDrawing(Drawing drawing) {
		// If the drawing we are added is a temporal, then, it should no more be one.
		if (temporalDrawing!= null && temporalDrawing.equals(drawing))
			temporalDrawing = null;
		
		drawingList.add(drawing);
		setChanged();
		notifyObservers(drawingList.indexOf(drawing));
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
		notifyObservers(this);
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
	}
	
	public void setUserId(int id) {
		this.userId = id;
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
	 */
	public void cleanUp() {
		removed = true;
		setChanged();
		notifyObservers(this);
		deleteObservers();
	}
	

	/**
	 * @return the removed
	 */
	public boolean isRemoved() {
		return removed;
	}


	/**
	 * @param temporalDrawing the temporalDrawing to set
	 */
	public void setTemporalDrawing(Drawing temporalDrawing) {
		this.temporalDrawing = temporalDrawing;
		setChanged();
		notifyObservers();
	}


	/**
	 * @return the temporalDrawing
	 */
	public Drawing getTemporalDrawing() {
		return temporalDrawing;
	}


	/**
	 * @param customProperties the customProperties to set
	 */
	public void setCustomProperties(PaintProperty[] customProperties) {
		this.customProperties = customProperties;
	}


	/**
	 * @return the customProperties
	 */
	public PaintProperty[] getCustomProperties() {
		return customProperties;
	}
}
