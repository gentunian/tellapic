package ar.com.tellapic.graphics;

import java.awt.Graphics2D;
import java.awt.Shape;

import ar.com.tellapic.AbstractUser;

public class DrawingShape extends AbstractDrawing {
	private PaintPropertyStroke strokeProperty;
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private Shape               shape;

	/**
	 * 
	 * @param name
	 */
	public DrawingShape(String name) {
		shape = null;
		strokeProperty = null;
		colorProperty  = null;
		alphaProperty  = null;
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

			if (shape != null)
				g.draw(shape);
		}
	}
}
