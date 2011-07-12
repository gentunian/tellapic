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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import ar.com.tellapic.utils.JFontChooser;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingTableEditorFactory {
	
	public static TableCellEditor getEditorForProperty(String propertyName) {
		TableCellEditor editor = null;
		
		if (propertyName.equals(DrawingShape.PROPERTY_STROKE_COLOR) ||
				propertyName.equals(DrawingShape.PROPERTY_FILL) ||
				propertyName.equals(DrawingText.PROPERTY_COLOR)) {
			ActionListener bl = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					JButton        src      = (JButton) e.getSource();
					TableCellPanel panel    = (TableCellPanel) src.getParent();
					Color          oldColor = (Color) panel.getValue();
					ColorSelector  selector = new ColorSelector(oldColor, 0, 0,true);
					selector.setLocationRelativeTo(null);
					selector.setModal(true);
					selector.setVisible(true);
					panel.setValue(selector.getSelectedColor());
				}
			};
			ActionListener tl = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JTextField     src      = (JTextField) e.getSource();
					TableCellPanel panel    = (TableCellPanel) src.getParent();
					panel.setValue(Color.decode(((JTextField)e.getSource()).getText()));
				}
			};
			editor = new CustomCellEditor(bl, tl,  new TableCellPanel(new ImageIcon(TableCellEditor.class.getResource("/icons/tools/color1.png"))));
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_END_CAPS)) {
			JComboBox cb = new JComboBox(PaintPropertyStroke.EndCapsType.values());
			cb.setRenderer(
					new PaintPropertyView.LabelComboBoxRenderer(
							cb.getBorder(),
							PaintPropertyView.END_CAPS_ICON_PATHS,
							PaintPropertyStroke.EndCapsType.values()
					)
			);
			editor = new DefaultCellEditor(cb);
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_LINE_JOINS)) {
			JComboBox cb = new JComboBox(PaintPropertyStroke.LineJoinsType.values());
			cb.setRenderer(
					new PaintPropertyView.LabelComboBoxRenderer(
							cb.getBorder(),
							PaintPropertyView.LINE_JOINS_ICON_PATHS,
							PaintPropertyStroke.LineJoinsType.values()
					)
			);
			editor = new DefaultCellEditor(cb);
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_VERTICAL_EDGES_SELECTED)) {
			JComboBox cb = new JComboBox(
					new Object[] {
							DrawingShape.VALUE_NONE_EDGE_SELECTED,
							DrawingShape.VALUE_VERTICAL_RIGHT_EDGE_SELECTED,
							DrawingShape.VALUE_VERTICAL_LEFT_EDGE_SELECTED,
							DrawingShape.VALUE_BOTH_EDGES_SELECTED
					}
			);
			editor = new DefaultCellEditor(cb);
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_HORIZONTAL_EDGES_SELECTED)) {
			JComboBox cb = new JComboBox(
					new Object[] {
							DrawingShape.VALUE_NONE_EDGE_SELECTED,
							DrawingShape.VALUE_HORIZONTAL_BOTTOM_EDGE_SELECTED,
							DrawingShape.VALUE_HORIZONTAL_TOP_EDGE_SELECTED,
							DrawingShape.VALUE_BOTH_EDGES_SELECTED
					}
			);
			
			editor = new DefaultCellEditor(cb);
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_DASH)) {
			editor = new DefaultCellEditor(new JTextField());
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_MITER_LIMIT)) {
			editor = new DefaultCellEditor(new JTextField());
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_OPACITY)) {
			editor = new SpinnerCellEditor(0.0, 1.0, 0.1);
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_WIDTH)) {
			editor = new SpinnerCellEditor(0.0, 100.0, 1.0);
			
		} else if (propertyName.equals(DrawingText.PROPERTY_FONT)) {
			ActionListener bl = new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					JButton        src      = (JButton) e.getSource();
					TableCellPanel panel    = (TableCellPanel) src.getParent();
					JFontChooser   fc       = new JFontChooser();
					
					int result = fc.showDialog(null);
					if (result == JFontChooser.OK_OPTION) {
						PaintPropertyFont oldFont = (PaintPropertyFont) panel.getValue();
						oldFont.setFont(fc.getSelectedFont());
						panel.setValue(oldFont); 
					}
				}
			};
			editor = new CustomCellEditor(bl, null, new TableCellPanel(new ImageIcon(TableCellEditor.class.getResource("/icons/tools/text.png"))));
			
		} else if (propertyName.equals(DrawingText.PROPERTY_TEXT)) {
			editor = new DefaultCellEditor(new JTextField());
			
//		} else if (propertyName.equals(DrawingShape.PROPERTY_FILL)) {
//			ActionListener bl = new ActionListener(){
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					JButton        src      = (JButton) e.getSource();
//					TableCellPanel panel    = (TableCellPanel) src.getParent();
//					Color          oldColor = (Color) panel.getValue();
//					ColorSelector  selector = new ColorSelector(oldColor, 0, 0, true);
//					selector.setLocationRelativeTo(null);
//					selector.setModal(true);
//					selector.setVisible(true);
//					panel.setValue(selector.getSelectedColor());
//				}
//			};
//			ActionListener tl = new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					JTextField     src      = (JTextField) e.getSource();
//					TableCellPanel panel    = (TableCellPanel) src.getParent();
//					panel.setValue(Color.decode(((JTextField)e.getSource()).getText()));
//				}
//			};
//			TableCellPanel panel = new TableCellPanel(bl, tl, new ImageIcon(TableCellEditor.class.getResource("/icons/tools/color1.png")));
//			editor = new CustomCellEditor(panel);
			
		} else if (propertyName.equals(DrawingShape.PROPERTY_VISIBILITY)) {
			editor = new DefaultCellEditor(new JCheckBox());
			
		}
		return editor;
	}
	
	
}
