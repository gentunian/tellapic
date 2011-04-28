/**
 * 
 */
package ar.com.tellapic.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Observable;

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
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setCurrentTool(ar.com.tellapic.graphics.Tool)
	 */
	@Override
	public synchronized void setCurrentTool(Tool tool) throws IllegalArgumentException {
		if (tool == null)
			throw new IllegalArgumentException("tool cannot be null");

		lastUsedTool = tool;
		
		setChanged();
		notifyObservers(new ActionData(SHOW_TOOL, lastUsedTool));
	}

	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxState#getLastUsedTool()
	 */
	@Override
	public synchronized Tool getLastUsedTool() {
		return lastUsedTool;
	}

	

	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxState#getTools()
	 */
	@Override
	public HashMap<String,Tool> getTools() {
		return tools;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#addTool(ar.com.tellapic.graphics.Tool)
	 */
	@Override
	public void addTool(Tool tool) throws IllegalArgumentException {
		if (tool == null)
			throw new IllegalArgumentException("tool cannot be null");
		
		tools.put(tool.getName(), tool);
//		Utils.logMessage("Tool added: "+tool.getName()+". Notifying observers...");
		tool.addObserver(DrawingAreaView.getInstance());
		setChanged();
		notifyObservers(new ActionData(ADD_TOOL, tool));
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setStrokePropertyCaps(int)
	 */
	@Override
	public void setStrokePropertyCaps(int cap) throws IllegalArgumentException {
		if (cap != BasicStroke.CAP_BUTT && cap != BasicStroke.CAP_ROUND && cap != BasicStroke.CAP_SQUARE)
			throw new IllegalArgumentException("cap value must be one of CAP_SQUARE, CAP_ROUND or CAP_BUTT");
		
		strokeProperty.setEndCaps(cap);
//		Utils.logMessage("stroke end caps property set to: "+strokeProperty.getEndCaps());
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setStroke(strokeProperty);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setStrokePropertyDash(float[], float)
	 */
	@Override
	public void setStrokePropertyDash(float[] dash, float dashPhase) throws IllegalArgumentException {
		if (dash == null)
			throw new IllegalArgumentException("dash cannot be null.");
		if (dash.length != 2)
			throw new IllegalArgumentException("dash must have only 2 elements.");
		if (dashPhase < 0)
			throw new IllegalArgumentException("dashPhase must be a non-negative number.");
		
		strokeProperty.setDash(dash);
		strokeProperty.setDash_phase(dashPhase);
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setStroke(strokeProperty);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setStrokePropertyJoins(int)
	 */
	@Override
	public void setStrokePropertyJoins(int join) throws IllegalArgumentException {
		if (join != BasicStroke.JOIN_BEVEL && join != BasicStroke.JOIN_MITER && join != BasicStroke.JOIN_ROUND)
			throw new IllegalArgumentException("join value must be one of JOIN_MITER, JOIN_BEVEL or JOIN_ROUND");
		
		strokeProperty.setLineJoins(join);
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setStroke(strokeProperty);
		}
//		Utils.logMessage("stroke line joins property set to: "+strokeProperty.getLineJoins());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setStrokePropertyMitterLimit(float)
	 */
	@Override
	public void setStrokePropertyMiterLimit(float width) {
		strokeProperty.setMiterLimit(width);
//		Utils.logMessage("stroke mitter limit property set to: "+strokeProperty.getMiterLimit());
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setStroke(strokeProperty);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setStrokePropertyWidth(float)
	 */
	@Override
	public void setStrokePropertyWidth(double width) {
		strokeProperty.setWidth(width);
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setStroke(strokeProperty);
		}
		setChanged();
		notifyObservers(new ActionData(UPDATE_TOOL, lastUsedTool));
//		Utils.logMessage("stroke width property set to: "+strokeProperty.getWidth());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setFontPropertyFace(java.lang.String)
	 */
	@Override
	public void setFontPropertyFace(String face) {
		fontProperty.setFace(face);
//		Utils.logMessage("Font face has been set to "+face);
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setFont(fontProperty);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setFontPropertySize(int)
	 */
	@Override
	public void setFontPropertySize(int size) {
		fontProperty.setSize(size);
//		Utils.logMessage("Font size has been set to "+size);
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setFont(fontProperty);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setFontPropertyStyle(int)
	 */
	@Override
	public void setFontPropertyStyle(int style) {
		fontProperty.setStyle(style);
//		Utils.logMessage("Font style has been set to "+style);
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setFont(fontProperty);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setAlphaPropertyValue(float)
	 */
	@Override
	public void setAlphaPropertyValue(double value) {
		alphaProperty.alpha = (float) value;
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setAlpha(alphaProperty);
		}
		setChanged();
		notifyObservers(new ActionData(UPDATE_TOOL, lastUsedTool));
//		Utils.logMessage("Alpha opacity has been set to "+value);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxState#getColorProperty()
	 */
	@Override
	public PaintPropertyColor getColorProperty() {
		return colorProperty;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxState#getFontProperty()
	 */
	@Override
	public PaintPropertyFont getFontProperty() {
		return fontProperty;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxState#getOpacityProperty()
	 */
	@Override
	public PaintPropertyAlpha getOpacityProperty() {
		return alphaProperty;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxState#getStrokeProperty()
	 */
	@Override
	public PaintPropertyStroke getStrokeProperty() {
		return strokeProperty;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#registerTool(int)
	 */
	@Override
	public void registerTool(int toolId) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#disableTool(ar.com.tellapic.graphics.Tool)
	 */
	@Override
	public void disableTool(Tool tool) {
		// don't notify if it was already disabled
		if (tool.isEnabled()) {
			tool.setVisible(false);
			setChanged();
			notifyObservers(tool);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#enableTool(ar.com.tellapic.graphics.Tool)
	 */
	@Override
	public void enableTool(Tool tool) {
		// Don't notify if it was already enabled
		if (!tool.isEnabled()) {
			tool.setVisible(true);
			setChanged();
			notifyObservers(tool);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setFontPropertyText(java.lang.String)
	 */
	@Override
	public void setFontPropertyText(String text) {
		fontProperty.setText(text);
//		Utils.logMessage("Font text has been set to "+text);

		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setFont(fontProperty);
		}
		setChanged();
		notifyObservers(new ActionData(UPDATE_TOOL, lastUsedTool));
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setCurrentTool(java.lang.String)
	 */
	@Override
	public void setCurrentTool(String toolName) throws IllegalArgumentException {
		if (toolName == null)
			throw new IllegalArgumentException("toolName cannot be null");
		
		Tool tool = tools.get(toolName);
		
		if (tool == null)
			throw new NoSuchElementException("No tool with name "+toolName+" found.");
		
		setCurrentTool(tool);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setColorPropertyValue(java.awt.Color)
	 */
	@Override
	public void setColorPropertyValue(Color color) {
		colorProperty.setColor(color);
		if (lastUsedTool instanceof DrawingTool) {
			((DrawingTool) lastUsedTool).setColor(colorProperty);
		}
		setChanged();
		notifyObservers(new ActionData(UPDATE_TOOL, lastUsedTool));
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#incrementZoom()
	 */
	@Override
	public void setZoomIn(boolean value) {
		Zoom zoom = (Zoom) tools.get("Zoom"); //TODO: use constant.
		
		zoom.setZoomIn(value);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxManager#setZoomValue(double)
	 */
	@Override
	public void setZoomValue(float value) {
		Zoom zoom = (Zoom) tools.get("Zoom"); //TODO: use constant.
		
		zoom.setZoom(value);
	}


	/**
	 * 
	 */
	public void setCurrentToolDefaultValues() {
		if (lastUsedTool instanceof DrawingTool) {
			if (((DrawingTool) lastUsedTool).hasAlphaCapability())
				setAlphaPropertyValue(((DrawingTool)lastUsedTool).getDefaultAlpha());
			
			if (((DrawingTool) lastUsedTool).hasColorCapability())
				setColorPropertyValue(((DrawingTool)lastUsedTool).getDefaultColor());
			
			if (((DrawingTool) lastUsedTool).hasFontCapability()) {
				setFontPropertyFace(((DrawingTool)lastUsedTool).getDefaultFontFace());
				setFontPropertySize((int) ((DrawingTool)lastUsedTool).getDefaultFontSize());
				setFontPropertyStyle(((DrawingTool)lastUsedTool).getDefaultFontStyle());
//				this.setFontPropertyText(((DrawingTool)lastUsedTool).getDefaultTe());
			}
			
			if (((DrawingTool) lastUsedTool).hasStrokeCapability()) {
				setStrokePropertyCaps(((DrawingTool)lastUsedTool).getDefaultCaps());
				setStrokePropertyJoins(((DrawingTool)lastUsedTool).getDefaultJoins());
				setStrokePropertyWidth(((DrawingTool)lastUsedTool).getDefaultWidth());
				setStrokePropertyMiterLimit(((DrawingTool)lastUsedTool).getDefaultMiterLimit());
			}
		}
	}
}
