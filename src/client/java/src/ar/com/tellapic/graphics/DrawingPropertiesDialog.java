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
import java.util.Observable;
import java.util.Observer;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.GroupLayout.Alignment;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingPropertiesDialog extends JPanel implements Observer {
	private static final long serialVersionUID = 1L;
	private JXTable                table;
	private DrawingTableCellEditor cellEditor;
	private DefaultTableRenderer   tableRenderer;
	
	/**
	 * Create the dialog.
	 */
	public DrawingPropertiesDialog(/*Frame parent, boolean modal, */) {
//		drawing = d;
		setBounds(100, 100, 454, 585);
		
		JScrollPane scrollPane = new JScrollPane();
		GroupLayout gl_contentPanel = new GroupLayout(this);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
					.addContainerGap())
		);
		tableRenderer = new DefaultTableRenderer(new DrawingCellComponentProvider());
		cellEditor    = new DrawingTableCellEditor();
		table         = new JXTable();
		
//		table.setHighlighters(HighlighterFactory.createSimpleStriping());
		table.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, Color.BLACK, Color.WHITE));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cellEditor.addCellEditorListener(table);
		
		scrollPane.setViewportView(table);
		setLayout(gl_contentPanel);
		setName("Properties");
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		AbstractDrawing  drawing = (AbstractDrawing) arg;
		
		table.setEnabled(drawing != null);
		
		if (drawing != null) {
			// If selection was not empty, we should set the drawing as the table model.
			// We don't care if the model was the same. Just set the model.
			table.setModel(drawing);
			table.getColumnModel().getColumn(1).setCellEditor(cellEditor);
			table.getColumnModel().getColumn(1).setCellRenderer(tableRenderer);
		}
	}
}
