package com.tellapic.graphics;

import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.tellapic.Utils;

public final class Ellipse extends Tool {
	private Ellipse2D           ellipse;
	private Point2D             firstPoint;
	private Dimension           size;
	private Drawing             temporalDrawing;
	private boolean             inUse;

	/*TODO: remove singleton for use 1 toolbox per client. 12/10/2010
	private static class EllipseHolder {
		private static final Ellipse ELLIPSE_INSTANCE = new Ellipse();	
	}
		
	public static Ellipse getInstance() {
		return EllipseHolder.ELLIPSE_INSTANCE;
	}
	*/
	
	public Ellipse() {
		super(Ellipse.class.getSimpleName(), "/icons/ellipse.png", Utils.msg.getString("ellipsetooltip"));
		firstPoint = new Point2D.Double();
		inUse      = false;
	}


	@Override
	public Drawing getDrawing() {
		if (inUse) {
			temporalDrawing.setShape(ellipse);
			return temporalDrawing;
		}
		
		return null;
	}

	
	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void init(double x, double y) {
		firstPoint.setLocation(x, y);
		ellipse = new Ellipse2D.Double(x, y, 0, 0);
		size    = new Dimension(0, 0);
		temporalDrawing = new Drawing(getName());
		inUse   = true;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#moveTo(double, double)
	 */
	@Override
	public void moveTo(double x, double y) {
		//TODO: check arguments
		firstPoint.setLocation(x, y);
		ellipse.setFrame(firstPoint, size);
	}


	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#getInit()
	 */
	@Override
	public Point2D getInit() {
		return firstPoint;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDraw(double x, double y, boolean symmetric) {
		if (inUse) {
			double initX   = firstPoint.getX();
			double initY   = firstPoint.getY();
			int    width   = (int) Math.abs(initX - x);
			int    height  = (int) Math.abs(initY - y);
			Dimension size = new Dimension(((symmetric)? Math.max(width, height) : width), ((symmetric)? Math.max(width, height) : height));
			Point2D  point = null;

			if (symmetric)
				point = new Point2D.Double( ((initX < x)? initX : initX - size.getWidth()), ((initY < y)? initY : initY - size.getHeight()));
			else
				point = new Point2D.Double( ((initX < x)? initX : x), ((initY < y)? initY : y));

			ellipse.setFrame(point, size);
		}
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
	 * @see com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaProperties() {
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
		if (inUse && !ellipse.isEmpty()) {
			temporalDrawing.setShape(ellipse);
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
