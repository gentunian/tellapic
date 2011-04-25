package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class Ellipse extends DrawingTool {
	private static final String ELLIPSE_ICON_PATH = "/icons/tools/ellipse.png";
	private static final String ELLIPSE_CURSOR_PATH = "/icons/tools/ellipse-cursor.png";
	
	private Ellipse2D           ellipse;
	private Point2D             firstPoint;
	private Dimension           size;
//	private Drawing             temporalDrawing;
	private boolean             inUse;
	
	
	
	public Ellipse(String name) {
		super(tellapicConstants.TOOL_ELLIPSE, name, ELLIPSE_ICON_PATH, Utils.msg.getString("ellipsetooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse      = false;
		temporalDrawing = new Drawing(getName());
	}
	
	
	public Ellipse() {
		this("Ellipse");
	}
	

	/**
	 * 
	 * @return
	 */
	public Dimension getSize() {
		return size;
	}
	
	
//	@Override
//	public Drawing getDrawing() {
//		temporalDrawing.setShape(ellipse);
//		return (Drawing) temporalDrawing.clone();
//	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
	 */
	@Override
	public void onPress(int x, int y, int button, int mask) {
		firstPoint.setLocation(x, y);
		ellipse = new Ellipse2D.Double(x, y, 0, 0);
		size    = new Dimension(0, 0);
		inUse   = true;
		temporalDrawing.setShape(ellipse);
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
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}
	
	
		/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
	 */
	@Override
	public void onRelease(int x, int y, int button, int mask) {
		if (inUse && !ellipse.isEmpty()) {
//			temporalDrawing.cloneProperties();
			inUse = false;
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#moveTo(double, double)
	 */
//	@Override
//	public void moveTo(double x, double y) {
//		
//		firstPoint.setLocation(x, y);
//		ellipse.setFrame(firstPoint, size);
//	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#getInit()
	 */
	@Override
	public Point2D getInit() {
		return firstPoint;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
	 */
	@Override
	public boolean isFilleable() {
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#onMove(double, double)
	 */
	@Override
	public void onMove(int x, int y) {

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
	 * @see ar.com.tellapic.graphics.Tool#onCancel()
	 */
	@Override
	public void onPause() {
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
	 * @see ar.com.tellapic.graphics.Tool#isOnDragSupported()
	 */
	@Override
	public boolean isOnDragSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isOnPressSupported()
	 */
	@Override
	public boolean isOnPressSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isOnReleaseSupported()
	 */
	@Override
	public boolean isOnReleaseSupported() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasAlphaCapability()
	 */
	@Override
	public boolean hasAlphaCapability() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasColorCapability()
	 */
	@Override
	public boolean hasColorCapability() {
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasFontCapability()
	 */
	@Override
	public boolean hasFontCapability() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasStrokeCapability()
	 */
	@Override
	public boolean hasStrokeCapability() {
		return true;
	}
}
