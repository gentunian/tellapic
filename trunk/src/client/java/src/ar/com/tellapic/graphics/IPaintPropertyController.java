/**
 * 
 */
package ar.com.tellapic.graphics;

import java.awt.Color;


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
	public void handleEndCapsChange(int value);
	
	/**
	 * 
	 * @param value
	 */
	public void handleLineJoinsChange(int value);
	
	/**
	 * 
	 * @param face
	 */
	public void handleFontFaceChange(String face);
	
	/**
	 * 
	 * @param size
	 */
	public void handleFontSizeChange(int size);
	
	/**
	 * 
	 * @param style
	 */
	public void handleFontStyleChange(int style);
	
	/**
	 * 
	 * @param text
	 */
	public void handleTextChange(String text);
	
	/**
	 * 
	 * @param color
	 */
	public void handleColorChange(Color color);
	
}
