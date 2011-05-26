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
import javax.swing.SwingUtilities;
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

//	private static final float MAX_ZOOM_FACTOR = 3.0f;
//
//	private static final float MIN_ZOOM_FACTOR = 0.25f;

	private Image                   background;
	private Image                   foreground;
//	private Grid                    grid;
	private RuleHeader              topRule;
	private RuleHeader              leftRule;
	//private Drawing                 temporalDrawing;
//	private ArrayList<Drawing>      temporalDrawings;
//	private ArrayList<AbstractUser> userList;
	private BufferedImage           backimage;
	private GraphicsEnvironment     ge;
	private GraphicsDevice          gd;
	private GraphicsConfiguration   gc;
	private RenderingHints          rh;
	private PaintPropertyController propertyController;
	private AbstractUser            user;
	private java.awt.Point          scrollingPoint;
	private StatusBar               statusBar;
	private float                   zoomX = 1f;
	private JPanel                  buttonCorner;
	private JScrollPane             scrollPane;
	private JViewport               viewPort;
	
	private int gridSize = 1;

	private Color gridColor = Color.gray;

	private boolean gridEnabled = false;

	private float gridTransparency = 0.5f;

	
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
//		temporalDrawings = new ArrayList<Drawing>();
//		userList         = new ArrayList<AbstractUser>();
		
		/* Get the graphic environment settings */
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		
		propertyController = null;
		
		/* This is the image to talk about. It should go to background */
		backimage        = null;
		
		/* The local user drawing over this area view */
		user             = UserManager.getInstance().getLocalUser();
		
		if (user == null)
			throw new NullPointerException("DrawingAreaView cannot be fetched before a local user creation.");
		
		setImage(SessionUtils.getSharedImage());
		
		//TODO: can't we notify the status bar our changes and avoid a reference to it?
		statusBar        = StatusBar.getInstance();
		
		/* Instantiate a default rendering quality for later use or change */
		rh = new RenderingHints(null);
		
//		scrollPane = new JScrollPane(this);
		
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
//				((RuleHeader)scrollPane.getColumnHeader().getView()).setIsMetric(v);
//				((RuleHeader)scrollPane.getRowHeader().getView()).setIsMetric(v);
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

//		scrollPane.setName(getName());
		
		int screenWidth  = gd.getDisplayMode().getWidth();
		int screenHeight = gd.getDisplayMode().getHeight();
		int startingWidth  = (int) (screenWidth  * .7f);
		int startingHeight = (int) (screenHeight * .9f);
		
		setName(Utils.msg.getString("drawingarea"));
