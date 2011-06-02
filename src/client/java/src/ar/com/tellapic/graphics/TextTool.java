package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

public class TextTool extends DrawingTool {
	private Point2D            firstPoint;
	private static final String TEXT_ICON_PATH = "/icons/tools/text.png";
	private static final String TEXT_CURSOR_PATH = "/icons/tools/text-cursor.png";
	private static final double DEFAULT_ALPHA = 1;
	private static final double DEFAULT_WIDTH = 5;
	private static final Color  DEFAULT_COLOR = Color.black;
	private static final String DEFAULT_FACE = "Droid-10";
	private static final double DEFAULT_SIZE = 12;
	private static final int    DEFAULT_STYLE = 0;
	
	private DrawingText            temporalDrawing;
	
	
	
	public TextTool(String name) {
		super(tellapicConstants.TOOL_TEXT, name, TEXT_ICON_PATH, Utils.msg.getString("texttooltip"), Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		firstPoint = new Point2D.Double();
		//temporalDrawing = new DrawingText(getName());
	}
	
	
	public TextTool() {
		this("Text");
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
		return true;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#hasStrokeCapability()
	 */
	@Override
	public boolean hasStrokeCapability() {
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
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
		return 0;
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
		return DEFAULT_FACE;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontSize()
	 */
	@Override
	public double getDefaultFontSize() {
		return DEFAULT_SIZE;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultFontStyle()
	 */
	@Override
	public int getDefaultFontStyle() {
		return DEFAULT_STYLE;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultJoins()
	 */
	@Override
	public int getDefaultJoins() {
		return 0;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getDefaultMiterLimit()
	 */
	@Override
	public float getDefaultMiterLimit() {
		return 10;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.DrawingTool#getTemporalDrawing()
	 */
	@Override
	public AbstractDrawing getTemporalDrawing() {
		return temporalDrawing;
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
			AbstractUser user = null;
			if (e instanceof RemoteMouseEvent) {
				user = ((RemoteMouseEvent)e).getUser();
			} else {
				user = UserManager.getInstance().getLocalUser();
			}
			IToolBoxState toolBoxState = user.getToolBoxModel();
			firstPoint.setLocation(e.getX(), e.getY());
			temporalDrawing = new DrawingText(getName());
			temporalDrawing.setTextX(e.getX());
			temporalDrawing.setTextY(e.getY());
			temporalDrawing.setAlpha(toolBoxState.getOpacityProperty());
			temporalDrawing.setColor(toolBoxState.getColorProperty());
			temporalDrawing.setNumber(toolBoxState.getAssignedNumber());
			temporalDrawing.setUser(user);
			user.setTemporalDrawing(temporalDrawing);
			setChanged();
			notifyObservers(temporalDrawing);
			e.consume();
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			if (temporalDrawing.getText().length() > 0) {
				AbstractUser user = null;
				if (e instanceof RemoteMouseEvent) {
					user = ((RemoteMouseEvent)e).getUser();
				} else {
					user = UserManager.getInstance().getLocalUser();
				}
				temporalDrawing.setTextX((int) firstPoint.getX());
				temporalDrawing.setTextY((int) firstPoint.getY());
				user.addDrawing(temporalDrawing);
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
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			firstPoint.setLocation(e.getX(), e.getY());
			AbstractUser user = null;
			if (e instanceof RemoteMouseEvent) {
				user = ((RemoteMouseEvent)e).getUser();
			} else {
				user = UserManager.getInstance().getLocalUser();
			}
			IToolBoxState toolBoxState = user.getToolBoxModel();
			if (temporalDrawing == null)
				temporalDrawing = new DrawingText(getName());
			temporalDrawing.setTextX(e.getX());
			temporalDrawing.setTextY(e.getY());
			temporalDrawing.setAlpha(toolBoxState.getOpacityProperty());
			temporalDrawing.setColor(toolBoxState.getColorProperty());
			temporalDrawing.setNumber(toolBoxState.getAssignedNumber());
			setChanged();
			notifyObservers(temporalDrawing);
			e.consume();
		}
	}
}
