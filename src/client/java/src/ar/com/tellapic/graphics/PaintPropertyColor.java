package ar.com.tellapic.graphics;

import java.awt.Color;


public class PaintPropertyColor extends PaintProperty {
	
	public static final String[] CLI_CMDS = new String[] { 
		"ar.com.tellapic.graphics.AbstractDrawing setColor({number_colorHexa_The_color_in_hexadecimal,_e.g._0xffaabb}) Sets this shape color in an hexadecimal fashion."
	};
	
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
		this.color = Color.black;
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
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "0x" + Integer.toHexString(color.getRed()) + Integer.toHexString(color.getGreen()) + Integer.toHexString(color.getBlue());
	}
}
