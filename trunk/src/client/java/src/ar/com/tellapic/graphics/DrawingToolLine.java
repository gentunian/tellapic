 package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

/**
 * 
 * @author seba
 *
 */
public class DrawingToolLine extends DrawingTool {
	private static final double ANGLE = 15;
	private static final double STEP = (ANGLE * Math.PI) / 180;
	private static final String LINE_ICON_PATH = "/icons/tools/line.png";
	@SuppressWarnings("unused")
	private static final String LINE_CURSOR_PATH = "/icons/tools/line-cursor.png";
	private static final double DEFAULT_WIDTH = 5;
	private static final double DEFAULT_ALPHA = 1;
	private static final int    DEFAULT_CAPS = 0;
	private static final Color  DEFAULT_COLOR = Color.black;
	private static final int    DEFAULT_JOINS = 0;
	private static final float  DEFAULT_MITER_LIMIT = 1;
	
	protected Point2D     firstPoint;

	
	/**
	 * 
	 * @param name
	 */
	public DrawingToolLine(String name) {
		this(tellapicConstants.TOOL_LINE, name);
	}
	
	/**
	 * 
	 */
	public DrawingToolLine() {
		this("DrawingToolLine");
	}

	/**
	 * @param toolMarker
	 * @param name
	 */
	public DrawingToolLine(int id, String name) {
		super(id, name, LINE_ICON_PATH, Utils.msg.getString("linetooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		setAlias("Line");
		COMMANDS = new String[][] {
				{ "line" },
				{ getClass().getPackage().getName()+".DrawingShape Draws a line from (x1, y1) to (x2, y2).", "int x1 The first point x coordinate", "int y1 The first point y  coordinate", "int x2 The second point x coordinate", "int y2 The second point y coordinate" }
		};
	}

	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasAlphaCapability()
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

	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasFillCapability()
	 */
	public boolean hasFillCapability() {
		return false;
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
		// TODO Auto-generated method stub
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
				if (isBeingUsed())
					throw new IllegalStateException("init cannot be called with the tool being used");
				float zoomX = ControlToolZoom.getInstance().getZoomValue();
				firstPoint.setLocation(e.getX()/zoomX, e.getY()/zoomX);
				setInUse(true);
				temporalDrawing = new DrawingShapeLine(user, getName(), firstPoint, firstPoint);
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
				DrawingShapeLine drawingLine = (DrawingShapeLine) temporalDrawing;
				if (drawingLine != null && drawingLine.length() > 0) {
					drawingLine.closeLine();
					if (getUser().isRemote())
						user.addDrawing(temporalDrawing);
					setChanged();
					notifyObservers(temporalDrawing);
				} 
//				else 
//					temporalDrawing = null;
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
					double angle = Math.atan2(e.getX()/zoomX - firstPoint.getX(), e.getY()/zoomX - firstPoint.getY()) + Math.PI/2;
					
					if ( angle < 0)
						angle = (Math.PI - Math.abs(angle)) + Math.PI;

					double newX = e.getX()/zoomX;
					double newY = e.getY()/zoomX;

					if (symmetric) {
						double steppedAngle = (Math.round(angle / STEP) * STEP);
						double dist = firstPoint.distance(e.getX()/zoomX, e.getY()/zoomX);
						newX = (int) (dist * Math.cos(steppedAngle - Math.PI) + firstPoint.getX());
						newY = (int) (dist * Math.sin(steppedAngle) + firstPoint.getY());
					}
					
					((DrawingShapeLine) temporalDrawing).setLine(firstPoint.getX(), firstPoint.getY(), newX, newY);
					setChanged();
					notifyObservers(temporalDrawing);
				}
				e.consume();
			}
		}
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public DrawingShape line(String x1, String y1, String x2, String y2) {
		double iX1 = 0;
		double iY1 = 0;
		double iX2 = 10;
		double iY2 = 10;
		
		try {
			iX1 = Double.parseDouble(x1);
			iY1 = Double.parseDouble(y1);
			iX2 = Double.parseDouble(x2);
			iY2 = Double.parseDouble(y2);
		} catch(Exception e) {
			Utils.logMessage("Wrong format. Setting line to default values.");
		}
		
		DrawingShapeLine drawing = new DrawingShapeLine(user,
				"CustomLine",
				iX1,
				iY1,
				iX2,
				iY2
		);
		
		drawing.setUser(user);
		user.addDrawing(drawing);
		setChanged();
		notifyObservers(drawing);
		
		return drawing;
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
		return false;
	}
}
