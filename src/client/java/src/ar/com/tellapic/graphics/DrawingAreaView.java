package ar.com.tellapic.graphics;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import ar.com.tellapic.AbstractUser;
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
	private Grid                    grid;
	private RuleHeader              topRule;
	private RuleHeader              leftRule;
	//private Drawing                 temporalDrawing;
	private ArrayList<Drawing>      temporalDrawings;
	private ArrayList<AbstractUser> userList;
	private BufferedImage           backimage;
	private GraphicsEnvironment     ge;
	private GraphicsDevice          gd;
	private GraphicsConfiguration   gc;
	private RenderingHints          rh;
	private PaintPropertyController propertyController;
	private AbstractUser            user;
	private java.awt.Point          scrollingPoint;
	private StatusBar               statusBar;
	private int zoomX = 1;
	private JPanel buttonCorner;
	
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
	private DrawingAreaView() {
		temporalDrawings = new ArrayList<Drawing>();
		userList         = new ArrayList<AbstractUser>();
		propertyController = null;
		backimage        = null;
		user             = UserManager.getInstance().getLocalUser();
		statusBar        = StatusBar.getInstance();
		
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		rh = new RenderingHints(null);
		
//		scrollPane = new JScrollPane(this);
		topRule   = new RuleHeader(RuleHeader.HORIZONTAL, true);
		leftRule = new RuleHeader(RuleHeader.VERTICAL, true);
		buttonCorner = new JPanel();
		JToggleButton isMetric     = new JToggleButton("cm", true);
		isMetric.setFont(new Font("SansSerif", Font.PLAIN, 8));
		isMetric.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JToggleButton isMetric = (JToggleButton) e.getSource();
				boolean v = (e.getStateChange() == ItemEvent.SELECTED);
//				((RuleHeader)scrollPane.getColumnHeader().getView()).setIsMetric(v);
//				((RuleHeader)scrollPane.getRowHeader().getView()).setIsMetric(v);
				JScrollPane parent = (JScrollPane) ((JViewport) getParent()).getParent();
				((RuleHeader)parent.getColumnHeader().getView()).setIsMetric(v);
				((RuleHeader)parent.getRowHeader().getView()).setIsMetric(v);
				if (v)
					isMetric.setText("cm");
				else
					isMetric.setText("in");
			}
		});
		buttonCorner.add(isMetric);
		
		setName(Utils.msg.getString("drawingarea"));
//		scrollPane.setName(getName());
		setPreferredSize(new Dimension(800,600));
		setVisible(true);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		setAutoscrolls(true);
		addAncestorListener(new AncestorListener(){
			@Override
			public void ancestorAdded(AncestorEvent event) {
				JScrollPane parent = (JScrollPane) ((JViewport) getParent()).getParent();
				topRule = ((RuleHeader)parent.getColumnHeader().getView());
				topRule.setPreferredWidth(backimage.getWidth());
				leftRule = ((RuleHeader)parent.getRowHeader().getView());
				leftRule.setPreferredHeight(backimage.getHeight());
			}
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			@Override
			public void ancestorRemoved(AncestorEvent event) {}
		});
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
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		setPreferredSize(new Dimension(backimage.getWidth(), backimage.getHeight()));
		grid = new Grid(backimage.getWidth(), backimage.getHeight());
		foreground = null;
		
