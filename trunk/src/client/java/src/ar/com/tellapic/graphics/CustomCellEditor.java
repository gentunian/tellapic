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
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import ar.com.tellapic.utils.Utils;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class CustomCellEditor implements TableCellEditor {

	private Component          editor;
	private CellEditorListener listener;
	
	/**
	 * @param panel
	 */
	public CustomCellEditor(final ActionListener buttonListener, final ActionListener textfieldListener, TableCellPanel panel) {
		ActionListener bl = null;
		ActionListener tl = null;
		if (buttonListener != null) {
			bl = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						buttonListener.actionPerformed(e);
						stopCellEditing();
					} catch(Exception ex) {
						Utils.logMessage("Edition incomplete");
					}
				}
			};
		}

		if (textfieldListener != null) {
			tl = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						textfieldListener.actionPerformed(e);
						stopCellEditing();
					} catch(Exception ex) {
						Utils.logMessage("Edition incomplete");
					}
				}
			};

		}
		panel.setTextFieldListener(tl);
		panel.setButtonListener(bl);
		editor = panel;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		((TableCellPanel)editor).setValue(value);
		return editor;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	@Override
	public void addCellEditorListener(CellEditorListener l) {
		listener = l;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#cancelCellEditing()
	 */
	@Override
	public void cancelCellEditing() {
		listener.editingCanceled(new ChangeEvent(this));
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return ((TableCellPanel)editor).getValue();
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
	 */
	@Override
	public boolean isCellEditable(EventObject anEvent) {
		if (anEvent instanceof MouseEvent) { 
			return ((MouseEvent)anEvent).getClickCount() >= 2;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
	 */
	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
	 */
	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#stopCellEditing()
	 */
	@Override
	public boolean stopCellEditing() {
		listener.editingStopped(new ChangeEvent(this));
		return true;
	}
}
