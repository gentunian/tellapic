/**
 * 
 */
package ar.com.tellapic.graphics;

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
		
		for(String toolName : ToolFactory.getRegisteredToolNames()) 
			model.addTool(ToolFactory.createTool(toolName));
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolViewController#selectToolEvent(ar.com.tellapic.graphics.Tool)
	 */
	@Override
	public void selectToolEvent(String toolName) {
		model.setCurrentTool(toolName);
	}	
}
