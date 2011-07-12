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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;

import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class MyEyeCheckBox extends JCheckBox {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyEyeCheckBox(String ttt) {
		setSelectedIcon(new ImageIcon(Utils.createIconImage(12, 12, "/icons/system/eye.png")));
		setPressedIcon(new ImageIcon(Utils.createIconImage(12, 12, "/icons/system/eye.png")));
		setIcon(new ImageIcon(Utils.createIconImage(12, 12, "/icons/system/eye-close.png")));
		setRolloverEnabled(false);
		setSelected(true);
		setPreferredSize(new Dimension(12,12));
		setMinimumSize(new Dimension(12,12));
		setMaximumSize(new Dimension(12,12));
		setBorder(new EmptyBorder(5,5,5,5));
		setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
		setAlignmentY(JCheckBox.CENTER_ALIGNMENT);
		setOpaque(true);
//		setToolTipText("Enable/Disable item visibility");
		setToolTipText(ttt);
		setBackground(Color.white);
	}
}
