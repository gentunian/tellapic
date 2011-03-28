package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class Rectangle extends Tool {
	private Point2D             firstPoint;
	private Rectangle2D         rectangle;
	private Drawing             temporalDrawing;
	private boolean             inUse;
	
	
	
	public Rectangle(String name) {
		super(tellapicConstants.TOOL_RECT, name, "/icons/rectangle.png", Utils.msg.getString("rectangletooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse = false;
		temporalDrawing = new Drawing(getName());
	}
	
	
	public Rectangle() {
		this("Rectangle");
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
	public void onPress(int x, int y, int button, int mask) {
		firstPoint.setLocation(x, y);
		rectangle = new Rectangle2D.Double(x, y, 0, 0);
		inUse = true;
		temporalDrawing.setShape(rectangle);
	}


	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDrag(int x, int y, int button, int mask) {
		if (inUse) {
			boolean symmetric = (mask & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK;
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
	 * @see ar.com.tellapic.graphics.Tool#onMove(double, double)
	 */
	@Override
	public Drawing onMove(int x, int y) {
		return null;
	}



	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
	 */
	@Override
	public Drawing onRelease(int x, int y, int button, int mask) {
		if (inUse && !rectangle.isEmpty()) {
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
	public void onCancel() {
		inUse = false;		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onRestore()
	 */
	@Override
	public void onRestore() {
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
		temporalDrawing.setAlpha(alpha);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setColor(ar.com.tellapic.graphics.PaintPropertyColor)
	 */
	@Override
	public void setColor(PaintPropertyColor color) {
		temporalDrawing.setColor(color);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setFont(ar.com.tellapic.graphics.PaintPropertyFont)
	 */
	@Override
	public void setFont(PaintPropertyFont font) {
		return;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setStroke(ar.com.tellapic.graphics.PaintPropertyStroke)
	 */
	@Override
	public void setStroke(PaintPropertyStroke stroke) {
		temporalDrawing.setStroke(stroke);
	}
}
