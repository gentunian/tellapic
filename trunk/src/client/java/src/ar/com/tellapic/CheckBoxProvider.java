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

import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jdesktop.swingx.renderer.BooleanValue;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.JRendererCheckBox;

import ar.com.tellapic.utils.Utils;

/**
 * 
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class CheckBoxProvider extends ComponentProvider<AbstractButton> {
	private static final long serialVersionUID = 1L;
	private Icon selectedIcon;
	private Icon deselectedIcon;
	private String toolTipText;

	CheckBoxProvider(String selIconPath, String deselIconPath, String ttt) {
		selectedIcon   = new ImageIcon(Utils.createIconImage(12, 12, selIconPath));
		deselectedIcon = new ImageIcon(Utils.createIconImage(12, 12, deselIconPath));
		toolTipText   = ttt;
	}


	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#configureState(org.jdesktop.swingx.renderer.CellContext)
	 */
	@Override
	protected void configureState(CellContext context) {
		rendererComponent.setHorizontalAlignment((int) AbstractButton.CENTER_ALIGNMENT);
		rendererComponent.setPreferredSize(new Dimension(12,12));
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#createRendererComponent()
	 */
	@Override
	protected AbstractButton createRendererComponent() {
		return new JRendererCheckBox();
	}

	/*
	 * 
	 */
	protected boolean getValueAsBoolean(CellContext context) {
		if (formatter instanceof BooleanValue) {
			return ((BooleanValue) formatter).getBoolean(context.getValue());
		}
		return Boolean.TRUE.equals(context.getValue());
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.renderer.ComponentProvider#format(org.jdesktop.swingx.renderer.CellContext)
	 */
	@Override
	protected void format(CellContext context) {
		rendererComponent.setIcon(deselectedIcon);
		rendererComponent.setSelectedIcon(selectedIcon);
		rendererComponent.setSelected(getValueAsBoolean(context));
		rendererComponent.setPreferredSize(new Dimension(12,12));
		rendererComponent.setToolTipText(toolTipText);
		//			rendererComponent.setText(getValueAsString(context));
	}
}
