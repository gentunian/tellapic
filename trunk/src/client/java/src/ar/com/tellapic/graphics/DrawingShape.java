package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import ar.com.tellapic.NetManager;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.TellapicAbstractUser;
import ar.com.tellapic.console.IConsoleCommand;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public abstract class DrawingShape extends AbstractDrawing {
	private static Class<?> COLUMN_CLASS[] = new Class<?>[] {
		String.class,
		Object.class
	};
	private static final String COLUMN_NAMES[] = new String[] {
		Utils.msg.getString("name"),
		Utils.msg.getString("value")
	};
	private static final int COLUMN_COUNT = 2;
	
	public static final String PROPERTY_PAINT_STROKE         = "StrokeProperty";
	public static final String PROPERTY_PAINT_FILL           = "FillProperty";
	public static final String PROPERTY_PAINT_ALPHA          = "AlphaProperty";
	public static final String PROPERTY_END_CAPS             = "End Caps";
	public static final String PROPERTY_LINE_JOINS           = "Line Joins";
	public static final String PROPERTY_MITER_LIMIT          = "Miter Limit";
	public static final String PROPERTY_WIDTH                = "Stroke Width";
	public static final String PROPERTY_STROKE_COLOR         = "Stroke Color";
	public static final String PROPERTY_OPACITY              = "Opacity";
	public static final String PROPERTY_FILL                 = "Fill Color";
	public static final String PROPERTY_DASH                 = "Stroke Dash";
	public static final String PROPERTY_X1_COORDINATE        = "X1";
	public static final String PROPERTY_X2_COORDINATE        = "X2";
	public static final String PROPERTY_Y1_COORDINATE        = "Y1";
	public static final String PROPERTY_Y2_COORDINATE        = "Y2";
	public static final String PROPERTY_LOCATION             = "Location";
	
	private Shape               bounds;
	private PaintPropertyStroke strokeProperty;
	private PaintPropertyAlpha  alphaProperty;
	private PaintPropertyFill   fillProperty;
	private Shape               shape;
	private TableModelListener  tml;
	
	
	public static String[][] COMMANDS = new String[][] {
		PaintPropertyStroke.CLI_CMDS,
		PaintPropertyFill.CLI_CMDS,
		PaintPropertyAlpha.CLI_CMDS
	};
	
	private static final Object[][] PAINT_PROPERTY = new Object[][] {
		{"PaintPropertyStroke", PaintPropertyStroke.class},
		{"PaintPropertyFill", PaintPropertyFill.class},
		{"PaintPropertyAlpha", PaintPropertyAlpha.class}
	};
	

	/**
	 * 
	 * @param name
	 */
	public DrawingShape(String name, boolean resizeable, boolean moveable, boolean fillable) {
		super(name, resizeable, moveable, fillable);
		
		/* Initialize this object main properties */
		shape          = null;
		strokeProperty = null;
		alphaProperty  = null;
		
		initSelectedEgdesState(false);
	}
	
	/**
	 * @param b
	 */
	private void initSelectedEgdesState(boolean b) {
		setLeftEdgeSelected(b);
		setRightEdgeSelected(b);
		setTopEdgeSelected(b);
		setBottomEdgeSelected(b);
	}

	/**
	 * 
	 */
	@Override
	protected void fireTableUpdate() {
		if (tml != null)
			tml.tableChanged(new TableModelEvent(this, 0, getRowCount()-1));
	}
	
	/**
	 * @param shape the shape to set
	 */
	public void setShape(Shape shape) {
		if (strokeProperty != null)
			bounds = strokeProperty.getStroke().createStrokedShape(shape);
		
		this.shape = shape;
		properties.put(PROPERTY_X1_COORDINATE, getFirstX());
		properties.put(PROPERTY_X2_COORDINATE, getLastX());
		properties.put(PROPERTY_Y1_COORDINATE, getFirstY());
		properties.put(PROPERTY_Y2_COORDINATE, getLastY());
		pcs.firePropertyChange(PROPERTY_LOCATION, 1, shape);
		fireTableUpdate();
		updateControlPoints();
	}
	
	/**
	 * @return the shape
	 */
	public Shape getShape() {
		return shape;
	}
	
	/**
	 * @return
	 */
	public PaintPropertyFill getPaintPropertyFill() {
		return fillProperty;
	}
	
	/**
	 * @return the stroke or null
	 */
	public PaintPropertyStroke getPaintPropertyStroke() {
		return strokeProperty;
	}

	/**
	 * @return the alpha or null
	 */
	public PaintPropertyAlpha getPaintPropertyAlpha() {
		return alphaProperty;
	}
	
	/**
	 * 
	 * @param property
	 * @return
	 */
	public DrawingShape setPaintPropertyFill(PaintPropertyFill property) {
		fillProperty = property;
		properties.put(PROPERTY_FILL, property.getFillPaint());
		pcs.firePropertyChange(PROPERTY_PAINT_FILL, 1, fillProperty); //FIXME: 
		fireTableUpdate();
		return this;
	}
	
	/**
	 * 
	 * @param property
	 */
	public DrawingShape setPaintPropertyStroke(PaintPropertyStroke property) {
		bounds = property.getStroke().createStrokedShape(shape);
		strokeProperty = property;
		properties.put(PROPERTY_END_CAPS, property.getEndCaps());
		properties.put(PROPERTY_LINE_JOINS, property.getLineJoins());
		properties.put(PROPERTY_WIDTH, (float)property.getWidth());
		properties.put(PROPERTY_MITER_LIMIT, property.getMiterLimit());
		properties.put(PROPERTY_STROKE_COLOR, property.getColor());
		pcs.firePropertyChange(PROPERTY_PAINT_STROKE, 1, strokeProperty); //FIXME: 
		fireTableUpdate();
		updateControlPoints();
		return this;
	}
	
	/**
	 * 
	 * @param property
	 */
	public DrawingShape setPaintPropertyAlpha(PaintPropertyAlpha property) {
		alphaProperty = property;
		properties.put(PROPERTY_OPACITY, property.getAlpha());
		pcs.firePropertyChange(PROPERTY_PAINT_ALPHA, 1, alphaProperty); //FIXME: 
		fireTableUpdate();
		return this;
	}
	
	/**
	 * 
	 * @param g
	 */
	@Override
	public void draw(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		PaintProperty overridenProperties[] = getUser().getCustomProperties();
		
		if (isVisible()) {
			/* Get the set properties for this drawing */
			Stroke     oStroke    = getPaintPropertyStroke().getStroke();
			Composite  oComposite = getPaintPropertyAlpha().getComposite();
			Color      oColor     = getPaintPropertyStroke().getColor();
			Paint      paint      = getFillableShapePaint();
			
			g.setRenderingHints(renderingHints);
			
			/* Override the alpha property if needed */
			if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA] != null)
				oComposite = ((PaintPropertyAlpha)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA]).getComposite();

			/* Override the stroke property if needed */
			if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_STROKE] != null)
				oStroke = ((PaintPropertyStroke)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_STROKE]).getStroke();
	
			/* Override the color property if needed (this will ALSO override the fill color)*/
			if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR] != null) {
				oColor = (((PaintPropertyColor)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR]).getColor());
				
			}
			
			if (shape != null) {
				
				/* Fill could be set or unset. If a fill is set we should fill the shape then. */
				if (getFillableShape() != null) {
					/* Set the paint for filling the shape */
					g.setPaint(paint);
					
					/* Get the fillable shape and fill it with the above painting */
					g.fill(getFillableShape());
				}
				
				/* Set the composite */
				g.setComposite(oComposite);
				
				/* Set the stroke */
				g.setStroke(oStroke);
				
				/* Set the stroke color */
				g.setColor(oColor);
				
				/* And finally, draw the shape */
				g.draw(shape);
				
				/* Call super.draw() for drawing selected states */
				super.draw(g); 
			}
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getBounds()
	 */
	@Override
	public Rectangle2D getBounds2D() {
		if (bounds != null)
			return bounds.getBounds2D();
		
		if (shape != null)
			return shape.getBounds2D();
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void addTableModelListener(TableModelListener l) {
		 tml = l;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASS[columnIndex];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return properties.size();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object value = null;
		if (columnIndex == 0) {
			value = properties.keySet().toArray()[rowIndex];
		} else {
			value = properties.values().toArray()[rowIndex];
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		/* We can't edit other user drawing */
		if (getUser().isRemote())
			return false;
		
		if (columnIndex == 0)
			return false;

		return isPropertyEditable(properties.keySet().toArray(new String[0])[rowIndex]);
	}

	/**
	 * 
	 * @param propertyName
	 * @return
	 */
	private boolean isPropertyEditable(String propertyName) {
		if (propertyName.equals(PROPERTY_NAME) ||propertyName.equals(PROPERTY_RESIZEABLE) ||
				propertyName.equals(PROPERTY_USER_NAME) ||propertyName.equals(PROPERTY_MOVEABLE) ||
				propertyName.equals(PROPERTY_NUMBER) || propertyName.equals(PROPERTY_SELECTION) ||
				propertyName.equals(PROPERTY_X1_COORDINATE) || propertyName.equals(PROPERTY_X2_COORDINATE) ||
				propertyName.equals(PROPERTY_Y1_COORDINATE) || propertyName.equals(PROPERTY_FILLABLE)
				|| propertyName.equals(PROPERTY_Y2_COORDINATE) || (propertyName.equals(PROPERTY_FILL) && !isFillable())) 
			return false;

		return true;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void removeTableModelListener(TableModelListener l) {
		tml = null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 1) {
			String propertyName = properties.keySet().toArray(new String[0])[rowIndex];
			if (propertyName.equals(PROPERTY_VISIBILITY)) {
				setVisible((Boolean) aValue);
				
			} else if (propertyName.equals(PROPERTY_STROKE_COLOR)) {
				try {
					strokeProperty.setColor((Color) aValue);
					setPaintPropertyStroke(strokeProperty);
				} catch(NumberFormatException e) {
					Utils.logMessage("No changes made, "+aValue+" is not a valid color.");
				}
				
			} else if (propertyName.equals(PROPERTY_END_CAPS)) {
				strokeProperty.setEndCaps(aValue.toString());
				setPaintPropertyStroke(strokeProperty);
				
			} else if (propertyName.equals(PROPERTY_DASH)) {

			} else if (propertyName.equals(PROPERTY_HORIZONTAL_EDGES_SELECTED)) {
				setHorizontalEdgePropertyValue((String) aValue);
				
			} else if (propertyName.equals(PROPERTY_LINE_JOINS)) {
				strokeProperty.setLineJoins(aValue.toString());
				setPaintPropertyStroke(strokeProperty);
				
			} else if (propertyName.equals(PROPERTY_MITER_LIMIT)) {
				strokeProperty.setMiterLimit((String) aValue);
				setPaintPropertyStroke(strokeProperty);
				
			} else if (propertyName.equals(PROPERTY_OPACITY)) {
				alphaProperty.setAlpha(Double.toString((Double) aValue));
				setPaintPropertyAlpha(alphaProperty);
				
			} else if (propertyName.equals(PROPERTY_VERTICAL_EDGES_SELECTED)) {
				setVerticalEdgePropertyValue((String) aValue);
				
			} else if (propertyName.equals(PROPERTY_WIDTH)) {
				strokeProperty.setWidth(Double.toString((Double)aValue));
				setPaintPropertyStroke(strokeProperty);
				
			} else if (propertyName.equals(PROPERTY_FILL)) {
				fillProperty.setFillColor((Color) aValue);
				setPaintPropertyFill(fillProperty);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#executeCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	public IConsoleCommand executeCommand(String cmd, Object[] args) {
		IConsoleCommand value    = null;
		boolean         executed = false;
		
		for(int i = 0; i < COMMANDS.length && !executed; i++) {
			for (int j = 0; j < COMMANDS[i].length && !executed; j++) {
				String commandName = COMMANDS[i][j].split(" ", 3)[1];
				commandName = commandName.substring(0, commandName.indexOf('('));
				if (cmd.equals(commandName)) {
					try {
						/* This is the method name to set paint properties, e.g. setPaintPropertyStroke(PaintPropertyStroke pps) */
						String methodName = "set"+PAINT_PROPERTY[i][0];

						/* This is the type the above method needs */
						Class<?> clazz = (Class<?>) PAINT_PROPERTY[i][1];

						/* This is THE method mentioned above */
						Method method = getClass().getMethod(methodName, clazz);

						Object object = getClass().getMethod(methodName.replace("set", "get")).invoke(this);

						/* Now, get the method we should invoke in the above atribute before being set by the first method */
						Method aMethod = object.getClass().getMethod(commandName, String.class);

						/* Invoke the above method in the atribute itself, e.g. strokeProperty.setColor("0xffffff") */
						aMethod.invoke(object, args);

						/* Now invoke the first method with the modified atribute */
						value = (IConsoleCommand) method.invoke(this, object);
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
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getCommandList()
	 */
	@Override
	public String[] getCommandList() {
		ArrayList<String> cmds = new ArrayList<String>();
		
		for(int i = 0; i < COMMANDS.length; i++) {
			for (int j = 0; j < COMMANDS[i].length; j++) {
				String commandName = COMMANDS[i][j].split(" ", 3)[1];
				commandName = commandName.substring(0, commandName.indexOf('('));
				cmds.add(commandName);
			}
		}
		
		return cmds.toArray(new String[0]);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getReturnTypeForCommand(java.lang.String)
	 */
	@Override
	public String getReturnTypeForCommand(String cmd){
		String type = null;
		
		for(int i = 0; i < COMMANDS.length; i++) {
			for (int j = 0; j < COMMANDS[i].length; j++) {
				String commandName = COMMANDS[i][j].split(" ", 3)[1];
				commandName = commandName.substring(0, commandName.indexOf('('));
				if (cmd.equals(commandName))
					type = COMMANDS[i][0].split(" ")[0];
			}
		}
		return type;//.replace("AbstractDrawing", "DrawingShape");
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getArgumentsTypesForCommand(java.lang.String)
	 */
	@Override
	public String[] getArgumentsTypesForCommand(String cmd){
		return getArgsNamesOrTypesOrDescsForCommand(cmd, 0);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getArgumentsNamesForCommand(java.lang.String)
	 */
	@Override
	public String[] getArgumentsNamesForCommand(String cmd){
		return getArgsNamesOrTypesOrDescsForCommand(cmd, 1);
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
		if (getCommandList().length == 0)
			return null;
		
		ArrayList<String> args = new ArrayList<String>();
		
		for(int i = 0; i < COMMANDS.length; i++) {
			for (int j = 0; j < COMMANDS[i].length; j++) {
				String completeCommand = COMMANDS[i][j].split(" ", 3)[1];
				if (cmd.equals(completeCommand.substring(0, completeCommand.indexOf('(')))) {
					String argumentsLine = completeCommand.replaceAll("\\(([^)]+)\\)", "$1");
					int end;
					do {
						int start = argumentsLine.indexOf('{');
						end   = argumentsLine.indexOf('}');
						args.add(argumentsLine.substring(start+1, end).split(" ", 3)[arg]);
						argumentsLine = argumentsLine.substring(end, argumentsLine.length());
					} while(end < argumentsLine.length());
				}
			}
		}
		
		return args.toArray(new String[0]);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getDescriptionForCommand(java.lang.String)
	 */
	@Override
	public String getDescriptionForCommand(String cmd) {
		String desc = null;
		for(int i = 0; i < COMMANDS.length; i++) {
			for (int j = 0; j < COMMANDS[i].length; j++) {
				String commandName = COMMANDS[i][j].split(" ", 3)[1];
				commandName = commandName.substring(0, commandName.indexOf('('));
				if (cmd.equals(commandName))
					desc = COMMANDS[i][0].split(" ")[2];
			}
		}
		
		return desc;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFillableShape()
	 */
	@Override
	public Shape getFillableShape() {
		if (!isFillable())
			return null;
		return shape;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFillableShapePaint()
	 */
	@Override
	public Paint getFillableShapePaint() {
		if (getPaintPropertyFill() != null)
			return getPaintPropertyFill().getFillPaint();
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendChanged()
	 */
	@Override
	public void sendChanged() {
		if (NetManager.getInstance().isConnected()) {
			tellapic.tellapic_send_fig(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_EDIT_FIG,
					0,
					SessionUtils.getId(), 
					getNumber(),
					(float) getPaintPropertyStroke().getWidth(),
					getPaintPropertyAlpha().getAlpha(),
					((Color) getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) getPaintPropertyFill().getFillPaint()).getAlpha(),
					getFirstX(),
					getFirstY(),
					getPaintPropertyStroke().getColor().getRed(),
					getPaintPropertyStroke().getColor().getGreen(),
					getPaintPropertyStroke().getColor().getBlue(),
					getPaintPropertyStroke().getColor().getAlpha(),
					getLastX(),
					getLastY(),
					getPaintPropertyStroke().getLineJoins().ordinal(),
					getPaintPropertyStroke().getEndCaps().ordinal(),
					getPaintPropertyStroke().getMiterLimit(),
					getPaintPropertyStroke().getDash_phase(),
					getPaintPropertyStroke().getDash()
			);
		}
	}
}