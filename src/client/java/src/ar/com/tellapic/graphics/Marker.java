package ar.com.tellapic.graphics;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import ar.com.tellapic.Utils;

public final class Marker extends Tool {
	private Line2D              line;
	private Point2D             firstPoint;
	private Drawing             temporalDrawing;
	private boolean             inUse;

	/*TODO: remove singleton for use 1 toolbox per client. 12/10/2010
	private static class MarkerHolder {
		private static final Marker MARKER_INSTANCE = new Marker();
	}
	
	
	public static Marker getInstance() {
		return MarkerHolder.MARKER_INSTANCE;
	}
	*/
	
	public Marker() {
		super(Marker.class.getSimpleName(), "/icons/marker.png", Utils.msg.getString("markertooltip"));
		firstPoint = new Point2D.Double();
		inUse = false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getDrawing()
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
		line  = new Line2D.Double(firstPoint, firstPoint);
		temporalDrawing = new Drawing(getName());
		inUse = true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#moveTo(double, double)
	 */
	@Override
	public void moveTo(double x, double y) {
		//TODO:
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
	public void onDraw(double x, double y, boolean symmetric) {
		if (inUse)
			if (symmetric)
				line.setLine(firstPoint.getX(), firstPoint.getY(), firstPoint.getX(), y);
			else
				line.setLine(firstPoint.getX(), firstPoint.getY(), x, firstPoint.getY());
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
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeProperties()
	 */
	@Override
	public boolean hasStrokeProperties() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onMove(double, double)
	 */
	@Override
	protected Drawing onMove(double x, double y) {
		return null;
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
		return inUse;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
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
	 * @see ar.com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	protected void onCancel() {
		inUse = false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isLiveModeSupported()
	 */
	@Override
	public boolean isLiveModeSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRestore()
	 */
	@Override
	public void onRestore() {
		inUse = true;
	}
}
