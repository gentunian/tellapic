package ar.com.tellapic.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.utils.Utils;

/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingAreaView extends JLabel implements Observer, Scrollable {
	private static final long serialVersionUID = 1L;

	private Image                   background;
	private Image                   foreground;
	private Image                   grid;

	//private Drawing                 temporalDrawing;
	private ArrayList<Drawing>      temporalDrawings;
	private ArrayList<AbstractUser> userList;
	private BufferedImage           backimage;
	private GraphicsEnvironment     ge;
	private GraphicsDevice          gd;
	private GraphicsConfiguration   gc;
	private RenderingHints          rh;

	private boolean gridEnabled = true;
	private int     gridSize = 10;

	private int zoomX = 1;

	private RuleHeader topRule;
	private RuleHeader leftRule;
	
	private static class Holder {
		private final static DrawingAreaView INSTANCE = new DrawingAreaView();
	}
	
	
	/*
	 * 
	 */
	private DrawingAreaView() {
		setName(Utils.msg.getString("drawingarea"));
		setPreferredSize(new Dimension(800,600));
		setLocation(400,100);
		setVisible(true);
		temporalDrawings = new ArrayList<Drawing>();
		userList = new ArrayList<AbstractUser>();
		backimage = null;
//		try {
//			backimage = ImageIO.read(getClass().getResource("/icons/backimage.jpg"));
//		} catch (IOException e) {
//		}
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		rh = new RenderingHints(null);
		//setPreferredSize(new Dimension(backimage.getWidth(), backimage.getHeight()));
		this.setAutoscrolls(true);
		this.addAncestorListener(new AncestorListener(){

			@Override
			public void ancestorAdded(AncestorEvent event) {
				JScrollPane parent = (JScrollPane) ((JViewport) getParent()).getParent();
				topRule = ((RuleHeader)parent.getColumnHeader().getView());
				topRule.setPreferredHeight(25);
				topRule.setPreferredWidth(backimage.getWidth());
//				
				leftRule = ((RuleHeader)parent.getRowHeader().getView());
				leftRule.setPreferredHeight(backimage.getHeight());
				leftRule.setPreferredWidth(25);
			}

			@Override
			public void ancestorMoved(AncestorEvent event) {
				// TODO Auto-generated method stub
			
			}

			@Override
			public void ancestorRemoved(AncestorEvent event) {
				// TODO Auto-generated method stub
				
			}});
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
		foreground = null;
		
//		background = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
//		foreground = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
//		drawBackgroundOff();
		
		repaint();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public static DrawingAreaView getInstance() {
		return Holder.INSTANCE;
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
		this.gridSize = gridSize;
		drawBackgroundOff();
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
	private void drawBackgroundOff() {
		Graphics2D frontArea = (Graphics2D) foreground.getGraphics();
		frontArea.drawImage(backimage, 0, 0, null);
		if(gridEnabled) {
			frontArea.setColor(Color.gray);
			
			/* How many dots (pixels) are in a cm? */
			int dpcm = (int)((double) RuleHeader.INCH / (double)2.546);
			
			/* How many lines will be in a cm? */
			int linesInCm =  gridSize/10;
			
			/* How long will be the space between lines */
			double divisionSize =  (dpcm / (double)(gridSize/10));
			
			/* How many vertical lines do we need to draw? */
			int vLines = (int) Math.round(getWidth() / divisionSize);

			/* How many horizontal lines do we need to draw? */
			int hLines = (int) Math.round(getHeight() / divisionSize);
			
			/* Take the problem as divide and conquer in the sense that */
			/* treat it as drawing lines between a centimeter. Repeat it */
			/* until we draw all centimeters. */
			for(int i = 0; i < vLines; i++) {
				int x = dpcm * i;;
				for (int j = 0; j < linesInCm; j++) {
					x += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
					frontArea.drawLine(x, 0, x, getHeight());
				}
			}
			
			for(int i = 0; i < hLines; i++) {
				int y = dpcm * i;;
				for (int j = 0; j < linesInCm; j++) {
					y += (int) ((j % 2 == 0)? Math.floor(divisionSize) : Math.ceil(divisionSize));
					frontArea.drawLine(0, y, getWidth(), y);
				}
			}
		}	
	}
	
	
	/**
	 * @param gridEnabled the gridEnabled to set
	 */
	public void setGridEnabled(boolean gridEnabled) {
		this.gridEnabled = gridEnabled;
		
		if (foreground == null)
			return;
		
		drawBackgroundOff();
		repaint();
	}


	/**
	 * @return the gridEnabled
	 */
	public boolean isGridEnabled() {
		return gridEnabled;
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
}
