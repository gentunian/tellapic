 package com.tellapic.graphics;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import com.tellapic.Utils;

/**
 * 
 * @author seba
 *
 */
public final class Line extends Tool {
	private static final double ANGLE = 15;
	private static final double STEP = (ANGLE * Math.PI) / 180;
	
	private Point2D             firstPoint;
	private Line2D              line;
	private Drawing             temporalDrawing;
	private boolean             inUse;

	/*TODO: remove singleton for use 1 toolbox per client. 12/10/2010
	private static class LineHolder {
		private static final Line LINE_INSTANCE = new Line();
	}
	
	
	public static Line getInstance() {
		return LineHolder.LINE_INSTANCE;
	}
	*/
	
	public Line() {
		super(Line.class.getSimpleName(), "/icons/line.png", Utils.msg.getString("linetooltip"));
		firstPoint = new Point2D.Double();
		inUse      = false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		if (inUse) {
			temporalDrawing.setShape(line);
			return temporalDrawing;
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#getInit()
	 */
	@Override
	public Point2D getInit() {
		return firstPoint;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void init(double x, double y) {
		if (inUse)
			throw new IllegalStateException("init cannot be called with the tool being used");
		
		firstPoint.setLocation(x, y);
		line            = new Line2D.Double(firstPoint, firstPoint);
		temporalDrawing = new Drawing(getName());
		inUse = true;
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
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDraw(double x, double y, boolean symmetric) {
		if (inUse) {
			double angle = Math.atan2(x - firstPoint.getX(), y - firstPoint.getY()) + Math.PI/2;
			if ( angle < 0)
				angle = (Math.PI - Math.abs(angle)) + Math.PI;

			int newX = (int) x;
			int newY = (int) y;

			if (symmetric) {
				double steppedAngle = (Math.round(angle / STEP) * STEP);
				double dist = firstPoint.distance(x, y);
				newX = (int) (dist * Math.cos(steppedAngle - Math.PI) + firstPoint.getX());
				newY = (int) (dist * Math.sin(steppedAngle) + firstPoint.getY());
			} 

			line.setLine(firstPoint.getX(), firstPoint.getY(), newX, newY);
		}
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaProperties() {
		return true;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#hasColorProperties()
	 */
	@Override
	public boolean hasColorProperties() {
		return true;
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
		return true;
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
		return inUse;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#onFinishDraw()
	 */
	@Override
	public Drawing onFinishDraw() {
		if (inUse && line.getP1().distance(line.getP2()) > 0.0) {
			temporalDrawing.setShape(line);
			temporalDrawing.cloneProperties();
			inUse = false;
			return temporalDrawing;
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	protected void onCancel() {
		inUse = false;
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
		inUse = true;
	}
}
