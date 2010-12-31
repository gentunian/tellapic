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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Random;

import javax.sound.sampled.Line;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
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
import ar.com.tellapic.graphics.DrawingLocalController;
import ar.com.tellapic.graphics.Ellipse;
import ar.com.tellapic.graphics.IToolBoxController;
import ar.com.tellapic.graphics.PaintPropertyController;
import ar.com.tellapic.graphics.PaintPropertyView;
import ar.com.tellapic.graphics.RemoteMouseEvent;
import ar.com.tellapic.graphics.ToolBoxController;
import ar.com.tellapic.graphics.ToolBoxModel;
import ar.com.tellapic.graphics.ToolView;
import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;

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
	private JFrame                  drawingWindow;
	
	public UserGUIBuilder(LocalUser user) {
		
		// Get the drawing area model instance. Its where all Drawing objects will live.
//		DrawingAreaModel  drawingAreaModel = DrawingAreaModel.getInstance();
		
		// Get the area where all the living Drawing objects will be drawn.
		DrawingAreaView   drawingAreaView  = DrawingAreaView.getInstance();
//		user.addObserver(drawingAreaView);
		
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
		DrawingLocalController  drawingController  = new DrawingLocalController();
		IToolBoxController      toolViewController = new ToolBoxController(model);
		


		// Instantiates all the GUIs.
		userView = UserView.getInstance();
		toolView = new ToolView(model);
		propertyView = new PaintPropertyView();	
		chatView = new ChatView(new ChatController());
		
		model.addObserver(propertyView);
		model.addObserver(toolView);
		
		propertyController.setDrawingController(drawingController);
		drawingController.setController(propertyController);
		drawingAreaView.addMouseMotionListener(drawingController);
		drawingAreaView.addMouseListener(drawingController);
		drawingAreaView.addMouseWheelListener(drawingController);
		toolView.setController(toolViewController);
		propertyView.setController(propertyController);
		
		JScrollPane scrollPane = new JScrollPane(drawingAreaView);
		scrollPane.setName(drawingAreaView.getName());
		
		
		/****************************************/
		/* Creates the dockable station and gui */
		/****************************************/
		drawingWindow = new JFrame("drawing window");
		CControl     control1 = new CControl(drawingWindow);
		CGrid            grid = new CGrid(control1);
		CContentArea  content = control1.getContentArea();
		SingleCDockable dock1 = wrapToDockable(toolView);
		SingleCDockable dock2 = wrapToDockable(propertyView);
		SingleCDockable dock3 = wrapToDockable(scrollPane);
		SingleCDockable dock4 = wrapToDockable(chatView);
		SingleCDockable dock5 = wrapToDockable(userView);
		
		drawingWindow.setPreferredSize(new Dimension(400,400));
		grid.add(0,   0, 50,   50, dock1);
		grid.add(0,  50, 50,  100, dock2);
		grid.add(50,  0, 300, 400, dock3);
		grid.add(350, 0 , 50,  50, dock5);
		grid.add(350,50,  50, 100, dock4);
		content.deploy(grid);
		drawingWindow.add(content);
		drawingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		drawingWindow.setLayout(new GridLayout(1,1));
		drawingWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
		drawingWindow.setJMenuBar(createMenuBar());
		drawingWindow.pack();
		drawingWindow.setVisible(true);
	}	
	
	
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar  = new JMenuBar();
		JMenu fileMenu    = new JMenu(Utils.msg.getString("file"));
		JMenu optionsMenu = new JMenu(Utils.msg.getString("options"));
		JMenu testMenu    = new JMenu("testing");
		final JMenu testMenuUserAction  = new JMenu("User Ations");
		JMenuItem addUserItem     = new JMenuItem("Add User");
		JMenuItem delUserItem     = new JMenuItem("Remove User");
		JMenu themeSubMenu = new JMenu(Utils.msg.getString("theme"));
		JCheckBoxMenuItem themeJava = new JCheckBoxMenuItem("JAVA");
		JCheckBoxMenuItem themeGTK  = new JCheckBoxMenuItem("GTK");
		ButtonGroup group = new ButtonGroup();
		
		addUserItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = JOptionPane.showInputDialog("User Name:");
				if (name == null)
					return;
				UserManager.getInstance().addUser(9, name);
				testMenuUserAction.setEnabled(true);
				JMenu userMenu = new JMenu(name);
				JMenuItem drawEllipseItem = new JMenuItem("Draw Ellipse");
				JMenuItem drawBoxItem     = new JMenuItem("Draw Box");
				JMenuItem drawLineItem    = new JMenuItem("Draw Line");
				//JMenuItem drawTextItem    = new JMenuItem("Draw Text");
				
				drawEllipseItem.setName(Ellipse.class.getSimpleName());
				drawEllipseItem.addActionListener(new Painter(userMenu));
				drawBoxItem.setName(Rectangle.class.getSimpleName());
				drawBoxItem.addActionListener(new Painter(userMenu));
				drawLineItem.setName(Line.class.getSimpleName());
				drawLineItem.addActionListener(new Painter(userMenu));
				
				userMenu.add(drawEllipseItem);
				userMenu.add(drawBoxItem);
				userMenu.add(drawLineItem);
				//userMenu.add(drawTextItem);
				testMenuUserAction.add(userMenu);
			}
		});
		
		delUserItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = JOptionPane.showInputDialog("User Name:");
				Utils.logMessage("Deleting user: "+name);
				UserManager.getInstance().delUser(name);
				for(int i = 0; i < testMenuUserAction.getMenuComponentCount(); i++) {
					JMenu menu = (JMenu)testMenuUserAction.getMenuComponent(i);
					Utils.logMessage("c :"+menu.getText());
					if (menu.getText().equals(name)) 
						testMenuUserAction.remove(menu);
				}
				testMenuUserAction.setEnabled(testMenuUserAction.getMenuComponentCount() > 0);
			}
		});		
		themeJava.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				try {
		            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		            SwingUtilities.updateComponentTreeUI(drawingWindow);
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
		            SwingUtilities.updateComponentTreeUI(drawingWindow);
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
		fileMenu.setMnemonic(KeyEvent.VK_F);
		optionsMenu.setMnemonic(KeyEvent.VK_O);
		menuBar.add(fileMenu);
		themeSubMenu.add(themeJava);
		themeSubMenu.add(themeGTK);
		optionsMenu.add(themeSubMenu);
		menuBar.add(optionsMenu);
		testMenuUserAction.setEnabled(false);
		testMenu.add(testMenuUserAction);
		testMenu.add(addUserItem);
		testMenu.add(delUserItem);
		menuBar.add(testMenu);
		return menuBar;
	}
	
	private SingleCDockable wrapToDockable(JComponent view) {
		return new DefaultSingleCDockable(view.getName(), view.getName(), view);
	}
	
	private class Painter implements ActionListener {
		private JMenu parent;
		
		public Painter(JMenu parent) {
			this.parent = parent;
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			final JMenuItem  source = (JMenuItem) e.getSource();
			final String userName = parent.getText();
			Utils.logMessage("Drawing for: "+userName);
			Thread t = new Thread(new Runnable() {
				public void run() {
					RemoteUser user = UserManager.getInstance().getRemoteUser(userName);
					java.util.Random r = new Random();
					r.setSeed(System.currentTimeMillis());
					
//					user.getToolBoxModel().setCurrentTool(source.getName());
					user.selectTool(source.getName());
					RemoteMouseEvent event = new RemoteMouseEvent(user, DrawingAreaView.getInstance(), 501, System.currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK, r.nextInt(DrawingAreaView.getInstance().getWidth()), r.nextInt(DrawingAreaView.getInstance().getHeight()), 1, false, MouseEvent.BUTTON1);
//					MouseEvent event = new MouseEvent(DrawingAreaView.getInstance(), 501, System.currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK, 10, 10, 1, false, MouseEvent.BUTTON1);
					DrawingAreaView.getInstance().dispatchEvent(event);
					int i = 1;
					for(i = 0; i < 300; i++) {
						event = new RemoteMouseEvent(user, DrawingAreaView.getInstance(), 506, System.currentTimeMillis(), MouseEvent.BUTTON1_DOWN_MASK, i, i, 0, false, MouseEvent.NOBUTTON);
						try {
							Thread.sleep(100);
							DrawingAreaView.getInstance().dispatchEvent(event);
						} catch(InterruptedException e1) {}
					}
					event = new RemoteMouseEvent(user, DrawingAreaView.getInstance(), 502, System.currentTimeMillis(), MouseEvent.NOBUTTON, i, i, 0, false, MouseEvent.BUTTON1);
					DrawingAreaView.getInstance().dispatchEvent(event);
				}
			});
			t.start();
		}
	}
}
