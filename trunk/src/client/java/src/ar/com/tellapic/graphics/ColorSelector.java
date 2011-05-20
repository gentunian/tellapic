/**
 *   Copyright (c) 2010 Sebastián Treu.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 * @author
 *         Sebastian Treu 
 *         sebastian.treu(at)gmail.com
 *
 */  
package ar.com.tellapic.graphics;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ColorSelector extends JDialog {
	private static final long serialVersionUID = 1L;
	private JLabel oldColor;
	private JLabel selectColor;
	private boolean newColor;
	boolean animating = false;
	
	public ColorSelector(Color color, int xPosition, int yPosition, final boolean alignLeft) {
		oldColor = new JLabel("Previous Color");
		selectColor = new JLabel("New Color");
		selectColor.setFont(Font.decode("Droid-10-bold"));
		oldColor.setFont(Font.decode("Droid-10-bold"));
		selectColor.setOpaque(true);
		oldColor.setOpaque(true);
		selectColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		oldColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		selectColor.setPreferredSize(new Dimension(200, 30));
		selectColor.setHorizontalTextPosition((int)JLabel.CENTER_ALIGNMENT);
		selectColor.setHorizontalAlignment(JLabel.CENTER);
		oldColor.setPreferredSize(new Dimension(150, 30));
		oldColor.setHorizontalTextPosition((int)JLabel.CENTER_ALIGNMENT);
		oldColor.setHorizontalAlignment(JLabel.CENTER);
		oldColor.setBorder(BorderFactory.createRaisedBevelBorder());
		oldColor.setBackground(color);
		oldColor.setForeground(
				new Color(
						((color.getRed() < 128)? 255 : 0),
						((color.getGreen() < 128)? 255 : 0),
						((color.getBlue()< 128)? 255:0)
				)
		);
		selectColor.setBorder(new LineBorder(Color.black, 2, true));
		selectColor.setDoubleBuffered(true);
		oldColor.setDoubleBuffered(true);
		selectColor.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				newColor = true;
				new Thread(new Runnable() {
					public void run() {
						int i = (int) getSize().getHeight();
						setSize(new Dimension(ColorSelectorPanel.PANEL_WIDTH, i--));
						Point loc = getLocation();
						if (!alignLeft)
							setLocation(loc.x + 150, loc.y);
						animating = true;
						while(i > 0) {
							try {
								Thread.sleep(0,10);
								setSize(new Dimension(ColorSelectorPanel.PANEL_WIDTH, i--));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						animating = false;
						dispose();
					}
				}).start();
			}
		});
		oldColor.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				newColor = false;
				new Thread(new Runnable() {
					public void run() {
						int i = (int) getSize().getHeight();
						setSize(new Dimension(ColorSelectorPanel.PANEL_WIDTH, i--));
						Point loc = getLocation();
						if (!alignLeft)
							setLocation(loc.x + 150, loc.y);
						animating = true;
						while(i > 0){
							try {
								Thread.sleep(0,10);
								setSize(new Dimension(ColorSelectorPanel.PANEL_WIDTH, i--));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						animating = false;
						
						dispose();
					}
				}).start();
			}
		});
		ColorSelectorPanel colorPanel = new ColorSelectorPanel();
		
		add(colorPanel, BorderLayout.NORTH);
		add(oldColor, BorderLayout.LINE_START);
		add(selectColor, BorderLayout.LINE_END);
		setUndecorated(true);
		setResizable(false);
		addComponentListener(new ComponentListener(){
			public void componentHidden(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentResized(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {
				new Thread(new Runnable() {
					public void run() {
						int i = 0;
						setSize(new Dimension(ColorSelectorPanel.PANEL_WIDTH, i));
						animating = true;
						while(getSize().getHeight() < ColorSelectorPanel.PANEL_HEIGHT +40) {
							try {
								Thread.sleep(0, 10);
								setSize(new Dimension(ColorSelectorPanel.PANEL_WIDTH, i++));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						animating = false;
						Point loc = getLocation();
						if (!alignLeft)
							setLocation(loc.x - 145, loc.y);
						repaint();
						pack();
					}
				}).start();
			}
		});
		
		
		setModal(true);
		if (alignLeft)
			setLocation(xPosition, yPosition);
		else
			setLocation(xPosition - ColorSelectorPanel.PANEL_WIDTH, yPosition);
		setVisible(true);
	}
	
	
	/**
	 * 
	 * @param g
	 */
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Image image = createImage(ColorSelectorPanel.PANEL_WIDTH, 250);
		Graphics2D i = (Graphics2D) image.getGraphics();
		if (newColor)
			i.setColor(selectColor.getBackground());
		else
			i.setColor(oldColor.getBackground());
		i.fillRect(0, 0, ColorSelectorPanel.PANEL_WIDTH, 250);
		i.setColor(Color.black);
		i.drawRect(0, 0, ColorSelectorPanel.PANEL_WIDTH - 1, getHeight());
		g2.drawImage(image, 0, 0, null);
		if (!animating)
			super.paint(g);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Color getSelectedColor() {
		if (newColor)
			return selectColor.getBackground();
		else
			return oldColor.getBackground();
	}
	
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	class ColorSelectorPanel extends JPanel implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = 1L;
		public static final int COLOR_WHEEL_RADIUS = 84;
		public static final int COLOR_WHEEL_DIAMETER = COLOR_WHEEL_RADIUS * 2;
		public static final int COLOR_WHEEL_WIDTH  = 20;
		public static final int COLOR_BOX_SIZE = 100;
		public static final int IMAGE_SIZE = 195;
		private static final int GAP = 5;
		public static final int PANEL_WIDTH = IMAGE_SIZE + GAP*2;
		public static final int PANEL_HEIGHT = 200;
		private static final int BOX_X = 47; 
		private static final int BOX_Y = 148; 

		private RenderingHints rh;
		private Robot robot;
		private Color color;
		private BufferedImage wheel;
		private BufferedImage marker;
		private BufferedImage mask;
		private int x;
		private int y;
		private int squareMarkX;
		private int squareMarkY;
		private int wheelMarkX;
		private int wheelMarkY;
		private boolean squareMarkSet = false;
		private boolean wheelMarkSet  = false;
		private boolean dragingWheel  = false;
		private boolean dragingSquare = false;
		private GradientPaint gradient;

		
		/**
		 * 
		 */
		public ColorSelectorPanel() {
			setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
			setBackground(Color.white);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			setDoubleBuffered(true);
			rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			try {
				wheel  = ImageIO.read(ColorSelectorPanel.class.getResource("/icons/system/wheel.png"));
				marker = ImageIO.read(ColorSelectorPanel.class.getResource("/icons/system/marker.png"));
				mask   = ImageIO.read(ColorSelectorPanel.class.getResource("/icons/system/mask.png"));
				robot = new Robot();
				color = null;
				addMouseListener(this);
				addMouseMotionListener(this);
				LineBorder roundedLineBorder = new LineBorder(Color.black, 4, true);
				setBorder(roundedLineBorder);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (AWTException e) {
				e.printStackTrace();
			}
		}

		
		/**
		 * 
		 */
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(!animating) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHints(rh);
				g2.drawImage(wheel, 5, 0,null);
				if (wheelMarkSet)
					g2.drawImage(marker, wheelMarkX - 8, wheelMarkY - 10, null);
				if (color == null) {
					color = oldColor.getBackground();
					squareMarkX = BOX_X + COLOR_BOX_SIZE/2;
					squareMarkY = BOX_X + COLOR_BOX_SIZE/2;
				}
				gradient = new GradientPaint(BOX_X + COLOR_BOX_SIZE / 2, BOX_X, Color.white, BOX_X + COLOR_BOX_SIZE/2, GAP + COLOR_BOX_SIZE, color);
				g2.setPaint(gradient);
				g2.fillRect(BOX_X + GAP, BOX_X, COLOR_BOX_SIZE, COLOR_BOX_SIZE);
				g2.drawImage(mask, BOX_X + GAP, BOX_X, null);
				g2.drawImage(marker, squareMarkX-8, squareMarkY-8, null);
			}
		}
		
		
		/**
		 * 
		 */
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D) g;
		
			Point point = new Point(squareMarkX, squareMarkY);
			SwingUtilities.convertPointToScreen(point, this);
			Color newColor = robot.getPixelColor(point.x, point.y);
			selectColor.setBackground(newColor);
			selectColor.setForeground(
				new Color(
						((newColor.getRed() < 128)? 255 : 0),
						((newColor.getGreen() < 128)? 255 : 0),
						((newColor.getBlue()< 128)? 255:0)
				)
			);
		}
		
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			
			if ((x >= BOX_X + GAP && y >= BOX_X) && (x < BOX_Y && y < BOX_Y)){
				squareMarkX = x;
				squareMarkY = y;
			} else {
				int xCenter = BOX_X + GAP + COLOR_BOX_SIZE/2;
				int yCenter = BOX_X + GAP + COLOR_BOX_SIZE/2;
				int normalizeX = x - xCenter;
				int normalizeY = -(y - yCenter);
				double modulo = Math.sqrt(Math.pow(normalizeX, 2) + Math.pow(normalizeY, 2));
				double angle = Math.acos(normalizeX/modulo);
				wheelMarkX = (int)Math.floor(COLOR_WHEEL_RADIUS*(normalizeX/modulo)) + xCenter;
				wheelMarkY = (int)Math.floor(COLOR_WHEEL_RADIUS*Math.sin(angle)) + ((y < yCenter)? -yCenter : yCenter);
				if (y < yCenter)
					wheelMarkY = -wheelMarkY;
				wheelMarkSet = true;
				color = new Color(wheel.getRGB(wheelMarkX, wheelMarkY));
			}
			repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			repaint();
		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			dragingWheel = false;
			dragingSquare = false;
			repaint();
		}


		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			
			if (!dragingWheel && (x >= BOX_X + GAP && y >= BOX_X) && (x < BOX_Y && y < BOX_Y)){
				dragingSquare = true;
				squareMarkX = x;
				squareMarkY = y;
				squareMarkSet = true;
			} else if (!dragingSquare){
				dragingWheel = true;
				int xCenter = BOX_X + GAP + COLOR_BOX_SIZE/2;
				int yCenter = BOX_X + GAP + COLOR_BOX_SIZE/2;
				int normalizeX = x - xCenter;
				int normalizeY = -(y - yCenter);
				double modulo = Math.sqrt(Math.pow(normalizeX, 2) + Math.pow(normalizeY, 2));
				double angle = Math.acos(normalizeX/modulo);
				wheelMarkX = (int)Math.ceil(COLOR_WHEEL_RADIUS*(normalizeX/modulo)) + xCenter;
				wheelMarkY = (int)Math.ceil(COLOR_WHEEL_RADIUS*Math.sin(angle)) + ((y < yCenter)? -yCenter : yCenter);
				if (y < yCenter)
					wheelMarkY = -wheelMarkY;
				wheelMarkSet = true;
				color = new Color(wheel.getRGB(wheelMarkX, wheelMarkY));
			}
			repaint();
		}


		/* (non-Javadoc)
		 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseMoved(MouseEvent e) {
			x = e.getX();
			y = e.getY();
			repaint();
		}
	}
}