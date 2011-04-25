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
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Keymap;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import ar.com.tellapic.UserManager;
import ar.com.tellapic.utils.Utils;

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
	private ArrayList<JTextPane>     chatTabs;
	
	
	public ChatView(IChatController c) {
		setName(Utils.msg.getString("chatview"));
		ChatClientModel.getInstance().addObserver(this);
		
		controller = c;
		chatTabs   = new ArrayList<JTextPane>();
		inputText  = new JTextField();
		tabbedPane = new JTabbedPane();
		//inputText.setPreferredSize(new Dimension(100,20));

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
//						.addContainerGap()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(tabbedPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(inputText, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//								.addContainerGap()
				)
		);
		
		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(inputText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap()
				)
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
				if (text.length() == 0)
					return;
				
				if (controller != null) {
					Map.Entry<String, Message> mapEntry = Message.build(UserManager.getInstance().getLocalUser().getName(), pvt? currentTabTitle : null, text);
					Message message = mapEntry.getValue();
					if (message == null) {
						updateTab(currentTabIndex, new String[]{mapEntry.getKey(), ""});
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
				if (((JTabbedPane)e.getSource()).getSelectedIndex() == -1)
					return;
				System.out.println("selected tab: "+((JTabbedPane)e.getSource()).getSelectedIndex());
				currentTabIndex = ((JTabbedPane)e.getSource()).getSelectedIndex();
				currentTabTitle = ((JTabbedPane)e.getSource()).getTitleAt(currentTabIndex);
				inputText.requestFocus();
				((ChatViewTabComponent)tabbedPane.getTabComponentAt(currentTabIndex)).setTitleColor(Color.black);
			}
		});

	}
	
	
	private int createNewTab(String title) {
		int exist = tabbedPane.indexOfTab(title);
		if (exist != -1)
			return exist;
		
		// Create and set properties for the tab text area
		final JTextPane content = new JTextPane();
		content.setEditable(false);
		content.setAutoscrolls(true);
		//content.setLineWrap(true);
		content.setFocusable(true);

		// Set a scroll bar to the tab text area and set scrollbar's properties
		JScrollPane areaScrollPane = new JScrollPane(content);
		areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		areaScrollPane.setAutoscrolls(true);
		areaScrollPane.setWheelScrollingEnabled(true);
		
		// Add a tab to the tabbed pane
		tabbedPane.addTab(title, areaScrollPane);

		// This creates a label with a button at the right
		JPanel tabComponent = new ChatViewTabComponent(tabbedPane);
		tabbedPane.setTabComponentAt(tabbedPane.indexOfTab(title), tabComponent); 

		//TODO: check this out. We need to have something to hold the chats (query the model?)
		chatTabs.add(content);
		
		return tabbedPane.indexOfTab(title); 
	}
	

	private void updateTab(int tabIndex, String[] message) {
		JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(tabIndex);
		JViewport   viewPort   = scrollPane.getViewport();
		JTextPane   textArea   = (JTextPane) viewPort.getView();
//		textArea.append(message);
		
		StyledDocument doc = textArea.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		Style user = doc.addStyle("user", def);
		Style text = doc.addStyle("text", def);
		StyleConstants.setBold(user, true);
		if (message[0].equals(UserManager.getInstance().getLocalUser().getName())) {
			StyleConstants.setItalic(user, true);
			StyleConstants.setItalic(text, true);
		}
		StyleConstants.setFontFamily(def, "Droid");
		doc.addStyle("text", def);
		doc.addStyle("user", def);
		try {
			doc.insertString(doc.getLength(), message[0]+": ", user);
			doc.insertString(doc.getLength(), message[1]+"\n", text);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (tabIndex != currentTabIndex)
			((ChatViewTabComponent)tabbedPane.getTabComponentAt(tabIndex)).setTitleColor(Color.red);
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

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
				String[] str = new String[] {
						message.getSender(),
						message.getText()
				};
				
				if (message.isPrivate()){
					String tabTitle = null;
					
					// Ensure that we aren't being loopbacked with our own messages.
					if (str[0].equals(UserManager.getInstance().getLocalUser().getName()))
						tabTitle = message.getReceiver();
					else
						tabTitle = str[0];
					// Check if the tab already exists
					int tabIndex = tabbedPane.indexOfTab(tabTitle);
					
					if ( tabIndex == -1) {
						// Create a new tab
						int newTab = createNewTab(tabTitle);
						// Set the message
						updateTab(newTab, str);
					}
					else
						// Set the message
						updateTab(tabIndex, str);
				} else {
					int mainTabIndex = tabbedPane.indexOfTab(Utils.msg.getString("main"));
					updateTab(mainTabIndex, str);
				}
			}
		}
	}


	/**
	 * @param name
	 */
	public void createNewChatTab(String name) {
		int index = createNewTab(name);
		tabbedPane.setSelectedIndex(index);
	}
}
