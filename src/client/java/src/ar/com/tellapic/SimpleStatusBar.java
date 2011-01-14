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
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class SimpleStatusBar extends JLabel implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String statusPrefix = "  Status:   ";
	
	
	private static class Holder {
		private static final SimpleStatusBar INSTANCE = new SimpleStatusBar();
	}
	 
	
	public static SimpleStatusBar getInstance() {
		return Holder.INSTANCE;
	}
	
	
	private SimpleStatusBar() {
		super();
		this.setPreferredSize(new Dimension(100, 24));
		this.setMaximumSize(new Dimension(99999, 24));
		this.setOpaque(true);
		this.setForeground(Color.black);
		this.setBackground(Color.lightGray);
		this.setBorder(BorderFactory.createMatteBorder(1, 5, 1, 1, Color.gray));
		this.setText("<html><b>"+statusPrefix+"</b><font color=red>disconnected</font></html>");
	}
	
	
	public void setStatus(String text, String color) {
		this.setText("<html><b>"+statusPrefix+"</b><font color="+color+">"+text+"</font><</html>");
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (((NetManager)o).isConnected())
			setStatus("connected", "green");
		else
			setStatus("disconnected", "red");
	}
}
