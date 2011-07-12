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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;

import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class DrawingCellComponentProvider extends ComponentProvider<JComponent> {
	
	private static final long serialVersionUID = 1L;
	private JCheckBox booleanComp;
	private JLabel    stringComp;
//	private ColorPanelCellEditor c;
	
	public DrawingCellComponentProvider() {
//		c = new ColorPanelCellEditor(null, null);
		booleanComp   = new JCheckBox();
		stringComp    = new JLabel();
		stringComp.setHorizontalAlignment(SwingConstants.LEFT);
		booleanComp.setOpaque(true);
		stringComp.setOpaque(true);
		defaultVisuals = createDefaultVisuals();
	}
	
	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#configureState(org.jdesktop.swingx.renderer.CellContext)
	 */
	@Override
	protected void configureState(CellContext context) {
		Object value = context.getValue();

		if (value instanceof Boolean) {
			booleanComp.setText(value.toString());
			booleanComp.setSelected((Boolean)value);
			booleanComp.setEnabled(context.isEditable());
			booleanComp.setRolloverEnabled(true);
			rendererComponent =  booleanComp;
		} else {
			stringComp.setText(value.toString());
			
			if (value instanceof Color) {
//				PaintPropertyColor ppc = (PaintPropertyColor) value;
				Color color = (Color) value;
				stringComp.setBackground(color);
				stringComp.setToolTipText("RGB Value: ("+color.getRed()+", "+color.getGreen()+", "+color.getBlue()+")");
				if (context.isSelected())
					stringComp.setBorder(BorderFactory.createMatteBorder(2,5,2,5, ((JTable)context.getComponent()).getSelectionBackground()));
				else
					stringComp.setBorder(BorderFactory.createMatteBorder(2,5,2,5, ((JTable)context.getComponent()).getBackground()));
				stringComp.setHorizontalAlignment(SwingConstants.CENTER);
				stringComp.setIcon(null);
				stringComp.setText(Utils.colorToHexa(color));
				rendererComponent = stringComp;
			} else if (value instanceof PaintPropertyStroke.EndCapsType) {
				PaintPropertyStroke.EndCapsType ect = (PaintPropertyStroke.EndCapsType) value;
				stringComp.setIcon(new ImageIcon(Utils.createIconImage(12, 12, PaintPropertyView.END_CAPS_ICON_PATHS[ect.ordinal()])));
				stringComp.setToolTipText(value.toString());
				stringComp.setHorizontalAlignment(SwingConstants.LEADING);
				rendererComponent = stringComp;
			} else if (value instanceof PaintPropertyStroke.LineJoinsType) {
				PaintPropertyStroke.LineJoinsType ljt = (PaintPropertyStroke.LineJoinsType) value;
				stringComp.setIcon(new ImageIcon(Utils.createIconImage(12, 12, PaintPropertyView.LINE_JOINS_ICON_PATHS[ljt.ordinal()])));
				stringComp.setToolTipText(value.toString());
				stringComp.setHorizontalAlignment(SwingConstants.LEADING);
				rendererComponent = stringComp;
				
//			} else if (value instanceof PaintPropertyFill) {
//				PaintPropertyFill ppf = (PaintPropertyFill) value;
//				stringComp.setBackground((Color) ppf.getFillPaint());
//				stringComp.setToolTipText("RGB Value: ("+((Color)ppf.getFillPaint()).getRed()+", "+((Color)ppf.getFillPaint()).getGreen()+", "+((Color)ppf.getFillPaint()).getBlue()+")");
//				if (context.isSelected())
//					stringComp.setBorder(BorderFactory.createMatteBorder(2,5,2,5, ((JTable)context.getComponent()).getSelectionBackground()));
//				else
//					stringComp.setBorder(BorderFactory.createMatteBorder(2,5,2,5, ((JTable)context.getComponent()).getBackground()));
//				stringComp.setHorizontalAlignment(SwingConstants.CENTER);
//				stringComp.setIcon(null);
//				rendererComponent = stringComp;
			} else {
				stringComp.setToolTipText(value.toString());
				stringComp.setHorizontalAlignment(SwingConstants.LEADING);
				stringComp.setIcon(null);
				rendererComponent = stringComp;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#createRendererComponent()
	 */
	@Override
	protected JComponent createRendererComponent() {
		return  null;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#format(org.jdesktop.swingx.renderer.CellContext)
	 */
	@Override
	protected void format(CellContext context) {
		
	}
	
	/**
	 * 
	 */
	public JComponent getRendererComponent(CellContext context) {
		if (context != null) {
			Object value = context.getValue();
			
			if (value instanceof Boolean) {
				rendererComponent =  booleanComp;
			} else {
				rendererComponent = stringComp;
			}
			configureVisuals(context);
			configureContent(context);
		}
//		rendererComponent.setEnabled(context.isEditable());
		
		return rendererComponent;
	}
}
