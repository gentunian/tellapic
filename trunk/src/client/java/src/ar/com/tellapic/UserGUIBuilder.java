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
package ar.com.tellapic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ar.com.tellapic.chat.ChatController;
import ar.com.tellapic.chat.ChatView;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.graphics.IToolBoxController;
import ar.com.tellapic.graphics.PaintPropertyController;
import ar.com.tellapic.graphics.PaintPropertyView;
import ar.com.tellapic.graphics.ToolBoxController;
import ar.com.tellapic.graphics.ToolBoxModel;
import ar.com.tellapic.graphics.ToolView;
import ar.com.tellapic.utils.Utils;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UserGUIBuilder {
  
	private UserView                userView;
	private ToolView                toolView;
	private PaintPropertyView       propertyView;
	private ChatView                chatView;
	private JFrame                  mainWindow;
	private JScrollPane             scrollPane;
//	private RuleHeader              topRule;
//	private RuleHeader              leftRule;
//	private JPanel                  buttonCorner;
//	private JToggleButton           isMetric;
	
	public UserGUIBuilder(LocalUser user) {
		// Each user has it owns toolbox. This can be a memory overhead issue, but its the bes way to reuse code
		// and allow concurrency easily.
		ToolBoxModel                         model = user.getToolBoxModel();
		
		// Get the user controllers. The controllers are:
		// - propertyController: It manages the paint property settings, such as font, stroke, and so on..
		// - drawingController: Receives events for drawing upon the selected tool. Its the last worker
		//                      where the Drawing object will be made.
		// - toolViewController: Selects the appropiate tool upon user interaction with the ToolView view.
		//                       For remote user, emulates the user selection on a tool with a received packet
		//                       from the net.
		PaintPropertyController propertyController = new PaintPropertyController(model);
		//DrawingLocalController  drawingController  = new DrawingLocalController();
		IToolBoxController      toolViewController = new ToolBoxController(model);
		
		// Instantiates all the GUIs.
		userView     = UserView.getInstance();
		toolView     = new ToolView(model);
		propertyView = new PaintPropertyView();	
		chatView     = new ChatView(new ChatController());
		
		model.addObserver(propertyView);
		model.addObserver(toolView);
		UserOptionsController c = new UserOptionsController(chatView);
		userView.setUserOptionsController(c);
		
		
		// Get the drawing area model instance. Its where all Drawing objects will live.
		//DrawingAreaModel  drawingAreaModel = DrawingAreaModel.getInstance();
		
		// Get the area where all the living Drawing objects will be drawn.
		DrawingAreaView   drawingAreaView  = DrawingAreaView.getInstance();
//		user.addObserver(drawingAreaView);
		//user.addObserver(userView);
		//user.notifyObservers(user);
		scrollPane = new JScrollPane(drawingAreaView);
		
//		topRule   = new RuleHeader(RuleHeader.HORIZONTAL, true);
//		leftRule = new RuleHeader(RuleHeader.VERTICAL, true);
//		buttonCorner = new JPanel();
//		JToggleButton isMetric     = new JToggleButton("cm", true);
//		isMetric.setFont(new Font("SansSerif", Font.PLAIN, 8));
//		isMetric.addItemListener(new ItemListener() {
//			@Override
//			public void itemStateChanged(ItemEvent e) {
//				JToggleButton isMetric = (JToggleButton) e.getSource();
//				boolean v = (e.getStateChange() == ItemEvent.SELECTED);
//				((RuleHeader)scrollPane.getColumnHeader().getView()).setIsMetric(v);
//				((RuleHeader)scrollPane.getRowHeader().getView()).setIsMetric(v);
//				if (v)
//					isMetric.setText("cm");
//				else
//					isMetric.setText("in");
//			}
//		});
//		buttonCorner.add(isMetric);
//		
		scrollPane.setName(drawingAreaView.getName());

//		propertyController.setDrawingController(drawingController);
		
		drawingAreaView.setPropertyController(propertyController);
		
		toolView.setController(toolViewController);
		propertyView.setController(propertyController);
		
		/****************************************/
		/* Creates the dockable station and gui */
		/****************************************/
		mainWindow = new JFrame(user.getName());
		mainWindow.setIconImage(Utils.createIconImage(112, 75, "/icons/system/logo_small.png"));
		mainWindow.getContentPane().add(StatusBar.getInstance(), BorderLayout.SOUTH);
		mainWindow.getContentPane().add(propertyView, BorderLayout.NORTH);
		
		CControl     control1 = new CControl(mainWindow);
		CGrid            grid = new CGrid(control1);
		CContentArea  content = control1.getContentArea();
		SingleCDockable dock1 = wrapToDockable(toolView);
		//SingleCDockable dock2 = wrapToDockable(propertyView);
		SingleCDockable dock3 = wrapToDockable(scrollPane);
		SingleCDockable dock4 = wrapToDockable(chatView);
		SingleCDockable dock5 = wrapToDockable(userView);
		ThemeMap t = control1.getThemes();
		t.select(ThemeMap.KEY_BASIC_THEME);
		
		mainWindow.setPreferredSize(new Dimension(800,600));
		
		grid.add(0, 0, 20, 20, dock1);
		grid.add(2, 0, 200, 40, dock3);
		grid.add(200, 1, 30, 50, dock5);
		grid.add(200, 2, 30, 50, dock4);
		
		content.deploy(grid);
		
		mainWindow.getContentPane().add(content, BorderLayout.CENTER);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//mainWindow.setLayout(new GridLayout(2,1));
		mainWindow.setJMenuBar(createMenuBar());
		mainWindow.pack();
		mainWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainWindow.setVisible(true);
	}	
	
	
	
	private JMenuBar createMenuBar() {
		
//		/* Adds the reconnect option */
//		fileMenu.add(buildFileMenu());
//		
//		/* Adds the Quality menu */
//		optionsMenu.add(buildQualityMenu());
//		
//		/* Adds the Theme menu */
//		viewMenu.add(buildThemeMenu());
//
//		/* Adds the View Menu */
//		viewMenu.add(buildViewMenu());
		
//		menuBar.add(fileMenu);
//		menuBar.add(viewMenu);
//		menuBar.add(optionsMenu);
//		menuBar.add(helpMenu);
		//		JMenu optionsMenu = new JMenu(Utils.msg.getString("options"));
//		JMenu helpMenu    = new JMenu("help");

		
		JMenuBar menuBar  = new JMenuBar();
		
		/* Adds the File option */
		menuBar.add(buildFileMenu());

		/* Adds the View Menu */
		menuBar.add(buildViewMenu());
		
		/* Adds the Quality menu */
		menuBar.add(buildOptionsMenu());
		
		/* Adds the Help menu */
		menuBar.add(buildHelpMenu());
		
		return menuBar;
	}

	
	/**
	 * @return
	 */
	private JMenu buildHelpMenu() {
		JMenu helpMenu = new JMenu(Utils.msg.getString("help"));
		
		
		return helpMenu;
	}



	/**
	 * @return
	 */
	private JMenu buildOptionsMenu() {
		JMenu optionsMenu = new JMenu("Options");
		
		optionsMenu.setMnemonic(KeyEvent.VK_O);
		
		optionsMenu.add(buildQualityMenu());
		
		return optionsMenu;
	}



	/**
	 * @return
	 */
	private JMenu buildFileMenu() {
		JMenu     fileMenu  = new JMenu(Utils.msg.getString("file"));
		JMenuItem reconnect = new JMenuItem(Utils.msg.getString("reconnect"));
		JMenuItem exit      = new JMenuItem(Utils.msg.getString("exit"));

		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		reconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				NetManager.getInstance().reconnect();
			}
		});

		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainWindow.dispose();
				// TODO: make a shutdown routine.
				System.exit(0);
			}
		});
		
		fileMenu.add(reconnect);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		
		return fileMenu;
	}



	/**
	 * @return
	 */
	private JMenu buildViewMenu() {
		JMenu viewMenu = new JMenu("view");
		
		JMenuItem gridSize = new JMenuItem("Grid Size...");
		JMenuItem gridColor = new JMenuItem("Grid Color...");
		JMenuItem gridTransparency = new JMenuItem("Grid Transparency...");
		
		JCheckBoxMenuItem grid  = new JCheckBoxMenuItem("Show Grid");
		JCheckBoxMenuItem ruler = new JCheckBoxMenuItem("Show Ruler");
		JCheckBoxMenuItem status = new JCheckBoxMenuItem("Show Status Bar");
		
		status.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				StatusBar.getInstance().setVisible((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		
		grid.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				DrawingAreaView.getInstance().setGridEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		
		
		ruler.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				DrawingAreaView.getInstance().setRulerEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		
		gridSize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] possibilities = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
				Integer i = (Integer)JOptionPane.showInputDialog(
						null,
						"Grid size (lines per centimeter):",
						"Select Grid Size",
						JOptionPane.PLAIN_MESSAGE,
						null,
						possibilities,
						DrawingAreaView.getInstance().getGridSize()
				);

				if (i != null)
					DrawingAreaView.getInstance().setGridSize(i);
			}
		});
		
		gridColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(DrawingAreaView.getInstance(), "Pick a Color", DrawingAreaView.getInstance().getGridColor());
				if (c != null) {
					DrawingAreaView.getInstance().setGridColor(c);
				}
			}
		});
		
		gridTransparency.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] possibilities = { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f };
				Float i = (Float)JOptionPane.showInputDialog(
						null,
						"Grid Transparency:",
						"Select Grid Transparency",
						JOptionPane.PLAIN_MESSAGE,
						null,
						possibilities,
						DrawingAreaView.getInstance().getGridTransparency()
				);

				if (i != null)
					DrawingAreaView.getInstance().setGridTransparency(i);
			}
		});
		
		grid.setSelected(true);
		ruler.setSelected(true);
		status.setSelected(true);
		
		viewMenu.add(buildThemeMenu());
		viewMenu.addSeparator();
		viewMenu.add(grid);
		viewMenu.add(ruler);
		viewMenu.add(status);
		viewMenu.addSeparator();
		viewMenu.add(gridSize);
		viewMenu.add(gridColor);
		viewMenu.add(gridTransparency);
		
		return viewMenu;
	}



	/**
	 * @return
	 */
	private JMenu buildThemeMenu() {
		JMenu root = new JMenu(Utils.msg.getString("theme"));
		
		JCheckBoxMenuItem themeJava = new JCheckBoxMenuItem("JAVA");
		JCheckBoxMenuItem themeGTK  = new JCheckBoxMenuItem("GTK");
		
		ButtonGroup group = new ButtonGroup();
		
		themeJava.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				try {
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(mainWindow);
				} catch (UnsupportedLookAndFeelException e) {
					//fallback
				} catch (ClassNotFoundException e) {
					//fallback
				} catch (InstantiationException e) {
					//fallback
				} catch (IllegalAccessException e) {
					//fallback
				}	
			}
		});
		
		themeGTK.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(mainWindow);
				} catch (UnsupportedLookAndFeelException e) {
					//fallback
				} catch (ClassNotFoundException e) {
					//fallback
				} catch (InstantiationException e) {
					//fallback
				} catch (IllegalAccessException e) {
					//fallback
				}	
			}
		});
		
		themeGTK.setSelected(true);
		group.add(themeGTK);
		group.add(themeJava);
		root.add(themeJava);
		root.add(themeGTK);
		
		return root;
	}



	/*
	 * Build the quality menu:
	 * 
	 *  Quality -> Text Antialising -> Antialias on
	 *                                 Antialias off
	 *                                 Antialias default
	 *                                 Antialias GASP
	 *                                 Antialias LCD HRGB
	 *                                 Antialias LCD HBGR
	 *                                 Antialias LCD VRGB
	 *                                 Antialias LCD VBGR
	 *                                 
	 *          -> Shape Antialiasing -> Antialias ON
	 *                                -> Antialias DEFAULT
	 *                                
	 *          -> Color Rendering -> Quality
	 *                             -> Speed
	 *                             -> DEFAULT
	 *                             
	 *          -> Dithering -> Disable
	 *                       -> Enable
	 *                       -> DEFAULT
	 *                       
	 *          -> Image Interpolation -> Bicubic
	 *                                 -> Bilinear
	 *                                 -> Near neighbor
	 *                                 
	 *          -> Rendering Quality -> Quality
	 *                               -> Speed
	 *                               -> Default
	 *                               
	 *          -> Stroke Normalization Control -> DEFAULT
	 *                                          -> Normalize
	 *                                          -> Pure
	 */
	private JMenu buildQualityMenu() {
		JMenu root                = new JMenu(Utils.msg.getString("quality"));
		JMenu aaText              = new JMenu("Text Antialising");
		JMenu aaShape             = new JMenu("Shape Antialising");
		JMenu colorRendering      = new JMenu("Color Rendering");
		JMenu dithering           = new JMenu("Dithering");
		JMenu imageInterpolation  = new JMenu("Image Interpolation");
		JMenu renderingQuality    = new JMenu("Rendering Quality");
		JMenu strokeNormalization = new JMenu("Stroke Normalization Control");

		root.add(
				buildAACheckBoxes(
						aaText,
						RenderingHints.KEY_TEXT_ANTIALIASING,
						new Object[] {RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
								RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,
								RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT,
								RenderingHints.VALUE_TEXT_ANTIALIAS_GASP,
								RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB,
								RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR,
								RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB,
								RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR
						}
				)
		);
		
		root.add(
				buildAACheckBoxes(
						aaShape,
						RenderingHints.KEY_ANTIALIASING,
						new Object[] { 
								RenderingHints.VALUE_ANTIALIAS_ON,
								RenderingHints.VALUE_ANTIALIAS_OFF,
								RenderingHints.VALUE_ANTIALIAS_DEFAULT
						}
				)
		);
		
		root.add(
				buildAACheckBoxes(
						colorRendering,
						RenderingHints.KEY_COLOR_RENDERING,
						new Object[] { 
								RenderingHints.VALUE_COLOR_RENDER_QUALITY,
								RenderingHints.VALUE_COLOR_RENDER_SPEED,
								RenderingHints.VALUE_COLOR_RENDER_DEFAULT
						}
				)
		);
		
		root.add(
				buildAACheckBoxes(
						dithering,
						RenderingHints.KEY_DITHERING,
						new Object[] {
								RenderingHints.VALUE_DITHER_DISABLE,
								RenderingHints.VALUE_DITHER_ENABLE,
								RenderingHints.VALUE_DITHER_DEFAULT
						}
				)
		);

		root.add(
				buildAACheckBoxes(
						imageInterpolation,
						RenderingHints.KEY_INTERPOLATION,
						new Object[] {
								RenderingHints.VALUE_INTERPOLATION_BICUBIC,
								RenderingHints.VALUE_INTERPOLATION_BILINEAR,
								RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
						}
				)
		);

		root.add(
				buildAACheckBoxes(
						renderingQuality,
						RenderingHints.KEY_RENDERING,
						new Object[]  {
								RenderingHints.VALUE_RENDER_QUALITY,
								RenderingHints.VALUE_RENDER_SPEED,
								RenderingHints.VALUE_RENDER_DEFAULT
						}
				)
		);
		
		root.add(
				buildAACheckBoxes(
						strokeNormalization, 
						RenderingHints.KEY_STROKE_CONTROL, 
						new Object[] {
								RenderingHints.VALUE_STROKE_DEFAULT,
								RenderingHints.VALUE_STROKE_NORMALIZE,
								RenderingHints.VALUE_STROKE_PURE
						}
				)
		);
		
		return root;
	}
	
	
	/*
	 * 
	 */
	private JMenu buildAACheckBoxes(JMenu root, Key key, Object[] values) {
		ButtonGroup group = new ButtonGroup();
		JCheckBoxMenuItem item = null;
		for(int i = 0; i < values.length; i++) {
			item = new JCheckBoxMenuItem(values[i].toString());
			group.add(item);
			item.addItemListener(new TextAliasingMenuItemListener(key, values[i]));
			root.add(item);
			
			if (item.getText().matches(".*default.*|.*Default.*|.*DEFAULT.*"))
				item.setSelected(true);
		}

		return root;	
	}
	
	
	/*
	 * 
	 */
	private class TextAliasingMenuItemListener implements ItemListener {
		private Object value;
		private Key    key;
		public TextAliasingMenuItemListener(Key key, Object v) {
			this.value = v;
			this.key   = key;
		}
		/* (non-Javadoc)
		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {
			RenderingHints rh = DrawingAreaView.getInstance().getRenderingHints();
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				rh.remove(key);
			} else {
				rh.put(key, value);
			}
			DrawingAreaView.getInstance().setRenderingHints(rh);
		}
	}
	
	
	/*
	 * 
	 */
	private SingleCDockable wrapToDockable(JComponent view) {
		return new DefaultSingleCDockable(view.getName(), view.getName(), view);
	}
	
	
	/*
	 * 
	 */
//	private class Painter implements ActionListener {
//		private JMenu parent;
//		
//		public Painter(JMenu parent) {
//			this.parent = parent;
//		}
//		
//		/* (non-Javadoc)
//		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//		 */
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			final JMenuItem  source = (JMenuItem) e.getSource();
//			final String userName = parent.getText();
//			Utils.logMessage("Drawing for: "+userName);
//			Thread t = new Thread(new Runnable() {
//				public void run() {
//					RemoteUser user = UserManager.getInstance().getRemoteUser(userName);
//					java.util.Random r = new Random();
//					r.setSeed(System.currentTimeMillis());
//					
//					user.getToolboxController().selectToolByName(source.getName());
//					RemoteMouseEvent event = new RemoteMouseEvent(user, DrawingAreaView.getInstance(), 501, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, r.nextInt(DrawingAreaView.getInstance().getWidth()), r.nextInt(DrawingAreaView.getInstance().getHeight()), 1, false, MouseEvent.BUTTON1);
//					DrawingAreaView.getInstance().dispatchEvent(event);
//					int i = 1;
//					for(i = 0; i < 300; i++) {
//						event = new RemoteMouseEvent(user, DrawingAreaView.getInstance(), 506, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, i, i, 0, false, MouseEvent.NOBUTTON);
//						try {
//							Thread.sleep(100);
//							DrawingAreaView.getInstance().dispatchEvent(event);
//						} catch(InterruptedException e1) {}
//					}
//					event = new RemoteMouseEvent(user, DrawingAreaView.getInstance(), 502, System.currentTimeMillis(), MouseEvent.NOBUTTON, i, i, 0, false, MouseEvent.BUTTON1);
//					DrawingAreaView.getInstance().dispatchEvent(event);
//				}
//			});
//			t.start();
//		}
//	}
}