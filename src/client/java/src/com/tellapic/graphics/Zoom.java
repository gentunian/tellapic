package com.tellapic.graphics;

import java.awt.geom.Point2D;

import com.tellapic.Utils;

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
		super(Zoom.class.getSimpleName(), "/icons/zoom.png", Utils.msg.getString("zoomtooltip"));
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#getInit()
	 */
	@Override
	public Point2D getInit() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void init(double x, double y) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#moveTo(double, double)
	 */
	@Override
	public void moveTo(double x, double y) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDraw(double x, double y, boolean simetric) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#hasColorProperties()
	 */
	@Override
	public boolean hasColorProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#hasFontProperties()
	 */
	@Override
	public boolean hasFontProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#hasStrokeProperties()
	 */
	@Override
	public boolean hasStrokeProperties() {
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#onMove(double, double)
	 */
	@Override
	protected Drawing onMove(double x, double y) {
		return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#isOnMoveSupported()
	 */
	@Override
	public boolean isOnMoveSupported() {
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#onFinishDraw()
	 */
	@Override
	public Drawing onFinishDraw() {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	protected void onCancel() {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#isLiveModeSupported()
	 */
	@Override
	public boolean isLiveModeSupported() {
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#onRestore()
	 */
	@Override
	public void onRestore() {
		// TODO Auto-generated method stub
		
	}
}
