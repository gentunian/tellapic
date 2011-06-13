package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class DrawingToolMarker extends DrawingToolLine {
	private static final String MARKER_ICON_PATH = "/icons/tools/marker.png";
	@SuppressWarnings("unused")
	private static final String MARKER_CURSOR_PATH = "/icons/tools/marker-cursor.png";
	private static final double DEFAULT_WIDTH = 20;
	private static final double DEFAULT_ALPHA = .5f;
	private static final int    DEFAULT_CAPS = 0;
	private static final Color  DEFAULT_COLOR = Color.green;
	private static final int    DEFAULT_JOINS = 0;
	private static final float  DEFAULT_MITER_LIMIT = 1;
	
	
	public DrawingToolMarker(String name) {
		//super(tellapicConstants.TOOL_MARKER, name, MARKER_ICON_PATH , Utils.msg.getString("markertooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		super(tellapicConstants.TOOL_MARKER, name);
		setIconPath(MARKER_ICON_PATH);
		setToolTipText(Utils.msg.getString("markertooltip"));
	}
	
	public DrawingToolMarker() {
		this("Marker");
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
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
				if (isBeingUsed()) {
					float zoomX = ControlToolZoom.getInstance().getZoomValue();
					DrawingShape t = (DrawingShape) getTemporalDrawing();
					boolean symmetric = e.isControlDown() | isSymmetricModeEnabled();
					if (symmetric)
						((Line2D)t.getShape()).setLine(firstPoint.getX(), firstPoint.getY(), firstPoint.getX(), e.getY()/zoomX);
					else
						((Line2D)t.getShape()).setLine(firstPoint.getX(), firstPoint.getY(), e.getX()/zoomX, firstPoint.getY());
					setChanged();
					notifyObservers(t);
				}
			}
			e.consume();
		}
	}
}
