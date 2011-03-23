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

import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ToolBoxWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ToolView          boxView;
	private PaintPropertyView propertyView;
	
	public ToolBoxWindow() {
		
	}
	
	public ToolBoxWindow(ToolView tBoxView, PaintPropertyView pPropertyView) {
		super("ToolBox");
		this.boxView      = tBoxView;
		this.propertyView = pPropertyView;
//		propertyView.setOpaque(true);
//		propertyView.setBackground(Color.black);
//		boxView.setOpaque(true);
//		boxView.setBackground(Color.blue);
		//int height = pPropertyView.getMinimumSize().height + tBoxView.getMinimumSize().height + 20;
		//setMinimumSize(new Dimension(pPropertyView.getMinimumSize().width, height));
		
//		Container   container = getContentPane();
//		GroupLayout layout    = new GroupLayout(container);
//		JSeparator  separator = new JSeparator();
//		separator.setOpaque(true);
//		separator.setBackground(Color.red);
//		 

		setJMenuBar(createMenuBar());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		System.out.println("tools view -> w: "+boxView.getSize().width+" h:"+boxView.getSize().height);
		System.out.println("property view -> w: "+propertyView.getSize().width+" h:"+propertyView.getSize().height);
		setVisible(true);

		pPropertyView.addContainerListener(new ContainerListener() {

			@Override
			public void componentAdded(ContainerEvent arg0) {
				int height = propertyView.getMinimumSize().height + boxView.getMinimumSize().height;
				int width  = Math.max(propertyView.getMinimumSize().width, boxView.getMinimumSize().width);
				//setMinimumSize(new Dimension(width, height));
				//System.out.println("added. w: "+arg0.getComponent().getSize().getWidth()+" h: "+arg0.getComponent().getSize().getHeight());
				pack();
			}

			@Override
			public void componentRemoved(ContainerEvent arg0) {
				int height = propertyView.getMinimumSize().height + boxView.getMinimumSize().height;
				int width  = Math.max((propertyView.getComponentCount() > 0)? propertyView.getMinimumSize().width : 0, boxView.getMinimumSize().width);
				System.out.println("box min width: "+boxView.getMinimumSize().width+" count: "+propertyView.getComponentCount()+"  w: "+width+" h: "+height);
				//setMinimumSize(new Dimension(width, height));
				pack();
			}
		});	
	}
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu optionsMenu = new JMenu("Options");
		JMenuItem item = new JMenuItem("Item");
		
		optionsMenu.setMnemonic(KeyEvent.VK_O);
		optionsMenu.add(item);
		menuBar.add(optionsMenu);
		return menuBar;
	}
}
