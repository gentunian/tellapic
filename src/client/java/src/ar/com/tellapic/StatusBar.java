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
import java.awt.Image;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class StatusBar extends JPanel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String statusPrefix = "  Status:   ";
	private JLabel connectionStatus;
	private JLabel mouseCoords;
	private JLabel toolInfo;
	
	private static class Holder {
		private static final StatusBar INSTANCE = new StatusBar();
	}
	 
	
	public static StatusBar getInstance() {
		return Holder.INSTANCE;
	}
	
	public StatusBar() {
		super();
		this.setPreferredSize(new Dimension(100, 24));
		this.setMaximumSize(new Dimension(99999, 24));
		this.setOpaque(true);
		this.setForeground(Color.black);
		this.setBackground(Color.lightGray);
		this.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.gray));
		mouseCoords = new JLabel();
		connectionStatus = new JLabel();
		toolInfo = new JLabel();
		connectionStatus.setText("<html><b>"+statusPrefix+"</b><font color=red>disconnected</font></html>");
		this.setLayout(new BorderLayout(20,1));
		add(mouseCoords, BorderLayout.LINE_START);
		add(connectionStatus, BorderLayout.LINE_END);
		add(toolInfo, BorderLayout.CENTER);
		setVisible(true);
	}
	
	
	/**
	 * 
	 * @param value
	 */
	public void setPing(String value) {
		//ping.setText("<html><b>Ping: </b>"+value+" ms");
	}
	
	
	/**
	 * 
	 * @param text
	 * @param color
	 */
	public void setStatus(String text, String color, double ping) {
		connectionStatus.setText("<html><b>"+statusPrefix+"</b><font color="+color+">"+text+"</font>  | <b>Latency: </b>"+String.format("%.2f",ping)+" ms</html>");
	}
	
	
	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void setMouseCoordinates(int x, int y) {
		mouseCoords.setText(" ("+x+", "+y+")");
	}
	

	/**
	 * 
	 * @param info
	 */
	public void setToolInfo(String iconPath, String info) {
		toolInfo.setIcon(createImageIcon(iconPath, ""));
		toolInfo.setText("<html><b>"+info+"</b></html>");
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof NetManager) {
			
			double ping = ((NetManager)o).getPing();

			if (((NetManager)o).isConnected())
				setStatus("connected.", "green", ping);
			else
				setStatus("disconnected.", "red", ping);
		}
	}

	/**
	 * @param b
	 */
	public void showMouseCoordinates(boolean b) {
		mouseCoords.setText("");
	}

	/**
	 * @param b
	 */
	public void setToolInfo(boolean b) {
		toolInfo.setText("");
		toolInfo.setIcon(null);
	}
	
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = Main.class.getResource(path);
		if (imgURL != null) {
			ImageIcon icon = new ImageIcon(imgURL, description);
			Image     img  = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
			return new ImageIcon(img);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}
