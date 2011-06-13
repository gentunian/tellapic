package ar.com.tellapic.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.SessionUtils;
import ar.com.tellapic.StatusBar;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.utils.Utils;

/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingAreaView extends JLabel implements Observer, Scrollable, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private Image                   background;
	private Image                   foreground;
	private RuleHeader              topRule;
	private RuleHeader              leftRule;
	private BufferedImage           backimage;
	private GraphicsEnvironment     ge;
	private GraphicsDevice          gd;
	private GraphicsConfiguration   gc;
	private java.awt.Point          scrollingPoint;
	private StatusBar               statusBar;
	private float                   zoomX = 1f;
	private JPanel                  buttonCorner;
	private JScrollPane             scrollPane;
	private JViewport               viewPort;
	private int                     gridSize = 1;
	private Color                   gridColor = Color.gray;
	private boolean                 gridEnabled = false;
	private float                   gridTransparency = 0.5f;

	
	private static class Holder {
		private final static DrawingAreaView INSTANCE = new DrawingAreaView();
	}
	

	/**
	 * 
	 * @return
	 */
	public static DrawingAreaView getInstance() {
		return Holder.INSTANCE;
	}
	
	
	/*
	 * 
	 */
	private DrawingAreaView() throws NullPointerException {
		/* Get the graphic environment settings */
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		
		/* This is the image to talk about. It should go to background */
		backimage        = null;

		setImage(SessionUtils.getSharedImage());
		
		//TODO: can't we notify the status bar our changes and avoid a reference to it?
		statusBar        = StatusBar.getInstance();
		
		/* Instantiate the top and botom ruler for measure screen size in 'real life' */
		topRule   = new RuleHeader(RuleHeader.HORIZONTAL, true);
		leftRule  = new RuleHeader(RuleHeader.VERTICAL, true);
		
		/* Create a button for switching between inches and centimeters */
		JToggleButton isMetric = new JToggleButton("cm", true);
		buttonCorner = new JPanel();
		isMetric.setFont(new Font("SansSerif", Font.PLAIN, 8));
		isMetric.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JToggleButton isMetric = (JToggleButton) e.getSource();
				boolean v = (e.getStateChange() == ItemEvent.SELECTED);
				scrollPane = (JScrollPane) ((JViewport) getParent()).getParent();
				((RuleHeader)scrollPane.getColumnHeader().getView()).setIsMetric(v);
				((RuleHeader)scrollPane.getRowHeader().getView()).setIsMetric(v);
				if (v)
					isMetric.setText("cm");
				else
					isMetric.setText("in");
			}
		});
		buttonCorner.add(isMetric);
		int screenWidth  = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();
		int startingWidth  = (int) (screenWidth  * .7f);
		int startingHeight = (int) (screenHeight * .9f);
		
		setName(Utils.msg.getString("drawingarea"));
		
		/* This same class will implement mouse event listeners */
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		setAutoscrolls(true);
		addAncestorListener(new AncestorListener(){
			@Override
			public void ancestorAdded(AncestorEvent event) {
				if (backimage != null) {
					scrollPane = (JScrollPane) ((JViewport) getParent()).getParent();
					viewPort = (JViewport) getParent();
					topRule = ((RuleHeader)scrollPane.getColumnHeader().getView());
					topRule.setPreferredWidth(backimage.getWidth());
					leftRule = ((RuleHeader)scrollPane.getRowHeader().getView());
					leftRule.setPreferredHeight(backimage.getHeight());
				}
			}
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			@Override
			public void ancestorRemoved(AncestorEvent event) {}
		});
		
		InputMap inputMap = getInputMap();
		inputMap.put(KeyStroke.getKeyStroke("control Z"), "removeLast");
		inputMap.put(KeyStroke.getKeyStroke("DELETE"), "removeSelectedDrawing");
		ActionMap actionMap = getActionMap();
		actionMap.put("removeLast", new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				UserManager.getInstance().getLocalUser().removeLastDrawing();
			}
		});
		actionMap.put("removeSelectedDrawing", new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				DrawingAreaModel.getInstance().removeSelectedDrawing();
			}
		});
		setVisible(true);
	}


	/**
	 * 
	 * @param enabled
	 */
	public void setRulerEnabled(boolean enabled) {
		JScrollPane scrollPane = (JScrollPane) ((JViewport) getParent()).getParent();
		if (enabled) {
			scrollPane.setColumnHeaderView(topRule);
			scrollPane.setRowHeaderView(leftRule);
			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);
			scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
			scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new Corner());
		} else {
			scrollPane.setColumnHeader(null);
			scrollPane.setRowHeader(null);
		}
	}
	
	/**
	 * 
	 * @param img
	 */
	public void setImage(BufferedImage img) {
		backimage = img;
		
		int screenWidth  = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();
		int startingWidth  = (int) (screenWidth  * .7f);
		int startingHeight = (int) (screenHeight * .9f);
		
		if (backimage == null) {
			Utils.logMessage("No image loaded, wtf?");
			try {
				backimage = ImageIO.read(getClass().getResourceAsStream("/icons/system/noimage.jpg"));
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		setMaximumSize(new Dimension(backimage.getWidth(), backimage.getHeight()));
		setPreferredSize(new Dimension(backimage.getWidth(), backimage.getHeight()));
		foreground = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
		background = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D  drawingArea = (Graphics2D) background.getGraphics();
		drawingArea.drawImage(backimage, 0, 0, null);
		repaint();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object data) {
		if (observable instanceof ControlToolZoom) {
			float newZoom = (Float) data;
			setZoom(
					(int)((ControlToolZoom)observable).getInit().getX(),
					(int) ((ControlToolZoom)observable).getInit().getY(),
					newZoom
			);
		}
		
		Graphics2D  drawingArea = (Graphics2D) background.getGraphics();
		drawingArea.drawImage(backimage, 0, 0, null);
		
		/* Prevent concurrent access */
		AbstractDrawing[] drawingArray = DrawingAreaModel.getInstance().getDrawings().toArray(new AbstractDrawing[0]);

		/** Draw only the visible and not selected drawings */
		for(int i = 0; i < drawingArray.length; i++)
			if (!drawingArray[i].isSelected() && drawingArray[i].isVisible())
				drawingArray[i].draw(drawingArea);

		/* Draw the selected drawings on top */
		for(int i = 0; i < drawingArray.length; i++)
			if (drawingArray[i].isSelected() && drawingArray[i].isVisible())
				drawingArray[i].draw(drawingArea);
		
		List<AbstractUser> users  = UserManager.getInstance().getUsers(); 
		for(int i = 0; i < users.size(); i++)
			if (users.get(i).isDrawing() && users.get(i).isVisible())
				users.get(i).getDrawing().draw(drawingArea);
				
		repaint();
	}
	
	
	//TODO: tener una estructura de datos que contenga lo que se tiene que dibujar
	//en lugar de dibujar todo todo el tiempo. Es decir, si no hubo ningún cambio
	//en la visibilidad de los usuarios, dibujar solamente lo ultimo que se agrego
	//al modelo. En cambio, si se dispara un "evento" para esconder la "capa" de un
	//usuario, llenar una estructura de dato o usar una ya existente en el modelo
	//para dibujar nuevamente todo lo que haya que dibujar. Esto implica tener que 
	//borrar dibujos previos (en el caso de esconder la "capa" de un usuario). Para
	//ello, y para mantener una compatibilidad con la idea de dibujar solamente lo
	//ultimo dibujado, se debe crear de cero (es decir, vacia) una capa transparente
	//que se dibuje arriba de la imagen de fondo.
	@Override
	public void paint(Graphics g1) {
		if (backimage == null)
			return;

		Graphics2D g = (Graphics2D) g1;
		g.drawImage(background, AffineTransform.getScaleInstance(zoomX, zoomX), null);
		
		if(gridEnabled) {

			g.setColor(gridColor);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, gridTransparency));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			/* How many dots (pixels) are in a cm? */
			int dpcm = (int) (((double) RuleHeader.INCH / (double)2.546) * (float)zoomX);

			/* How many lines will be in a cm? */
			int linesInCm =  getGridSize();

			/* How long will be the space between lines */
			double divisionSize = ((double)dpcm / (double)(getGridSize()));

			/* How many vertical unit lines do we need to draw? */
			int vLines = (int) Math.round(getWidth() / dpcm);

			/* How many horizontal unit lines do we need to draw? */
			int hLines = (int) Math.round(getHeight() / dpcm);

			/* Take the problem as divide and conquer in the sense that */
			/* treat it as drawing lines between a centimeter. Repeat it */
			/* until we draw all centimeters. */
			for(int i = 0; i < vLines; i++) {
				int x = dpcm * i;;
				for (int j = 0; j < linesInCm; j++) {
					x += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
					g.drawLine(x, 0, x, getHeight());
				}
			}

			for(int i = 0; i < hLines; i++) {
				int y = dpcm * i;;
				for (int j = 0; j < linesInCm; j++) {
					y += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
					g.drawLine(0, y, getWidth(), y);
				}
			}
		}
	}
	
	/**
	 * @param gridSize the gridSize to set
	 */
	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
		drawBackimage(foreground.getGraphics());
		repaint();
	}

	/**
	 * @return the gridSize
	 */
	public int getGridSize() {
		return gridSize;
	}

	/*
	 * 
	 */
	private void drawBackimage(Graphics drawingArea) {
		drawingArea.drawImage(backimage, 0, 0, null);
		((Graphics2D)drawingArea).drawImage(background, 0, 0, null);
	}
	
	/**
	 * 
	 * @param color
	 */
	public void setGridColor(Color color) {
		gridColor = color;
		repaint();
	}
	
	/**
	 * 
	 * @return
	 */
	public Color getGridColor() {
		return gridColor;
	}
	
	/**
	 * @param gridEnabled the gridEnabled to set
	 */
	public void setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		
		if (foreground == null)
			return;
		repaint();
	}

	/**
	 * @return the gridEnabled
	 */
	public boolean isGridEnabled() {
		return gridEnabled;
	}

	/**
	 * @return
	 */
	public float getGridTransparency() {
		return gridTransparency;
	}

	/**
	 * 
	 * @param alpha
	 */
	public void setGridTransparency(float alpha) {
		gridTransparency  = alpha;
		repaint();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width - 1;
		} else {
			return visibleRect.height - 1;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int currentPosition = 0;
		if (orientation == SwingConstants.HORIZONTAL) {
			currentPosition = visibleRect.x;
		} else {
			currentPosition = visibleRect.y;
		}

		//Return the number of pixels between currentPosition
		//and the nearest tick mark in the indicated direction.
		if (direction < 0) {
			int newPosition = currentPosition -
			(currentPosition / 1)* 1;
			return (newPosition == 0) ? 1 : newPosition;
		} else {
			return ((currentPosition / 1) + 1) * 1 - currentPosition;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent event) {

	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		/* Do nothing if some coordinate is negative */
		if (event.getX() < 0 || event.getY() < 0)
			return;

		/* Do scroll with middle button. Take a point for future references */
		if (event.getButton() == MouseEvent.BUTTON2) {
			scrollingPoint = new java.awt.Point(event.getX(), event.getY());
			return;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent event) {
		
		int xZoomOffset = (int)(event.getX() / zoomX);
		int yZoomOffset = (int)(event.getY() / zoomX);
		
		statusBar.setMouseCoordinates(xZoomOffset, yZoomOffset);
		topRule.update(event.getX(), event.getY(), zoomX);
		leftRule.update(event.getX(), event.getY(), zoomX);
		
		/* Do scroll if we are dragging with the middle button. */
		/* Use the point taken as reference in MousePessed.     */
		if ((event.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK) {
			/* Get the visible rectangle from the drawing areas */
			java.awt.Rectangle clipRect = ((DrawingAreaView) event.getSource()).getVisibleRect();
			
			/* Initialize the upper left corner for the rectangle used */
			/* for scrolling to that area.                             */
			/*                                                         */
			/*    scroll to  r            scroll to  r                 */
			/*               ^                       ^                 */
			/*             +--+                    +--+                */
			/*             |  |                    |  |                */
			/*             +--+--------------------+--+                */
			/*                |                    |                   */
			/*                |     visible        |                   */
			/*                |    rectangle       |                   */
			/*                |    (clipRect)      |                   */
			/*                |                    |                   */
			/*             +--+--------------------+--+                */
			/*             |  |                    |  |                */
			/*             +--+                    +--+                */
			/*               ^                       ^                 */
			/*    scroll to  r             scroll to r                 */
			/*                                                         */
			/*                                                         */
			int x = 0;
			int y = 0;
			java.awt.Rectangle r = null;
			
			/* Accommodate r upper-left corner upon this event coordinates */
			if (scrollingPoint.x >= event.getX())
				x = clipRect.x + clipRect.width;
			else
				x = clipRect.x - Math.abs(scrollingPoint.x - event.getX());

			if (scrollingPoint.y >= event.getY())
				y = clipRect.y + clipRect.height;
			else
				y = clipRect.y - Math.abs(scrollingPoint.y - event.getY());
			
			r = new java.awt.Rectangle(
					x,
					y,
					Math.abs(scrollingPoint.x - event.getX()),
					Math.abs(scrollingPoint.y - event.getY())
			);
			
			/* Scroll to r */
			scrollRectToVisible(r);

			/* End here, as we don't want anything than scrolling */
			return;
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {

	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent event) {
		int xZoomOffset = (int) (event.getX() / zoomX); 
		int yZoomOffset = (int) (event.getY() / zoomX);
		
		statusBar.setMouseCoordinates(xZoomOffset, yZoomOffset);
//		statusBar.setMouseCoordinates(event.getX(), event.getY());
		topRule.update(event.getX(), event.getY(), zoomX);
		leftRule.update(event.getX(), event.getY(), zoomX);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent event) {
		statusBar.showMouseCoordinates(false);
		statusBar.setToolInfo(false);
	}
	
	/*
	 *
	 */
	@Override
	public void mouseEntered(MouseEvent event) {

	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		event.consume();
		int step = (event.getWheelRotation() < 0)? 1 : -1;
		AbstractUser localUser = UserManager.getInstance().getLocalUser();
		Tool tool = localUser.getToolBoxModel().getLastUsedTool();
		
		ControlToolZoom zoomTool = ControlToolZoom.getInstance();
		localUser.getToolBoxModel().setCurrentTool(zoomTool);
		zoomTool.setZoomIn(step > 0);
		MouseEvent newEvent = new MouseEvent(
				this,
				MouseEvent.MOUSE_CLICKED,
				System.currentTimeMillis(),
				event.getModifiers(),
				(int)(event.getX() / zoomX),
				(int)(event.getY() / zoomX),
				0,
				false
		); 
		zoomTool.mouseClicked(newEvent);
		if (tool != null)
			localUser.getToolBoxModel().setCurrentTool(tool);
	}
	
	
	/**
	 * @param step
	 */
	private void setZoom(int x, int y, float newZoom) {
		topRule.update(x, y, newZoom);
		leftRule.update(x, y, newZoom);
		
		Dimension d = new Dimension((int)(backimage.getWidth()*newZoom), (int)(backimage.getHeight()*newZoom));

		setSize(d);
		setPreferredSize(d);
		
		java.awt.Rectangle visibleRect = viewPort.getVisibleRect();
		int xOffset = (int) ((visibleRect.width  - RuleHeader.WIDTH) / 2);
		int yOffset = (int) ((visibleRect.height - RuleHeader.HEIGHT)/ 2);
		int xCenter = (int) (x * newZoom);
		int yCenter = (int) (y * newZoom);

		java.awt.Rectangle newVisibleRect = new java.awt.Rectangle(
				xCenter - xOffset,
				yCenter - yOffset,
				xOffset * 2,
				yOffset * 2
		);

//		System.out.println("Original Size: "+backimage.getWidth()+"x"+backimage.getHeight());
//		System.out.println("New Size: "+((int)(backimage.getWidth()*newZoom)+"x"+(int)(backimage.getHeight()*newZoom)));
//		System.out.println("new zoom: "+newZoom+" old zoom: "+zoomX);
//		System.out.println("event: "+x+","+y);
//		System.out.println("xOffset: "+xOffset+" yOffset: "+yOffset);
//		System.out.println("Visible w: "+(int) (visibleRect.width / newZoom)+" h: "+(int) (visibleRect.height / newZoom));
//		System.out.println("Center in: "+xCenter+","+yCenter+". Check: "+newVisibleRect.getCenterX()+","+newVisibleRect.getCenterY());
//		System.out.println("Top: "+newVisibleRect.x+","+newVisibleRect.y);
//		System.out.println("Bottom: "+(newVisibleRect.width + newVisibleRect.x)+","+(newVisibleRect.height + newVisibleRect.y));
		
		scrollRectToVisible(newVisibleRect);
		zoomX = newZoom;
		repaint();
	}
}
