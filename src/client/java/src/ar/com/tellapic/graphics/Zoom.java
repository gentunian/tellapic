package ar.com.tellapic.graphics;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ar.com.tellapic.utils.Utils;

public final class Zoom extends ControlTool implements ComboBoxModel{
	
	private Point2D firstPoint;
	private boolean inUse;
	private boolean zoomIn;
	private float zoomFactor;
	private final float zoomStep = 0.25f; 
	public static final String ZOOM_ICON_PATH = "/icons/tools/zoom.png";
	public static final String ZOOMIN_CURSOR_PATH = "/icons/tools/zoomInCursor.png";
	public static final String ZOOMOUT_CURSOR_PATH = "/icons/tools/zoomOutCursor.png";
	public static final String ZOOMIN_ICON_PATH = "/icons/tools/zoomin.png";
	public static final String ZOOMOUT_ICON_PATH = "/icons/tools/zoomout.png";
	public static final String ZOOMTOFIT_ICON_PATH = "/icons/tools/zoomtofit.png";
	public static final String ZOOMTOSIZE_ICON_PATH = "/icons/tools/zoomtosize.png";
	public static final float MAX_ZOOM_FACTOR = 3.0f;
	public static final float MIN_ZOOM_FACTOR = 0.25f;
	
	private static final String[] values = new String[] { "25%", "50%", "75%", "100%", "125%", "150%", "175%", "200%", "225%", "250%", "275%", "300%"};
	
	private int current = 3;
	
	private static class Holder {
		private static Zoom INSTANCE = new Zoom();
	}
	
	private Cursor zoomOutCursor;
	private Cursor zoomInCursor;
	private ListDataListener dataListener;
	
	private Zoom() {
		super(99, Zoom.class.getSimpleName(), ZOOM_ICON_PATH, Utils.msg.getString("zoomtooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		firstPoint = new Point2D.Double();
		zoomFactor = 1;
		Toolkit toolkit = Toolkit.getDefaultToolkit();
//		Image image = toolkit.getImage(ZOOMOUT_ICON_PATH);
		Image image = Utils.createIconImage(16, 16, ZOOMOUT_ICON_PATH);
		zoomOutCursor = toolkit.createCustomCursor(image, new Point(10,10), getName()+"Out");
		image = Utils.createIconImage(16, 16, ZOOMIN_ICON_PATH);
		zoomInCursor = toolkit.createCustomCursor(image, new Point(10,10), getName()+"In");
		
		setZoomIn(true);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static Zoom getInstance() {
		return Holder.INSTANCE;
	}
	
	
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#getInit()
//	 */
//	@Override
//	public Point2D getInit() {
//		return firstPoint;
//	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.ControlTool#hasZoomCapability()
	 */
	@Override
	public boolean hasZoomCapability() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.Tool#isBeingUsed()
	 */
	@Override
	public boolean isBeingUsed() {
		return inUse;
	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnDragSupported()
//	 */
//	@Override
//	public boolean isOnDragSupported() {
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnMoveSupported()
//	 */
//	@Override
//	public boolean isOnMoveSupported() {
//		return false;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnPressSupported()
//	 */
//	@Override
//	public boolean isOnPressSupported() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#isOnReleaseSupported()
//	 */
//	@Override
//	public boolean isOnReleaseSupported() {
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onDrag(int, int, int, int)
//	 */
//	@Override
//	public void onDrag(int x, int y, int button, int mask) {
//
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onMove(int, int)
//	 */
//	@Override
//	public void onMove(int x, int y) {
//
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onPause()
//	 */
//	@Override
//	public void onPause() {
//		inUse = false;
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onPress(int, int, int, int)
//	 */
//	@Override
//	public void onPress(int x, int y, int button, int mask) {
//		inUse = true;
//		firstPoint.setLocation(x, y);
////		zoomFactor += ((zoomIn)? zoomStep : -1*zoomStep);
//		
//		if (zoomIn) {
//			zoomFactor += zoomStep;
//			if (zoomFactor > MAX_ZOOM_FACTOR) {
//				zoomFactor = MAX_ZOOM_FACTOR;
//				current = values.length - 1;
//			} else {
//				current++;
//			}
//		} else {
//			zoomFactor -= zoomStep;
//			if (zoomFactor < MIN_ZOOM_FACTOR) {
//				zoomFactor = MIN_ZOOM_FACTOR;
//				current = 0;
//			} else {
//				current --;
//			}
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onRelease(int, int, int, int)
//	 */
//	@Override
//	public void onRelease(int x, int y, int button, int mask) {
//		inUse = false;
//		dataListener.contentsChanged(new ListDataEvent(values, ListDataEvent.CONTENTS_CHANGED, 0, values.length));
//		setChanged();
//		notifyObservers(zoomFactor);
//	}
//
//	/* (non-Javadoc)
//	 * @see ar.com.tellapic.graphics.Tool#onRestore()
//	 */
//	@Override
//	public void onRestore() {
//		if (!inUse)
//			inUse = true;
//	}


	/**
	 * @param value
	 */
	public void setZoomIn(boolean value) {
		zoomIn = value;
		if (zoomIn)
			setCursor(zoomInCursor);
		else
			setCursor(zoomOutCursor);
	}


	/**
	 * @param value
	 */
	public void setZoom(float value) {
		zoomFactor = value;
		
		if (zoomFactor > MAX_ZOOM_FACTOR)
			zoomFactor = MAX_ZOOM_FACTOR;
		
		if (zoomFactor < MIN_ZOOM_FACTOR)
			zoomFactor = MIN_ZOOM_FACTOR;
		
		if (value == 1) //TODO: LOL WTF?
			current = 3;
		
		dataListener.contentsChanged(new ListDataEvent(values, ListDataEvent.CONTENTS_CHANGED, 0, values.length));
		setChanged();
		notifyObservers(zoomFactor);
	}


	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#getSelectedItem()
	 */
	@Override
	public Object getSelectedItem() {
		return values[current];
	}


	/* (non-Javadoc)
	 * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
	 */
	@Override
	public void setSelectedItem(Object anItem) {
		zoomFactor = Float.valueOf(((String)anItem).substring(0, ((String)anItem).length() - 1)) / 100;
		
		if (zoomFactor > MAX_ZOOM_FACTOR)
			zoomFactor = MAX_ZOOM_FACTOR;
		
		if (zoomFactor < MIN_ZOOM_FACTOR)
			zoomFactor = MIN_ZOOM_FACTOR;
		
		for(int i = 0; i < values.length; i++) {
			if (values[i].equals(anItem))
				current = i;
		}
		setChanged();
		notifyObservers(zoomFactor);
	}


	/* (non-Javadoc)
	 * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void addListDataListener(ListDataListener l) {
		dataListener = l;
	}


	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public Object getElementAt(int index) {
		return values[index];
	}


	/* (non-Javadoc)
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return values.length;
	}


	/* (non-Javadoc)
	 * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
	 */
	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}
}
