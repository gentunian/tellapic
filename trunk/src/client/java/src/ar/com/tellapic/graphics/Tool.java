package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;

import ar.com.tellapic.StatusBar;
import ar.com.tellapic.TellapicAbstractUser;
import ar.com.tellapic.console.IConsoleCommand;

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
public abstract class Tool extends Observable implements MouseListener, MouseMotionListener, MouseWheelListener, IConsoleCommand {
	private int                        id;
	private String                     name;
	private String                     alias;
	private String                     iconPath;
	private String                     toolTipText;
	private Cursor                     toolCursor;
	private boolean                    visible;
	private boolean                    selected;
	private boolean                    inUse;
	protected TellapicAbstractUser     user;
	protected String[][]               COMMANDS;
	
	
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
	public void setUser(TellapicAbstractUser user) {
		this.user = user;
	}

	/**
	 * @return the user
	 */
	public TellapicAbstractUser getUser() {
		return user;
	}
	
	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#executeCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	public IConsoleCommand executeCommand(String cmd, Object[] args) {
		String[] cmdList = getCommandList();
		IConsoleCommand value = null;
		boolean executed = false;
		int j = 0;
		for(int i = 0; i < cmdList.length && !executed; i++) {
			if (cmd.equals(cmdList[i])) {
				try {
//					Class<Tool> toolClass      = (Class<Tool>) Class.forName(toolClassName);
//					Method method = getClass().getMethod(cmd, getArgumentTypesForCommand(cmd));
					Method[] methods = getClass().getMethods();
					Method method = null;
					for(j = 0; j < methods.length && !methods[j].getName().equals(cmd); j++);
					if (j == methods.length)
						throw new NoSuchMethodException("No method "+cmd+" was found.");
					
					method = methods[j];
					value = (IConsoleCommand) method.invoke(this, args);
					executed = true;
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		return value;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getCommandList()
	 */
	@Override
	public String[] getCommandList() {
		if (COMMANDS == null)
			return null;
		
		String[] cmds = new String[COMMANDS[0].length];
		
		for (int i = 0; i < COMMANDS[0].length; i++)
			cmds[i] = COMMANDS[0][i];
		
		return cmds;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getReturnTypeForCommand(java.lang.String)
	 */
	@Override
	public String getReturnTypeForCommand(String cmd){
		String[] cmdList = getCommandList();
		String type = null;
		
		for(int i = 0; i < cmdList.length; i++) {
			if (cmd.equals(cmdList[i])) {
				type = COMMANDS[i+1][0].split(" ")[0];
			}
		}
		
		return type;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getArgumentsTypesForCommand(java.lang.String)
	 */
	@Override
	public String[] getArgumentsTypesForCommand(String cmd){
		return getArgsNamesOrTypesOrDescsForCommand(cmd, 0); //TODO: USE CONSTANTS
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getArgumentsNamesForCommand(java.lang.String)
	 */
	@Override
	public String[] getArgumentsNamesForCommand(String cmd){
		return getArgsNamesOrTypesOrDescsForCommand(cmd, 1); //TODO: USE CONSTANTS
	}

	/**
	 * @param cmd
	 * @return
	 */
	@Override
	public String[] getArgumentsDescriptionsForCommand(String cmd) {
		return getArgsNamesOrTypesOrDescsForCommand(cmd, 2); //TODO: USE CONSTANTS
	}
	
	/**
	 * 
	 * @param cmd
	 * @param name
	 * @return
	 */
	private String[] getArgsNamesOrTypesOrDescsForCommand(String cmd, int arg) {
		String[] cmdList = getCommandList();
		if (cmdList == null)
			return null;
		
		String[] args = null;
		
		for(int i = 0; i < cmdList.length; i++) {
			if (cmd.equals(cmdList[i])) {
				args = new String[COMMANDS[i+1].length - 1];
				for(int j = 1; j < COMMANDS[i+1].length; j++) {
					args[j-1] = (COMMANDS[i+1][j]).split(" ", 3)[arg];
				}
				break; //FIXME: Write a better loop
			}
		}
		
		return args;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getDescriptionForCommand(java.lang.String)
	 */
	@Override
	public String getDescriptionForCommand(String cmd) {
		String[] cmdList = getCommandList();
		if (cmdList == null)
			return null;
		
		String desc = null;
		
		for(int i = 0; i < cmdList.length; i++) {
			if (cmd.equals(cmdList[i])) {
				desc = COMMANDS[i+1][0].split(" ", 2)[1];
				break; //FIXME: Write a better loop
			}
		}
		
		return desc;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract boolean isLiveModeSupported();
	
}
