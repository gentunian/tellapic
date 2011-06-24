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
package ar.com.tellapic.console;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ConsolePanel extends JPanel implements IConsoleView {

	private static final long serialVersionUID = 1L;
	private JTextField              thistextField;
	private IConsoleModelController consoleController;
	/**
	 * Create the panel.
	 */
	public ConsolePanel(IConsoleModelController controller) {
		consoleController = controller;

		thistextField = new JTextField();
		thistextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField textField = (JTextField) e.getSource();
				consoleController.handleInput(textField.getText());
				textField.setText("");
			}
		});
		thistextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				consoleController.handleKeyEvent(e);
			}
		});

		thistextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Hide");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ConsolePanel.this.setVisible(false);
			}
		});
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(thistextField, GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(thistextField, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
						.addComponent(btnNewButton, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
					.addContainerGap())
		);
		setLayout(groupLayout);

	}
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		thistextField.setText((String) arg);
	}
}
