package com.tellapic.graphics;

import java.awt.BasicStroke;
import java.awt.Stroke;

public final class PaintPropertyStroke extends PaintProperty {
	private float width;
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
	}
	/*
	private static class PaintPropertyStrokeHolder {
		private static final PaintPropertyStroke INSTANCE = new PaintPropertyStroke();
	}
	
	public static PaintPropertyStroke getInstance() {
		return PaintPropertyStrokeHolder.INSTANCE;
	}
	*/
	
	/**
	 * @param width the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}


	/**
	 * @return the width
	 */
	public float getWidth() {
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
		return new BasicStroke(width, endCaps, lineJoins, miterLimit, dash, dash_phase);
	}
}
