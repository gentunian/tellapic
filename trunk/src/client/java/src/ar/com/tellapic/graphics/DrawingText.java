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
package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
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

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingText extends AbstractDrawing {
	
	public static final String PROPERTY_TEXT_X     = "y";
	public static final String PROPERTY_TEXT_Y     = "x";
	public static final String PROPERTY_FONT       = "Font";
	public static final String PROPERTY_TEXT       = "Text";
	public static final String PROPERTY_COLOR      = "Text Color";
	public static final String PROPERTY_OPACITY    = "Opacity";
	public static final String PROPERTY_FILL       = "Fill Color";
	public static final String PROPERTY_LOCATION   = "Location";
	
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private PaintPropertyFont   fontProperty;
	private PaintPropertyFill   fillProperty;
	private int                 textX;
	private int                 textY;
	private TableModelListener  tml;
	
	private static Class<?> COLUMN_CLASS[] = new Class<?>[] {
		String.class,
		Object.class
	};
	
	private static final String COLUMN_NAMES[] = new String[] {
		Utils.msg.getString("name"),
		Utils.msg.getString("value")
	};
	private static final int COLUMN_COUNT = 2;
	
	public static String[][]    COMMANDS = new String[][] {
		PaintPropertyFont.CLI_CMDS,
		PaintPropertyColor.CLI_CMDS,
		PaintPropertyAlpha.CLI_CMDS
	};
	
	private static final Object[][] PAINT_PROPERTY = new Object[][] {
		{"PaintPropertyFont", PaintPropertyFont.class},
		{"PaintPropertyColor", PaintPropertyColor.class},
		{"PaintPropertyAlpha", PaintPropertyAlpha.class}
	};
	
	
	/**
	 * 
	 * @param name
	 */
	public DrawingText(TellapicAbstractUser user, String name) {
		super(name, false, true, true);
		
		colorProperty  = null;
		alphaProperty  = null;
		fontProperty   = null;
		fillProperty   = null;
		
		setName(name);
		setVisible(true);
		setLeftEdgeSelected(false);
		setRightEdgeSelected(false);
		setTopEdgeSelected(false);
		setBottomEdgeSelected(false);
		setPaintPropertyAlpha((PaintPropertyAlpha) user.getToolBoxModel().getOpacityProperty().clone());
		setPaintPropertyFont((PaintPropertyFont) user.getToolBoxModel().getFontProperty().clone());
		setPaintPropertyColor((PaintPropertyColor) user.getToolBoxModel().getColorProperty().clone());
		setPaintPropertyFill((PaintPropertyFill) user.getToolBoxModel().getFillProperty().clone());
		setRenderingHints((RenderingHints) user.getToolBoxModel().getRenderingHints().clone());
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		PaintProperty overridenProperties[] = getUser().getCustomProperties();
		
		if (isVisible()) {
			if (fontProperty != null) {
				
				
				/* Get the set properties for this drawing */
				Font      oFont       = getPaintPropertyFont().getFont();
				Composite oComposite  = getPaintPropertyAlpha().getComposite();
				Color     oColor      = getPaintPropertyColor().getColor();
				Paint     paint       = getFillableShapePaint();

				if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA] != null)
					oComposite = ((PaintPropertyAlpha)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA]).getComposite();

				if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR] != null)
					oColor = (((PaintPropertyColor)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR]).getColor());
			
				if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_FONT] != null) {
					oFont = (((PaintPropertyFont)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_FONT]).getFont());
					/* Only override the fill property if this drawing was instructed to have one */
					if (paint != null)
						paint = oColor;
				}
				
				/* Fill could be set or unset. If a fill is set we should fill the shape then. */
				if (paint != null) {
					/* Set the paint for filling the shape */
					g.setPaint(paint);
					
					/* Get the fillable shape and fill it with the above painting */
					g.fill(getFillableShape());
				}
				
				/* Set the rendering hints */
				g.setRenderingHints(renderingHints);
				
				/* Set the composite */
				g.setComposite(oComposite);
				
				/* Set the color */
				g.setColor(oColor);
				
				/* Set the font */
				g.setFont(oFont);
				
				/* And finally, draw the string */
				g.drawString(getText(), getFirstX(), getFirstY());
				
				/* Call the super class method as he knows how to draw control points */
				super.draw(g);
			}
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getText()
	 */
	public String getText() {
		return fontProperty.getText();
	}

	/**
	 * @param textX the textX to set
	 */
	public void setTextX(int textX) {
		if (textX<0)
			textX=0;
		Object oldValue = properties.get(PROPERTY_TEXT_X);
		this.textX = textX;
		properties.put(PROPERTY_TEXT_X, textX);
		pcs.firePropertyChange(PROPERTY_TEXT_X, oldValue, textX);
		fireTableUpdate();
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void setTextCoordinates(int x, int y){
		if (x<0 || y<0)
			x = y = 0;
		setTextX(x);
		setTextY(y);
		pcs.firePropertyChange(PROPERTY_LOCATION, null, this);
	}

	/**
	 * @param textY the textY to set
	 */
	public void setTextY(int textY) {
		if (textY<0)
			textY=0;
		Object oldValue = properties.get(PROPERTY_TEXT_Y);
		this.textY = textY;
		properties.put(PROPERTY_TEXT_Y, textY);
		pcs.firePropertyChange(PROPERTY_TEXT_Y, oldValue, textY);
		fireTableUpdate();
	}

	/**
	 * 
	 * @param property
	 */
	public DrawingText setPaintPropertyFont(PaintPropertyFont property) {
		Utils.logMessage("Setting font to: "+property);
		fontProperty = property;
		properties.put(PROPERTY_FONT, fontProperty);
		if (fontProperty != null)
			properties.put(PROPERTY_TEXT, fontProperty.getText());
		pcs.firePropertyChange(PROPERTY_FONT, 1, fontProperty);//FIXME:
		fireTableUpdate();
		return this;
	}
	
	/**
	 * @param paintPropertyAlpha
	 */
	public DrawingText setPaintPropertyAlpha(PaintPropertyAlpha paintPropertyAlpha) {
		alphaProperty = paintPropertyAlpha;
		properties.put(PROPERTY_OPACITY, alphaProperty.getAlpha());
		pcs.firePropertyChange(PROPERTY_OPACITY, 1, alphaProperty);//FIXME:
		fireTableUpdate();
		return this;
	}

	/**
	 * @param paintPropertyColor
	 */
	public DrawingText setPaintPropertyColor(PaintPropertyColor paintPropertyColor) {
		colorProperty = paintPropertyColor;
		properties.put(PROPERTY_COLOR, colorProperty.getColor());
		pcs.firePropertyChange(PROPERTY_COLOR, 1, colorProperty);//FIXME:
		fireTableUpdate();
		return this;
	}
	
	/**
	 * @param clone
	 */
	public DrawingText setPaintPropertyFill(PaintPropertyFill property) {
		fillProperty = property;
		properties.put(PROPERTY_FILL, fillProperty.getFillPaint());
		pcs.firePropertyChange(PROPERTY_FILL, 1, fillProperty);//FIXME:
		fireTableUpdate();
		return this;
	}

	/**
	 * @return
	 */
	public PaintPropertyFill getPaintPropertyFill() {
		return fillProperty;
	}
	
	/**
	 * @return
	 */
	public PaintPropertyFont getPaintPropertyFont() {
		return fontProperty;
	}

	/**
	 * @return
	 */
	public PaintPropertyAlpha getPaintPropertyAlpha() {
		return alphaProperty;
	}

	/**
	 * @return
	 */
	public PaintPropertyColor getPaintPropertyColor() {
		return colorProperty;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getBounds()
	 */
	@Override
	public Rectangle2D getBounds2D() {
		if (fontProperty != null) {
			Rectangle2D r = fontProperty.getFont().getStringBounds(getText(), ((Graphics2D)DrawingAreaView.getInstance().getGraphics()).getFontRenderContext());
			r.setFrame(getFirstX(), getFirstY() - r.getHeight() + 2, r.getWidth(), r.getHeight());
			return  r;
		}
		
		return null;
	}



	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(int, int, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double x, double y, ControlPoint controlPoint) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	@Override
	protected void fireTableUpdate() {
		if (tml != null)
			tml.tableChanged(new TableModelEvent(this));
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
		if (propertyName.equals(PROPERTY_NAME) || propertyName.equals(PROPERTY_RESIZEABLE) ||
				propertyName.equals(PROPERTY_USER_NAME) ||propertyName.equals(PROPERTY_MOVEABLE) ||
				propertyName.equals(PROPERTY_NUMBER) || propertyName.equals(PROPERTY_SELECTION)|| 
				propertyName.equals(PROPERTY_FILLABLE) || propertyName.equals(PROPERTY_TEXT_X) ||
				propertyName.equals(PROPERTY_TEXT_Y)) 
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
				
			} else if (propertyName.equals(PROPERTY_COLOR)) {
				try {
					colorProperty.setColor(aValue.toString());
					setPaintPropertyColor(colorProperty);
				} catch(NumberFormatException e) {
					Utils.logMessage("No changes made, "+aValue+" is not a valid color.");
				}
				
			} else if (propertyName.equals(PROPERTY_TEXT)) {
				if (fontProperty != null)
					fontProperty.setText((String) aValue);
				setPaintPropertyFont(fontProperty);

			} else if (propertyName.equals(PROPERTY_HORIZONTAL_EDGES_SELECTED)) {
				setHorizontalEdgePropertyValue((String) aValue);
	
			} else if (propertyName.equals(PROPERTY_OPACITY)) {
				alphaProperty.setAlpha(Double.toString((Double) aValue));
				setPaintPropertyAlpha(alphaProperty);
				
			} else if (propertyName.equals(PROPERTY_VERTICAL_EDGES_SELECTED)) {
				setVerticalEdgePropertyValue((String) aValue);
				
			} else if (propertyName.equals(PROPERTY_FONT)) {
				if (aValue != null)
					setPaintPropertyFont((PaintPropertyFont) aValue);
				
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
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double xOffset, double yOffset) {
		setTextCoordinates((int)(textX + xOffset), (int)(textY + yOffset)); 
	}
	
	/*
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
		return type;//.replace("AbstractDrawing", "DrawingText");
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
		return getBounds2D();
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


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstX()
	 */
	@Override
	public int getFirstX() {
		return textX;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getFirstY()
	 */
	@Override
	public int getFirstY() {
		return textY;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastX()
	 */
	@Override
	public int getLastX() {
		return (int) this.getBounds2D().getMaxX();
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getLastY()
	 */
	@Override
	public int getLastY() {
		return (int) this.getBounds2D().getMaxY();
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendChanged()
	 */
	@Override
	public void sendChanged() {
		if (NetManager.getInstance().isConnected()) {
			tellapic.tellapic_send_text(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_TEXT,
					SessionUtils.getId(), 
					getNumber(),
					getPaintPropertyFont().getFont().getSize2D(),
					getPaintPropertyAlpha().getAlpha(),
					((Color) getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) getPaintPropertyFill().getFillPaint()).getAlpha(),
					getLastX(),
					getLastY(),
//					getFirstX(),
//					getFirstY(),
					(int)getBounds2D().getX(),
					(int)getBounds2D().getY(),
					getPaintPropertyFont().getColor().getRed(),
					getPaintPropertyFont().getColor().getGreen(),
					getPaintPropertyFont().getColor().getBlue(),
					getPaintPropertyFont().getColor().getAlpha(),
					getPaintPropertyFont().getStyle().ordinal(),
					getPaintPropertyFont().getFace().length(),
					getPaintPropertyFont().getFace(),
					getText().length(),
					getText()
			);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDeferred()
	 */
	@Override
	public void sendDeferred() {
		if (NetManager.getInstance().isConnected()) {
			tellapic.tellapic_send_text(
					NetManager.getInstance().getSocket(),
					tellapicConstants.TOOL_TEXT,
					SessionUtils.getId(), 
					0,
					getPaintPropertyFont().getFont().getSize2D(),
					getPaintPropertyAlpha().getAlpha(),
					((Color) getPaintPropertyFill().getFillPaint()).getRed(),
					((Color) getPaintPropertyFill().getFillPaint()).getGreen(),
					((Color) getPaintPropertyFill().getFillPaint()).getBlue(),
					((Color) getPaintPropertyFill().getFillPaint()).getAlpha(),
					getLastX(),
					getLastY(),
					(int)getBounds2D().getX(),
					(int)getBounds2D().getY(),
//					getFirstX(),
//					getFirstY(),
					getPaintPropertyFont().getColor().getRed(),
					getPaintPropertyFont().getColor().getGreen(),
					getPaintPropertyFont().getColor().getBlue(),
					getPaintPropertyFont().getColor().getAlpha(),
					getPaintPropertyFont().getStyle().ordinal(),
					getPaintPropertyFont().getFace().length(),
					getPaintPropertyFont().getFace(),
					getText().length(),
					getText()
			);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendDragged(MouseEvent event) {
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendPressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendPressed(MouseEvent event) {
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#sendReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void sendReleased(MouseEvent event) {
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int x1, int y1, int x2, int y2) {
		setTextCoordinates(x1, y1);
	}
}
