package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class Marker extends DrawingTool {
	private static final String MARKER_ICON_PATH = "/icons/tools/marker.png";
	private static final String MARKER_CURSOR_PATH = "/icons/tools/marker-cursor.png";
	private static final double DEFAULT_WIDTH = 20;
	private static final double DEFAULT_ALPHA = .5f;
	private static final int    DEFAULT_CAPS = 0;
	private static final Color  DEFAULT_COLOR = Color.green;
	private static final int    DEFAULT_JOINS = 0;
	private static final float  DEFAULT_MITER_LIMIT = 1;
	private Line2D              line;
	private Point2D             firstPoint;
	private DrawingShape        temporalDrawing;
	private boolean             inUse;

	
	
	public Marker(String name) {
		super(tellapicConstants.TOOL_MARKER, name, MARKER_ICON_PATH , Utils.msg.getString("markertooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse = false;
		temporalDrawing = new DrawingShape(getName());
		//setToolCursor(MARKER_ICON_PATH, 2, 14);
	}
	
	public Marker() {
		this("Marker");
	}


//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#getInit()
//	 */
//	@Override
//	public Point2D getInit() {
//		return firstPoint;
//	}
//
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
//	 */
//	@Override
//	public void onPress(int x, int y, int button, int mask) {
//		firstPoint.setLocation(x, y);
//		line  = new Line2D.Double(firstPoint, firstPoint);
//		inUse = true;
//		temporalDrawing.setShape(line);
//		setChanged();
//		notifyObservers(temporalDrawing);
//	}
//
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#draw(double, double)
//	 */
//	@Override
//	public void onDrag(int x, int y, int button, int mask) {
//		if (inUse) {
//			boolean symmetric = (mask & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK;
//			if (symmetric)
//				line.setLine(firstPoint.getX(), firstPoint.getY(), firstPoint.getX(), y);
//			else
//				line.setLine(firstPoint.getX(), firstPoint.getY(), x, firstPoint.getY());
//			setChanged();
//			notifyObservers(temporalDrawing);
//		}
//	}
//	
//	
//	
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
//	 */
//	@Override
//	public void onRelease(int x, int y, int button, int mask) {
//		if (inUse && line.getP1().distance(line.getP2()) > 0.0) {
////			temporalDrawing.cloneProperties();
//			inUse = false;
//			setChanged();
//			notifyObservers(temporalDrawing);
//		}
//	}
//
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onCancel()
//	 */
//	@Override
//	public void onPause() {
//		inUse = false;
//	}
//
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onRestore()
//	 */
//	@Override
//	public void onRestore() {
//		inUse = true;
//	}
//
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
//	 */
//	@Override
//	public boolean isFilleable() {
//		return false;
//	}


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


//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnMoveSupported()
//	 */
//	@Override
//	public boolean isOnMoveSupported() {
//		return false;
//	}
//
//
	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		return inUse;
	}
//
//	
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isLiveModeSupported()
//	 */
//	@Override
//	public boolean isLiveModeSupported() {
//		return true;
//	}

//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#setAplha(ar.com.tellapic.graphics.PaintPropertyAlpha)
//	 */
//	@Override
//	public void setAlpha(PaintPropertyAlpha alpha) {
//		temporalDrawing.setAlpha(alpha);
//		setChanged();
//		notifyObservers(temporalDrawing);
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#setColor(ar.com.tellapic.graphics.PaintPropertyColor)
//	 */
//	@Override
//	public void setColor(PaintPropertyColor color) {
//		temporalDrawing.setColor(color);
//		setChanged();
//		notifyObservers(temporalDrawing);
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#setFont(ar.com.tellapic.graphics.PaintPropertyFont)
//	 */
//	@Override
//	public void setFont(PaintPropertyFont font) {
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#setStroke(ar.com.tellapic.graphics.PaintPropertyStroke)
//	 */
//	@Override
//	public void setStroke(PaintPropertyStroke stroke) {
//		temporalDrawing.setStroke(stroke);
//		setChanged();
//		notifyObservers(temporalDrawing);
//	}


//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.DrawingTool#isOnDragSupported()
//	 */
//	@Override
//	public boolean isOnDragSupported() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.DrawingTool#isOnPressSupported()
//	 */
//	@Override
//	public boolean isOnPressSupported() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.DrawingTool#isOnReleaseSupported()
//	 */
//	@Override
//	public boolean isOnReleaseSupported() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onMove(int, int)
//	 */
//	@Override
//	public void onMove(int x, int y) {
//		// TODO Auto-generated method stub
//		
//	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultWidth()
	 */
	@Override
	public double getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultAlpha()
	 */
	@Override
	public double getDefaultAlpha() {
		return DEFAULT_ALPHA;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultCaps()
	 */
	@Override
	public int getDefaultCaps() {
		return DEFAULT_CAPS;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultColor()
	 */
	@Override
	public Color getDefaultColor() {
		return DEFAULT_COLOR;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontFace()
	 */
	@Override
	public String getDefaultFontFace() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontSize()
	 */
	@Override
	public double getDefaultFontSize() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontStyle()
	 */
	@Override
	public int getDefaultFontStyle() {
			return 0;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultJoins()
	 */
	@Override
	public int getDefaultJoins() {
		return DEFAULT_JOINS;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultMiterLimit()
	 */
	@Override
	public float getDefaultMiterLimit() {
		return DEFAULT_MITER_LIMIT;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getTemporalDrawing()
	 */
	@Override
	public AbstractDrawing getTemporalDrawing() {
		return temporalDrawing;
	}

//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.DrawingTool#setPaintProperties(ar.com.tellapic.graphics.PaintProperty[])
//	 */
//	@Override
//	public void setPaintProperties(PaintProperty[] properties) {
//		for(int i = 0; i < properties.length; i++) {
//			if (properties[i] instanceof PaintPropertyStroke) {
//				temporalDrawing.setStroke((PaintPropertyStroke) properties[i]);
//			} else if (properties[i] instanceof PaintPropertyAlpha) {
//				temporalDrawing.setAlpha((PaintPropertyAlpha) properties[i]);
//			} else if (properties[i] instanceof PaintPropertyColor) {
//				temporalDrawing.setColor((PaintPropertyColor) properties[i]);
//			}
//		}
//		setChanged();
//		notifyObservers(temporalDrawing);
//	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			AbstractUser user = null;
			if (e instanceof RemoteMouseEvent) {
				user = ((RemoteMouseEvent)e).getUser();
			} else {
				user = UserManager.getInstance().getLocalUser();
			}
			IToolBoxState toolBoxState = user.getToolBoxModel();
			firstPoint.setLocation(e.getX(), e.getY());
			line  = new Line2D.Double(firstPoint, firstPoint);
			inUse = true;
			temporalDrawing = new DrawingShape(getName());
			temporalDrawing.setShape(line);
			temporalDrawing.setAlpha(toolBoxState.getOpacityProperty());
			temporalDrawing.setColor(toolBoxState.getColorProperty());
			temporalDrawing.setStroke(toolBoxState.getStrokeProperty());
			temporalDrawing.setNumber(toolBoxState.getAssignedNumber());
			temporalDrawing.setUser(user);
			user.setTemporalDrawing(temporalDrawing);
			e.consume();
			setChanged();
			notifyObservers(temporalDrawing);
		}
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (inUse && line.getP1().distance(line.getP2()) > 0.0) {
				if (e instanceof RemoteMouseEvent) {
					AbstractUser user = ((RemoteMouseEvent)e).getUser();
					user.addDrawing(temporalDrawing);
				}
				inUse = false;
				setChanged();
				notifyObservers(temporalDrawing);
			}
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (inUse) {
				boolean symmetric = e.isControlDown();
				if (symmetric)
					line.setLine(firstPoint.getX(), firstPoint.getY(), firstPoint.getX(), e.getY());
				else
					line.setLine(firstPoint.getX(), firstPoint.getY(), e.getX(), firstPoint.getY());
				setChanged();
				notifyObservers(temporalDrawing);
			}
			e.consume();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
