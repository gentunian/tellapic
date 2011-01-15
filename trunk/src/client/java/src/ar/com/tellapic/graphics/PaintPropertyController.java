package ar.com.tellapic.graphics;

public class PaintPropertyController implements IPaintPropertyController {
	

	private IToolBoxManager        model;
	//private PaintPropertyView      view;
	private DrawingLocalController drawingController; 
	
	public PaintPropertyController(IToolBoxManager model) {
		this.model = model;
		//this.view  = view;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleOpacityChange(int)
	 */
	@Override
	public void handleOpacityChange(float value) {
		if (value < 0f || value > 1f)
			return;
		
		model.setAlphaPropertyValue(value);
		
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleWidthChange(int)
	 */
	@Override
	public void handleWidthChange(int value) {
		// ignore negative widths.
		if (value < 0)
			return;
		
		model.setStrokePropertyWidth(value);
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleEndCapsChange(int)
	 */
	@Override
	public void handleEndCapsChange(int value) {
		model.setStrokePropertyCaps(value);
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleLineJoinsChange(int)
	 */
	@Override
	public void handleLineJoinsChange(int value) {
		model.setStrokePropertyJoins(value);
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontFaceChange(java.lang.String)
	 */
	@Override
	public void handleFontFaceChange(String face) {
		model.setFontPropertyFace(face);
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontSizeChange(int)
	 */
	@Override
	public void handleFontSizeChange(int size) {
		model.setFontPropertySize(size);
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontStyleChange(int)
	 */
	@Override
	public void handleFontStyleChange(int style) {
		model.setFontPropertyStyle(style);
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleTextChange(java.lang.String)
	 */
	@Override
	public void handleTextChange(String text) {
		model.setFontPropertyText(text);
		if (drawingController != null)
			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}
	
	public void setDrawingController(DrawingLocalController c) {
		drawingController = c;
	}
}