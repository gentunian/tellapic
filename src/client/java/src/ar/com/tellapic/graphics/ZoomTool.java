package ar.com.tellapic.graphics;

import java.awt.Component;
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

public final class ZoomTool extends ControlTool implements ComboBoxModel{
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
	
	private Point2D     firstPoint;
	private boolean     inUse;
	private boolean     zoomIn;
	private float       zoomFactor;
	private final float zoomStep = 0.25f; 
	private int         current = 3;
	private Cursor      zoomOutCursor;
	private Cursor      zoomInCursor;
	private ListDataListener dataListener;
	
	/*
	 * 
	 */
	private static class Holder {
		private static ZoomTool INSTANCE = new ZoomTool();
	}
	
	private ZoomTool() {
		super(99, ZoomTool.class.getSimpleName(), ZOOM_ICON_PATH, Utils.msg.getString("zoomtooltip"), Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
	public static ZoomTool getInstance() {
		return Holder.INSTANCE;
	}
	
	/**
	 * 
	 * @return
	 */
	public Point2D getInit() {
		return firstPoint;
	}

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
		if (isSelected() && !e.isConsumed()) {
			inUse = true;
			firstPoint.setLocation(e.getX(), e.getY());
			if (zoomIn) {
				zoomFactor += zoomStep;
				if (zoomFactor > MAX_ZOOM_FACTOR) {
					zoomFactor = MAX_ZOOM_FACTOR;
					current = values.length - 1;
				} else {
					current++;
				}
			} else {
				zoomFactor -= zoomStep;
				if (zoomFactor < MIN_ZOOM_FACTOR) {
					zoomFactor = MIN_ZOOM_FACTOR;
					current = 0;
				} else {
					current --;
				}
			}
			dataListener.contentsChanged(new ListDataEvent(values, ListDataEvent.CONTENTS_CHANGED, 0, values.length));
			setChanged();
			notifyObservers(zoomFactor);
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			Component component = e.getComponent();
//			AbstractUser user = UserManager.getInstance().getLocalUser();
			if (zoomIn) {
				setCursor(zoomInCursor);
				component.setCursor(zoomInCursor); 
			}
			else {
				setCursor(zoomOutCursor);
				component.setCursor(zoomOutCursor);
			}
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		if (isSelected() && !e.isConsumed()) {
			Component component = e.getComponent();
			component.setCursor(null);
		}
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


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.ControlTool#hasMoveCapability()
	 */
	@Override
	public boolean hasMoveCapability() {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see ar.com.tellapic.graphics.ControlTool#hasResizeCapability()
	 */
	@Override
	public boolean hasResizeCapability() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return
	 */
	public float getZoomValue() {
		return zoomFactor;
	}
}
