package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class DrawingToolEllipse extends DrawingTool {
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
	
	/**
	 * 
	 * @param name
	 */
	public DrawingToolEllipse(String name) {
		super(tellapicConstants.TOOL_ELLIPSE, name, ELLIPSE_ICON_PATH, Utils.msg.getString("ellipsetooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		setAlias("Ellipse");
		COMMANDS = new String[][] {
				{ "circle", "ellipse" },
				{ getClass().getPackage().getName()+".DrawingShape Draws a circle with center (x, y) and the specified diameter.", "int x The center x coordinate", "int y The center y coordinate", "int diameter The circle diameter" },
				{ getClass().getPackage().getName()+".DrawingShape Draws the ellipse that fits in the rectangle specified by (top, left) (left + width, top + height)", "int left The left coordinate", "int top The top coordinate ", "int width The ellipse width frame", "int height The ellipse frame height" }
		};
	}
	
	/**
	 * 
	 */
	public DrawingToolEllipse() {
		this("DrawingToolEllipse");
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#hasAlphaCapability()
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
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				float zoomX = ControlToolZoom.getInstance().getZoomValue();
				firstPoint.setLocation(e.getX()/zoomX, e.getY()/zoomX);
				setInUse(true);
				temporalDrawing = new DrawingShapeEllipse(user, getName(), e.getX()/zoomX, e.getY()/zoomX, 0, 0);
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
				DrawingShapeEllipse drawingEllipse = (DrawingShapeEllipse) temporalDrawing;
				if (drawingEllipse != null && !drawingEllipse.isEmpty()) {
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

					((DrawingShapeEllipse) temporalDrawing).setFrame(point, size);
					
					setChanged();
					notifyObservers(temporalDrawing);
				}
			}
			e.consume();
		}
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @return
	 */
	public DrawingShape circle(String x, String y, String diameter) {
		int left = 0;
		int top  = 0;
		try {
			left = Integer.parseInt(x) - Integer.parseInt(diameter)/2;
			top  = Integer.parseInt(y) - Integer.parseInt(diameter)/2;
		} catch(Exception e) {
			Utils.logMessage("Wrong format. Setting circle default values");
		}
		
		return ellipse(String.valueOf(left), String.valueOf(top), diameter, diameter);
	}
	
	/**
	 * 
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @return
	 */
	public DrawingShape ellipse(String left, String top, String width, String height) {
		double ileft   = 0;
		double itop    = 0;
		double iwidth  = 100;
		double iheight = 100;
		try {
			ileft   = Double.parseDouble(left);
			itop    = Double.parseDouble(top);
			iwidth  = Double.parseDouble(width);
			iheight =Double.parseDouble(height);
		} catch(Exception e) {
			Utils.logMessage("Wrong format. Setting ellipse default values.");
		}
		DrawingShapeEllipse drawing = new DrawingShapeEllipse(user, "CustomEllipse",
				ileft,
				itop,
				iwidth,
				iheight
		);

//		IToolBoxState toolBoxState = user.getToolBoxModel();

//		drawing.setPaintPropertyAlpha(toolBoxState.getOpacityProperty());
//		drawing.setPaintPropertyColor(toolBoxState.getColorProperty());
//		drawing.setPaintPropertyStroke(toolBoxState.getStrokeProperty());
//		drawing.setRenderingHints(toolBoxState.getRenderingHints());
		drawing.setUser(user);
//		drawing.cloneProperties();
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
		// TODO Auto-generated method stub
		return false;
	}
}
