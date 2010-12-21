/**
 * 
 */
package com.tellapic.graphics;

import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Observable;

import com.tellapic.Utils;

/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ToolBoxModel extends Observable implements IToolBoxManager, IToolBoxState {

	public static final int ADD_TOOL    = 0;
	public static final int UPDATE_TOOL = 1;
	public static final int SHOW_TOOL   = 2;
	
	private HashMap<String,Tool>  tools;
	private Tool                  lastUsedTool;
	private PaintPropertyStroke   strokeProperty;
	private PaintPropertyColor    colorProperty;
	private PaintPropertyFont     fontProperty;
	private PaintPropertyAlpha    alphaProperty;
	
	public class ActionData {
		private int action;
		private Tool data;
		
		public ActionData(int action, Tool data) {
			this.action = action;
			this.data   = data;
		}

		/**
		 * @param action the action to set
		 */
		public void setAction(int action) {
			this.action = action;
		}

		/**
		 * @return the action
		 */
		public int getAction() {
			return action;
		}

		/**
		 * @param data the data to set
		 */
		public void setData(Tool data) {
			this.data = data;
		}

		/**
		 * @return the data
		 */
		public Tool getData() {
			return data;
		}
	}
	
	
	/**
	 * Public Constructor.
	 * 
	 * Creates a tool box model with an empty Tool list.
	 */
	public ToolBoxModel() {
		tools          = new HashMap<String,Tool>();
		strokeProperty = new PaintPropertyStroke();
		colorProperty  = new PaintPropertyColor();
		fontProperty   = new PaintPropertyFont();
		alphaProperty  = new PaintPropertyAlpha();
		lastUsedTool   = null;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setCurrentTool(com.tellapic.graphics.Tool)
	 */
	@Override
	public synchronized void setCurrentTool(Tool tool) throws IllegalArgumentException {
		if (tool == null)
			throw new IllegalArgumentException("tool cannot be null");
		
		Utils.logMessage("Tool selected "+tool.getName());
		lastUsedTool = tool;
		setChanged();
		notifyObservers(new ActionData(SHOW_TOOL, lastUsedTool));
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxState#getLastUsedTool()
	 */
	@Override
	public synchronized Tool getLastUsedTool() {
		return lastUsedTool;
	}

	

	/*
	 * (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxState#getTools()
	 */
	@Override
	public HashMap<String,Tool> getTools() {
		return tools;
	}

	
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#addTool(com.tellapic.graphics.Tool)
	 */
	@Override
	public void addTool(Tool tool) throws IllegalArgumentException {
		if (tool == null)
			throw new IllegalArgumentException("tool cannot be null");
				
		tools.put(tool.getName(), tool);
		Utils.logMessage("Tool added: "+tool.getName()+". Notifying observers...");
		setChanged();
		notifyObservers(new ActionData(ADD_TOOL, tool));
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setStrokePropertyCaps(int)
	 */
	@Override
	public void setStrokePropertyCaps(int cap) throws IllegalArgumentException {
		if (cap != BasicStroke.CAP_BUTT && cap != BasicStroke.CAP_ROUND && cap != BasicStroke.CAP_SQUARE)
			throw new IllegalArgumentException("cap value must be one of CAP_SQUARE, CAP_ROUND or CAP_BUTT");
		
		strokeProperty.setEndCaps(cap);
		Utils.logMessage("stroke end caps property set to: "+strokeProperty.getEndCaps());
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setStrokePropertyDash(float[], float)
	 */
	@Override
	public void setStrokePropertyDash(float[] dash, float dashPhase) {
		// TODO Auto-generated method stub
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setStrokePropertyJoins(int)
	 */
	@Override
	public void setStrokePropertyJoins(int join) throws IllegalArgumentException {
		if (join != BasicStroke.JOIN_BEVEL && join != BasicStroke.JOIN_MITER && join != BasicStroke.JOIN_ROUND)
			throw new IllegalArgumentException("join value must be one of JOIN_MITER, JOIN_BEVEL or JOIN_ROUND");
		
		strokeProperty.setLineJoins(join);
		Utils.logMessage("stroke line joins property set to: "+strokeProperty.getLineJoins());
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setStrokePropertyMitterLimit(float)
	 */
	@Override
	public void setStrokePropertyMiterLimit(float width) {
		strokeProperty.setMiterLimit(width);
		Utils.logMessage("stroke mitter limit property set to: "+strokeProperty.getMiterLimit());
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setStrokePropertyWidth(float)
	 */
	@Override
	public void setStrokePropertyWidth(float width) {
		strokeProperty.setWidth(width);
		setChanged();
		notifyObservers(new ActionData(UPDATE_TOOL, lastUsedTool));
		Utils.logMessage("stroke width property set to: "+strokeProperty.getWidth());
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setFontPropertyFace(java.lang.String)
	 */
	@Override
	public void setFontPropertyFace(String face) {
		fontProperty.setFace(face);
		Utils.logMessage("Font face has been set to "+face);
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setFontPropertySize(int)
	 */
	@Override
	public void setFontPropertySize(int size) {
		fontProperty.setSize(size);
		Utils.logMessage("Font size has been set to "+size);
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setFontPropertyStyle(int)
	 */
	@Override
	public void setFontPropertyStyle(int style) {
		fontProperty.setStyle(style);
		Utils.logMessage("Font style has been set to "+style);
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setAlphaPropertyValue(float)
	 */
	@Override
	public void setAlphaPropertyValue(float value) {
		alphaProperty.alpha = value;
		Utils.logMessage("Alpha opacity has been set to "+value);
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxState#getColorProperty()
	 */
	@Override
	public PaintPropertyColor getColorProperty() {
		return colorProperty;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxState#getFontProperty()
	 */
	@Override
	public PaintPropertyFont getFontProperty() {
		return fontProperty;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxState#getOpacityProperty()
	 */
	@Override
	public PaintPropertyAlpha getOpacityProperty() {
		return alphaProperty;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxState#getStrokeProperty()
	 */
	@Override
	public PaintPropertyStroke getStrokeProperty() {
		return strokeProperty;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#registerTool(int)
	 */
	@Override
	public void registerTool(int toolId) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#disableTool(com.tellapic.graphics.Tool)
	 */
	@Override
	public void disableTool(Tool tool) {
		// don't notify if it was already disabled
		if (tool.isEnabled()) {
			tool.setEnabled(false);
			setChanged();
			notifyObservers(tool);
		}
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#enableTool(com.tellapic.graphics.Tool)
	 */
	@Override
	public void enableTool(Tool tool) {
		// Don't notify if it was already enabled
		if (!tool.isEnabled()) {
			tool.setEnabled(true);
			setChanged();
			notifyObservers(tool);
		}
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setFontPropertyText(java.lang.String)
	 */
	@Override
	public void setFontPropertyText(String text) {
		fontProperty.setText(text);
		Utils.logMessage("Font text has been set to "+text);
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.IToolBoxManager#setCurrentTool(java.lang.String)
	 */
	@Override
	public void setCurrentTool(String toolName) throws IllegalArgumentException {
		if (toolName == null)
			throw new IllegalArgumentException("toolName cannot be null");
		
		lastUsedTool = tools.get(toolName);
		if (lastUsedTool == null)
			throw new NoSuchElementException("No tool with name "+toolName+" found.");
		setChanged();
		notifyObservers(new ActionData(SHOW_TOOL, lastUsedTool));
	}
}
