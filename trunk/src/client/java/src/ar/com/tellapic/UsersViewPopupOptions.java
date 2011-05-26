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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import ar.com.tellapic.chat.ChatViewController;
import ar.com.tellapic.graphics.Drawing;
import ar.com.tellapic.graphics.PaintPropertyColor;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UsersViewPopupOptions extends JPopupMenu {

	
	private static final String USER_FONT = "Droid-10-italic";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ChatViewController controller;
	protected int x;
	protected int y;
	
	
	public UsersViewPopupOptions(Object data, ChatViewController c) throws NullPointerException {
		if (data == null)
			throw new NullPointerException("data cannot be null.");

		if (c == null)
			throw new NullPointerException("c cannot be null.");
		
		controller = c;
		if (data instanceof AbstractUser)
			buildUserPopup((AbstractUser) data);
		else if (data instanceof Drawing) 
			buildDrawingPopup((Drawing)data);
		
		addPopupMenuListener(new PopupMenuListener(){
			public void popupMenuCanceled(PopupMenuEvent e) {}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				x = getLocationOnScreen().x;
				y = getLocationOnScreen().y;
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
		});
	}
	
	
	/**
	 * @param data
	 */
	private void buildDrawingPopup(final Drawing drawing) {
		/* +------------------------------+ */
		/* |          <ToolName>          | /
		/* +------------------------------+ */ 
		/* |    Delete                    | */
		/* |    Hide                      | */
		/* +------------------------------+ */
		JMenuItem name     = new JMenuItem(drawing.getName());
		JMenuItem delete   = new JMenuItem(Utils.msg.getString("delete"));
		JCheckBoxMenuItem hide = new JCheckBoxMenuItem(Utils.msg.getString("hide"));
		
		name.setEnabled(false);
		name.setHorizontalTextPosition(JLabel.CENTER);
		delete.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
		hide.setAccelerator(KeyStroke.getKeyStroke("H"));
		hide.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				drawing.getUser().setDrawingVisible(drawing, !(e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("ASDFAS: "+e.getSource());
				AbstractUser user = drawing.getUser();
				user.removeDrawing(drawing);
			}
		});
		hide.setSelected(!drawing.isVisible());
		name.setFont(Font.decode(USER_FONT));
		name.setAlignmentX(JPopupMenu.CENTER_ALIGNMENT);
		add(name);
		addSeparator();
		/* Only allow removing from local user */
		if (!drawing.getUser().isRemote())
			add(delete);
		add(hide);
	}


	private void buildUserPopup(final AbstractUser user) {
		/* +------------------------------+ */
		/* |          <username>          | /
		/* +------------------------------+ */ 
		/* |    Initiate chat             | */
		/* |    Set custom color...       | */
		/* |    Hide                      | */
		/* +------------------------------+ */
		JMenuItem name     = new JMenuItem(user.getName());
		JMenuItem doChat   = new JMenuItem("Initiate chat...");
		JMenuItem setColor = new JMenuItem("Set custom color...");
		JCheckBoxMenuItem hide = new JCheckBoxMenuItem("Hide");
		
		name.setEnabled(false);
		name.setHorizontalTextPosition(JLabel.CENTER);
		doChat.setAccelerator(KeyStroke.getKeyStroke("ENTER"));
		setColor.setAccelerator(KeyStroke.getKeyStroke("C"));
		hide.setAccelerator(KeyStroke.getKeyStroke("H"));
		hide.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				user.setVisible(!(e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		setColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CustomPropertiesDialog popup = null;
				PaintPropertyColor c = null;
				try {
					c = (PaintPropertyColor) user.getCustomProperty(AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
				} catch (NoSuchPropertyTypeException e2) {
					e2.printStackTrace();
				}
				popup = new CustomPropertiesDialog(null, true, user, c);
				popup.setLocation(x - popup.getSize().width, y);
				popup.setVisible(true);
				if (popup.getReturnStatus() != CustomPropertiesDialog.RET_CANCEL) {
					try {
						Color color = popup.getCustomColor();
						if (color != null)
							user.setCustomProperty(new PaintPropertyColor(color), AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
						else
							user.removeCustomProperty(AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
					} catch (NoSuchPropertyTypeException e1) {
						e1.printStackTrace();
					} catch (WrongPropertyTypeException e1) {
						e1.printStackTrace();
					}
				} else
					try {
						user.removeCustomProperty(AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
					} catch (NoSuchPropertyTypeException e1) {
						e1.printStackTrace();
					}
			}
		});
		doChat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.initiateChat(user);
			}
		});
		hide.setSelected(!user.isVisible());
		name.setFont(Font.decode(USER_FONT));
		name.setAlignmentX(JPopupMenu.CENTER_ALIGNMENT);
		add(name);
		addSeparator();
		add(doChat);
		add(hide);
		add(setColor);
	}
}
