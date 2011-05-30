package ar.com.tellapic.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.graphics.ControlPoint.ControlType;

public class DrawingShape extends AbstractDrawing {
	private PaintPropertyStroke strokeProperty;
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private Shape               shape;
	private BasicStroke         selectedShapeStroke;
	private AlphaComposite      selectedAlphaComposite;
	
	
	/**
	 * 
	 * @param name
	 */
	public DrawingShape(String name) {
		shape = null;
		strokeProperty = null;
		colorProperty  = null;
		alphaProperty  = null;
		controlPoints  = new ControlPoint[8];
		selectedAlphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
		selectedShapeStroke = new BasicStroke(2, 0, 0, 10, new float[] { 5, 5}, 0);
		setName(name);
		setVisible(true);
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
		if (strokeProperty == null)
			return null;
		
		return strokeProperty;
	}
	
	/**
	 * @return the color or null
	 */
	public PaintPropertyColor getPaintPropertyColor() {
		if (colorProperty == null)
			return null;
		
		return colorProperty;
	}

	/**
	 * @return the alpha or null
	 */
	public PaintPropertyAlpha getPaintPropertyAlpha() {
		if (alphaProperty == null)
			return null;
		
		return alphaProperty;
	}

	public void setStroke(PaintPropertyStroke property) {
		strokeProperty = property;
	}
	
	public void setColor(PaintPropertyColor property) {
		colorProperty = property;
	}


	public void setAlpha(PaintPropertyAlpha property) {
		alphaProperty = property;
	}
	
	
	public void cloneProperties() {
		alphaProperty  = (PaintPropertyAlpha) alphaProperty.clone();
		strokeProperty = (PaintPropertyStroke) strokeProperty.clone();
		colorProperty  = (PaintPropertyColor) colorProperty.clone();
		controlPoints  = controlPoints.clone();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
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
		if (isSelected()) {
			int i = 0;
			for(ControlType type : ControlPoint.ControlType.values()) {
				try {
					controlPoints[i++] = new ControlPoint(type, shape, Color.white);
				} catch (IllegalControlPointTypeException e) {
					e.printStackTrace();
				}
			}
		}
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

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#isResizeable()
	 */
	@Override
	public boolean isResizeable() {
		return (shape instanceof RectangularShape || shape instanceof Line2D);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#resize(int, int, ar.com.tellapic.graphics.ControlPoint)
	 */
	@Override
	public void resize(double eventX, double eventY, ControlPoint controlPoint) {
		if (isResizeable()) {
			switch(controlPoint.getType()) {
			case BOTTOM_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(shape.getX(), shape.getY(), shape.getWidth(), (eventY < shape.getY())? 0 : eventY - shape.getY());
				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine(line.getX1(), line.getY1(), line.getX2(), (eventY < line.getY1())? line.getY1() : eventY);
				}
				break;
			case TOP_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(shape.getX(), (eventY > shape.getMaxY())? shape.getY() : eventY, shape.getWidth(), (eventY > shape.getMaxY())? 0 : shape.getMaxY() - eventY);

				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine(line.getX1(), (eventY > line.getY2())? line.getY2() : eventY, line.getX2(), line.getY2());
				}
				break;
			case TOP_LEFT_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(
							((eventX > shape.getMaxX())? shape.getMaxX() : eventX), 
							((eventY > shape.getMaxY())? shape.getY() : eventY), 
							((eventX > shape.getMaxX())? 0 : shape.getMaxX() - eventX),
							((eventY > shape.getMaxY())? 0 : shape.getMaxY() - eventY)
					);
				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine((eventX > line.getX2())? line.getX2() : eventX, (eventY > line.getY2())? line.getY2() : eventY, line.getX2(), line.getY2());
				}
				break;
			case TOP_RIGHT_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(shape.getX(), ((eventY > shape.getMaxY())? shape.getMaxY() : eventY), ((eventX < shape.getX())? 0 : eventX - shape.getX()), ((eventY > shape.getMaxY())? 0 : shape.getMaxY() - eventY));

				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine(line.getX1(), (eventY > line.getY2())? line.getY2() : eventY, (eventX < line.getX1())? line.getX1() : eventX, line.getY2());
				}
				break;
				
			case BOTTOM_RIGHT_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(shape.getX(), shape.getY(), ((eventX < shape.getX())? 0 : eventX - shape.getX()), ((eventY < shape.getY())? 0 : eventY - shape.getY()));

				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine(line.getX1(), line.getY1(), (eventX < line.getX1())? line.getX1() : eventX,  (eventY < line.getY1())? line.getY1() : eventY);
				}
				break;
				
			case BOTTOM_LEFT_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(((eventX > shape.getMaxX())? shape.getMaxX() : eventX), shape.getY(), ((eventX > shape.getMaxX())? 0 : shape.getMaxX() - eventX), ((eventY < shape.getY())? 0 : eventY - shape.getY()));

				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine((eventX > line.getX2())? line.getX2() : eventX, line.getY1(), line.getX2(), (eventY < line.getY1())? line.getY1() : eventY); 
				}
				break;
				
			case LEFT_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(((eventX > shape.getMaxX())? shape.getMaxX() : eventX), shape.getY(), ((eventX > shape.getMaxX())? 0 : shape.getMaxX() - eventX), shape.getHeight());
				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine((eventX > line.getX2())? line.getX2() : eventX, line.getY1(), line.getX2(), line.getY2());
				}
				break;
				
			case RIGHT_CONTROL_POINT:
				if (getShape() instanceof RectangularShape) {
					RectangularShape shape = (RectangularShape) getShape();
					shape.setFrame(shape.getX(), shape.getY(), ((eventX < shape.getX())? 0 : eventX - shape.getX()), shape.getHeight());
				} else if (getShape() instanceof Line2D) {
					Line2D line = (Line2D) getShape();
					line.setLine(line.getX1(), line.getY1(), (eventX < line.getX1())? line.getX1() : eventX, line.getY2());
				}
				break;
			}
			setSelected(true);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#move(double, double)
	 */
	@Override
	public void move(double firstX, double firstY, double relativeX, double relativeY) {
		if (getShape() instanceof RectangularShape) {
			RectangularShape shape = (RectangularShape) getShape();
			shape.setFrame(relativeX - firstX, relativeY - firstY, shape.getWidth(), shape.getHeight());
		} else if (getShape() instanceof Line2D) {
			Line2D line = (Line2D) getShape();
			Rectangle2D r = getShape().getBounds2D();
			r.setFrame(relativeX - firstX, relativeY - firstY, r.getWidth(), r.getHeight());
			line.setLine(r.getX(), r.getY(), r.getX() + r.getWidth(), r.getY() + r.getHeight());
		}
		
		setSelected(true);
	}
}