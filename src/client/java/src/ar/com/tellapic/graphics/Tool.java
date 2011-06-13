package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.StatusBar;

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
public abstract class Tool extends Observable implements MouseListener, MouseMotionListener {
	private int              id;
	private String           name;
	private String           iconPath;
	private String           toolTipText;
	private Cursor           toolCursor;
	private boolean          visible;
	private boolean          selected;
	private boolean          inUse;
	protected AbstractUser     user;
	
	
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
		this.inUse       = false;
	}
	
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

	/**
	 * @param b
	 */
	public void setSelected(boolean b) {
		selected = b;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		if(isSelected() && !e.isConsumed()) {
			StatusBar.getInstance().setToolInfo(getIconPath(), getToolTipText());
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				setInUse(true);
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			setInUse(false);
		}
	}
	
	/**
	 * @param b
	 */
	public void setInUse(boolean b) {
		inUse = b;
	}
	
	/**
	 * 
	 */
	public boolean isBeingUsed() {
		return inUse;
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
}
