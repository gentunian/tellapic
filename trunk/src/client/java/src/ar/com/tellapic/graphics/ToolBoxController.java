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
		Object[] toolNames = ToolFactory.getRegisteredToolsClassNames().values().toArray();
		for(int i = 0; i < toolNames.length; i++)
			model.addTool(ToolFactory.createTool((String)toolNames[i]));
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IToolBoxController#selectToolEvent(ar.com.tellapic.graphics.Tool)
	 */
	@Override
	public void selectToolByName(String toolName) {
		model.setCurrentTool(toolName);
	}
}
