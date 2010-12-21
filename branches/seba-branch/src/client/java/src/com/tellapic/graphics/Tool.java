package com.tellapic.graphics;

import java.awt.geom.Point2D;

/**
 * 
 * @author Sebastian Treu, mailTo: sebastian.treu (at) gmail.com
 *
 */
public abstract class Tool {
	//TODO: id for?
	private int      id;
	private String   name;
	private String   iconPath;
	private String   toolTipText;
	private boolean  enabled;

	
	public Tool(String name, String iconPath) {
		this(name, iconPath, "No description provided.");
	}
	
	/**
	 * Public Constructor. Subclasses must call this constructor.
	 * @param type the tool type being created
	 * @param name the tool name being created
	 */
	public Tool(String name, String iconPath, String description) throws IllegalArgumentException {
		if (name == null || iconPath == null)
			throw new IllegalArgumentException();
		
		this.enabled     = true;
		this.toolTipText = description;
		this.iconPath    = iconPath;
		this.name        = name;
	}
	
	
	/**
	 * 
	 */
	public abstract void onRestore();
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public abstract void init(double x, double y);

	/**
	 * Returns the *current* state of this drawing tool.
	 * @return the drawing object that represents the current state of this tool.
	 */
	public abstract Drawing getDrawing();
	
	/**
	 * 
	 * @return the name the tool has.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return the ToolType type the tool is.
	 */
	public int getToolId() {
		return id;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean isLiveModeSupported();
	
	/**
	 * Returns wether or not this tool supports drawing itself without being
	 * initiated by a press event, i.e. moving the tool within the drawing
	 * area being not a drag event.
	 * @return true if the move feature is supported.
	 */
	public abstract boolean isOnMoveSupported();
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public abstract void moveTo(double x, double y);
	
	/**
	 * Event that represent the tool being used on the drawing area.
	 * @param x the x coordinate .
	 * @param y the y coordinate.
	 * @param symmetric wether or not this tool can use its symmetric feature (if it have any).
	 */
	protected abstract void onDraw(double x, double y, boolean symmetric);
	
	/**
	 * Event that represents a "on moving" update of the tool. Tools must
	 * update (if it's available on the tool) it's location from the top
	 * left corner.
	 * @param x
	 * @param y
	 */
	protected abstract Drawing onMove(double x, double y);

	/**
	 * Event that represents ending the use of the tool. If its called, future
	 * calls to {@link com.tellapic.graphics.Tool#isBeingUsed()} will return false
	 * until someone calls {@link com.tellapic.graphics.Tool#init(double, double)}.
	 * @return The last drawing object state of this tool
	 */
	protected abstract Drawing onFinishDraw();
	
	/**
	 * 
	 */
	//TODO: rename to: onPause();
	protected abstract void onCancel();
	
	/**
	 * 
	 * @return true if the tool is being used. False otherwise
	 */
	public abstract boolean isBeingUsed();
	
	/**
	 * 
	 * @return
	 */
	public abstract Point2D getInit();
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean isFilleable();
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean hasStrokeProperties();
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean hasColorProperties();
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean hasFontProperties();
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean hasAlphaProperties();
	
	/**
	 * 
	 * @param path
	 */
	public void setIconPath(String path) {
		iconPath = path;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getIconPath() {
		return iconPath;
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setEnabled(boolean value) {
		enabled = value;
	}
		
	/**
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;	
	}


	/**
	 * @param toolTipText the toolTipText to set
	 */
	public void setToolTipText(String toolTipText) {
		this.toolTipText = toolTipText;
	}


	/**
	 * @return the toolTipText
	 */
	public String getToolTipText() {
		return toolTipText;
	}
}
