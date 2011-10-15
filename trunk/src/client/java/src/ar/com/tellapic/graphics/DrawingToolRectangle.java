package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class DrawingToolRectangle extends DrawingTool {
	private static final String RECTANGLE_ICON_PATH = "/icons/tools/rectangle.png"; 
	@SuppressWarnings("unused")
	private static final String RECTANGLE_CURSOR_PATH = "/icons/tools/rectangle-cursor.png";
	private static final double DEFAULT_WIDTH = 5;
	private static final double DEFAULT_ALPHA = 1;
	private static final int    DEFAULT_CAPS = 0;
	private static final Color  DEFAULT_COLOR = Color.black;
	private static final int    DEFAULT_JOINS = 0;
	private static final float  DEFAULT_MITER_LIMIT = 1;
	private Point2D             firstPoint;
//	private Point2D             lastPoint;
	 
	
	/**
	 * 
	 * @param name
	 */
	public DrawingToolRectangle(String name) {
		super(tellapicConstants.TOOL_RECT, name, RECTANGLE_ICON_PATH, Utils.msg.getString("rectangletooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
//		lastPoint  = new Point2D.Double();
		COMMANDS = new String[][] {
				{ "rectangle", "frame", "square" },
				{ getClass().getPackage().getName()+".DrawingShape Draws a rectangle with frame (x1, y1) and (x2, y2).", "int x1 The first x coordinate", "int y1 The first y coordinate", "int x2 The second x coordinate", "int y2 The second y coordinate" },
				{ getClass().getPackage().getName()+".DrawingShape Draws a rectangle with frame (left, top) and (left + width, top + height).", "int x The left coordinate", "int y The top coordinate", "int w The width size", "int h The height size"},
				{ getClass().getPackage().getName()+".DrawingShape Draws a square in the specified position.", "int x The left coordinate", "int y The top coordinate", "int size The size of the sides"}
		};
		setAlias("Rectangle");
	}
	
	/**
	 * 
	 */
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

	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasFillCapability()
	 */
	public boolean hasFillCapability() {
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
				setInUse(true);
				firstPoint.setLocation(e.getX() / zoomX, e.getY() / zoomX);
				temporalDrawing = new DrawingShapeRectangle(user, getName(), firstPoint.getX(), firstPoint.getY(), 0, 0);
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
					if (getUser().isRemote())
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
//					firstPoint.setLocation(initX, initY);
//					lastPoint.setLocation(initX + width, initY + height);
					((DrawingShapeRectangle) temporalDrawing).setRect(initX, initY, width, height);
//					((DrawingShapeRectangle) temporalDrawing).setRect(firstPoint, lastPoint);
					setChanged();
					notifyObservers(temporalDrawing);
				}
			}
			e.consume();
		}
	}
	
	/**
	 * This tool knows how to build DrawingShapeRectangle objects. This helper method, creates a DrawingShapeRectangle object
	 * with the specified values and the default user toolbox configuration. Its provides an easy way to create the mentioned
	 * object, for clients such as: a command line, external events, etc, that are not necessarily mouse events.
	 *
	 * @param x the left x coordinate
	 * @param y the top y coordinate
	 * @param w the rectangle width
	 * @param h the rectangle height
	 * @return A DrawingShapeRectangle will be created, with default user toolbox configuration.
	 */
	public DrawingShape frame(String x, String y, String w, String h) {
		int iX = 0;
		int iY = 0;
		int iW = 100;
		int iH = 100;
		try {
			iX = Integer.valueOf(x);
			iY = Integer.valueOf(y);
			iW = Integer.valueOf(w);
			iH = Integer.valueOf(h);
		} catch(Exception e) {
			Utils.logMessage("Wrong format. Setting frame to default values at (0,0).");
		}
		
		DrawingShapeRectangle drawing = new DrawingShapeRectangle(
				user,
				"CustomRectangle",
				iX,
				iY,
				iW,
				iH
		);
		drawing.setUser(user);
		user.addDrawing(drawing);
		setChanged();
		notifyObservers(drawing);
		
		return drawing;
	}
	
	/**
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public DrawingShape rectangle(String x1, String y1, String x2, String y2) {
		int x = Integer.parseInt(x1) < Integer.parseInt(x2)? Integer.parseInt(x1) : Integer.parseInt(x2) ;
		int y = Integer.parseInt(y1) < Integer.parseInt(y2)? Integer.parseInt(y1) : Integer.parseInt(y2);
		int width  = Math.abs(Integer.parseInt(x1) - Integer.parseInt(x2));
		int height = Math.abs(Integer.parseInt(y1) - Integer.parseInt(y2));
		
		return frame(String.valueOf(x), String.valueOf(y), String.valueOf(width), String.valueOf(height));
	}
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param size
	 * @return
	 */
	public DrawingShape square(String x, String y, String size) {
		return frame(x, y, size, size);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isLiveModeSupported()
	 */
	@Override
	public boolean isLiveModeSupported() {
		// TODO Auto-generated method stub
		return false;
	}
}
