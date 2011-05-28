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

import java.awt.Graphics2D;

import ar.com.tellapic.AbstractUser;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingText extends AbstractDrawing {
	private PaintPropertyColor  colorProperty;
	private PaintPropertyAlpha  alphaProperty;
	private PaintPropertyFont   fontProperty;

	private int       textX;
	private int       textY;
	
	/**
	 * 
	 * @param name
	 */
	public DrawingText(String name) {
		colorProperty  = null;
		alphaProperty  = null;
		fontProperty   = null;
		setName(name);
		setVisible(true);
	}
	
	/**
	 * 
	 * @param property
	 */
	public void setFont(PaintPropertyFont property) {
		fontProperty = property;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.AbstractDrawing#draw(java.awt.Graphics2D)
	 */
	@Override
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

			if (overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_FONT] != null) 
				g.setFont(((PaintPropertyFont)overridenProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_FONT]).getFont());
			else if (fontProperty != null)
				g.setFont(getPaintPropertyFont().getFont());
					
			g.drawString(getText(), getTextX(), getTextY());
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
	 * @param paintPropertyAlpha
	 */
	public void setAlpha(PaintPropertyAlpha paintPropertyAlpha) {
		alphaProperty = paintPropertyAlpha;
	}

	/**
	 * @param paintPropertyColor
	 */
	public void setColor(PaintPropertyColor paintPropertyColor) {
		colorProperty = paintPropertyColor;
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
}
