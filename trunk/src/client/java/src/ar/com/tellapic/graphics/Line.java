 package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

/**
 * 
 * @author seba
 *
 */
public class Line extends DrawingTool {
	private static final double ANGLE = 15;
	private static final double STEP = (ANGLE * Math.PI) / 180;
	
	private Point2D             firstPoint;
	private Line2D              line;
	private Drawing             temporalDrawing;
	private boolean             inUse;


	public Line(String name) {
		super(tellapicConstants.TOOL_LINE, name, "/icons/line.png", Utils.msg.getString("linetooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse      = false;
		temporalDrawing = new Drawing(getName());
	}
	
	public Line() {
		this("Line");
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
		temporalDrawing.setShape(line);
		return (Drawing) temporalDrawing.clone();
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
		if (inUse)
			throw new IllegalStateException("init cannot be called with the tool being used");
		
		firstPoint.setLocation(x, y);
		line            = new Line2D.Double(firstPoint, firstPoint);
		inUse = true;
		temporalDrawing.setShape(line);
		setChanged();
		notifyObservers(temporalDrawing);
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
	 */
	@Override
	public void onDrag(int x, int y, int button, int mask) {
		if (inUse) {
			boolean symmetric = (mask & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK;
			double angle = Math.atan2(x - firstPoint.getX(), y - firstPoint.getY()) + Math.PI/2;
			if ( angle < 0)
				angle = (Math.PI - Math.abs(angle)) + Math.PI;

			int newX = x;
			int newY = y;

			if (symmetric) {
				double steppedAngle = (Math.round(angle / STEP) * STEP);
				double dist = firstPoint.distance(x, y);
				newX = (int) (dist * Math.cos(steppedAngle - Math.PI) + firstPoint.getX());
				newY = (int) (dist * Math.sin(steppedAngle) + firstPoint.getY());
			} 

			line.setLine(firstPoint.getX(), firstPoint.getY(), newX, newY);
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
	 */
	@Override
	public void onRelease(int x, int y, int button, int mask) {
		if (inUse && line.getP1().distance(line.getP2()) > 0.0) {
//			temporalDrawing.cloneProperties();
			inUse = false;
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	public void onPause() {
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
	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		return false;
	}



	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasAlphaProperties()
	 */
	@Override
	public boolean hasAlphaCapability() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasColorProperties()
	 */
	@Override
	public boolean hasColorCapability() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasFontProperties()
	 */
	@Override
	public boolean hasFontCapability() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeProperties()
	 */
	@Override
	public boolean hasStrokeCapability() {
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
		setChanged();
		notifyObservers(temporalDrawing);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setColor(ar.com.tellapic.graphics.PaintPropertyColor)
	 */
	@Override
	public void setColor(PaintPropertyColor color) {
		temporalDrawing.setColor(color);
		setChanged();
		notifyObservers(temporalDrawing);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setFont(ar.com.tellapic.graphics.PaintPropertyFont)
	 */
	@Override
	public void setFont(PaintPropertyFont font) {
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setStroke(ar.com.tellapic.graphics.PaintPropertyStroke)
	 */
	@Override
	public void setStroke(PaintPropertyStroke stroke) {
		temporalDrawing.setStroke(stroke);
		setChanged();
		notifyObservers(temporalDrawing);
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#isOnDragSupported()
	 */
	@Override
	public boolean isOnDragSupported() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#isOnPressSupported()
	 */
	@Override
	public boolean isOnPressSupported() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#isOnReleaseSupported()
	 */
	@Override
	public boolean isOnReleaseSupported() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onMove(int, int)
	 */
	@Override
	public void onMove(int x, int y) {
		// TODO Auto-generated method stub
		
	}
}
