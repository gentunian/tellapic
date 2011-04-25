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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class TreePopupOptions extends JPopupMenu {

	
	private static final String USER_FONT = "Droid-10-italic";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserOptionsController controller;
	protected int x;
	protected int y;
	
	
	public TreePopupOptions(Object data, UserOptionsController c) throws NullPointerException {
		if (data == null)
			throw new NullPointerException("data cannot be null.");

		if (c == null)
			throw new NullPointerException("c cannot be null.");
		
		controller = c;
		if (data instanceof AbstractUser)
			buildUserPopup((AbstractUser) data);
		
		addPopupMenuListener(new PopupMenuListener(){
			public void popupMenuCanceled(PopupMenuEvent e) {}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				x = getLocationOnScreen().x;
				y = getLocationOnScreen().y;
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
		});
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
		hide.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				controller.setUserVisible(user, !(e.getStateChange() == ItemEvent.SELECTED));
			}
		});
		setColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.showCustomColorPopup(user, x, y);
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
