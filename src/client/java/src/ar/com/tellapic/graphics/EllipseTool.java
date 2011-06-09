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

public class EllipseTool extends DrawingTool {
	private static final String ELLIPSE_ICON_PATH = "/icons/tools/ellipse.png";
	@SuppressWarnings("unused")
	private static final String ELLIPSE_CURSOR_PATH = "/icons/tools/ellipse-cursor.png";
	
	private static final double DEFAULT_ALPHA = 1;
	private static final int    DEFAULT_CAPS = 0;
	private static final Color  DEFAULT_COLOR = Color.white;
	private static final int    DEFAULT_JOINS = 0;
	private static final float  DEFAULT_MITER_LIMIT = 1;
	private static final double DEFAULT_WIDTH = 5;
	
	private Point2D             firstPoint;
	private boolean             inUse;
	
	
	
	public EllipseTool(String name) {
		super(tellapicConstants.TOOL_ELLIPSE, name, ELLIPSE_ICON_PATH, Utils.msg.getString("ellipsetooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		inUse      = false;
	}
	
	
	public EllipseTool() {
		this("Ellipse");
	}
	


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		return inUse;
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
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				AbstractUser user = null;
				if (e instanceof RemoteMouseEvent) {
					user = ((RemoteMouseEvent)e).getUser();
				} else {
					user = UserManager.getInstance().getLocalUser();
				}
				IToolBoxState toolBoxState = user.getToolBoxModel();
				float zoomX = ZoomTool.getInstance().getZoomValue();
				firstPoint.setLocation(e.getX()/zoomX, e.getY()/zoomX);
				inUse   = true;
				temporalDrawing = new DrawingShapeEllipse(getName(), e.getX()/zoomX, e.getY()/zoomX, 0, 0);
				((DrawingShape) temporalDrawing).setAlpha(toolBoxState.getOpacityProperty());
				((DrawingShape) temporalDrawing).setColor(toolBoxState.getColorProperty());
				((DrawingShape) temporalDrawing).setStroke(toolBoxState.getStrokeProperty());
				temporalDrawing.setRenderingHints(toolBoxState.getRenderingHints());
				temporalDrawing.setNumber(toolBoxState.getAssignedNumber());
				temporalDrawing.setUser(user);
				user.setTemporalDrawing(temporalDrawing);
				setChanged();
				notifyObservers(temporalDrawing);
			}
			e.consume();
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				if (inUse && !((DrawingShape) temporalDrawing).getShape().getBounds2D().isEmpty()) {
					AbstractUser user = null;
					if (e instanceof RemoteMouseEvent) {
						user = ((RemoteMouseEvent)e).getUser();
					} else {
						user = UserManager.getInstance().getLocalUser();
					}
					temporalDrawing.cloneProperties();
					user.addDrawing(temporalDrawing);
					setChanged();
					notifyObservers(temporalDrawing);
				}
				inUse = false;
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
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				if (inUse) {
					float zoomX = ZoomTool.getInstance().getZoomValue();
					boolean symmetric = e.isControlDown() || isSymmetricModeEnabled();
					double initX   = firstPoint.getX();
					double initY   = firstPoint.getY();
					int    width   = (int) Math.abs(initX - e.getX()/zoomX);
					int    height  = (int) Math.abs(initY - e.getY()/zoomX);
					Dimension size = new Dimension(((symmetric)? Math.max(width, height) : width), ((symmetric)? Math.max(width, height) : height));
					Point2D  point = null;

					if (symmetric)
						point = new Point2D.Double( ((initX < e.getX()/zoomX)? initX : initX - size.getWidth()), ((initY < e.getY()/zoomX)? initY : initY - size.getHeight()));
					else
						point = new Point2D.Double( ((initX < e.getX()/zoomX)? initX : e.getX()/zoomX), ((initY < e.getY()/zoomX)? initY : e.getY()/zoomX));

					((Ellipse2D)((DrawingShape) temporalDrawing).getShape()).setFrame(point, size);
					setChanged();
					notifyObservers(temporalDrawing);
				}
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
