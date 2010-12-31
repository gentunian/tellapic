package ar.com.tellapic.graphics;

import java.awt.Color;


public class PaintPropertyColor extends PaintProperty {
	
	public PaintPropertyColor() {
		super(PaintPropertyType.COLOR);
	}
	
	/*
	private static class PaintPropertyColorHolder {
		private static final PaintPropertyColor INSTANCE = new PaintPropertyColor();
	}

	public static PaintPropertyColor getInstance() {
		return PaintPropertyColorHolder.INSTANCE;
	}
	*/
	
	public Color getColor() {
		//TODO: complete
		return Color.black;
	}
}
