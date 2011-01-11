package ar.com.tellapic.graphics;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import ar.com.tellapic.AbstractUser;
import ar.com.tellapic.UserManager;
import ar.com.tellapic.Utils;

/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingAreaView extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;

	private Image                   background;
	private Image                   foreground;
	private Drawing                 temporalDrawing;
	private ArrayList<Drawing>      temporalDrawings;
	private ArrayList<AbstractUser> userList;
	private BufferedImage           backimage;
	private GraphicsEnvironment     ge;
	private GraphicsDevice          gd;
	private GraphicsConfiguration   gc;
	
	
	private static class Holder {
		private final static DrawingAreaView INSTANCE = new DrawingAreaView();
	}
	
	private DrawingAreaView() {
		setName(Utils.msg.getString("drawingarea"));
		setPreferredSize(new Dimension(800,600));
		setLocation(400,100);
		setVisible(true);
		temporalDrawings = new ArrayList<Drawing>();
		userList = new ArrayList<AbstractUser>();
		backimage = null;
		try {
			backimage = ImageIO.read(getClass().getResource("/icons/backimage.jpg"));
		} catch (IOException e) {
		}
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		setPreferredSize(new Dimension(backimage.getWidth(), backimage.getHeight()));
	}
	
	
	public void setImage(BufferedImage img) {
		backimage = img;
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();
		setPreferredSize(new Dimension(backimage.getWidth(), backimage.getHeight()));
		
		background = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
		foreground = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D frontArea     = (Graphics2D) foreground.getGraphics();
		frontArea.drawImage(backimage, 0, 0, null);

		repaint();
	}
	
	
	public static DrawingAreaView getInstance() {
		return Holder.INSTANCE;
	}
	
	public void addPainter(AbstractUser user) {
		userList.add(user);
	}


	//TODO: REFACTOR THIS THINKING ON MULTIPLE ACCESS TO THE DRAWING AREA VIEW (A.K.A. MULTI-THREADED)
	@Override
	public void update(Observable o, Object data) {
		Utils.logMessage("Updating drawing area view: "+data);
		AbstractUser user = (AbstractUser) o;
		Integer     index = null;
		
		// If an user has finish drawing, it will provide an index. Or, if an add drawing remote event
		// adds a drawing to a remote user drawing list, then 'data' will be the index of that added drawing
		if (data instanceof Integer)
			index = (Integer) data;
		
		//TODO: this makes no sense to be put here. This should be used just once when
		//a background image (backImage) is loaded and this panel is displayed. Remove this!
		if (background == null)
			background = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);

		if (foreground == null)
			foreground = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);

		
		Graphics2D permanentArea = (Graphics2D) background.getGraphics();
		Graphics2D frontArea     = (Graphics2D) foreground.getGraphics();
		
		
		/****************************************/
		/*   The image will never be modified   */
		/****************************************/
		frontArea.drawImage(backimage, 0, 0, null);
		
		
		if (!user.isRemoved() && user.isVisible()) {
			Utils.logMessage("\tUser is visible: "+user);
			Drawing firstNotDrawn   = (index == null)? null : user.getDrawings().get(index);
			
			//TODO: Se deberia tener una lista de todos los temporales que se estan dibujando de
			//todos los usuarios. Es decir, los temporales de los usuarios que estan dibujando.
			Drawing temporalDrawing = user.getTemporalDrawing();
						
			
			if (firstNotDrawn != null) {
				drawDrawing(permanentArea, firstNotDrawn, null);
				Utils.logMessage("\tFirstNotDrawn drawn: "+user);
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
	public void paint(Graphics g) {
		if (foreground == null) {
			foreground = gc.createCompatibleImage(backimage.getWidth(), backimage.getHeight(), Transparency.TRANSLUCENT);
			Graphics2D frontArea = (Graphics2D) foreground.getGraphics();
			frontArea.drawImage(backimage, 0, 0, null);
			
			Utils.logMessage("foreground was null");
		}
		
//		Graphics2D frontArea = (Graphics2D) foreground.getGraphics();
//		
//		//The image will never be modified
//		frontArea.drawImage(backimage, 0, 0, null);
//		
//		//This layer is translucent and holds each drawing on it
//		frontArea.drawImage(background, 0, 0, null);
		
		//Creates the sensation of drawing things while moving the mouse
		//TODO: this is shared in the sense that adding or removing a painter
		//is concurrent. While we are getting the size of the list a painter
		//can be added concurrently. Take note on this, and if analize if could
		//be a problem. Same happens when removing (do we really need to remove a painter?
		//Seems that not removing a painter will not cause a leak, will it?)
//		for(int i=0; i < temporalDrawings.size(); i++) {
//			Drawing drawing = temporalDrawings.get(i);
//			if (drawing != null)
//				drawDrawing(frontArea, drawing);
//				//temporalDrawings.set(i, null);
//		}
		
		//Draws the temporal drawings creating the ilusion of drawing.
		Graphics2D frontArea = (Graphics2D) foreground.getGraphics();
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
	
	
	public void update(Drawing drawing, int i) {
		//temporalDrawing = drawing;
		temporalDrawings.set(i, drawing);
		repaint();
	}
	
	
	private void drawDrawing(Graphics2D g, Drawing drawing, PaintProperty[] overridedProperties) {
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
		else
			g.drawString(drawing.getText(), drawing.getTextX(), drawing.getTextY());
	}
}
