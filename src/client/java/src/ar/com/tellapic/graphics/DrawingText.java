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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;

import ar.com.tellapic.TellapicAbstractUser;
import ar.com.tellapic.console.IConsoleCommand;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingText extends AbstractDrawing {
	
	public static final int TEXT_X_SET         = 2;
	public static final int TEXT_Y_SET         = 3;
	public static final int FONT_PROPERTY_SET  = 4;
	public static final int ALPHA_PROPERTY_SET = 5;
	public static final int COLOR_PROPERTY_SET = 6;
	
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private PaintPropertyFont   fontProperty;
	private int                 textX;
	private int                 textY;
	public static String[][]    COMMANDS = new String[][] {
		{ "setText", "setTextPosition" },
		{ "DrawingText", "String text"},
		{ "DrawingText", "int x", "int y"}
	};
	
	/**
	 * 
	 * @param name
	 */
	public DrawingText(String name) {
		super(name, true, true);
		colorProperty  = null;
		alphaProperty  = null;
		fontProperty   = null;
		setName(name);
		setVisible(true);
		
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#draw(java.awt.Graphics2D)
	 */
	@Override
	public void draw(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		PaintProperty overridenProperties[] = getUser().getCustomProperties();
		if (isVisible()) {
			g.setRenderingHints(renderingHints);
			
			if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA] != null)
				g.setComposite(((PaintPropertyAlpha)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA]).getComposite());
			else if (alphaProperty != null)
				g.setComposite(getPaintPropertyAlpha().getComposite());

			if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR] != null)
				g.setColor(((PaintPropertyColor)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR]).getColor());
			else if (colorProperty != null)
				g.setColor(getPaintPropertyColor().getColor());

			if (overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_FONT] != null) 
				g.setFont(((PaintPropertyFont)overridenProperties[TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_FONT]).getFont());
			else if (fontProperty != null) {
				g.setFont(getPaintPropertyFont().getFont());
				g.drawString(getText(), getTextX(), getTextY());
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
		this.textX = textX;
		setChanged();
		notifyObservers(new Object[] {TEXT_X_SET});
	}

	/**
	 * @return the textX
	 */
	public int getTextX() {
		return textX;
	}

	/**
	 * @param textY the textY to set
	 */
	public void setTextY(int textY) {
		this.textY = textY;
		setChanged();
		notifyObservers(new Object[] {TEXT_Y_SET});
	}

	/**
	 * @return the textY
	 */
	public int getTextY() {
		return textY;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#cloneProperties()
	 */
	@Override
	public void cloneProperties() {
		alphaProperty  = (PaintPropertyAlpha) alphaProperty.clone();
		fontProperty = (PaintPropertyFont) fontProperty.clone();
		colorProperty  = (PaintPropertyColor) colorProperty.clone();
	}
	
	/**
	 * 
	 * @param property
	 */
	public void setPaintPropertyFont(PaintPropertyFont property) {
		fontProperty = property;
		setChanged();
		notifyObservers(new Object[] {FONT_PROPERTY_SET});
	}
	
	/**
	 * @param paintPropertyAlpha
	 */
	public void setPaintPropertyAlpha(PaintPropertyAlpha paintPropertyAlpha) {
		alphaProperty = paintPropertyAlpha;
		setChanged();
		notifyObservers(new Object[] {ALPHA_PROPERTY_SET});
	}

	/**
	 * @param paintPropertyColor
	 */
	public void setPaintPropertyColor(PaintPropertyColor paintPropertyColor) {
		colorProperty = paintPropertyColor;
		setChanged();
		notifyObservers(new Object[] {COLOR_PROPERTY_SET});
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
		Rectangle2D r = fontProperty.getFont().getStringBounds(getText(), ((Graphics2D)DrawingAreaView.getInstance().getGraphics()).getFontRenderContext());
		r.setFrame(getTextX(), getTextY()-r.getHeight(), r.getWidth(), r.getHeight()); 
		return  r;
	}



	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(int, int, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double x, double y, ControlPoint controlPoint) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
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
		this.textX += xOffset;
		this.textY += yOffset;
		updateControlPoints();
//		setChanged();
//		notifyObservers();
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
		String[] cmdList = getCommandList();
		IConsoleCommand value = null;
		boolean executed = false;
		
		for(int i = 0; i < cmdList.length && !executed; i++) {
			if (cmd.equals(cmdList[i])) {
				try {
//					Class<Tool> toolClass      = (Class<Tool>) Class.forName(toolClassName);
					Method method = DrawingToolRectangle.class.getMethod(cmd);
//					Method method = DrawingToolRectangle.class.getMethod(cmd);
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
		return (String[]) COMMANDS[0];
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getArgumentsNamesForCommand(java.lang.String)
	 */
	@Override
	public String[] getArgumentsNamesForCommand(String cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getArgumentsTypesForCommand(java.lang.String)
	 */
	@Override
	public String[] getArgumentsTypesForCommand(String cmd) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleCommand#getReturnTypeForCommand(java.lang.String)
	 */
	@Override
	public String getReturnTypeForCommand(String cmd) {
		// TODO Auto-generated method stub
		return null;
	}

}
