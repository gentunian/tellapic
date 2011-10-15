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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;

import ar.com.tellapic.console.ConsoleModel;
import ar.com.tellapic.console.IConsoleModelController;
import ar.com.tellapic.graphics.AbstractDrawing;
import ar.com.tellapic.graphics.DrawingShape;
import ar.com.tellapic.graphics.DrawingText;
import ar.com.tellapic.graphics.Tool;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class TellapicLocalUser extends TellapicAbstractUser implements MouseListener, MouseMotionListener, MouseWheelListener {
	private ConsoleModel                     console;
	private IConsoleModelController          consoleController;
	
	public static final String LOCAL_NAME = "Local";
	
	private static class Holder {
		private static final TellapicLocalUser INSTANCE = new TellapicLocalUser(0, LOCAL_NAME);
	}
	
	/**
	 * 
	 * @param id
	 * @param name
	 */
	private TellapicLocalUser(int id, String name) {
		super(id, name);
		setRemote(false);
		console           = new ConsoleModel();
		setConsoleController(new TellapicConsoleModelController(console, getToolboxController(), this));
	}
	
	/**
	 * 
	 * @return
	 */
	public static TellapicLocalUser getInstance() {
		return Holder.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.TellapicAbstractUser#addDrawing(ar.com.tellapic.graphics.AbstractDrawing)
	 */
	@Override
	public synchronized boolean addDrawing(AbstractDrawing drawing) {
		boolean added = super.addDrawing(drawing);
		
		if (added)
			drawing.sendDeferred();
		
		return added;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.AbstractUser#removeDrawing(ar.com.tellapic.graphics.AbstractDrawing)
	 */
	@Override
	public synchronized boolean removeDrawing(AbstractDrawing drawing) {
		boolean removed = super.removeDrawing(drawing);
		
		if (removed)
			drawing.sendRemoved();
		
		return removed;
	}

	/**
	 * @param consoleController the consoleController to set
	 */
	public void setConsoleController(IConsoleModelController consoleController) {
		this.consoleController = consoleController;
	}

	/**
	 * @return the consoleController
	 */
	public IConsoleModelController getConsoleController() {
		return consoleController;
	}

	/**
	 * @return the console
	 */
	public ConsoleModel getConsole() {
		return this.console;
	}

	/**
	 * @param console the console to set
	 */
	public void setConsole(ConsoleModel console) {
		this.console = console;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		AbstractDrawing drawing  = (AbstractDrawing) evt.getSource();
		String          property = evt.getPropertyName();
		
//		Utils.logMessage("property changed: "+property+" to value: "+evt.getNewValue()+" on object: "+drawing.hashCode());
		
		if (property.equals(DrawingShape.PROPERTY_SELECTION)) {
			boolean selected = (Boolean) evt.getNewValue();
			if (selected) {
				for(AbstractDrawing d : getDrawings()) {
					if (!d.equals(drawing)) {
						d.setSelected(false);
					}
				}
			}
//		} else if (
//				property.equals(DrawingShape.PROPERTY_DASH)          || property.equals(DrawingShape.PROPERTY_END_CAPS) || 
//				property.equals(DrawingShape.PROPERTY_FILL)          || property.equals(DrawingText.PROPERTY_FILL) || 
//				property.equals(DrawingShape.PROPERTY_LINE_JOINS)    || property.equals(DrawingShape.PROPERTY_MITER_LIMIT) || 
//				property.equals(DrawingShape.PROPERTY_OPACITY)       || property.equals(DrawingShape.PROPERTY_STROKE_COLOR) || 
//				property.equals(DrawingShape.PROPERTY_WIDTH)         || property.equals(DrawingText.PROPERTY_COLOR) ||
//				property.equals(DrawingShape.PROPERTY_X1_COORDINATE) || property.equals(DrawingShape.PROPERTY_X2_COORDINATE) || 
//				property.equals(DrawingShape.PROPERTY_Y1_COORDINATE) || property.equals(DrawingShape.PROPERTY_Y2_COORDINATE) || 
//				property.equals(DrawingText.PROPERTY_FONT)           || property.equals(DrawingText.PROPERTY_TEXT_Y) || 
//				property.equals(DrawingText.PROPERTY_TEXT)           || property.equals(DrawingText.PROPERTY_TEXT_X)) {
//			
//			drawing.sendChanged();
			
		} else if (
				property.equals(DrawingShape.PROPERTY_PAINT_ALPHA) || property.equals(DrawingShape.PROPERTY_PAINT_FILL) ||
				property.equals(DrawingShape.PROPERTY_PAINT_STROKE)|| property.equals(DrawingShape.PROPERTY_LOCATION) ||
				property.equals(DrawingText.PROPERTY_LOCATION) || property.equals(DrawingText.PROPERTY_TEXT) ||
				property.equals(DrawingText.PROPERTY_COLOR)|| property.equals(DrawingText.PROPERTY_OPACITY) || 
				property.equals(DrawingText.PROPERTY_FILL) || property.equals(DrawingText.PROPERTY_FONT)) {
			Utils.logMessage("drawing.sendChanged()");
			drawing.sendChanged();
		}
		
		setChanged();
		notifyObservers(new Object[]{DRAWING_CHANGED, drawing, drawingList.indexOf(drawing)});
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
		Tool tool = getToolBoxModel().getLastUsedTool();
//		Utils.logMessage("Local user mouse entered drawing area with tool: "+tool);
		if (tool != null)
			tool.mouseEntered(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		Tool tool = getToolBoxModel().getLastUsedTool();
//		Utils.logMessage("Local user mouse exited drawing area with tool: "+tool);
		if (tool != null)
			tool.mouseExited(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
			
			Tool tool = getToolBoxModel().getLastUsedTool();
			//		Utils.logMessage("Local user mouse pressed in drawing area with tool: "+tool);
			if (tool != null){
				tool.mousePressed(e);

				if (tool.isLiveModeSupported()) {
					AbstractDrawing drawing = getDrawing();
					if (drawing != null)
						drawing.sendPressed(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != MouseEvent.BUTTON1_DOWN_MASK) {

			Tool tool = getToolBoxModel().getLastUsedTool();
			Utils.logMessage("Local user mouse released in drawing area with tool: "+tool);
			if (tool != null){
				tool.mouseReleased(e);
				AbstractDrawing drawing = getDrawing();
				if (drawing != null) {
					addDrawing(drawing);
					if (tool.isLiveModeSupported())
						drawing.sendReleased(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Tool tool = getToolBoxModel().getLastUsedTool();
//		Utils.logMessage("Local user mouse dragged in drawing area with tool: "+tool);
		if (tool != null) {
			tool.mouseDragged(e);
			if (tool.isLiveModeSupported() && e.getX() > 0 && e.getY() > 0) {
				AbstractDrawing drawing = getDrawing();
				if (drawing != null) {
					drawing.sendDragged(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Tool tool = getToolBoxModel().getLastUsedTool();
//		Utils.logMessage("Local user mouse moved in drawing area with tool: "+tool);
		if (tool != null)
			tool.mouseMoved(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		Tool tool = getToolBoxModel().getLastUsedTool();
		if (tool != null)
			tool.mouseWheelMoved(e);
	}
}
