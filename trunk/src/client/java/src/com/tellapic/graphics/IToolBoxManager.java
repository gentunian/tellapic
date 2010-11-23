/**
 * 
 */
package com.tellapic.graphics;


/**
 * The tool box interface to manage the tool box model. Most method
 * implemented here will modify the model. To query the model state
 * see {@link com.tellapic.graphics.IToolBoxState}
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IToolBoxManager {

	/**
	 * 
	 * @param tool
	 */
	public void enableTool(Tool tool);
	
	/**
	 * 
	 * @param tool
	 */
	public void disableTool(Tool tool);
	
	/**
	 * Adds a tool to the model list of tools
	 * 
	 * @param tool the tool to be added
	 * @throws IllegalArgumentException if tool is null
	 */
	public void addTool(Tool tool) throws IllegalArgumentException;	
	
	
	/**
	 * Sets tool to be the current used tool.
	 * 
	 * @param tool the tool to be selected as the current tool.
	 * @throws IllegalArgumentException if tool is null
	 */
	public void setCurrentTool(Tool tool) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param toolName
	 * @throws IllegalArgumentException
	 */
	public void setCurrentTool(String toolName) throws IllegalArgumentException;
	
	/**
	 * Sets the toolType as the current tool. Only 1 type of tool is allowed and
	 * every Tool is a singleton.
	 * 
	 * @param toolType the ToolType to be selected as the current tool.
	 * @throws NoSuchElementException if toolType is not found
	 */
	//public void setCurrentTool(Tool.ToolType toolType) throws NoSuchElementException;
	
	/**
	 * Sets the stroke width
	 * @param width the width to set
	 */
	public void setStrokePropertyWidth(float width);
	
	/**
	 * Sets the end caps to use
	 * @param cap the cap to use. Could be one of CAP_SQUARE, CAP_ROUND or CAP_BUTT
	 * @throws IllegalArgumentException if cap is none of the above values
	 */
	public void setStrokePropertyCaps(int cap) throws IllegalArgumentException;
	
	/**
	 * Sets the line joins to use
	 * @param join the line joins to use. Could be one of BEVEL_JOIN, MITER_JOIN, and ROUND_JOIN
	 * @throws IllegalArgumentException if join is none of the above values.
	 */
	public void setStrokePropertyJoins(int join) throws IllegalArgumentException;
	
	/**
	 * 
	 * @param dash the dash array
	 * @param dashPhase the dash phase
	 */
	public void setStrokePropertyDash(float[] dash, float dashPhase);
	
	/**
	 * 
	 * @param width
	 */
	public void setStrokePropertyMiterLimit(float width);
	
	/**
	 * 
	 * @param face
	 */
	public void setFontPropertyFace(String face);
	
	/**
	 * 
	 * @param size
	 */
	public void setFontPropertySize(int size);
	
	/**
	 * 
	 * @param text
	 */
	public void setFontPropertyText(String text);
	
	/**
	 * 
	 * @param style
	 */
	public void setFontPropertyStyle(int style);
	
	/**
	 * 
	 * @param value
	 */
	public void setAlphaPropertyValue(float value);
	
	/**
	 * Tools must be registered in the model
	 * @param toolId
	 */
	public void registerTool(int toolId);
}
