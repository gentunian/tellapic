package ar.com.tellapic.graphics;

import java.awt.BasicStroke;
import java.awt.Stroke;

public final class PaintPropertyStroke extends PaintProperty {
	private double width;
	private int   endCaps;
	private int   lineJoins;
	private float miterLimit;
	private float dash[];
	private float dash_phase;
	
	public PaintPropertyStroke() {
		super(PaintPropertyType.STROKE);
		width       = 1f;
		endCaps     = BasicStroke.CAP_SQUARE;
		lineJoins   = BasicStroke.JOIN_MITER;
		miterLimit = 10f;
		dash_phase  = 0f;
		/* Alternate entries in the array represent the user space lengths of the opaque and
		 * transparent segments of the dashes. As the pen moves along the outline of the Shape
		 * to be stroked, the user space distance that the pen travels is accumulated.
		 * The distance value is used to index into the dash array.
		 * The pen is opaque when its current cumulative distance maps to an even element of
		 * the dash array and transparent otherwise. */
		dash = new float[]{ 0, 0}; 
	}
	
	
	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}
	
	
	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}


	/**
	 * @param endCaps the endCaps to set
	 */
	public void setEndCaps(int endCaps) {
		this.endCaps = endCaps;
	}


	/**
	 * @return the endCaps
	 */
	public int getEndCaps() {
		return endCaps;
	}


	/**
	 * @param lineJoins the lineJoins to set
	 */
	public void setLineJoins(int lineJoins) {
		this.lineJoins = lineJoins;
	}


	/**
	 * @return the lineJoins
	 */
	public int getLineJoins() {
		return lineJoins;
	}


	/**
	 * @param miterLimit the miterLimit to set
	 */
	public void setMiterLimit(float miterLimit) {
		this.miterLimit = miterLimit;
	}


	/**
	 * @return the mitterLimit
	 */
	public float getMiterLimit() {
		return miterLimit;
	}


	/**
	 * @param dash the dash to set
	 */
	public void setDash(float dash[]) {
		this.dash = dash;
	}


	/**
	 * @return the dash
	 */
	public float[] getDash() {
		return dash;
	}


	/**
	 * @param dash_phase the dash_phase to set
	 */
	public void setDash_phase(float dash_phase) {
		this.dash_phase = dash_phase;
	}


	/**
	 * @return the dash_phase
	 */
	public float getDash_phase() {
		return dash_phase;
	}


	/**
	 * 
	 * @return
	 */
	public Stroke getStroke() {
		float[] newdash = null;
		if (dash[0] != 0 && dash[1] != 0)
			newdash = dash;
		
		return new BasicStroke((float) width, endCaps, lineJoins, miterLimit, newdash, dash_phase);
	}
}
