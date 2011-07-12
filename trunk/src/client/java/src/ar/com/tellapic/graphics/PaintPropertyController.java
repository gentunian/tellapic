package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.RenderingHints.Key;

import ar.com.tellapic.graphics.PaintPropertyFont.FontStyle;
import ar.com.tellapic.graphics.PaintPropertyStroke.EndCapsType;
import ar.com.tellapic.graphics.PaintPropertyStroke.LineJoinsType;

public class PaintPropertyController implements IPaintPropertyController {
	
	private IToolBoxManager        model;
	
	/**
	 * 
	 * @param model
	 */
	public PaintPropertyController(IToolBoxManager model) {
		this.model = model;
		//this.view  = view;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleOpacityChange(int)
	 */
	@Override
	public void handleOpacityChange(double value) {
		if (value <= 0 || value > 1)
			return;
		
		model.setAlphaPropertyValue(value);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleWidthChange(int)
	 */
	@Override
	public void handleWidthChange(double value) {
		// ignore negative widths.
		if (value < 0)
			return;
		
		model.setStrokePropertyWidth(value);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleEndCapsChange(int)
	 */
	@Override
	public void handleEndCapsChange(EndCapsType value) {
		model.setStrokePropertyCaps(value);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleLineJoinsChange(int)
	 */
	@Override
	public void handleLineJoinsChange(LineJoinsType value) {
		model.setStrokePropertyJoins(value);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontFaceChange(java.lang.String)
	 */
	@Override
	public void handleFontFaceChange(String face) {
		model.setFontPropertyFace(face);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontSizeChange(int)
	 */
	@Override
	public void handleFontSizeChange(float size) {
		model.setFontPropertySize(size);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontStyleChange(int)
	 */
	@Override
	public void handleFontStyleChange(FontStyle style) {
		model.setFontPropertyStyle(style);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleTextChange(java.lang.String)
	 */
	@Override
	public void handleTextChange(String text) {
		model.setFontPropertyText(text);
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleColorChange(java.awt.Color)
	 */
	@Override
	public void handleStrokeColorChange(Color color) {
		model.setStrokePropertyColorValue(color);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleDashChange(float[])
	 */
	@Override
	public void handleDashChange(float[] dash, float dashphase) {
		model.setStrokePropertyDash(dash, dashphase);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
	 */
	@Override
	public void handleRenderingHint(Key key, Object value) {
		if (value != null)
			model.addRenderingHint(key, value);
		else
			model.removeRenderingHint(key);
	}

	
	/* @see ar.com.tellapic.graphics.IPaintPropertyController#handleZoomChange(boolean)
	 */
	@Override
	public void handleZoomChange(boolean value) {
		ControlToolZoom.getInstance().setZoomIn(value);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleZoomChange(double)
	 */
	@Override
	public void handleZoomChange(float value) {
		ControlToolZoom.getInstance().setZoom(value);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFillColorChange(java.awt.Color)
	 */
	@Override
	public void handleFillColorChange(Color c) {
		model.setFillPropertyColor(c);
	}
}
