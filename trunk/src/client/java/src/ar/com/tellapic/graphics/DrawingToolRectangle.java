package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class DrawingToolRectangle extends DrawingTool {
	private Point2D             firstPoint;
	private static final String RECTANGLE_ICON_PATH = "/icons/tools/rectangle.png"; 
	@SuppressWarnings("unused")
	private static final String RECTANGLE_CURSOR_PATH = "/icons/tools/rectangle-cursor.png";
	private static final double DEFAULT_WIDTH = 5;
	private static final double DEFAULT_ALPHA = 1;
	private static final int    DEFAULT_CAPS = 0;
	private static final Color  DEFAULT_COLOR = Color.black;
	private static final int    DEFAULT_JOINS = 0;
	private static final float  DEFAULT_MITER_LIMIT = 1; 

	/**
	 * 
	 * @param name
	 */
	public DrawingToolRectangle(String name) {
		super(tellapicConstants.TOOL_RECT, name, RECTANGLE_ICON_PATH, Utils.msg.getString("rectangletooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
	}
	
	
	public DrawingToolRectangle() {
		this("DrawingToolRectangle");
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasAlphaCapability()
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
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				float zoomX = ControlToolZoom.getInstance().getZoomValue();
				IToolBoxState toolBoxState = user.getToolBoxModel();
				setInUse(true);
				firstPoint.setLocation(e.getX() / zoomX, e.getY() / zoomX);
				temporalDrawing = new DrawingShapeRectangle(getName(), firstPoint.getX(), firstPoint.getY(), 0, 0);
				((DrawingShape) temporalDrawing).setAlpha(toolBoxState.getOpacityProperty());
				((DrawingShape) temporalDrawing).setColor(toolBoxState.getColorProperty());
				((DrawingShape) temporalDrawing).setStroke(toolBoxState.getStrokeProperty());
				temporalDrawing.setRenderingHints(toolBoxState.getRenderingHints());
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
				DrawingShapeRectangle drawingRectangle = (DrawingShapeRectangle) temporalDrawing;
				if (drawingRectangle != null && !drawingRectangle.isEmpty()) {
					temporalDrawing.cloneProperties();
					user.addDrawing(temporalDrawing);
					setChanged();
					notifyObservers(temporalDrawing);
				} else
					temporalDrawing = null;
			}
			setInUse(false);
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
				if (isBeingUsed()) {
					float zoomX = ControlToolZoom.getInstance().getZoomValue();
					boolean symmetric = e.isControlDown() || isSymmetricModeEnabled();
					double initX  = firstPoint.getX();
					double initY  = firstPoint.getY();
					double width  = Math.abs(firstPoint.getX() - e.getX()/zoomX);
					double height = Math.abs(firstPoint.getY() - e.getY()/zoomX);
					
					if (symmetric) {
						width  = Math.max(width, height);
						height = width;
						initX  = (initX <  e.getX()/zoomX)? initX : initX - width;
						initY  = (initY < e.getY()/zoomX)? initY : initY - height;
					} else {
						initX  = (initX <  e.getX()/zoomX)? initX :  e.getX()/zoomX;
						initY  = (initY < e.getY()/zoomX)? initY : e.getY()/zoomX;
					}
					((Rectangle2D)((DrawingShape) temporalDrawing).getShape()).setRect(initX, initY, width, height);
					setChanged();
					notifyObservers(temporalDrawing);
				}
			}
			e.consume();
		}
	}
}
