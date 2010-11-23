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
package com.tellapic.graphics;

import com.tellapic.UserManager;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Main {

	static DrawingAreaModel drawingAreaModel;
	static DrawingAreaView   dav;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		  // UI theme
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (UnsupportedLookAndFeelException e) {
//            //fallback
//        } catch (ClassNotFoundException e) {
//            //fallback
//        } catch (InstantiationException e) {
//            //fallback
//        } catch (IllegalAccessException e) {
//            //fallback
//        }
		ToolFactory.registerTool(Ellipse.class.getName());
		ToolFactory.registerTool(Line.class.getName());
		ToolFactory.registerTool(Zoom.class.getName());
		ToolFactory.registerTool(Rectangle.class.getName());
		ToolFactory.registerTool(Text.class.getName());
		ToolFactory.registerTool(Marker.class.getName());
		
		// simulate we are connected with id = 0
		UserManager userManager = UserManager.getInstance();
		userManager.createLocalUser(0);
		
		//UserGUIBuilder gui = new UserGUIBuilder((LocalUser)userManager.getLocalUser());
//		JFrame frame = new JFrame("test");
//		UserView view = new UserView();
//		frame.add(view);
//		frame.pack();
//		frame.setVisible(true);
//		
//		ToolBoxModel           model  = new ToolBoxModel();
//		ToolView           toolsView  = new ToolView();
//		model.addObserver(toolsView);
//		
//		ToolViewController      toolViewController = new ToolViewController(model);
//		PaintPropertyView       propertyView       = new PaintPropertyView();
//		PaintPropertyController propertyController = new PaintPropertyController(model, propertyView);
//
//		model.addObserver(propertyView);
//		
//		toolsView.setController(toolViewController);
//		propertyView.setController(propertyController);
		
		//ToolBoxWindow t = new ToolBoxWindow(toolsView, propertyView);
		//ToolBoxWindow t = new ToolBoxWindow();
		//t.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		CControl control1 = new CControl(t);
//		t.setLayout(new GridLayout(1,1));
//		t.add(control1.getContentArea());
//		drawingAreaModel = DrawingAreaModel.getInstance();
//		ChatView     chatView = new ChatView();
//		JFrame  drawingWindow = new JFrame("drawing window");
//		CControl     control1 = new CControl(drawingWindow);
//		dav = DrawingAreaView.getInstance();
//		SingleCDockable dock1 = wrapToDockable(toolsView);
//		SingleCDockable dock2 = wrapToDockable(propertyView);
//		SingleCDockable dock3 = wrapToDockable(dav);
//		SingleCDockable dock4 = wrapToDockable(chatView);
//		CGrid grid = new CGrid(control1);
//		drawingWindow.setPreferredSize(new Dimension(400,400));
//		grid.add(0,   0, 50,  50, dock1);
//		grid.add(0, 50, 50, 100, dock2);
//		grid.add(50, 0, 300, 400, dock3);
//		grid.add(350, 0, 50, 100, dock4);
//		CContentArea  content = control1.getContentArea();
//		content.deploy(grid);
//		drawingWindow.add(content);
//		drawingWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		drawingWindow.setLayout(new GridLayout(1,1));
//		drawingWindow.pack();
//		drawingWindow.setVisible(true);
//		
//		
//		DrawingLocalController drawingController = new DrawingLocalController(model, drawingAreaModel, dav);
//		
//		propertyController.setDrawingController(drawingController);
//		drawingController.setController(propertyController);
//		drawingAreaModel.addObserver(dav);
//		dav.addMouseMotionListener(drawingController);
//		dav.addMouseListener(drawingController);
//		dav.addMouseWheelListener(drawingController);
		
//		CControl control2 = new CControl(dav);
//		dav.add(control2.getContentArea());
//		SingleCDockable dock3 = wrapToDockable(new ChatView());
//		control2.add(dock3);
//		dock3.setVisible(true);
//		ChatClientModel chatModel = new ChatClientModel();
//		ChatController chatController = new ChatController(chatModel, chatView);
//		chatView.setController(chatController);
//		chatModel.addObserver(chatView);
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		new Thread(new Runnable() {
//			public void run() {
//				test(10);
//			}
//		}).start();
//		new Thread(new Runnable() {
//			public void run() {
//				test(30);
//			}
//		}).start();
//		new Thread(new Runnable() {
//			public void run() {
//				test(50);
//			}
//		}).start();
//		new Thread(new Runnable() {
//			public void run() {
//				test(80);
//			}
//		}).start();
	}

   
//	private static void test(int x) {
//		ToolBoxModel            model = new ToolBoxModel();
//		ToolViewController      controller = new ToolViewController(model);
//		PaintPropertyController propertyController = new PaintPropertyController(model);
//		//DrawingLocalController  drawingController  = new DrawingLocalController(model, drawingAreaModel, dav);
//		
//		controller.selectToolEvent("Line");
//		propertyController.handleEndCapsChange(0);
//		propertyController.handleLineJoinsChange(0);
//		propertyController.handleOpacityChange(1.0f);
//		propertyController.handleWidthChange(10);
//		
//		JTextField dummy = new JTextField();
//		MouseEvent event1 = new MouseEvent(dummy, 0, System.currentTimeMillis(), 0, x, 10, 0, false, MouseEvent.BUTTON1);
//		drawingController.mousePressed(event1);
//		int i = 0;
//		for(i = 0; i < 200; i++) {
//			MouseEvent event = new MouseEvent(dummy, i+1, System.currentTimeMillis(), 0, x, 10+i, 0, false);
//			try {
//				Thread.sleep(100);
//				drawingController.mouseDragged(event);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		MouseEvent event = new MouseEvent(dummy, i+1, System.currentTimeMillis(), 0, x, i, 0, false, MouseEvent.BUTTON1);
//		drawingController.mouseReleased(event);
//	}
}
