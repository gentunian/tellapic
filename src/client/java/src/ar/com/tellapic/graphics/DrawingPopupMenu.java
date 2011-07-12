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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import ar.com.tellapic.TellapicAbstractUser;
import ar.com.tellapic.UserGUIBuilder;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingPopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 1L;
	private AbstractDrawing drawing;
	private JFrame parent;
	
	public DrawingPopupMenu(JFrame parent, AbstractDrawing d) {
		this.parent = parent;
		drawing = d;
		buildDrawingPopup();
	}

	
	private void buildDrawingPopup() {
		/* +------------------------------+ */
		/* |          <ToolName>          | /
		/* +------------------------------+ */ 
		/* |    Delete                    | */
		/* |    Hide                      | */
		/* +------------------------------+ */
		JMenuItem name     = new JMenuItem(drawing.getName());
		JMenuItem delete   = new JMenuItem(Utils.msg.getString("delete"));
		JCheckBoxMenuItem hide = new JCheckBoxMenuItem(Utils.msg.getString("hide"));
		JMenuItem properties = new JMenuItem(Utils.msg.getString("properties"));
		
		name.setEnabled(false);
		name.setHorizontalTextPosition(JLabel.CENTER);
		delete.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
		hide.setAccelerator(KeyStroke.getKeyStroke("H"));
		properties.setAccelerator(KeyStroke.getKeyStroke("P"));
		hide.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				drawing.setVisible(!(e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TellapicAbstractUser user = drawing.getUser();
				user.removeDrawing(drawing);
				drawing.setSelected(false);
			}
		});
//		properties.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				DrawingPropertiesDialog dialog = new DrawingPropertiesDialog(parent, false, drawing);
//				dialog.setLocationRelativeTo(null);
//				dialog.setVisible(true);
//			}
//		});
		hide.setSelected(!drawing.isVisible());
		name.setFont(Utils.MAIN_FONT);
		name.setAlignmentX(JPopupMenu.CENTER_ALIGNMENT);
		add(name);
		add(properties);
		addSeparator();
		/* Only allow removing from local user */
		if (!drawing.getUser().isRemote())
			add(delete);
		add(hide);
	}
}
