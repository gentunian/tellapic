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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.Icon;
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
public class TableCellPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField lblNewLabel;
	private JButton    btnNewButton;
	private Object     value;
	
	/**
	 * Create the panel.
	 */
	public TableCellPanel(Icon buttonIcon) {
		setMinimumSize(new Dimension(1, 1));
		setAlignmentY(Component.TOP_ALIGNMENT);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setBackground(Color.WHITE);
		
		btnNewButton = new JButton("");
		btnNewButton.setIcon(buttonIcon);
		btnNewButton.setMinimumSize(new Dimension(107, 1));
		
		lblNewLabel = new JTextField();
		lblNewLabel.setFont(new Font("Droid Sans", Font.PLAIN, 11));
		lblNewLabel.setMinimumSize(new Dimension(4, 1));
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 552, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
				.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
		);
		setLayout(groupLayout);
	}
	
	/**
	 * 
	 * @param t
	 */
	public void setValue(Object t) {
		value = t;
	}
	
	/**
	 * 
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param bl the bl to set
	 */
	public void setTextFieldListener(ActionListener textfieldListener ) {
		if (textfieldListener != null)
			lblNewLabel.addActionListener(textfieldListener);
		else {
			lblNewLabel.setEditable(false);
			lblNewLabel.setEnabled(false);
		}
	}

	/**
	 * @return the bl
	 */
	public void setButtonListener(ActionListener buttonListener) {
		if (buttonListener != null)
			btnNewButton.addActionListener(buttonListener);
		else
			btnNewButton.setEnabled(false);
	}
}
