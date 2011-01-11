package ar.com.tellapic.graphics;

import java.awt.geom.Point2D;

import ar.com.tellapic.Utils;
import ar.com.tellapic.lib.tellapic;

public final class Text extends Tool {
	private Point2D            firstPoint;
	private Drawing            temporalDrawing;
	
	/*TODO: remove singleton for use 1 toolbox per client. 12/10/2010
	private static class TextHolder {
		private static final Text TEXT_INSTANCE = new Text();
	}

	
	public static Text getInstance() {
		return TextHolder.TEXT_INSTANCE;
	}
	*/
	
	public Text() {
		super(tellapic.TOOL_TEXT, Text.class.getSimpleName(), "/icons/text.png", Utils.msg.getString("texttooltip"));
		firstPoint = new Point2D.Double();
		temporalDrawing = new Drawing(getName());
		temporalDrawing.setShape(null);
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		temporalDrawing.setTextX((int) firstPoint.getX());
		temporalDrawing.setTextY((int) firstPoint.getY());
		return temporalDrawing;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getInit()
	 */
	@Override
	public Point2D getInit() {
		return firstPoint;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void init(double x, double y) {
		firstPoint.setLocation(x, y);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#moveTo(double, double)
	 */
	@Override
	public void moveTo(double x, double y) {
		firstPoint.setLocation(x, y);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDraw(double x, double y, boolean simetric) {
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaProperties() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasColorProperties()
	 */
	@Override
	public boolean hasColorProperties() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasFontProperties()
	 */
	@Override
	public boolean hasFontProperties() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeProperties()
	 */
	@Override
	public boolean hasStrokeProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onMove(double, double)
	 */
	@Override
	protected Drawing onMove(double x, double y) {
		firstPoint.setLocation(x, y);
		temporalDrawing.setTextX((int) x);
		temporalDrawing.setTextY((int) y);
		return temporalDrawing;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isOnMoveSupported()
	 */
	@Override
	public boolean isOnMoveSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
	 */
	@Override
	public Drawing onFinishDraw() {
		if (temporalDrawing.getText().length() > 0) {
			temporalDrawing.setTextX((int) firstPoint.getX());
			temporalDrawing.setTextY((int) firstPoint.getY());
			temporalDrawing.cloneProperties();
			return temporalDrawing;
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	protected void onCancel() {
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isLiveModeSupported()
	 */
	@Override
	public boolean isLiveModeSupported() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRestore()
	 */
	@Override
	public void onRestore() {

	}	
}
