package ar.com.tellapic.graphics;

import java.awt.Color;

public class PaintPropertyController implements IPaintPropertyController {
	

	private IToolBoxManager        model;
	//private PaintPropertyView      view;
//	private DrawingLocalController drawingController; 
	
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
		
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
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
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleEndCapsChange(int)
	 */
	@Override
	public void handleEndCapsChange(int value) {
		model.setStrokePropertyCaps(value);
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleLineJoinsChange(int)
	 */
	@Override
	public void handleLineJoinsChange(int value) {
		model.setStrokePropertyJoins(value);
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontFaceChange(java.lang.String)
	 */
	@Override
	public void handleFontFaceChange(String face) {
		model.setFontPropertyFace(face);
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontSizeChange(int)
	 */
	@Override
	public void handleFontSizeChange(int size) {
		model.setFontPropertySize(size);
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleFontStyleChange(int)
	 */
	@Override
	public void handleFontStyleChange(int style) {
		model.setFontPropertyStyle(style);
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleTextChange(java.lang.String)
	 */
	@Override
	public void handleTextChange(String text) {
		model.setFontPropertyText(text);
//		if (drawingController != null)
//			drawingController.updateFromOutside(((ToolBoxModel)model).getLastUsedTool().getDrawing());
	}
	
//	public void setDrawingController(DrawingLocalController c) {
//		drawingController = c;
//	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleColorChange(java.awt.Color)
	 */
	@Override
	public void handleColorChange(Color color) {
		model.setColorPropertyValue(color);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleDashChange(float[])
	 */
	@Override
	public void handleDashChange(float[] dash, float dashphase) {
		model.setStrokePropertyDash(dash, dashphase);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleZoomChange(boolean)
	 */
	@Override
	public void handleZoomChange(boolean b) {
		model.setZoomIn(b);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.IPaintPropertyController#handleZoomChange(double)
	 */
	@Override
	public void handleZoomChange(float zoom) {
		model.setZoomValue(zoom);
	}
}