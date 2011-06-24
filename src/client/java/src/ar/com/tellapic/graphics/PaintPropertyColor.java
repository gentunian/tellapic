package ar.com.tellapic.graphics;

import java.awt.Color;


public class PaintPropertyColor extends PaintProperty {
	
	private Color color;
	
	public PaintPropertyColor(String color) {
		super(PaintPropertyType.COLOR);
		this.color = Color.decode(color);
	}
	
	public PaintPropertyColor(Color color) {
		super(PaintPropertyType.COLOR);
		this.color = color;
	}
	
	public PaintPropertyColor() {
		super(PaintPropertyType.COLOR);
	}
	
	public Color getColor() {
		return color;
	}

	/**
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * 
	 * @param color
	 */
	public void setColor(String color) {
		this.color = Color.decode(color);
	}
	
	/**
	 * @return
	 */
	public int getRed() {
		return color.getRed();
	}

	/**
	 * @return
	 */
	public int getGreen() {
		return color.getGreen();
	}

	/**
	 * @return
	 */
	public int getBlue() {
		return color.getBlue();
	}
}
