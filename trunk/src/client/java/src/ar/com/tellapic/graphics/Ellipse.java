package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class Ellipse extends DrawingTool {
	private static final String ELLIPSE_ICON_PATH = "/icons/tools/ellipse.png";
	private static final String ELLIPSE_CURSOR_PATH = "/icons/tools/ellipse-cursor.png";
	
	private static final double DEFAULT_ALPHA = 1;
	private static final int    DEFAULT_CAPS = 0;
	private static final Color  DEFAULT_COLOR = Color.white;
	private static final int    DEFAULT_JOINS = 0;
	private static final float  DEFAULT_MITER_LIMIT = 1;
	private static final double DEFAULT_WIDTH = 5;
	
	private Ellipse2D           ellipse;
	private Point2D             firstPoint;
	private Dimension           size;
	private DrawingShape        temporalDrawing;
	private boolean             inUse;
	
	
	
	public Ellipse(String name) {
		super(tellapicConstants.TOOL_ELLIPSE, name, ELLIPSE_ICON_PATH, Utils.msg.getString("ellipsetooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse      = false;
		temporalDrawing = new DrawingShape(getName());
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
	
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#init(double, double)
//	 */
//	@Override
//	public void onPress(int x, int y, int button, int mask) {
//		firstPoint.setLocation(x, y);
//		ellipse = new Ellipse2D.Double(x, y, 0, 0);
//		size    = new Dimension(0, 0);
//		inUse   = true;
//		temporalDrawing.setShape(ellipse);
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
//			double initX   = firstPoint.getX();
//			double initY   = firstPoint.getY();
//			int    width   = (int) Math.abs(initX - x);
//			int    height  = (int) Math.abs(initY - y);
//			Dimension size = new Dimension(((symmetric)? Math.max(width, height) : width), ((symmetric)? Math.max(width, height) : height));
//			Point2D  point = null;
//
//			if (symmetric)
//				point = new Point2D.Double( ((initX < x)? initX : initX - size.getWidth()), ((initY < y)? initY : initY - size.getHeight()));
//			else
//				point = new Point2D.Double( ((initX < x)? initX : x), ((initY < y)? initY : y));
//
//			ellipse.setFrame(point, size);
//			setChanged();
//			notifyObservers(temporalDrawing);
//		}
//	}
//	
//	
//		/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onFinishDraw()
//	 */
//	@Override
//	public void onRelease(int x, int y, int button, int mask) {
//		if (inUse && !ellipse.isEmpty()) {
////			temporalDrawing.cloneProperties();
//			inUse = false;
//			setChanged();
//			notifyObservers(temporalDrawing);
//		}
//	}
//	
//	
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
//	 * @see ar.com.tellapic.graphics.Tool#isFilleable()
//	 */
//	@Override
//	public boolean isFilleable() {
//		return true;
//	}
//	
//	
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onMove(double, double)
//	 */
//	@Override
//	public void onMove(int x, int y) {
//
//	}
//	
//	
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnMoveSupported()
//	 */
//	@Override
//	public boolean isOnMoveSupported() {
//		return false;
//	}
	
	
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
//	 * @see ar.com.tellapic.graphics.Tool#onCancel()
//	 */
//	@Override
//	public void onPause() {
//		inUse = false;
//	}
//	
//	
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isLiveModeSupported()
//	 */
//	@Override
//	public boolean isLiveModeSupported() {
//		return false;
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
//	 * @see ar.com.tellapic.graphics.Tool#isOnDragSupported()
//	 */
//	@Override
//	public boolean isOnDragSupported() {
//		return true;
//	}
//
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnPressSupported()
//	 */
//	@Override
//	public boolean isOnPressSupported() {
//		return true;
//	}
//
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnReleaseSupported()
//	 */
//	@Override
//	public boolean isOnReleaseSupported() {
//		return true;
//	}


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
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultWidth()
	 */
	@Override
	public double getDefaultWidth() {
		return DEFAULT_WIDTH;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getTemporalDrawing()
	 */
	@Override
	public AbstractDrawing getTemporalDrawing() {
		return temporalDrawing;
	}
	
//	/**
//	 * 
//	 */
//	@Override
//	public void setPaintProperties(PaintProperty properties[]) {
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
			ellipse = new Ellipse2D.Double(e.getX(), e.getY(), 0, 0);
			size    = new Dimension(0, 0);
			inUse   = true;
			temporalDrawing = new DrawingShape(getName());
			temporalDrawing.setShape(ellipse);
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
			if (inUse && !ellipse.isEmpty()) {
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
				double initX   = firstPoint.getX();
				double initY   = firstPoint.getY();
				int    width   = (int) Math.abs(initX - e.getX());
				int    height  = (int) Math.abs(initY - e.getY());
				Dimension size = new Dimension(((symmetric)? Math.max(width, height) : width), ((symmetric)? Math.max(width, height) : height));
				Point2D  point = null;

				if (symmetric)
					point = new Point2D.Double( ((initX < e.getX())? initX : initX - size.getWidth()), ((initY < e.getY())? initY : initY - size.getHeight()));
				else
					point = new Point2D.Double( ((initX < e.getX())? initX : e.getX()), ((initY < e.getY())? initY : e.getY()));

				ellipse.setFrame(point, size);
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
