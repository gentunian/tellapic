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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ar.com.tellapic.NetManager.WrongPacketException;
import ar.com.tellapic.graphics.EllipseNet;
import ar.com.tellapic.graphics.LineNet;
import ar.com.tellapic.graphics.MarkerNet;
import ar.com.tellapic.graphics.PenNet;
import ar.com.tellapic.graphics.RectangleNet;
import ar.com.tellapic.graphics.TextNet;
import ar.com.tellapic.graphics.ToolFactory;
import ar.com.tellapic.graphics.Zoom;
import ar.com.tellapic.lib.tellapicConstants;
import ar.com.tellapic.utils.Utils;

import com.sun.xml.internal.bind.v2.runtime.property.Property;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Main {
	private static final int HOST_ARG = 0;
	private static final int PORT_ARG = 1;
	private static final int NAME_ARG = 2;
	private static final int PWD_ARG  = 3;
	private static int result;
	private static MainDialog main;
	private static NetManager netManager;
	
	
	static {
		System.loadLibrary("tellapicjava");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* Start network manager by getting and instance */
		netManager = NetManager.getInstance();
		
		/* Set the look and feel */
		setLookAndFeel();
		
		if (args.length == 0) {
			/* We were invoked without arguments, show the Main Dialog. */
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					/* Dialog is modal, so wait until it's disposed */
					MainDialog main = showMainDialog();
					verifyDialogInput(main);
				}
			});
		} else if (args.length == 4){
			/* We were invoked with, maybe, the correct arguments. */
			if (argumentsCheck(args)) {
				if (tryJoin(args))
					initiate(SessionUtils.getId(), SessionUtils.getUsername());
				else {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(null, Utils.msg.getString("errorconnect"), Utils.msg.getString("errorconnecttitle"), JOptionPane.ERROR_MESSAGE);
							Utils.shutdown();
						}
					});
				}
			}
		}
	}



	/**
	 * @param args
	 * @return
	 */
	private static boolean argumentsCheck(String[] args) {
		return true;
	}



	private static void verifyDialogInput(final MainDialog main) {
		if (main.isUserInput()) {
			switch(main.getOption()) {

			case MainDialog.CREATE_TAB:
				/* We should create a server instance */
				break;
				
			case MainDialog.JOIN_TAB:
			case MainDialog.FAVOURITE_TAB:
				/* We connect/join to a known server */
				System.out.println("Joining to server "+main.getRemoteHost()+":"+main.getRemotePort());
				/* Dont run this on the EDT */
				new Thread(new Runnable() {
					public void run() {
						if (tryJoin(null))
							initiate(SessionUtils.getId(), SessionUtils.getUsername());
						else {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									JOptionPane.showMessageDialog(null, Utils.msg.getString("errorconnect"), Utils.msg.getString("errorconnecttitle"), JOptionPane.ERROR_MESSAGE);
									Utils.shutdown();
								}
							});
						}
					}
				}).start();
				break;
			}

		} else {
			//TODO: if not user option
		}
	}
	
	
	/**
	 * Try connecting to the server. If we succeded, return true. False otherwise.
	 */
	private static boolean tryJoin(String[] args) {
		String name = null;
		String pwd = null;
		String host = null;
		String port = null;
		if (args != null) {
			host = args[HOST_ARG];
			port = args[PORT_ARG];
			name = args[NAME_ARG];
			pwd = args[PWD_ARG];
			
		} else {
			host = main.getRemoteHost();
			port = main.getRemotePort();
			name = main.getUsername();
			pwd = main.getPassword();
		}
		try {
			/* Send a message to the network manager to connect to a known server. */
			result = netManager.connect(host, port, name, pwd);
			return (result != 0);
		} catch(WrongPacketException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	/**
	 * 
	 * @return
	 */
	private static MainDialog showMainDialog() {
		JFrame frame = new JFrame();
		frame.setIconImage(Utils.createIconImage(112, 75, "/icons/system/logo_small.png"));
		main = new MainDialog(frame);
		main.setVisible(true);
		return main;
	}
	
	
	/**
	 * 
	 * @param id
	 * @param name
	 */
	private static void initiate(final int id, final String name) {
		ToolFactory.registerToolClassName(99, Zoom.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_ELLIPSE, EllipseNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_LINE, LineNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_RECT, RectangleNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_TEXT, TextNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_MARKER, MarkerNet.class.getName());
		ToolFactory.registerToolClassName(tellapicConstants.TOOL_PATH, PenNet.class.getName());
		
		final UserManager userManager = UserManager.getInstance();
		final LocalUser luser = (LocalUser) userManager.createUser(id, name, false);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				UserManager userManager = UserManager.getInstance();
				new UserGUIBuilder(luser);
			}
		});
		
	}
	
	
	/**
	 * 
	 */
	private static void setLookAndFeel() {
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
	}
}