//		setPreferredSize(new Dimension(startingWidth, startingHeight));
//		setMaximumSize(new Dimension(screenWidth, screenHeight));
//		topRule.setPreferredWidth(startingWidth);
//		leftRule.setPreferredHeight(startingHeight);
		
		/* This same class will implement mouse event listeners */
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		setAutoscrolls(true);
		addAncestorListener(new AncestorListener(){
			@Override
			public void ancestorAdded(AncestorEvent event) {
//				System.out.println("ancestor: "+event.getAncestor());
//				System.out.println("ancestor: "+event.getAncestorParent());
//				System.out.println("ancestor: "+event.getComponent());
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
		ActionMap actionMap = getActionMap();
		actionMap.put("removeLast", new AbstractAction(){
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				user.removeLastDrawing();
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
	 * @param rh
	 */
	public void setRenderingHints(RenderingHints rh) {
		this.rh = rh;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public RenderingHints getRenderingHints() {
		return rh;
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
	
	

	
	/**
	 * 
	 * @param user
	 */
//	public void addPainter(AbstractUser user) {
//		userList.add(user);
//	}
	
//	Graphics2D permanentArea;

	//TODO: REFACTOR THIS THINKING ON MULTIPLE ACCESS TO THE DRAWING AREA VIEW (A.K.A. MULTI-THREADED)
	@Override
	public void update(Observable observable, Object data) {
//		Utils.logMessage("Updating drawing area view: "+data);
//		AbstractUser user = (AbstractUser) o;
//		Integer     index = null;
		
		// If an user has finish drawing, it will provide an index. Or, if an add drawing remote event
		// adds a drawing to a remote user drawing list, then 'data' will be the index of that added drawing
//		if (data instanceof Integer)
//			index = (Integer) data;
		
		//TODO: this makes no sense to be put here. This should be used just once when
		//a background image (backImage) is loaded and this panel is displayed. Remove this!
//		if (background == null && backimage != null)
//			background = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
//
//		if (foreground == null)
//			return;

//		permanentArea = (Graphics2D) background.getGraphics();
		
//		Graphics2D permanentArea = (Graphics2D) backimage.getGraphics();
//		Graphics2D permanentArea     = (Graphics2D) foreground.getGraphics();
		
		/****************************************/
		/*   The image will never be modified   */
		/****************************************/
//		drawBackimage(permanentArea);
		

//		if (!user.isRemoved() && user.isVisible()) {
			//Utils.logMessage("\tUser is visible: "+user);
//			Drawing firstNotDrawn   = (index == null)? null : user.getDrawings().get(index);
			
			//TODO: Se deberia tener una lista de todos los temporales que se estan dibujando de
			//todos los usuarios. Es decir, los temporales de los usuarios que estan dibujando.
			//Drawing temporalDrawing = user.getTemporalDrawing();
						
			
//			if (firstNotDrawn != null) {
//				drawDrawing(permanentArea, firstNotDrawn, null);
				
//				Utils.logMessage("\tFirstNotDrawn drawn: "+user);
//			} 

			
			/**********************************************************/
			/* This layer is translucent and holds each drawing on it */
			/**********************************************************/
//			frontArea.drawImage(background, 0, 0, null);

//			if (temporalDrawing != null) {
//				drawDrawing(frontArea, temporalDrawing, null);
//				Utils.logMessage("\tTemporalDrawing drawn: "+user);
//			}
//		} 
//		else
//			/**********************************************************/
//			/* This layer is translucent and holds each drawing on it */
//			/**********************************************************/
//			frontArea.drawImage(background, 0, 0, null);
		
		Graphics2D  drawingArea = (Graphics2D) background.getGraphics();
		drawingArea.drawImage(backimage, 0, 0, null);
		drawingArea.setRenderingHints(rh);
		
		for(AbstractUser user : UserManager.getInstance().getUsers()) {
			if (!user.isRemoved() && user.isVisible()) {
				for(Drawing drawing : user.getDrawings())
					drawDrawing(drawingArea, drawing, user.getCustomProperties());
				
				if (user.isDrawing())
					drawDrawing(drawingArea, user.getDrawing(), user.getCustomProperties());
			}
		}
		
		if (observable instanceof DrawingTool && ((DrawingTool)observable).isBeingUsed()) {
			DrawingTool drawingTool = (DrawingTool) observable;
			Drawing         drawing = drawingTool.getTemporalDrawing();
			AbstractUser user = drawing.getUser();
			
			if (user != null && user.isVisible())
				drawDrawing(drawingArea, drawing, user.getCustomProperties());
			
		} else if (observable instanceof Zoom) {
			float newZoom = (Float) data;
			setZoom(
					(int)((Zoom)observable).getInit().getX(),
					(int) ((Zoom)observable).getInit().getY(),
					newZoom
			);
		}
		
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
//		super.paint(g1);
		
		if (backimage == null)
			return;

		Graphics2D g = (Graphics2D) g1;
//		Graphics2D frontArea = (Graphics2D) background.getGraphics();
		
//		drawBackimage(frontArea);
		
		/* This will set the rendering quality */
//		frontArea.setRenderingHints(rh);
		
		/* */
//		frontArea.drawImage(backimage, 0, 0, null);
//		frontArea.drawImage(background, 0, 0, null);
//		frontArea.drawImage(background, AffineTransform.getScaleInstance(zoomX, zoomX), null);
		
		/* This draws the temporal 'while drawing' object of each user, if any */
//		for(AbstractUser user : UserManager.getInstance().getUsers().values()) {
//			if (!user.isRemoved() && user.isVisible()) {
//				Drawing d = user.getTemporalDrawing();
//				if (d != null) {
//					drawDrawing(frontArea, d, null);
//				} else {
//					for(Drawing drawing : user.getDrawings()) {
//						System.out.println("LOL");
//						drawDrawing(frontArea, drawing, null);
//					}
//				}
//			}
//		}

//		((Graphics2D)g).drawImage(foreground, 0, 0, null);
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
//		((Graphics2D)g).scale(zoomX, zoomX);
		
//		((Graphics2D)g).drawImage(foreground, AffineTransform.getScaleInstance(zoomX, zoomX), null);
	}
	
	
	/**
	 * 
	 * @param drawing
	 * @param i
	 */
//	public void update(Drawing drawing, int i) {
//		//temporalDrawing = drawing;
//		temporalDrawings.set(i, drawing);
//		repaint();
//	}
	
	
	private void drawDrawing(Graphics2D g, Drawing drawing, PaintProperty[] overridedProperties) {
		if (drawing != null && drawing.isVisible()) {
			g.setRenderingHints(rh);
			
			if (drawing.hasAlphaProperty()) {
				if (overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA] != null)
					g.setComposite(((PaintPropertyAlpha)overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_ALPHA]).getComposite());
				else
					g.setComposite(drawing.getPaintPropertyAlpha().getComposite());
			}
			
			if (drawing.hasColorProperty()) {
				if (overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR] != null)
					g.setColor(((PaintPropertyColor)overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR]).getColor());
				else
					g.setColor(drawing.getPaintPropertyColor().getColor());
			}
			
			if (drawing.hasStrokeProperty()) {
				if (overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_STROKE] != null)
					g.setStroke(((PaintPropertyStroke)overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_STROKE]).getStroke());
				else
					g.setStroke(drawing.getPaintPropertyStroke().getStroke());
			}
			
			if (drawing.hasShape())
				g.draw(drawing.getShape());
			
			if (drawing.hasFontProperty()) {
					if (overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_FONT] != null) 
						g.setFont(((PaintPropertyFont)overridedProperties[AbstractUser.CUSTOM_PAINT_PROPERTY_FONT]).getFont());
					else
						g.setFont(drawing.getPaintPropertyFont().getFont());
					
				g.drawString(drawing.getText(), drawing.getTextX(), drawing.getTextY());
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
//		Graphics2D frontArea = (Graphics2D) foreground.getGraphics();
		

//		Image zoomedBackimage = null;
//		if (doZoom) {
//			zoomedBackimage = backimage.getScaledInstance((int)(backimage.getWidth() * zoomX), (int) (backimage.getHeight() * zoomX), BufferedImage.SCALE_REPLICATE);
//		}
//		else {
//			zoomedBackimage = backimage;
//		}
		

		drawingArea.drawImage(backimage, 0, 0, null);
		
//		((Graphics2D)drawingArea).drawImage(backimage, AffineTransform.getScaleInstance(zoomX, zoomX), null);
		
//		((Graphics2D)drawingArea).drawImage(zoomedBackimage, 0, 0, null);
		//		if (grid != null && grid.isGridEnabled())
		//			frontArea.drawImage(grid, AffineTransform.getScaleInstance(zoomX, zoomX), null);
		//			frontArea.drawImage(grid, 0, 0, null);

//		if(gridEnabled) {
//			
//			drawingArea.setColor(gridColor);
//			((Graphics2D)drawingArea).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, gridTransparency));
////			
//			/* How many dots (pixels) are in a cm? */
//			int dpcm = (int) (((double) RuleHeader.INCH / (double)2.546) * (float)zoomX);
//
//			/* How many lines will be in a cm? */
//			int linesInCm =  getGridSize();
//
//			/* How long will be the space between lines */
//			double divisionSize = ((double)dpcm / (double)(getGridSize()));
//
//			/* How many vertical unit lines do we need to draw? */
//			int vLines = (int) Math.round(getWidth() / dpcm);
//
//			/* How many horizontal unit lines do we need to draw? */
//			int hLines = (int) Math.round(getHeight() / dpcm);
//
//			/* Take the problem as divide and conquer in the sense that */
//			/* treat it as drawing lines between a centimeter. Repeat it */
//			/* until we draw all centimeters. */
//			for(int i = 0; i < vLines; i++) {
//				int x = dpcm * i;;
//				for (int j = 0; j < linesInCm; j++) {
//					x += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
//					drawingArea.drawLine(x, 0, x, getHeight());
//				}
//			}
//
//			for(int i = 0; i < hLines; i++) {
//				int y = dpcm * i;;
//				for (int j = 0; j < linesInCm; j++) {
//					y += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
//					drawingArea.drawLine(0, y, getWidth(), y);
//				}
//			}
//		}
//		((Graphics2D)drawingArea).drawImage(foreground, 0, 0, null);
		((Graphics2D)drawingArea).drawImage(background, 0, 0, null);
//		((Graphics2D)drawingArea).drawImage(background, AffineTransform.getScaleInstance(oldZoom, oldZoom), null);
	}

	
	
	/**
	 * 
	 * @param color
	 */
	public void setGridColor(Color color) {
		gridColor = color;
//		drawBackimage(foreground.getGraphics());
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
		
//		drawBackimage(foreground.getGraphics());
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
		
		//drawBackimage(foreground.getGraphics());
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
//		Tool tool = user.getToolBoxModel().getLastUsedTool();
//		
//		if (tool == null)
//			return;
//		
//		if (tool.isOnPressSupported() && tool.isOnReleaseSupported()) {
//			tool.onPress(event.getX(), event.getY(), event.getButton(), event.getModifiers());
//			tool.onRelease(event.getX(), event.getY(), event.getButton(), event.getModifiers());
//		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent event) {
		//Utils.printEventInfo(event);
		
		/* Do nothing if some coordinate is negative */
		if (event.getX() < 0 || event.getY() < 0)
			return;

		/* Do scroll with middle button. Take a point for future references */
		if (event.getButton() == MouseEvent.BUTTON2) {
			scrollingPoint = new java.awt.Point(event.getX(), event.getY());
			return;
		}
		
		
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();

		int xZoomOffset = (int)(event.getX() / zoomX);
		int yZoomOffset = (int)(event.getY() / zoomX);
		
		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		/* If button is the left one, start using the tool */
		if (event.getButton() == MouseEvent.BUTTON1) {
			if (usedTool instanceof DrawingTool) {
				DrawingTool drawingTool = (DrawingTool) usedTool;
				Drawing temporalDrawing = drawingTool.getTemporalDrawing();
				
				temporalDrawing.setUser(user);
				user.setTemporalDrawing(temporalDrawing);
				
				if (drawingTool.hasAlphaCapability())
					drawingTool.setAlpha(toolBoxState.getOpacityProperty());

				if (drawingTool.hasColorCapability())
					drawingTool.setColor(toolBoxState.getColorProperty());

				if (drawingTool.hasStrokeCapability())
					drawingTool.setStroke(toolBoxState.getStrokeProperty());

				if (drawingTool.hasFontCapability())
					drawingTool.setFont(toolBoxState.getFontProperty());
			}
			
			if (usedTool.isOnPressSupported())
				usedTool.onPress(xZoomOffset, yZoomOffset, event.getButton(), event.getModifiers());

		} else {
			/* If we press another button, just stop using the tool */
			//TODO: The tool is actually paused. Rename the tool's method onCancel() to onPaused().
			usedTool.onPause();
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
		
		Tool usedTool = user.getToolBoxModel().getLastUsedTool();
		
		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		if (usedTool.isOnDragSupported()) {
			/* Do nothing if some coordinate is negative */
			if (event.getX() < 0 || event.getY() < 0)
				usedTool.onPause();
			else {
				if (usedTool.isBeingUsed())
					
					usedTool.onDrag(xZoomOffset, yZoomOffset, event.getButton(), event.getModifiersEx());
				else
					usedTool.onRestore();
			}
//			if (usedTool instanceof DrawingTool) {
				// This will trigger an update() to the DrawingAreaView
//				user.setTemporalDrawing(((DrawingTool)usedTool).getDrawing());
//			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent event) {
		//Utils.printEventInfo(event);
		Tool         usedTool = user.getToolBoxModel().getLastUsedTool();
		
		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		int xZoomOffset = (int)(event.getX() / zoomX);
		int yZoomOffset = (int)(event.getY() / zoomX);
		
		/* Release event won't use temporal drawing anymore */
		user.setTemporalDrawing(null);
		
		if (usedTool.isBeingUsed() && event.getButton() == MouseEvent.BUTTON1) {
			
			if (usedTool.isOnReleaseSupported())
				usedTool.onRelease(xZoomOffset, yZoomOffset, event.getButton(), event.getModifiersEx());
			
			if (usedTool instanceof DrawingTool) {
			
//				Drawing drawing = ((DrawingTool)usedTool).finishDrawing();
//				if (drawing == null) 
//					return;
			
				// This will trigger an update() to the DrawingAreaView
				//user.addDrawing(drawing);
			
				return;
			}
		}
		
		
		if (event.getButton() == MouseEvent.BUTTON3 && event.getModifiersEx() == java.awt.event.InputEvent.BUTTON1_DOWN_MASK) {
			usedTool.onRestore();
			return;
		}
		
		//TODO: Reveer esto (?)
//		if (event.getButton() == MouseEvent.BUTTON3)
//			user.setTemporalDrawing(null);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseMoved(MouseEvent event) {
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool              usedTool = toolBoxState.getLastUsedTool();
		
		int xZoomOffset = (int) (event.getX() / zoomX); 
		int yZoomOffset = (int) (event.getY() / zoomX);
		
		statusBar.setMouseCoordinates(xZoomOffset, yZoomOffset);
//		statusBar.setMouseCoordinates(event.getX(), event.getY());
		topRule.update(event.getX(), event.getY(), zoomX);
		leftRule.update(event.getX(), event.getY(), zoomX);
		
		if (usedTool == null)
			return;
		
		if (usedTool.isOnMoveSupported()) {
			if (usedTool instanceof DrawingTool) {
				DrawingTool drawingTool = (DrawingTool) usedTool;
				Drawing     drawing = drawingTool.getTemporalDrawing();
				drawing.setUser(user);
				if (drawingTool.hasAlphaCapability())
					drawing.setAlpha(toolBoxState.getOpacityProperty());
				
				if (drawingTool.hasColorCapability())
					drawing.setColor(toolBoxState.getColorProperty());
				
				if (drawingTool.hasStrokeCapability())
					drawing.setStroke(toolBoxState.getStrokeProperty());
				
				if (drawingTool.hasFontCapability())
					drawing.setFont(toolBoxState.getFontProperty());
				
				if (drawingTool.hasColorCapability())
					drawing.setColor(toolBoxState.getColorProperty());
				
//				drawingTool.onMove(event.getX(), event.getY());
				drawingTool.onMove(xZoomOffset, yZoomOffset);
				
//				user.setTemporalDrawing(drawingTool.getDrawing());
			}
			
		}
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
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool              usedTool = toolBoxState.getLastUsedTool();
	
		if (usedTool == null)
			return;
	
		setCursor(usedTool.getCursor());
		statusBar.setToolInfo(usedTool.getIconPath(), usedTool.getToolTipText());
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		int step = (event.getWheelRotation() < 0)? 1 : -1;
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
		
		if (usedTool == null) {
			Zoom zoom = Zoom.getInstance();
			zoom.setZoomIn(step > 0);
			zoom.onPress((int)(event.getX() / zoomX), (int) (event.getY() / zoomX), 0, 0);
			zoom.onRelease((int)(event.getX() / zoomX), (int) (event.getY() / zoomX), 0, 0);
			return;
		}
		
		if (propertyController == null)
			return;
		
		//if (usedTool.hasZoomProperties())
		
		if (usedTool instanceof DrawingTool ) {

			/* If the tool is currently in use */
			if (usedTool.isBeingUsed()) {
				
				/* This is for changing tool properties while drawing */
				if (!((DrawingTool)usedTool).isLiveModeSupported()) {
					/* If shift is pressed change the opacity if the tool supports it*/
					if (event.isShiftDown()) {

						/* Does this tool support opacity changes? */
						if (((DrawingTool)usedTool).hasAlphaCapability())
							propertyController.handleOpacityChange(toolBoxState.getOpacityProperty().alpha + 0.1f * step);

						/* else, change the width */
					} else {

						/* Does this tool support stroke width changes? */
						if (((DrawingTool)usedTool).hasStrokeCapability())
							propertyController.handleWidthChange((int)toolBoxState.getStrokeProperty().getWidth() + step);

						/* Does this tool support font width changes? */
						else if (((DrawingTool)usedTool).hasFontCapability())
							propertyController.handleFontSizeChange(toolBoxState.getFontProperty().getSize() + step);
					}
				}
				return;
			}

			Zoom zoom = Zoom.getInstance();
			zoom.setZoomIn(step > 0);
			zoom.onPress((int)(event.getX() / zoomX), (int) (event.getY() / zoomX), 0, 0);
			zoom.onRelease((int)(event.getX() / zoomX), (int) (event.getY() / zoomX), 0, 0);
			
//			if (((DrawingTool)usedTool).getDrawing() != null)
//				user.setTemporalDrawing(((DrawingTool)usedTool).getDrawing());
			
		} else {
			/* This is a control tool if it's not a drawing tool*/
			if (usedTool instanceof Zoom) {
				((Zoom)usedTool).setZoomIn(step > 0);
				usedTool.onPress(event.getX(), event.getY(), 0, 0);
				usedTool.onRelease(event.getX(), event.getY(), 0, 0);
			}
		}
	}
	
	
	/**
	 * @param step
	 */
	private void setZoom(int x, int y, float newZoom) {
//		int xCenter = (int) (event.getX() / zoomX);
//		int yCenter = (int) (event.getY() / zoomX);
//		float oldZoomX = zoomX;
//		float zoomStep = 0.25f * ((event.getWheelRotation() < 0)? 1 : -1);
//		zoomX += zoomStep;
		
		if (newZoom < Zoom.MIN_ZOOM_FACTOR)
			newZoom = Zoom.MIN_ZOOM_FACTOR;
		
		if (newZoom > Zoom.MAX_ZOOM_FACTOR)
			newZoom = Zoom.MAX_ZOOM_FACTOR;
		
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


	/**
	 * 
	 * @param c
	 */
	public void setPropertyController(PaintPropertyController c) {
		propertyController = c;
	}
}
