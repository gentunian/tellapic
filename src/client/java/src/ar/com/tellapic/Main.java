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

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ar.com.tellapic.graphics.DrawingAreaModel;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.graphics.EllipseNet;
import ar.com.tellapic.graphics.LineNet;
import ar.com.tellapic.graphics.MarkerNet;
import ar.com.tellapic.graphics.RectangleNet;
import ar.com.tellapic.graphics.TextNet;
import ar.com.tellapic.graphics.ToolFactory;
import ar.com.tellapic.graphics.Zoom;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Main {

	static DrawingAreaModel  drawingAreaModel;
	static DrawingAreaView   dav;
	
	static {
		System.loadLibrary("tellapicjava");
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

	  // UI theme
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            //fallback
        } catch (ClassNotFoundException e) {
            //fallback
        } catch (InstantiationException e) {
            //fallback
        } catch (IllegalAccessException e) {
            //fallback
        }
		
		if (args.length == 0) {
			MainDialog main = new MainDialog();
			main.setVisible(true);
			if (main.isUserInput()) {
				switch(main.getOption()) {
				
				case MainDialog.CREATE_TAB:
					break;
					
				case MainDialog.JOIN_TAB:
					System.out.println("Joining to server "+main.getRemoteHost()+":"+main.getRemotePort());
					int r = NetManager.getInstance().connect(main.getRemoteHost(), main.getRemotePort(), main.getJoinUsername(), main.getJoinPassword());
					if (r > 0)
						initiate(SessionUtils.getId(), SessionUtils.getUsername());
					else
						JOptionPane.showMessageDialog(null, Utils.msg.getString("errorconnect"), Utils.msg.getString("errorconnecttitle"), JOptionPane.ERROR_MESSAGE);
					break;
					
				default:
					break;
				}
			} else {
				//TODO
			}
		} else {
			//TODO
		}
		System.out.println("HOLA");
	}
	
	public static int verifyDialogInput(MainDialog main) {
		//TODO: do what this menthod is mentioned to be
		return main.getOption();
	}
	
	
	public static void initiate(int id, String name) {
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_ELLIPSE, EllipseNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_LINE, LineNet.class.getName());
		ToolFactory.registerToolClassName(99, Zoom.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_RECT, RectangleNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_TEXT, TextNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_MARKER, MarkerNet.class.getName());
		
		UserManager userManager = UserManager.getInstance();
		userManager.createLocalUser(id, name);
	}
	
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
