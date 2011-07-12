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
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ColorSelector extends JDialog {
	private static final long        serialVersionUID = 1L;
	private static final int         SELECT_BUTTON_WIDTH = 200;
	private static final int         BACK_BUTTON_WIDTH   = 150;
	private ColorLabel               oldColor;
	private ColorLabel               selectColor;
	private boolean                  newColor;
	private IColorSelectorController controller;
	private int                      alpha = 255;
	
	/**
	 * 
	 * @param parent
	 * @param color
	 * @param xPosition
	 * @param yPosition
	 * @param alignLeft
	 * @param c
	 */
	public ColorSelector(JDialog parent, Color color, int xPosition, int yPosition, boolean alignLeft, IColorSelectorController c) {
		this(parent, color, xPosition, yPosition, alignLeft);
		controller = c;
	}
	
	/**
	 * 
	 * @param parent
	 * @param color
	 * @param xPosition
	 * @param yPosition
	 * @param alignLeft
	 */
	public ColorSelector(JDialog parent, Color color, int xPosition, int yPosition, boolean alignLeft) {
		super(parent);
		/* Use this two labels as buttons for choosing the actual color */
		oldColor    = new ColorLabel("Previous Color");
		selectColor = new ColorLabel("New Color");
		
		/* Set the labels properties */
		selectColor.setFont(Font.decode("Droid-10-bold"));
		selectColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		selectColor.setOpaque(true);
		selectColor.setPreferredSize(new Dimension(SELECT_BUTTON_WIDTH, 30));
		selectColor.setHorizontalTextPosition((int)JLabel.CENTER_ALIGNMENT);
		selectColor.setHorizontalAlignment(JLabel.CENTER);
		selectColor.setBorder(new LineBorder(Color.black, 2, true));
		selectColor.setDoubleBuffered(true);
		
		/* When "new color" button is pressed, hide this dialog with the newColor boolean value set to true */
		selectColor.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				newColor = true;
				setVisible(false);
			}
		});
		
		/* Set the labels properties */
		oldColor.setOpaque(true);
		oldColor.setFont(Font.decode("Droid-10-bold"));
		oldColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		oldColor.setPreferredSize(new Dimension(BACK_BUTTON_WIDTH, 30));
		oldColor.setHorizontalTextPosition((int)JLabel.CENTER_ALIGNMENT);
		oldColor.setHorizontalAlignment(JLabel.CENTER);
		oldColor.setBorder(BorderFactory.createRaisedBevelBorder());
		oldColor.setBackground(color);
		oldColor.setForeground(reverseColor(color));
		oldColor.setDoubleBuffered(true);
		
		/* When "previous color" button is pressed, hide this dialog with the newColor boolean value set to false */
		oldColor.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				newColor = false;
				setVisible(false);
			}
		});

		/* Creates the color panel */
		ColorSelectorPanel colorPanel = new ColorSelectorPanel();
		
		/* Creates the panel where the "new color" and "previous color" buttons will be placed */
		JPanel labelPanel = new JPanel();
		
		/* Sets the buttons panel properties */
		labelPanel.setOpaque(true);
		labelPanel.setBackground(Color.white);
		labelPanel.add(oldColor, BorderLayout.LINE_START);
		labelPanel.add(selectColor, BorderLayout.LINE_END);
		
		/* Creates the slider for the transparency value */
		JSlider alphaSlider = new JSlider(0, 255, alpha);
		
		/* Sets the slider properties */
		alphaSlider.setOrientation(SwingConstants.VERTICAL);
		alphaSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider src = (JSlider) e.getSource();
				alpha = src.getValue();
				selectColor.repaint();
			}
		});
		
		/* Add the slider, the color panel and the button panel to this dialog */
		add(alphaSlider, BorderLayout.LINE_END);
		add(colorPanel, BorderLayout.CENTER);
		add(labelPanel, BorderLayout.PAGE_END);

		setIconImage(Utils.createIconImage(12, 12, "/icons/tools/color1.png"));
		setTitle("Tellapic -"+Utils.msg.getString("pickcolor"));
		
		/* Even if we don't press any button, but we lost focus, hide this dialog */
		addWindowFocusListener(new WindowAdapter(){
			public void windowLostFocus(WindowEvent e) {
				ColorSelector.this.setVisible(false);
			}
		});
		
		/* Notify controller if any, that this dialog is disposing */
		addComponentListener(new ComponentAdapter(){
			@Override
			public void componentHidden(ComponentEvent e) {
				if (controller != null)
					controller.handleColorChange(getSelectedColor());
				dispose();
			}
		});
		
		/* Undecorate this dialog */
		setUndecorated(true);
		setResizable(false);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		if (alignLeft)
			setLocation(xPosition, yPosition);
		else
			setLocation(xPosition - SELECT_BUTTON_WIDTH - BACK_BUTTON_WIDTH, yPosition);
		
		pack();
	}
	
	/**
	 * 
	 * @param color
	 * @param xPosition
	 * @param yPosition
	 * @param alignLeft
	 */
	public ColorSelector(Color color, int xPosition, int yPosition, boolean alignLeft) {
		this(null, color, xPosition, yPosition, alignLeft);
	}
	
	/**
	 * 
	 * @return
	 */
	public Color getSelectedColor() {
		if (newColor)
			return selectColor.getColor();
		else
			return oldColor.getColor();
	}
	
	/**
	 * 
	 * @param color
	 * @return
	 */
	private Color reverseColor(Color color) {
		return new Color(
				((color.getRed() < 128)? 255 : 0),
				((color.getGreen() < 128)? 255 : 0),
				((color.getBlue()< 128)? 255:0),
				color.getAlpha()
		);
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
		public static final int   COLOR_WHEEL_RADIUS = 84;
		public static final int   COLOR_WHEEL_DIAMETER = COLOR_WHEEL_RADIUS * 2;
		public static final int   COLOR_WHEEL_WIDTH  = 20;
		public static final int   COLOR_BOX_SIZE = 100;
		public static final int   IMAGE_SIZE = 195;
		private static final int  GAP = 5;
		public static final int   PANEL_WIDTH = IMAGE_SIZE + GAP*2;
		public static final int   PANEL_HEIGHT = 200;
		private static final int  BOX_X = 47; 
		private static final int  BOX_Y = 148; 

		private RenderingHints rh;
		private Robot          robot;
		private Color          color;
		private BufferedImage  wheel;
		private BufferedImage  marker;
		private BufferedImage  mask;
		private int            x;
		private int            y;
		private int            squareMarkX;
		private int            squareMarkY;
		private int            wheelMarkX;
		private int            wheelMarkY;
		private boolean        wheelMarkSet  = false;
		private boolean        dragingWheel  = false;
		private boolean        dragingSquare = false;
		private GradientPaint  gradient;

		
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

		
		
		/**
		 * 
		 */
		public void paint(Graphics g) {
			super.paint(g);
			//Graphics2D g2 = (Graphics2D) g;
		
			Point point = new Point(squareMarkX, squareMarkY);
			SwingUtilities.convertPointToScreen(point, this);
			Color newColor = robot.getPixelColor(point.x, point.y);

			selectColor.setBackground(newColor);
			selectColor.setForeground(reverseColor(newColor));
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
	
	
	private class ColorLabel extends JLabel {
		private static final long serialVersionUID = 1L;
		private Color color;
		private int   textWidth;
		/**
		 * @param string
		 */
		public ColorLabel(String string) {
			super(string);
			if (string != null)
				textWidth = getStringWidth(string);
		}

		/**
		 * 
		 * @return
		 */
		private int getStringWidth(String text) {
			FontMetrics metrics = getFontMetrics(getFont());
			return metrics.stringWidth(text);
		}
		
		/*
		 * (non-Javadoc)
		 * @see javax.swing.JLabel#setText(java.lang.String)
		 */
		@Override
		public void setFont(Font font) {
			super.setFont(font);
			textWidth = getStringWidth(getText());
		}
	
		/**
		 * 
		 */
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Color c = getBackground();
			Rectangle r = getVisibleRect();
			
			for(int j = 0; j < r.height/10; j++) {
				Color firstColor;
				Color secondColor;
				if (j % 2 == 0) {
					firstColor = Color.black;
					secondColor = Color.gray;
				} else {
					firstColor = Color.gray;
					secondColor = Color.black;
				}
				for(int i = 0; i < r.width/10; i++) {
					if (i % 2 == 0) 
						g2.setColor(firstColor);
					else
						g2.setColor(secondColor);
					g2.fillRect(i + i*10, j + j*10, 10, 10);
				}
			}
			color = new MyColor(c.getRed(), c.getGreen(), c.getBlue(), alpha);
			g2.setColor(color);
			g2.fill(r);
			g2.setFont(getFont());
			g2.setColor(getForeground());
			g2.drawString(getText(), (getWidth() - textWidth) / 2, getHeight()/2);
		}
		
		/**
		 * 
		 * @return
		 */
		public Color getColor() {
			return color;
		}
	}
}
