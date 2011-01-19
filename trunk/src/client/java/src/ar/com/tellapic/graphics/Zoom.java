package ar.com.tellapic.graphics;

import java.awt.geom.Point2D;

import ar.com.tellapic.utils.Utils;

public final class Zoom extends Tool {
	/*TODO: remove singleton for use 1 toolbox per client. 12/10/2010
	private static class ZoomHolder {
		private static final Zoom ZOOM_INSTANCE = new Zoom();
	}
	
	
	public static Zoom getInstance() {
		return ZoomHolder.ZOOM_INSTANCE;
	}
	*/
	
	public Zoom() {
		super(99, Zoom.class.getSimpleName(), "/icons/zoom.png", Utils.msg.getString("zoomtooltip"));
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getInit()
	 */
	@Override
	public Point2D getInit() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void onPress(int x, int y, int button, int mask) {
		// TODO Auto-generated method stub
		
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDrag(int x, int y, int button, int mask) {
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#moveTo(double, double)
	 */
	@Override
	public void moveTo(double x, double y) {
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onMove(double, double)
	 */
	@Override
	public Drawing onMove(int x, int y) {
		return null;
	}
	
	
	/*
	 * non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
	 */
	@Override
	public Drawing onRelease(int x, int y, int button, int mask) {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	public void onCancel() {
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRestore()
	 */
	@Override
	public void onRestore() {
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasColorProperties()
	 */
	@Override
	public boolean hasColorProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasFontProperties()
	 */
	@Override
	public boolean hasFontProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeProperties()
	 */
	@Override
	public boolean hasStrokeProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isOnMoveSupported()
	 */
	@Override
	public boolean isOnMoveSupported() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isLiveModeSupported()
	 */
	@Override
	public boolean isLiveModeSupported() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setAlpha(ar.com.tellapic.graphics.PaintPropertyAlpha)
	 */
	@Override
	public void setAlpha(PaintPropertyAlpha alpha) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setColor(ar.com.tellapic.graphics.PaintPropertyColor)
	 */
	@Override
	public void setColor(PaintPropertyColor color) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setFont(ar.com.tellapic.graphics.PaintPropertyFont)
	 */
	@Override
	public void setFont(PaintPropertyFont font) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setStroke(ar.com.tellapic.graphics.PaintPropertyStroke)
	 */
	@Override
	public void setStroke(PaintPropertyStroke stroke) {
		// TODO Auto-generated method stub
		
	}
}
