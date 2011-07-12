package ar.com.tellapic.graphics;

import java.awt.AlphaComposite;
import java.awt.Composite;

import ar.com.tellapic.utils.Utils;

public class PaintPropertyAlpha extends PaintProperty {
		
	public static final String[] CLI_CMDS = new String[] { 
		"ar.com.tellapic.graphics.AbstractDrawing setAlpha({float_alpha_A_value_between_0.0_and_1.0_describing_the_opacity_being_0.5_==_50%_f.i.}) Sets this shape transparency value from 0.0 to 1.0."
	};
	
	private float alpha;
	
	/**
	 * 
	 * @param a
	 */
	public PaintPropertyAlpha(float a) {
		super(PaintPropertyType.ALPHA);
		alpha = a;
	}
	
	/**
	 * 
	 */
	public PaintPropertyAlpha() {
		super(PaintPropertyType.ALPHA);
		alpha = 1.0f;
	}
	
	/**
	 * 
	 * @param alpha
	 */
	public void setAlpha(String alpha) {
		try {
			this.alpha = Float.valueOf(alpha);
		} catch(Exception e) {
			Utils.logMessage("Wrong format. Setting alpha value to 1.0.");
			setAlpha(1);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public float getAlpha() {
		return alpha;
	}
	
	/**
	 * 
	 * @return
	 */
	public Composite getComposite() {
		return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
	}

	/**
	 * @param value
	 */
	public void setAlpha(float value) {
		this.alpha = value;
	}
}
