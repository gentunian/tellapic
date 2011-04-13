package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.geom.Point2D;
import java.util.Observable;

/**
 * Abstract class Tool.
 * 
 * This class represents an abstract tool. Concrete classes must implement abstract methods.
 * 
 * The Tool class and derivatives are abstract concepts of tools. They do not hold drawing
 * properties, they only represents a form of drawing drawn on screen with its coordinates.
 * 
 * Each Tool generates a Drawing. That Drawing object has paint properties that can be obtained
 * to represent that Tool on the screen. The Drawing object will have a shape if the Tool can
 * generate Shapes, and will have some text if the Tool can generate Text. A Tool cannot generated
 * both text and shape.
 * 
 * Tool will support different kind of network-modes. A Tool can support live mode, which is a
 * mode that reports each motion on the local client to the server. Instead, non live mode 
 * (or deferred mode) will only report the motion when the tool has finish its use. This reports
 * are not the Tool responsibility. Users of Tool objects must query the tool for what mode the
 * tool support, and then do whatever the user think it needs to be done.
 * 
 * A Tool must not know of the existence of any network resource nor how to use it. Neither a Tool
 * must know how it can be sent over a network resource. Though, concrete Tool objects can be
 * extended to emulate this feature. For instance, extending Ellipse as EllipseNetworkAware and
 * overriding: onDrag(), onPress(), onRelease(), onMove(), etc..
 * 
 * The Tool object must have an icon path that views will use to render an image of how this tool
 * will look like. Tools can also have a tooltip text that describes its behaviour as an optional
 * resource. It's mandatory an id and a name for each created tool.
 * 
 * @author Sebastián Treu, mailTo: sebastian.treu (at) gmail.com
 *
 */
public abstract class Tool extends Observable {
	
	private int              id;
	private String           name;
	private String           iconPath;
	private String           toolTipText;
	private boolean          visible;
	private Cursor           toolCursor;

	
	/**
	 * Public Constructor. Subclasses must call this constructor.
	 * @param id
	 * @param name
	 * @param iconPath
	 */
	public Tool(int id, String name, String iconPath) {
		this(id, name, iconPath, "No description provided.", null);
	}
	
	
	/**
	 * Public Constructor. Subclasses must call this constructor.
	 * @param type the tool type being created
	 * @param name the tool name being created
	 */
	public Tool(int id, String name, String iconPath, String description, Cursor cursor) throws IllegalArgumentException {
		if (name == null || iconPath == null)
			throw new IllegalArgumentException();
		
		this.visible     = true;
		this.toolTipText = description;
		this.iconPath    = iconPath;
		this.name        = name;
		this.id          = id;
		this.toolCursor  = (cursor == null)? Cursor.getDefaultCursor() : cursor;
	}
	
	
	/**
	 * 
	 */
	public abstract void onRestore();
	
	
	/**
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public abstract void onPress(int x, int y, int button, int mask);

	/**
	 * Call this when dragging the tool on the drawing area so it can update itself.
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 * @param symmetric whether or not this tool can use its symmetric feature (if it have any).
	 */
	public abstract void onDrag(int x, int y, int button, int mask);
	
	
	/**
	 * Call this when moving the tool (without dragging) on the drawing area so it can
	 * update itself if the tool support on-move mode: {@link ar.com.tellapic.graphics.Tool#isOnMoveSupported()}
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 */
	public abstract void onMove(int x, int y);
	
	
	/**
	 * Event that represents ending the use of the tool. If its called, future
	 * calls to {@link ar.com.tellapic.graphics.Tool#isBeingUsed()} should return false
	 * until someone calls {@link ar.com.tellapic.graphics.Tool#init(double, double)}.
	 * @return The last drawing object state of this tool.
	 */
	public abstract void onRelease(int x, int y, int button, int mask);
	
	
	/**
	 * 
	 */
	//TODO: rename to: onPause();
	public abstract void onPause();
	
	
	/**
	 * Returns the *current state* of this drawing tool.
	 * @return the drawing object that represents the current state of this tool.
	 */
//	public abstract Drawing getDrawing();
	
	
	/**
	 * Get the name of this tool.
	 * @return the name the tool has.
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Get the id of this tool.
	 * @return the ToolType type the tool is.
	 */
	public int getToolId() {
		return id;
	}
	
	
	/**
	 * Inform whether or not live mode is supported. Live mode is a way or feature
	 * of the tool that tells if the tool can report each motion to the tool user.
	 * @return true if live mode is support, false otherwise.
	 */
//	public abstract boolean isLiveModeSupported();
	
	
	/**
	 * Returns whether or not this tool supports drawing itself without being
	 * initiated by a press event, i.e. moving the tool within the drawing
	 * area being not a drag event.
	 * @return true if the move feature is supported.
	 */
	public abstract boolean isOnMoveSupported();
	public abstract boolean isOnPressSupported();
	public abstract boolean isOnDragSupported();
	public abstract boolean isOnReleaseSupported();
	
	
	
	/**
	 * This method should change the init. Take into account that will change the
	 * first point location to (x,y).
	 * @param x the x coordinate.
	 * @param y the y coordinate.
	 */
//	public abstract void moveTo(double x, double y);
	
	
	/**
	 * 
	 * @return true if the tool is being used. False otherwise
	 */
	public abstract boolean isBeingUsed();
	
	
	/**
	 * Get the first point where this Tool has been started.
	 * @return returns the first point of this Tool.
	 */
	public abstract Point2D getInit();
	
	
	/**
	 * 
	 * @return True if the Drawing object can be filled with colors, false otherwise (e.g. a line).
	 */
//	public abstract boolean isFilleable();
	
	
	/**
	 * This method tells if the generated Drawing object will have stroke properties.
	 * @return True if the Tool generates a Drawing object with stroke properties, false otherwise.
	 */
//	public abstract boolean hasStroke();
	
	
	/**
	 * This method tells if the generated Drawing object will have color properties.
	 * @return
	 */
//	public abstract boolean hasColor();
	
	
	/**
	 * This method tells if the generated Drawing object will have font properties.
	 * @return
	 */
//	public abstract boolean hasFont();
	
	
	/**
	 * This method tells if the generated Drawing object will have composite properties.
	 * @return
	 */
//	public abstract boolean hasAlpha();
	
	
	/**
	 * 
	 * @return
	 */
//	public abstract boolean hasZoomProperties();
	
	
	/**
	 * 
	 * @param alpha
	 */
//	public abstract void setAlpha(PaintPropertyAlpha alpha);
	
	
	/**
	 * 
	 * @param color
	 */
//	public abstract void setColor(PaintPropertyColor color);
	
	
	/**
	 * 
	 * @param stroke
	 */
//	public abstract void setStroke(PaintPropertyStroke stroke);
	
	
	/**
	 * 
	 * @param font
	 */
//	public abstract void setFont(PaintPropertyFont font);
	
	
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
	public void setVisible(boolean value) {
		visible = value;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return visible;	
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
	
	
	/**
	 * 
	 * @param cursor
	 */
	public void setCursor(Cursor cursor) {
		toolCursor = cursor;
	}
	
	
	/**
	 * 
	 * @param cursor
	 * @return
	 */
	public Cursor getCursor() {
		return toolCursor;
	}
}
