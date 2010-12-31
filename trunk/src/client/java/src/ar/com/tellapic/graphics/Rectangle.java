package ar.com.tellapic.graphics;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ar.com.tellapic.Utils;

public final class Rectangle extends Tool {
	private Point2D             firstPoint;
	private Rectangle2D         rectangle;
	private Drawing             temporalDrawing;
	private boolean             inUse;
	
	/*TODO: remove singleton for use 1 toolbox per client. 12/10/2010
	private static class RectangleHolder {
		private static final Rectangle RECTANGLE_INSTANCE = new Rectangle();
	}
	
	
	public static Rectangle getInstance() {
		return RectangleHolder.RECTANGLE_INSTANCE;
	}
	*/
	
	public Rectangle() {
		super(Rectangle.class.getSimpleName(), "/icons/rectangle.png", Utils.msg.getString("rectangletooltip"));
		firstPoint = new Point2D.Double();
		inUse = false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		if (inUse) {
			temporalDrawing.setShape(rectangle);
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
		rectangle       = new Rectangle2D.Double(x, y, 0, 0);
		temporalDrawing = new Drawing(getName());
		inUse = true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#moveTo(double, double)
	 */
	@Override
	public void moveTo(double x, double y) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDraw(double x, double y, boolean symmetric) {
		if (inUse) {
			double initX  = firstPoint.getX();
			double initY  = firstPoint.getY();
			double width  = Math.abs(firstPoint.getX() - x);
			double height = Math.abs(firstPoint.getY() - y);

			if (symmetric) {
				width  = Math.max(width, height);
				height = width;
				initX  = (initX < x)? initX : initX - width;
				initY  = (initY < y)? initY : initY - height;
			} else {
				initX  = (initX < x)? initX : x;
				initY  = (initY < y)? initY : y;
			}
			rectangle.setRect(initX, initY, width, height);
		}
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
		if (inUse && !rectangle.isEmpty()) {
			temporalDrawing.setShape(rectangle);
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
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRestore()
	 */
	@Override
	public void onRestore() {
		inUse = true;
	}
}
