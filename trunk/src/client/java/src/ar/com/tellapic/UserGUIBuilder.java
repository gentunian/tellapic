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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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

import ar.com.tellapic.chat.ChatModelController;
import ar.com.tellapic.chat.ChatView;
import ar.com.tellapic.chat.ChatViewController;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.graphics.PaintPropertyView;
import ar.com.tellapic.graphics.Tool;
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
  
	private UsersView               userView;
	private ToolView                toolView;
	private PaintPropertyView       propertyView;
	private ChatView                chatView;
	private JFrame                  mainWindow;
	private JScrollPane             scrollPane;

	
	public UserGUIBuilder(LocalUser user) throws NullPointerException{
		if (user == null)
			throw new NullPointerException("LocalUser cannot be null.");
		
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
//		PaintPropertyController propertyController = new PaintPropertyController(model);
//		IToolBoxController      toolViewController = new ToolBoxController(model);
//		
//		user.setToolboxController(toolViewController);
//		user.setPaintController(propertyController);
		
		// Instantiates all the GUIs.
		userView     = UsersView.getInstance();
		toolView     = new ToolView(model);
		propertyView = new PaintPropertyView();	
		chatView     = new ChatView(new ChatModelController());
		
		model.addObserver(propertyView);
		model.addObserver(toolView);
		ChatViewController c = new ChatViewController(chatView);
		userView.setChatViewController(c);
		
		
		// Get the area where all the living Drawing objects will be drawn.
		DrawingAreaView   drawingAreaView  = DrawingAreaView.getInstance();
		scrollPane = new JScrollPane(drawingAreaView);
		scrollPane.setName(drawingAreaView.getName());
//		drawingAreaView.setPropertyController(propertyController);
		toolView.setToolBoxController(user.getToolboxController());
		propertyView.setPaintPropertyController(user.getPaintController());
		for(Tool tool : model.getTools().values()) {
			drawingAreaView.addMouseListener(tool);
			drawingAreaView.addMouseMotionListener(tool);
		}

		/****************************************/
		/* Creates the dockable station and gui */
		/****************************************/
		mainWindow = new JFrame("Tellapic - "+user.getName());
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
		
		grid.add(0, 0, 10, 200, dock1);
		grid.add(10, 0, 350, 200, dock3);
		grid.add(360, 0, 100, 100, dock5);
		grid.add(360, 100, 100, 100, dock4);

		
		content.deploy(grid);
		
		mainWindow.getContentPane().add(content, BorderLayout.CENTER);
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {
				Utils.shutdown();
			}
			public void windowClosing(WindowEvent e) {
				mainWindow.dispose();
			}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
		});
		//mainWindow.setLayout(new GridLayout(2,1));
		mainWindow.setJMenuBar(createMenuBar());
		mainWindow.pack();
		mainWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainWindow.setVisible(true);
	}	
	
	
	
	private JMenuBar createMenuBar() {
	
		JMenuBar menuBar  = new JMenuBar();
		
		/* Adds the File option */
		menuBar.add(buildFileMenu());

		/* Adds the View Menu */
		menuBar.add(buildViewMenu());
		
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
	private JMenu buildFileMenu() {
		JMenu     fileMenu  = new JMenu(Utils.msg.getString("file"));
		JMenuItem reconnect = new JMenuItem(Utils.msg.getString("reconnect"));
		JMenuItem exit      = new JMenuItem(Utils.msg.getString("exit"));

		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		reconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						/* Dont run this on the EDT */
						NetManager.getInstance().reconnect();
					}
				}).start();
			}
		});

		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainWindow.dispose();
				Utils.shutdown();
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
	 * 
	 */
	private SingleCDockable wrapToDockable(JComponent view) {
		return new DefaultSingleCDockable(view.getName(), view.getName(), view);
	}
}
