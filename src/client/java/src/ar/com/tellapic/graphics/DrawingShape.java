package ar.com.tellapic.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.graphics.ControlPoint.ControlType;
import ar.com.tellapic.utils.Utils;

public abstract class DrawingShape extends AbstractDrawing {
	public static enum PropertyType {
		NAME,
		OWNER,
		NUMBER,
		DRAWINGBYTE,
		DRAWINGBYTEEXT,
		WIDTH,
		OPACITY,
		COLOR,
		X1COORD,
		Y1COORD,
		X2COORD,
		Y2COORD,
		JOIN,
		CAPS,
		MITERLIMIT,
		VISIBLE,
		RESIZEABLE,
		MOVEABLE,
		CONTROLPOINT_1,
		CONTROLPOINT_2,
		CONTROLPOINT_3,
		CONTROLPOINT_4,
		CONTROLPOINT_5,
		CONTROLPOINT_6,
		CONTROLPOINT_7,
		CONTROLPOINT_8,
		DASH
	};
	protected static final int  COLUMN_COUNT = 3;
	protected static final String COLUMN_NAMES[] = new String[] {
		Utils.msg.getString("name"),
		Utils.msg.getString("value"),
		Utils.msg.getString("show")
	};
	protected static final int NAME_COLUMN  = 0;
	protected static final int VALUE_COLUMN = 1;
	protected static final int EXTRA_COLUMN = 2;
	protected static Class<?> COLUMN_CLASS[] = new Class<?>[] {
		String.class,
		Object.class,
		Boolean.class
	};
	private PaintPropertyStroke strokeProperty;
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private Shape               shape;
	private BasicStroke         selectedShapeStroke;
	private BasicStroke         selectedEdgesStroke;
	private AlphaComposite      selectedAlphaComposite;
	private boolean             isLeftEdgeSelected;
	private boolean             isRightEdgeSelected;
	private boolean             isTopEdgeSelected;
	private boolean             isBottomEdgeSelected;
	
	
	/**
	 * 
	 * @param name
	 */
	public DrawingShape(String name, boolean resizeable, boolean moveable) {
		setResizeable(resizeable);
		setMoveable(moveable);
		createPropertiesMatrix();
		/* This 2 properties are not changeable */
		properties[PropertyType.MOVEABLE.ordinal()][VALUE_COLUMN]   = moveable;
		properties[PropertyType.RESIZEABLE.ordinal()][VALUE_COLUMN] = resizeable;
		properties[PropertyType.MOVEABLE.ordinal()][EXTRA_COLUMN]   = false;
		properties[PropertyType.RESIZEABLE.ordinal()][EXTRA_COLUMN] = false;
		shape = null;
		strokeProperty = null;
		colorProperty  = null;
		alphaProperty  = null;
		selectedAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
		selectedShapeStroke = new BasicStroke(2, 0, 0, 1, new float[] { 5, 5}, 0);
		selectedEdgesStroke = new BasicStroke(1, 0, 0); //, new float[] { 5, 5}, 0);
		setVisible(true);
		setName(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		properties[PropertyType.VISIBLE.ordinal()][VALUE_COLUMN] = visible;
		properties[PropertyType.VISIBLE.ordinal()][EXTRA_COLUMN] = visible;
		super.setVisible(visible);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		properties[PropertyType.NAME.ordinal()][VALUE_COLUMN] = name;
		super.setName(name);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setUser(ar.com.tellapic.AbstractUser)
	 */
	@Override
	public void setUser(AbstractUser user) {
		properties[PropertyType.OWNER.ordinal()][VALUE_COLUMN] = user.getName();
		super.setUser(user);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#setNumber(long)
	 */
	@Override
	public void setNumber(long n) {
		properties[PropertyType.NUMBER.ordinal()][VALUE_COLUMN] = n;
		super.setNumber(n);
	}
	
	/**
	 * @param shape the shape to set
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	/**
	 * @return the shape
	 */
	public Shape getShape() {
		return shape;
	}
	
	/**
	 * @return the stroke or null
	 */
	public PaintPropertyStroke getPaintPropertyStroke() {
		return strokeProperty;
	}
	
	/**
	 * @return the color or null
	 */
	public PaintPropertyColor getPaintPropertyColor() {
		return colorProperty;
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
	 */
	public void setStroke(PaintPropertyStroke property) {
		strokeProperty = property;
	}
	
	/**
	 * 
	 * @param property
	 */
	public void setColor(PaintPropertyColor property) {
		colorProperty = property;
	}

	/**
	 * 
	 * @param property
	 */
	public void setAlpha(PaintPropertyAlpha property) {
		alphaProperty = property;
	}
	
	/**
	 * This method should be called when a drawing is finished. A drawing being drawn, is using
	 * user toolbox properties instances, and if we don't clone this properties, when the user
	 * change toolbox properties, such as color, this drawing will be drawn with the latest color used.
	 */
	public void cloneProperties() {
		alphaProperty  = (PaintPropertyAlpha) alphaProperty.clone();
		strokeProperty = (PaintPropertyStroke) strokeProperty.clone();
		colorProperty  = (PaintPropertyColor) colorProperty.clone();
		if (controlPoints != null)
			controlPoints  = controlPoints.clone();
		/* Now, get the values from the REAL properties instances for this drawing, not the toolbox instances. */
		properties[PropertyType.CAPS.ordinal()][VALUE_COLUMN]       = strokeProperty.getEndCaps();
		properties[PropertyType.JOIN.ordinal()][VALUE_COLUMN]       = strokeProperty.getLineJoins();
		properties[PropertyType.MITERLIMIT.ordinal()][VALUE_COLUMN] = strokeProperty.getMiterLimit();
		properties[PropertyType.WIDTH.ordinal()][VALUE_COLUMN]      = strokeProperty.getWidth();
		properties[PropertyType.DASH.ordinal()][VALUE_COLUMN]       = 0;
		properties[PropertyType.COLOR.ordinal()][VALUE_COLUMN]      = colorProperty.getColor().toString();
		properties[PropertyType.OPACITY.ordinal()][VALUE_COLUMN]    = alphaProperty.alpha;
		/* Take advantage of knowing that this drawing is finished and set more properties */
		Rectangle2D rect = shape.getBounds2D();
		properties[PropertyType.X1COORD.ordinal()][VALUE_COLUMN] = rect.getX();
		properties[PropertyType.Y1COORD.ordinal()][VALUE_COLUMN] = rect.getY();
		properties[PropertyType.X2COORD.ordinal()][VALUE_COLUMN] = rect.getMaxX();
		properties[PropertyType.Y2COORD.ordinal()][VALUE_COLUMN] = rect.getMaxY();
		properties[PropertyType.X1COORD.ordinal()][EXTRA_COLUMN] = isLeftEdgeSelected();
		properties[PropertyType.Y1COORD.ordinal()][EXTRA_COLUMN] = isTopEdgeSelected();
		properties[PropertyType.X2COORD.ordinal()][EXTRA_COLUMN] = isRightEdgeSelected();
		properties[PropertyType.Y2COORD.ordinal()][EXTRA_COLUMN] = isBottomEdgeSelected();
	}
	
	/**
	 * 
	 * @param g
	 */
	public void draw(Graphics2D g) {
		PaintProperty overridenProperties[] = getUser().getCustomProperties();
		if (isVisible()) {
			//g.setRenderingHints(rh);
			
			if (overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA] != null)
				g.setComposite(((PaintPropertyAlpha)overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA]).getComposite());
			else if (alphaProperty != null)
				g.setComposite(getPaintPropertyAlpha().getComposite());

			if (overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR] != null)
				g.setColor(((PaintPropertyColor)overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR]).getColor());
			else if (colorProperty != null)
				g.setColor(getPaintPropertyColor().getColor());

			if (overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_STROKE] != null)
				g.setStroke(((PaintPropertyStroke)overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_STROKE]).getStroke());
			else if (strokeProperty != null)
				g.setStroke(getPaintPropertyStroke().getStroke());