//		background = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
//		foreground = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
//		drawBackgroundOff();
		
		repaint();
	}
	
	

	
	/**
	 * 
	 * @param user
	 */
	public void addPainter(AbstractUser user) {
		userList.add(user);
	}


	//TODO: REFACTOR THIS THINKING ON MULTIPLE ACCESS TO THE DRAWING AREA VIEW (A.K.A. MULTI-THREADED)
	@Override
	public void update(Observable o, Object data) {
		//Utils.logMessage("Updating drawing area view: "+data);
		AbstractUser user = (AbstractUser) o;
		Integer     index = null;
		
		// If an user has finish drawing, it will provide an index. Or, if an add drawing remote event
		// adds a drawing to a remote user drawing list, then 'data' will be the index of that added drawing
		if (data instanceof Integer)
			index = (Integer) data;
		
		//TODO: this makes no sense to be put here. This should be used just once when
		//a background image (backImage) is loaded and this panel is displayed. Remove this!
		if (background == null && backimage != null)
			background = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);

		if (foreground == null)
			return;
		
		Graphics2D permanentArea = (Graphics2D) background.getGraphics();
		//Graphics2D frontArea     = (Graphics2D) foreground.getGraphics();
		
		/****************************************/
		/*   The image will never be modified   */
		/****************************************/
		drawBackgroundOff();
		

		if (!user.isRemoved() && user.isVisible()) {
			//Utils.logMessage("\tUser is visible: "+user);
			Drawing firstNotDrawn   = (index == null)? null : user.getDrawings().get(index);
			
			//TODO: Se deberia tener una lista de todos los temporales que se estan dibujando de
			//todos los usuarios. Es decir, los temporales de los usuarios que estan dibujando.
			//Drawing temporalDrawing = user.getTemporalDrawing();
						
			
			if (firstNotDrawn != null) {
				drawDrawing(permanentArea, firstNotDrawn, null);
//				Utils.logMessage("\tFirstNotDrawn drawn: "+user);
			}
			
			/**********************************************************/
			/* This layer is translucent and holds each drawing on it */
			/**********************************************************/
//			frontArea.drawImage(background, 0, 0, null);

//			if (temporalDrawing != null) {
//				drawDrawing(frontArea, temporalDrawing, null);
//				Utils.logMessage("\tTemporalDrawing drawn: "+user);
//			}
		} 
