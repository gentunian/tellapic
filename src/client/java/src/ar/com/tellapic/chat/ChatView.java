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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Keymap;

import ar.com.tellapic.UserManager;
import ar.com.tellapic.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ChatView extends JPanel implements Observer {

	private static final long serialVersionUID = -4356214471573728672L;

	private int                      currentTabIndex;
	private String                   currentTabTitle;
	private JTabbedPane              tabbedPane;
	private JTextField               inputText;
	private IChatController          controller;
	private ArrayList<JTextArea>     chatTabs;
	
	
	public ChatView(IChatController c) {
		setName(Utils.msg.getString("chatview"));
		ChatClientModel.getInstance().addObserver(this);
		
		controller = c;
		chatTabs   = new ArrayList<JTextArea>();
		inputText  = new JTextField();
		tabbedPane = new JTabbedPane();
		inputText.setPreferredSize(new Dimension(100,20));

		Keymap map = inputText.getKeymap();
		map.addActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK),
				new AbstractAction("Change Tab"){
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						tabbedPane.setSelectedIndex((currentTabIndex+1)%tabbedPane.getTabCount());
					}
				});
		
		map.addActionForKeyStroke(
				KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK),
				new AbstractAction("Change Tab"){
					private static final long serialVersionUID = 1L;
					@Override
					public void actionPerformed(ActionEvent e) {
						if (currentTabIndex == 0)
							tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
						else
							tabbedPane.setSelectedIndex((currentTabIndex-1)%tabbedPane.getTabCount());
					}
				});
		
		GroupLayout layout = new GroupLayout(ChatView.this);
		setLayout(layout);
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(tabbedPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
								.addComponent(inputText, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
								.addContainerGap())
		);
		
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(inputText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap())
		);

		inputText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO: The input should be recognized by some chat protocol.
				// then, it should build a Message object to be sent to the model
				// and through the net.

				// If we are in the general tab, then the message will be 
				// broadcasted to all connected clients. However, if the message is
				// like: "/msg <to> <text>" it won't be deliver to all connected clients.
				// So, as a general rule, the text is first analyzed by Message.build().
				// If "/msg" is found, the currentTabTitle will be ignored. 
				// If "/msg" is not found and the current tab is the general tab, 
				// then 'null' will be passed as the 'to' argument for Message.build(). Finally,
				// if "/msg" is not found and we aren't in the general tab, the currentTabTitle
				// will be set as an argument indicating the recipient of the message.
				boolean pvt = (currentTabIndex != 0);

				// The input text
				String text = inputText.getText();

				if (controller != null) {
					Map.Entry<String, Message> mapEntry = Message.build(UserManager.getInstance().getLocalUser().getName(), pvt? currentTabTitle : null, text);
					Message message = mapEntry.getValue();
					if (message == null) {
						updateTab(currentTabIndex, mapEntry.getKey());
					} else {
						controller.handleInput(message, true);
					}
				}
				// clears the input field
				inputText.setText("");
			}
		});
		createNewTab(Utils.msg.getString("main"));
		tabbedPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("selected tab: "+((JTabbedPane)e.getSource()).getSelectedIndex());
				currentTabIndex = ((JTabbedPane)e.getSource()).getSelectedIndex();
				currentTabTitle = ((JTabbedPane)e.getSource()).getTitleAt(currentTabIndex);
				inputText.requestFocus();
				((ChatViewTabComponent)tabbedPane.getTabComponentAt(currentTabIndex)).setTitleColor(Color.black);
			}
		});

	}
	
	
	private int createNewTab(String title) {
		// Create and set properties for the tab text area
		final JTextArea content = new JTextArea();
		content.setEditable(false);
		content.setAutoscrolls(true);
		content.setLineWrap(true);
		content.setFocusable(true);

		// Set a scroll bar to the tab text area and set scrollbar's properties
		JScrollPane areaScrollPane = new JScrollPane(content);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));

		// Add a tab to the tabbed pane
		tabbedPane.addTab(title, areaScrollPane);

		// This creates a label with a button at the right
		JPanel tabComponent = new ChatViewTabComponent(tabbedPane);
		tabbedPane.setTabComponentAt(tabbedPane.indexOfTab(title), tabComponent); 

		//TODO: check this out. We need to have something to hold the chats (query the model?)
		chatTabs.add(content);
		
		return tabbedPane.indexOfTab(title); 
	}
	

	private void updateTab(int tabIndex, String message) {
		JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(tabIndex);
		JViewport   viewPort   = scrollPane.getViewport();
		JTextArea   textArea   = (JTextArea) viewPort.getView();
		textArea.append(message);
		if (tabIndex != currentTabIndex)
			((ChatViewTabComponent)tabbedPane.getTabComponentAt(tabIndex)).setTitleColor(Color.red);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object data) {
		Utils.logMessage("Updating ChatView with: "+data);
		if (data != null) {
			if (data instanceof Message) {
				Message message = (Message) data;
				String   sender = message.getSender();
				String   text   = "<"+sender+"> "+message.getText()+"\n";
				
				if (message.isPrivate()){
					String tabTitle = null;
					
					// Ensure that we aren't being loopbacked with our own messages.
					if (sender.equals(UserManager.getInstance().getLocalUser().getName()))
						tabTitle = message.getReceiver();
					else
						tabTitle = sender;
					// Check if the tab already exists
					int tabIndex = tabbedPane.indexOfTab(tabTitle);
					
					if ( tabIndex == -1) {
						// Create a new tab
						int newTab = createNewTab(tabTitle);
						// Set the message
						updateTab(newTab, text);
					}
					else
						// Set the message
						updateTab(tabIndex, text);
				} else {
					int mainTabIndex = tabbedPane.indexOfTab(Utils.msg.getString("main"));
					updateTab(mainTabIndex, text);
				}
			}
		}
	}
}