			if (shape != null) {
				g.draw(shape);
				if (isSelected()) {
					g.setComposite(selectedAlphaComposite);
					g.setColor(Color.yellow);
					g.setStroke(selectedShapeStroke);
					g.draw(shape.getBounds2D());
				}
				g.setStroke(selectedEdgesStroke);
				g.setColor(Color.orange);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.7f));
				if (isLeftEdgeSelected())
					g.drawLine((int)shape.getBounds2D().getX(), 0, (int)shape.getBounds2D().getX(), Integer.MAX_VALUE);
				if (isRightEdgeSelected())
					g.drawLine((int)shape.getBounds2D().getMaxX(), 0, (int)shape.getBounds2D().getMaxX(), Integer.MAX_VALUE);
				if (isTopEdgeSelected())
					g.drawLine(0, (int)shape.getBounds2D().getY(), Integer.MAX_VALUE, (int)shape.getBounds2D().getY());
				if (isBottomEdgeSelected())
					g.drawLine(0, (int)shape.getBounds2D().getMaxY(), Integer.MAX_VALUE, (int)shape.getBounds2D().getMaxY());
			}
		}
	}

	/**
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}
	
	/**
	 * 
	 */
	public void setSelected(boolean value) {
		selected = value;
		if (isSelected() && !getUser().isRemote()) {
			createControlPoints();
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * 
	 */
	private void createControlPoints() {
		int i = PropertyType.CONTROLPOINT_1.ordinal();
		for(ControlPoint point : controlPoints) {
			try {
				point.setControlPoint(getShape());
				properties[i][VALUE_COLUMN] = point;
				properties[i][EXTRA_COLUMN] = point.isSelected();
				i++;
			} catch (IllegalControlPointTypeException e) {
				e.printStackTrace();
			}
		}	
	}
	
	/**
	 * @param isLeftEdgeSelected the isLeftEdgeSelected to set
	 */
	public void setLeftEdgeSelected(boolean isLeftEdgeSelected) {
		this.isLeftEdgeSelected = isLeftEdgeSelected;
		setChanged();
		notifyObservers();

	}

	/**
	 * @return the isLeftEdgeSelected
	 */
	public boolean isLeftEdgeSelected() {
		return isLeftEdgeSelected;
	}

	/**
	 * @param isRightEdgeSelected the isRightEdgeSelected to set
	 */
	public void setRightEdgeSelected(boolean isRightEdgeSelected) {
		this.isRightEdgeSelected = isRightEdgeSelected;
		setChanged();
		notifyObservers();
	}

	/**
	 * @return the isRightEdgeSelected
	 */
	public boolean isRightEdgeSelected() {
		return isRightEdgeSelected;
	}

	/**
	 * @param isTopEdgeSelected the isTopEdgeSelected to set
	 */
	public void setTopEdgeSelected(boolean isTopEdgeSelected) {
		this.isTopEdgeSelected = isTopEdgeSelected;
		setChanged();
		notifyObservers();
	}

	/**
	 * @return the isTopEdgeSelected
	 */
	public boolean isTopEdgeSelected() {
		return isTopEdgeSelected;
	}

	/**
	 * @param isBottomEdgeSelected the isBottomEdgeSelected to set
	 */
	public void setBottomEdgeSelected(boolean isBottomEdgeSelected) {
		this.isBottomEdgeSelected = isBottomEdgeSelected;
		setChanged();
		notifyObservers();
	}

	/**
	 * @return the isBottomEdgeSelected
	 */
	public boolean isBottomEdgeSelected() {
		return isBottomEdgeSelected;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#getBounds()
	 */
	@Override
	public Rectangle2D getBounds2D() {
		if (shape == null)
			return null;
			
		return shape.getBounds2D();
	}

	/**
	 * 
	 */
	public void updateControlPoints() {
		for(ControlPoint point : controlPoints){
			try {
				point.setControlPoint(getShape());
			} catch (IllegalControlPointTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		return properties.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return properties[rowIndex][columnIndex];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == NAME_COLUMN)
			return false;
		
		if (columnIndex == VALUE_COLUMN)
			return !(rowIndex <= PropertyType.DRAWINGBYTEEXT.ordinal() || rowIndex >= PropertyType.RESIZEABLE.ordinal());
		else
			return  (rowIndex == PropertyType.VISIBLE.ordinal() || 
					(rowIndex >= PropertyType.CONTROLPOINT_1.ordinal() && rowIndex <= PropertyType.CONTROLPOINT_8.ordinal()) ||
					(rowIndex >= PropertyType.X1COORD.ordinal() && rowIndex <= PropertyType.Y2COORD.ordinal())
					);
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void removeTableModelListener(TableModelListener l) {}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		properties[rowIndex][columnIndex] = aValue;
		if (columnIndex == VALUE_COLUMN) {
			
			if (rowIndex == PropertyType.X1COORD.ordinal() && isResizeable()) {
				resize(Double.parseDouble((String)aValue), shape.getBounds2D().getY(), getControlPointByType(ControlType.LEFT_CONTROL_POINT));
			} else if (rowIndex == PropertyType.X2COORD.ordinal() && isResizeable()) {
				resize(Double.parseDouble((String)aValue), shape.getBounds2D().getY(), getControlPointByType(ControlType.RIGHT_CONTROL_POINT));
			} else if (rowIndex == PropertyType.Y1COORD.ordinal() && isResizeable()) {
				resize(shape.getBounds2D().getX(), Double.parseDouble((String)aValue) , getControlPointByType(ControlType.TOP_CONTROL_POINT));
			} else if (rowIndex == PropertyType.Y2COORD.ordinal() && isResizeable()) {
				resize(shape.getBounds2D().getX(), Double.parseDouble((String)aValue), getControlPointByType(ControlType.BOTTOM_CONTROL_POINT));
			}
		} else if (columnIndex == EXTRA_COLUMN) {
			boolean value = (Boolean) aValue;
			if (rowIndex >= PropertyType.CONTROLPOINT_1.ordinal() && rowIndex <= PropertyType.CONTROLPOINT_8.ordinal()) {
				ControlPoint cp = (ControlPoint) properties[rowIndex][VALUE_COLUMN];
				cp.setSelected(value);
			}else if (rowIndex == PropertyType.X1COORD.ordinal()) {
				setLeftEdgeSelected(value);
			} else if (rowIndex == PropertyType.X2COORD.ordinal()) {
				setRightEdgeSelected(value);
			} else if (rowIndex == PropertyType.Y1COORD.ordinal()) {
				setTopEdgeSelected(value);
			} else if (rowIndex == PropertyType.Y2COORD.ordinal()) {
				setBottomEdgeSelected(value);
			} else if (rowIndex == PropertyType.VISIBLE.ordinal()) {
				setVisible(value);
			}
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#createPropertiesNames()
	 */
	protected void createPropertiesMatrix() {
		properties = new Object[PropertyType.values().length][COLUMN_COUNT];
		for (PropertyType property : PropertyType.values()) {
			String key[] = property.toString().toLowerCase().split("_");;
			properties[property.ordinal()][NAME_COLUMN] = Utils.msg.getString(key[0]) + ((key.length > 1)? " "+key[1] : "");
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
//		ListSelectionModel lsm = (ListSelectionModel)e.getSource();
//		int selectedIndex = -1;
//		if (lsm.isSelectionEmpty()) {
//			return;
//		} else {
//			int minIndex = lsm.getMinSelectionIndex();
//			int maxIndex = lsm.getMaxSelectionIndex();
//			for (int i = minIndex; i <= maxIndex; i++) {
//				if (lsm.isSelectedIndex(i)) {
//					selectedIndex = i;
//				}
//			}
//		}
//
//		if (selectedIndex >= PropertyType.CONTROLPOINT_1.ordinal() && selectedIndex <= PropertyType.CONTROLPOINT_8.ordinal()) {
//		
//		} else if (selectedIndex == PropertyType.X1COORD.ordinal()) {
//			setLeftEdgeSelected(true);
//			setRightEdgeSelected(false);
//			setTopEdgeSelected(false);
//			setBottomEdgeSelected(false);
//		} else if (selectedIndex == PropertyType.X2COORD.ordinal()) {
//			setRightEdgeSelected(true);
//			setLeftEdgeSelected(false);
//			setTopEdgeSelected(false);
//			setBottomEdgeSelected(false);
//		} else if (selectedIndex == PropertyType.Y1COORD.ordinal()) {
//			setTopEdgeSelected(true);
//			setRightEdgeSelected(false);
//			setLeftEdgeSelected(false);
//			setBottomEdgeSelected(false);
//		} else if (selectedIndex == PropertyType.Y2COORD.ordinal()) {
//			setBottomEdgeSelected(true);
//			setRightEdgeSelected(false);
//			setTopEdgeSelected(false);
//			setLeftEdgeSelected(false);
//		}
//		DrawingAreaView.getInstance().update(null, null);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
}