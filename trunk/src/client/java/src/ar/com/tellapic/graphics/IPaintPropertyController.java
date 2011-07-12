/**
 * 
 */
package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.RenderingHints;

import ar.com.tellapic.graphics.PaintPropertyFont.FontStyle;
import ar.com.tellapic.graphics.PaintPropertyStroke.EndCapsType;
import ar.com.tellapic.graphics.PaintPropertyStroke.LineJoinsType;


/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IPaintPropertyController {

	/**
	 * 
	 * @param value
	 */
	public void handleDashChange(float[] dash, float dashphase);
	
	/**
	 * 
	 * @param value
	 */
	public void handleWidthChange(double value);
	
	/**
	 * 
	 * @param value
	 */
	public void handleOpacityChange(double value);
	
	/**
	 * 
	 * @param value
	 */
	public void handleEndCapsChange(EndCapsType value);
	
	/**
	 * 
	 * @param value
	 */
	public void handleLineJoinsChange(LineJoinsType value);
	
	/**
	 * 
	 * @param face
	 */
	public void handleFontFaceChange(String face);
	
	/**
	 * 
	 * @param size
	 */
	public void handleFontSizeChange(float size);
	
	/**
	 * 
	 * @param style
	 */
	public void handleFontStyleChange(FontStyle style);
	
	/**
	 * 
	 * @param text
	 */
	public void handleTextChange(String text);
	
	/**
	 * 
	 * @param color
	 */
	public void handleStrokeColorChange(Color color);
	
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void handleRenderingHint(RenderingHints.Key key, Object value);
	
	/**
	 * @param b
	 */
	public void handleZoomChange(boolean b);

	/**
	 * @param zoom
	 */
	public void handleZoomChange(float zoom);

	/**
	 * @param c
	 */
	public void handleFillColorChange(Color c);
	
}
