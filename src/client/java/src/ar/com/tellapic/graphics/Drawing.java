package ar.com.tellapic.graphics;

import java.awt.Shape;

public class Drawing extends AbstractDrawing implements Cloneable {
	private PaintPropertyStroke strokeProperty;
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private PaintPropertyFont   fontProperty;
//	private PaintPropertyZoom   zoomProperty;
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
		setVisible(true);
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
	public PaintPropertyStroke getPaintPropertyStroke() {
		if (strokeProperty == null)
			return null;
		
		return strokeProperty;
	}
	
	/**
	 * @return the color or null
	 */
	@Override
	public PaintPropertyColor getPaintPropertyColor() {
		if (colorProperty == null)
			return null;
		
		return colorProperty;
	}

	/**
	 * @return the alpha or null
	 */
	@Override
	public PaintPropertyAlpha getPaintPropertyAlpha() {
		if (alphaProperty == null)
			return null;
		
		return alphaProperty;
	}

	/**
	 * @return the font or null
	 */
	@Override
	public PaintPropertyFont getPaintPropertyFont() {
		if (fontProperty == null)
			return null;
		
		return fontProperty;
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
	
	private void cloneProperties() {
		if (hasAlphaProperty())
			alphaProperty  = (PaintPropertyAlpha) alphaProperty.clone();
		
		if (hasStrokeProperty())
			strokeProperty = (PaintPropertyStroke) strokeProperty.clone();
		
		if (hasFontProperty())
			fontProperty   = (PaintPropertyFont) fontProperty.clone();
		
		if (hasColorProperty())
			colorProperty  = (PaintPropertyColor) colorProperty.clone();
		
//		if (hasZoomProperty())
//			zoomProperty = (PaintPropertyZoom) zoomProperty.clone();
	}
	
	
//	@Override
//	public boolean hasZoomProperty() {
//		return zoomProperty != null;
//	}

	@Override
	public boolean hasAlphaProperty() {
		return alphaProperty != null;
	}
	
	@Override
	public boolean hasStrokeProperty() {
		return strokeProperty != null;
	}

	@Override
	public boolean hasColorProperty() {
		return colorProperty != null;
	}

	@Override
	public boolean hasFontProperty() {
		return fontProperty != null;
	}	
	
	
	/**
	 * 
	 * @return
	 */
	public boolean hasShape() {
		return shape != null;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

//	/**
//	 * @param zoomProperty the zoomProperty to set
//	 */
//	public void setPaintPropertyZoom(PaintPropertyZoom zoomProperty) {
//		this.zoomProperty = zoomProperty;
//	}
//
//	/**
//	 * @return the zoomProperty
//	 */
//	public PaintPropertyZoom getPaintPropertyZoom() {
//		return zoomProperty;
//	}

	
	@Override
	public Object clone() {
		try {
			cloneProperties();
			return super.clone();
		}
		catch (CloneNotSupportedException e) {
			// This should never happen
			throw new InternalError(e.toString());
		}
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
