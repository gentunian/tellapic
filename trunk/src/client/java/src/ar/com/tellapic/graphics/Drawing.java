package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;

public class Drawing extends AbstractDrawing {
	private PaintPropertyStroke strokeProperty;
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private PaintPropertyFont   fontProperty;
	private Shape               shape;
	
	//private boolean   notDrawnYet;
	private String    name;
	private int       textX;
	private int       textY;
	
	
	public Drawing(String name) {
		this.name = name;
		shape = null;
		strokeProperty = null;
		colorProperty  = null;
		alphaProperty  = null;
		fontProperty   = null;
		//notDrawnYet    = true;
	}
	
	/**
	 * 
	 * @param x
	 */
	public void setTextX(int x) {
		textX = x;
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
	}
	
	/**
	 * @return the textY
	 */
	public int getTextY() {
		return textY;
	}

	/**
	 * @return the text
	 */
	@Override
	public String getText() {
		return fontProperty.getText();
	}
	
	/**
	 * @param shape the shape to set
	 */
	@Override
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	/**
	 * @return the shape
	 */
	@Override
	public Shape getShape() {
		return shape;
	}
	
	/**
	 * @return the stroke or null
	 */
	@Override
	public Stroke getStroke() {
		if (strokeProperty == null)
			return null;
		
		return strokeProperty.getStroke();
	}
	
	/**
	 * @return the color or null
	 */
	@Override
	public Color getColor() {
		if (colorProperty == null)
			return null;
		
		return colorProperty.getColor();
	}

	/**
	 * @return the alpha or null
	 */
	@Override
	public Composite getAlpha() {
		if (alphaProperty == null)
			return null;
		
		return alphaProperty.getComposite();
	}

	/**
	 * @return the font or null
	 */
	@Override
	public Font getFont() {
		if (fontProperty == null)
			return null;
		
		return fontProperty.getFont();
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

	public void setFont(PaintPropertyFont property) {
		fontProperty = property;
	}
	
	public void cloneProperties() {
		if (hasAlphaProperty())
			alphaProperty  = (PaintPropertyAlpha) alphaProperty.clone();
		
		if (hasStrokeProperty())
			strokeProperty = (PaintPropertyStroke) strokeProperty.clone();
		
		if (hasFontProperty())
			fontProperty   = (PaintPropertyFont) fontProperty.clone();
		
		if (hasColorProperty())
			colorProperty  = (PaintPropertyColor) colorProperty.clone();
	}
	
	public boolean hasAlphaProperty() {
		return alphaProperty != null;
	}
	
	public boolean hasStrokeProperty() {
		return strokeProperty != null;
	}

	public boolean hasColorProperty() {
		return colorProperty != null;
	}

	public boolean hasFontProperty() {
		return fontProperty != null;
	}	
	
	public boolean hasShape() {
		return shape != null;
	}
	
	public String toString() {
		return name;
	}

	/**
	 * @param notDrawnYet the notDrawnYet to set
	 */
//	public void setNotDrawnYet(boolean notDrawnYet) {
//		this.notDrawnYet = notDrawnYet;
//	}

	/**
	 * @return the notDrawnYet
	 */
//	public boolean isNotDrawnYet() {
//		return notDrawnYet;
//	}
}
