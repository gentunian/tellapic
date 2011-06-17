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
package ar.com.tellapic.chat;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ChatWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel           contentPane;
	private ChatUsersView    usersView;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatWindow frame = new ChatWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ChatWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JCheckBoxMenuItem chckbxmntmShowUsers = new JCheckBoxMenuItem("Show Users");
		chckbxmntmShowUsers.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (usersView != null) {
					usersView.setVisible(e.getStateChange() == ItemEvent.SELECTED);
					JSplitPane pane = (JSplitPane) contentPane.getComponent(0);
					pane.setDividerLocation(0.86);
				}
				pack();
			}
		});
		chckbxmntmShowUsers.setSelectedIcon(new ImageIcon(ChatWindow.class.getResource("/icons/system/users.png")));
		chckbxmntmShowUsers.setSelected(true);
		chckbxmntmShowUsers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
		menuBar.add(chckbxmntmShowUsers);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(.7);
		splitPane.setContinuousLayout(true);
		contentPane.add(splitPane, BorderLayout.CENTER);
	}
	
	public void addMessagesView(ChatMessagesView view) {
		JSplitPane pane = (JSplitPane) contentPane.getComponent(0);
		pane.setLeftComponent(view);
//		messagesView = view;
	}
	
	public void addUsersView(ChatUsersView view) {
		JSplitPane pane = (JSplitPane) contentPane.getComponent(0);
		pane.setRightComponent(view);
		usersView = view;
	}
}
