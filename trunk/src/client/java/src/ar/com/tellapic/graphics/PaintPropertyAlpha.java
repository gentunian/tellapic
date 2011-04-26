package ar.com.tellapic.graphics;

import java.awt.AlphaComposite;
import java.awt.Composite;

public class PaintPropertyAlpha extends PaintProperty {
		
	//TODO: private field
	public float alpha;
	
	
	public PaintPropertyAlpha(float a) {
		super(PaintPropertyType.ALPHA);
		alpha = a;
	}
	
	
	public PaintPropertyAlpha() {
		super(PaintPropertyType.ALPHA);
		alpha = 1.0f;
	}
	
	/*
	private static class PaintPropertyAlphaHolder {
		private static final PaintPropertyAlpha INSTANCE = new PaintPropertyAlpha();
	}
	
	public static PaintPropertyAlpha getInstance() {
		return PaintPropertyAlphaHolder.INSTANCE;
	}
	*/
	
	public Composite getComposite() {
		return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}
}
