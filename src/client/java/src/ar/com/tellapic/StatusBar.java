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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;

import ar.com.tellapic.utils.Utils;

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
	private HashMap<String, Color> colors;
	private JLabel connectionStatusLabel;
	private JLabel connectionStatus;
	private JLabel toolInfo;
	private JLabel pingLabel;
	private JLabel ping;
	private JLabel mouseCoords;
	private IsConnectingLabel t;
	
	private static class Holder {
		private static final StatusBar INSTANCE = new StatusBar();
	}
	 
	
	public static StatusBar getInstance() {
		return Holder.INSTANCE;
	}
	
	public StatusBar() {
		super();
		ping = new JLabel("0 ms");
		mouseCoords = new JLabel();
		toolInfo = new JLabel();
		connectionStatusLabel = new JLabel(Utils.msg.getString("status"));
		pingLabel = new JLabel(Utils.msg.getString("latency"));
		pingLabel.setLabelFor(ping);
		connectionStatusLabel.setLabelFor(connectionStatus);
		connectionStatus = new JLabel();
		pingLabel.setFont(new Font("Droid Sans", 1, 12)); // NOI18N
		connectionStatusLabel.setFont(new Font("Droid Sans", 1, 12)); // NOI18N
		toolInfo.setFont(new Font("Droid Sans", 1, 12));
		mouseCoords.setFont(new Font("Droid Sans", 0, 12));
		ping.setFont(new Font("Droid Sans", 0, 12));
		connectionStatus.setFont(new Font("Droid Sans", 0, 12));
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setMaximumSize(new Dimension(10, 24));
		separator.setMinimumSize(new Dimension(10, 24));
		separator.setPreferredSize(new Dimension(10, 24));
		
		setPreferredSize(new Dimension(100, 24));
		setMaximumSize(new Dimension(99999, 24));
		setOpaque(true);
		setForeground(Color.black);
		setBackground(Color.lightGray);
		setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.gray));
		//connectionStatus.setText("<html><b>"+statusPrefix+"</b><font color=red>disconnected</font></html>");
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(mouseCoords)
						.addGap(18, 18, 18)
						.addComponent(toolInfo)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 392, Short.MAX_VALUE)
						.addComponent(connectionStatusLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(connectionStatus)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(pingLabel)
						.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
						.addComponent(ping)
						.addContainerGap())
		);
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
								.addComponent(separator, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
								.addComponent(mouseCoords, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(ping, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(pingLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(connectionStatus, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(connectionStatusLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(toolInfo, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap(54, Short.MAX_VALUE))
		);
		//setLayout(new BorderLayout(20,1));
		//		add(mouseCoords, BorderLayout.LINE_START);
		//		add(connectionStatus, BorderLayout.LINE_END);
		//		add(toolInfo, BorderLayout.CENTER);
		//		add(new JLabel("test"), BorderLayout.LINE_END);
		colors = new HashMap<String, Color>();
		colors.put("Connected", Color.blue);
		colors.put("Connecting...", Color.yellow);
		colors.put("Disconnected", Color.red);
		setVisible(true);
	}


	/**
	 * 
	 * @param value
	 */
	public void setPing(double value) {
		ping.setText(String.format("%.2f",value)+" ms");
	}
	
	
	/**
	 * 
	 * @param text
	 * @param color
	 */
	public void setStatus(String text, Color color) {
		if (text.matches(NetManager.CONNECTING_STRING)) {
			
			if (t != null) {
				if (!t.isRunning()) {
					t = new IsConnectingLabel(connectionStatus, NetManager.CONNECTING_STRING, color, '.', 3);
					t.start();
				}
			} else {
				t = new IsConnectingLabel(connectionStatus, NetManager.CONNECTING_STRING, color, '.', 3);
				t.start();
			}
		} else {
			if (t != null) {
				t.setRunning(false);
				while(t.isAlive());
			}
			connectionStatus.setText(text);
			connectionStatus.setForeground(color);
		}
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
			String status = (((NetManager)o).getStatus());
			setStatus(status, colors.get(status));
			setPing(ping);
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
		// WTF?!?!?!!?!??
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
	
	private class IsConnectingLabel extends Thread {
		private boolean running;
		private JLabel  label;
		private String  text;
		private int     textLen;
		private char    ending;
		private int     endingNumber;
		
		public IsConnectingLabel(JLabel l, String t, Color color, char c, int n) {
			running = false;
			label = l;
			text  = t;
			ending = c;
			endingNumber = n;
			
			FontMetrics  metrics = label.getFontMetrics(label.getFont());
			String str = t;
			for(int i = 0; i < n; i++)
				str+=c;
			textLen = metrics.stringWidth(str);
			label.setForeground(color);
		}
		
		/**
		 * @return
		 */
		public boolean isRunning() {
			return running;
		}

		public void run() {
			int count = 0;
			running = true;
			Dimension d = label.getMinimumSize();
			label.setMinimumSize(new Dimension(textLen, 24));
			while(running) {
				try {
					Thread.sleep(800);
					if (count%endingNumber == 0)
						label.setText(text);
					else {
						String wtf = label.getText();
						label.setText(wtf+ending);
					}
					
					count++;
				} catch (InterruptedException e) {
					running = false;
				}
			}
			label.setMinimumSize(d);
		}
		
		public void setRunning(boolean isRunning) {
			running = isRunning;
		}
	}
}
