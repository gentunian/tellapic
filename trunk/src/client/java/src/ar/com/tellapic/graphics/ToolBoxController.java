/**
 * 
 */
package ar.com.tellapic.graphics;

import java.util.Map;

/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ToolBoxController implements IToolBoxController {
	
	private IToolBoxManager model;

	
	@SuppressWarnings("unused")
	private ToolBoxController() {}

		
	/**
	 * 
	 * @param model
	 * @param view
	 */
	public ToolBoxController(IToolBoxManager model) {		
		this.model = model;
		
		for(String toolName : ToolFactory.getRegisteredToolNames().values()) 
			model.addTool(ToolFactory.createTool(toolName));
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxController#selectToolEvent(ar.com.tellapic.graphics.Tool)
	 */
	@Override
	public void selectToolByName(String toolName) {
		model.setCurrentTool(toolName);
	}
}
