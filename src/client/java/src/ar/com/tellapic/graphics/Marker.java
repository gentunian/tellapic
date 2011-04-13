package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class Marker extends DrawingTool {
	private Line2D              line;
	private Point2D             firstPoint;
	private Drawing             temporalDrawing;
	private boolean             inUse;

	
	
	public Marker(String name) {
		super(tellapicConstants.TOOL_MARKER, name, "/icons/marker.png", Utils.msg.getString("markertooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse = false;
		temporalDrawing = new Drawing(getName());
	}
	
	public Marker() {
		this("Marker");
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getDrawing()
	 */
	@Override
	public Drawing getDrawing() {
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
		firstPoint.setLocation(x, y);
		line  = new Line2D.Double(firstPoint, firstPoint);
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
			if (symmetric)
				line.setLine(firstPoint.getX(), firstPoint.getY(), firstPoint.getX(), y);
			else
				line.setLine(firstPoint.getX(), firstPoint.getY(), x, firstPoint.getY());
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
	 * @see ar.com.tellapic.graphics.Tool#hasColorCapability()
	 */
	@Override
	public boolean hasColorCapability() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasFontCapability()
	 */
	@Override
	public boolean hasFontCapability() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeCapability()
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
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#setAplha(ar.com.tellapic.graphics.PaintPropertyAlpha)
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
