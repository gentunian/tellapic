/**
 * 
 */
package ar.com.tellapic.graphics;

import java.awt.RenderingHints;
import java.util.HashMap;

/** 
 * The tool box interface to query the tool box model. Most method
 * implemented here will query the model. To query the model state
 * see {@link ar.com.tellapic.graphics.IToolBoxManager}
 * 
 * @author Sebastian Treu
 *
 */
public interface IToolBoxState {

	/**
	 * The implementor should return the model backed data.
	 * 
	 * @return the list of tools the model has
	 */
	public HashMap<String,Tool> getTools();
		
	/**
	 * Returns the last selected tool, or null if no tools are loaded.
	 * 
	 * @return the last used tool as a Tool object, or null.
	 */
	public Tool getLastUsedTool();
	
	/**
	 * 
	 * @return
	 */
	public PaintPropertyStroke getStrokeProperty();
	
	/**
	 * 
	 * @return
	 */
	public PaintPropertyFont getFontProperty();
	
	/**
	 * 
	 * @return
	 */
	public PaintPropertyAlpha getOpacityProperty();
	
	/**
	 * 
	 * @return
	 */
	public PaintPropertyColor getColorProperty();

	/**
	 * @return
	 */
	public PaintPropertyFill getFillProperty();

	/**
	 * @return
	 */
	public RenderingHints getRenderingHints();

}