//		else
//			/**********************************************************/
//			/* This layer is translucent and holds each drawing on it */
//			/**********************************************************/
//			frontArea.drawImage(background, 0, 0, null);
	
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
	public void paint(Graphics g) {
		if (backimage == null)
			return;
		
		if (foreground == null) {
			foreground = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
			drawBackgroundOff();
		}
//		double zoom = ((zoomX < 0)? ((double)1/(double)(-1*zoomX)) : zoomX);
//		((Graphics2D)g).scale(zoom, zoom);
		
		Graphics2D frontArea = (Graphics2D) foreground.getGraphics();
		frontArea.setRenderingHints(rh);
		frontArea.drawImage(background, 0, 0, null);
		
		for(AbstractUser user : UserManager.getInstance().getUsers().values()) {
			if (!user.isRemoved() && user.isVisible()) {
				Drawing d = user.getTemporalDrawing();
				if (d != null) {
					drawDrawing(frontArea, d, null);
				}
			}
		}		
		
		g.drawImage(foreground, 0, 0, null);
	}
	
	
	/**
	 * 
	 * @param drawing
	 * @param i
	 */
	public void update(Drawing drawing, int i) {
		//temporalDrawing = drawing;
		temporalDrawings.set(i, drawing);
		repaint();
	}
	
	
	private void drawDrawing(Graphics2D g, Drawing drawing, PaintProperty[] overridedProperties) {
		g.setRenderingHints(rh);
		if (drawing.hasAlphaProperty())
			g.setComposite(drawing.getComposite());
		
		if (drawing.hasColorProperty())
			g.setColor(drawing.getColor());
		
		if (drawing.hasStrokeProperty())
			g.setStroke(drawing.getStroke());
		
		if (drawing.hasFontProperty())
			g.setFont(drawing.getFont());
		
		if (drawing.hasShape())
			g.draw(drawing.getShape());
		
		if (drawing.hasFontProperty())
			g.drawString(drawing.getText(), drawing.getTextX(), drawing.getTextY());
	}


	/**
	 * @param gridSize the gridSize to set
	 */
	public void setGridSize(int gridSize) {
		grid.setGridSize(gridSize);
		drawBackgroundOff();
		repaint();
	}


	/**
	 * @return the gridSize
	 */
	public int getGridSize() {
		return grid.getGridSize();
	}


	/*
	 * 
	 */
	private void drawBackgroundOff() {
		Graphics2D frontArea = (Graphics2D) foreground.getGraphics();
		frontArea.drawImage(backimage, 0, 0, null);
		if (grid.isGridEnabled())
			frontArea.drawImage(grid, 0, 0, null);
		
//		if(gridEnabled) {
//			frontArea.setColor(Color.gray);
//			
//			/* How many dots (pixels) are in a cm? */
//			int dpcm = (int)((double) RuleHeader.INCH / (double)2.546);
//			
//			/* How many lines will be in a cm? */
//			int linesInCm =  gridSize;
//			
//			/* How long will be the space between lines */
//			double divisionSize =  ((double)dpcm / (double)(gridSize));
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
//					frontArea.drawLine(x, 0, x, getHeight());
//				}
//			}
//			
//			for(int i = 0; i < hLines; i++) {
//				int y = dpcm * i;;
//				for (int j = 0; j < linesInCm; j++) {
//					y += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
//					frontArea.drawLine(0, y, getWidth(), y);
//				}
//			}
//		}	
	}
	
	
	/**
	 * 
	 * @param color
	 */
	public void setGridColor(Color color) {
		grid.setGridColor(color);
		drawBackgroundOff();
		repaint();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Color getGridColor() {
		return grid.getGridColor();
	}
	
	
	/**
	 * @param gridEnabled the gridEnabled to set
	 */
	public void setGridEnabled(boolean gridEnabled) {

		grid.setGridEnabled(gridEnabled);
		
		if (foreground == null)
			return;
		
		drawBackgroundOff();
		repaint();
	}


	/**
	 * @return the gridEnabled
	 */
	public boolean isGridEnabled() {
		return grid.isGridEnabled();
	}


	/**
	 * 
	 */
	public void doZoomIn() {
		zoomX++;
		if (zoomX == 0)
			zoomX++;
		repaint();
	}


	/**
	 * 
	 */
	public void doZoomOut() {
		zoomX--;
		if (zoomX == 0)
			zoomX--;
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


	/**
	 * @return
	 */
	public Object getGridTransparency() {
		return grid.getGridTransparency();
	}


	/**
	 * 
	 * @param alpha
	 */
	public void setGridTransparency(float alpha) {
		grid.setGridTransparency(alpha);
		
		drawBackgroundOff();
		repaint();
	}


		/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent event) {
		Tool tool = user.getToolBoxModel().getLastUsedTool();
		
		if (tool == null)
			return;
		
		if (tool.getName().equals("Zoom")) {
			if (event.getButton() == MouseEvent.BUTTON1)
				DrawingAreaView.getInstance().doZoomIn();
			else
				DrawingAreaView.getInstance().doZoomOut();
		}
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
		
		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		/* If button is the left one, start using the tool */
		if (event.getButton() == MouseEvent.BUTTON1) {
			if (usedTool.hasAlphaProperties())
				usedTool.setAlpha(toolBoxState.getOpacityProperty());

			if (usedTool.hasColorProperties())
				usedTool.setColor(toolBoxState.getColorProperty());

			if (usedTool.hasStrokeProperties())
				usedTool.setStroke(toolBoxState.getStrokeProperty());

			if (usedTool.hasFontProperties())
				usedTool.setFont(toolBoxState.getFontProperty());
			
			usedTool.onPress(event.getX(), event.getY(), event.getButton(), event.getModifiers());

		} else {
			/* If we press another button, just stop using the tool */
			//TODO: The tool is actually paused. Rename the tool's method onCancel().
			usedTool.onCancel();
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(MouseEvent event) {
//		Utils.printEventInfo(event);
		statusBar.setMouseCoordinates(event.getX(), event.getY());
		topRule.update(event.getX(), event.getY());
		leftRule.update(event.getX(), event.getY());
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
			//DrawingAreaView.getInstance().scrollRectToVisible(r);
			scrollRectToVisible(r);
			return;
		}
		
		
		Tool usedTool = user.getToolBoxModel().getLastUsedTool();
		
		
		/* Do nothing with an empty tool */
		if (usedTool == null)
			return;
		
		
		if (usedTool.isBeingUsed()) {
			usedTool.onDrag(event.getX(), event.getY(), event.getButton(), event.getModifiersEx());
			
			// This will trigger an update() to the DrawingAreaView
			user.setTemporalDrawing(usedTool.getDrawing());
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
		
		if (usedTool.isBeingUsed() && event.getButton() == MouseEvent.BUTTON1) {
			Drawing drawing = usedTool.onRelease(event.getX(), event.getY(), event.getButton(), event.getModifiersEx());
			
			if (drawing == null) 
				return;
			
			// This will trigger an update() to the DrawingAreaView
			user.addDrawing(drawing);
			
			return;
		}
		
		
		if (event.getButton() == MouseEvent.BUTTON3 && event.getModifiersEx() == java.awt.event.InputEvent.BUTTON1_DOWN_MASK) {
			usedTool.onRestore();
			return;
		}
		
		if (event.getButton() == MouseEvent.BUTTON3)
			user.setTemporalDrawing(null);
		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseAdapter#mouseMoved(java.awt.event.MouseEvent)
	 */
	//TODO: is it possible to think this mouse wheel event be "live" from remote users? Does it make sense?
	@Override
	public void mouseMoved(MouseEvent event) {
		IToolBoxState toolBoxState = user.getToolBoxModel();
		Tool usedTool = toolBoxState.getLastUsedTool();
		statusBar.setMouseCoordinates(event.getX(), event.getY());
		topRule.update(event.getX(), event.getY());
		leftRule.update(event.getX(), event.getY());
		
		if (usedTool == null)
			return;
		
		
		
		if (usedTool.isOnMoveSupported()) {
			Drawing drawing = usedTool.getDrawing();
			if (usedTool.hasAlphaProperties())
				drawing.setAlpha(toolBoxState.getOpacityProperty());
			if (usedTool.hasColorProperties())
				drawing.setColor(toolBoxState.getColorProperty());
			if (usedTool.hasStrokeProperties())
				drawing.setStroke(toolBoxState.getStrokeProperty());
			if (usedTool.hasFontProperties())
				drawing.setFont(toolBoxState.getFontProperty());
			if (usedTool.hasColorProperties())
				drawing.setColor(toolBoxState.getColorProperty());
			
			// TODO: do we really need send every time the drawing? Its a reference, change the value
			// and use it later on the view.
			//view.update(usedTool.onMove(event.getX(), event.getY()), id);

			//solution?
			user.setTemporalDrawing(usedTool.onMove(event.getX(), event.getY()));
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
		Tool usedTool = toolBoxState.getLastUsedTool();
	
		if (usedTool == null)
			return;
	
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
		
		if (usedTool == null)
			return;
		
		if (propertyController == null)
			return;
		
		if (usedTool.isBeingUsed()) {
			if (event.isShiftDown()) {
				if (usedTool.hasAlphaProperties())
					propertyController.handleOpacityChange(toolBoxState.getOpacityProperty().alpha + 0.1f * step);

			} else {
				if (usedTool.hasStrokeProperties())
					propertyController.handleWidthChange((int)toolBoxState.getStrokeProperty().getWidth() + step);

				else if (usedTool.hasFontProperties())
					propertyController.handleFontSizeChange(toolBoxState.getFontProperty().getSize() + step);
			}
			if (usedTool.getDrawing() != null)
				user.setTemporalDrawing(usedTool.getDrawing());
		}
	}
	
	
	/**
	 * 
	 * @param c
	 */
	public void setPropertyController(PaintPropertyController c) {
		propertyController = c;
	}
}
