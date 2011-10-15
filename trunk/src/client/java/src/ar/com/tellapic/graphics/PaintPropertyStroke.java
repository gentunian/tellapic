package ar.com.tellapic.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import ar.com.tellapic.utils.Utils;

public final class PaintPropertyStroke extends PaintProperty {
	public enum LineJoinsType {
		JOIN_MITER,
		JOIN_ROUND,
		JOIN_BEVEL
	};
	
	public enum EndCapsType {
		CAP_BUTT,
		CAP_ROUND,
		CAP_SQUARE
	};
	
	
	public static final String[] CLI_CMDS = new String[] {
		"ar.com.tellapic.graphics.AbstractDrawing setColor({String_color_The_color_stroke}) Sets this shape stroke color.",
		"ar.com.tellapic.graphics.AbstractDrawing setTransparency({float_alpha_The_alpha_value}) Sets this shape stroke transparency value. Must be a value between 0.0 and 1.0.",
		"ar.com.tellapic.graphics.AbstractDrawing setWidth({float_width_The_width_size}) Sets this shape stroke width size.",
		"ar.com.tellapic.graphics.AbstractDrawing setMiterLimit({float_ml_The_miter_limit,_a_value_greater_than_1.0}) Sets this shape stroke miter limit.",
		"ar.com.tellapic.graphics.AbstractDrawing setLineJoins({int_lj_Line_joins._Possible_values_are:_0,_1,_2_for_Miter,_Round,_Bevel}) Sets this shape stroke line joins.",
		"ar.com.tellapic.graphics.AbstractDrawing setEndCaps({int_ec_End_caps._Possible_values_are:_0,_1,_2_for_Butt,_Round,_Square}) Sets this shape stroke end caps."
	};
	
	
	private Color         color;
	private int           alpha;
	private double        width;
	private EndCapsType   endCaps;
	private LineJoinsType lineJoins;
	private float         miterLimit;
	private float         dash[];
	private float         dash_phase;


	private StrokeProvider strokeProvider;
	
	public PaintPropertyStroke() {
		super(PaintPropertyType.STROKE);
		alpha        = -1;
		color        = Color.white;
		width        = 1;
		endCaps      = EndCapsType.CAP_SQUARE;
		lineJoins    = LineJoinsType.JOIN_MITER;
		miterLimit   = 10;
		dash_phase   = 0;
		/* Alternate entries in the array represent the user space lengths of the opaque and
		 * transparent segments of the dashes. As the pen moves along the outline of the Shape
		 * to be stroked, the user space distance that the pen travels is accumulated.
		 * The distance value is used to index into the dash array.
		 * The pen is opaque when its current cumulative distance maps to an even element of
		 * the dash array and transparent otherwise. */
		dash = new float[]{ 1, 0, 1, 0}; 
	}
	
	/**
	 * 
	 * @param alpha
	 */
	public void setTransparency(int alpha) {
		if (alpha < 0 || alpha > 255)
			throw new IllegalArgumentException("Transparency value must be between 0 and 255");
		this.alpha = alpha;
		setColor(color);
	}
	
	/**
	 * 
	 * @param alpha
	 */
	public void setTransparency(String alpha) {
		try {
			setTransparency(Integer.parseInt(alpha));
		} catch (Exception e) {
			Utils.logMessage("Wrong format. Setting stroke transparency to 255");
			setTransparency(255);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public int getTransparency() {
		return this.alpha;
	}
	
	/**
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		if (alpha != -1) {
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
		}
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
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}
	
	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		if (width < 0)
			throw new IllegalArgumentException("Width cannot be a negative value.");
		this.width = width;
	}
	
	/**
	 * @param width the width to set
	 */
	public void setWidth(String width) {
		try {
			setWidth(Double.valueOf(width));
		} catch(Exception e) {
			Utils.logMessage("Wrong format. Setting stroke width to 5.0");
			setWidth(5.0);
		}
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
	public void setEndCaps(EndCapsType endCaps) {
		this.endCaps = endCaps;
	}

	/**
	 * @param endCaps the endCaps to set
	 */
	public void setEndCaps(String endCaps) {
		for (EndCapsType ecType : EndCapsType.values()) {
			if (ecType.toString().equals(endCaps)) {
				setEndCaps(ecType);
				return;
			}
		}
		Utils.logMessage("Wrong format. Setting end caps to "+EndCapsType.CAP_SQUARE.toString()+".");
		setEndCaps(EndCapsType.CAP_SQUARE);
	}
	
	/**
	 * @return the endCaps
	 */
	public EndCapsType getEndCaps() {
		return endCaps;
	}


	/**
	 * @param lineJoins the lineJoins to set
	 */
	public void setLineJoins(LineJoinsType lineJoins) {
		this.lineJoins = lineJoins;
	}

	/**
	 * 
	 * @param lineJoins
	 */
	public void setLineJoins(String lineJoins) {
		for (LineJoinsType ljType : LineJoinsType.values()) {
			if (ljType.toString().equals(lineJoins)) {
				setLineJoins(ljType);
				return;
			}
		}
		
		Utils.logMessage("Wrong format. Setting line joins to "+LineJoinsType.JOIN_MITER.toString()+".");
		setLineJoins(LineJoinsType.JOIN_MITER);
	}
	
	/**
	 * @return the lineJoins
	 */
	public LineJoinsType getLineJoins() {
		return lineJoins;
	}

	/**
	 * @param miterLimit the miterLimit to set
	 */
	public void setMiterLimit(float miterLimit) {
		if (miterLimit < 1) 
			throw new IllegalArgumentException("Miter limit must be >= 1.0f");
		this.miterLimit = miterLimit;
	}

	/**
	 * @param miterLimit the miterLimit to set
	 */
	public void setMiterLimit(String miterLimit) {
		try {
			setMiterLimit(Float.valueOf(miterLimit));
		} catch(Exception e) {
			Utils.logMessage("Wrong format. Setting miter limit to 10.0.");
			setMiterLimit(10.0f);
		}
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
	 * @param strokeProvider
	 */
	public void setStrokeProvider(StrokeProvider strokeProvider) {
		this.strokeProvider = strokeProvider;
	}
	
	/**
	 * 
	 * @return
	 */
	public Stroke getStroke() {
		float[] newdash = null;
		if (dash[0] != 0 && dash[1] != 0)
			newdash = dash;
		
		if (strokeProvider != null)
			return strokeProvider.buildStroke((float) width, endCaps.ordinal(), lineJoins.ordinal(), miterLimit, newdash, dash_phase);
		else
			return new BasicStroke((float) width, endCaps.ordinal(), lineJoins.ordinal(), miterLimit, newdash, dash_phase);
	}
}
