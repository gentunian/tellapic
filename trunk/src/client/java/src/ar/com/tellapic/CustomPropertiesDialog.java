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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import ar.com.tellapic.graphics.ColorSelector;
import ar.com.tellapic.graphics.PaintPropertyColor;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class CustomPropertiesDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	public static final int   RET_CANCEL = 0;
	public static final int   RET_OK = 1;
	
	private Color             defaultColor = Color.white;
	private boolean           customColor = false;
	private AbstractUser      user;
	private JCheckBox         enablePreview;
	

	/**
	 * 
	 * @param parent
	 * @param modal
	 * @param user
	 * @param color
	 */
	public CustomPropertiesDialog(Frame parent, boolean modal, AbstractUser user, PaintPropertyColor color) {
		super(parent, modal);
		this.user = user;
		if (color != null) {
			defaultColor = color.getColor();
			customColor = true;
		}
		setIconImage(Utils.createIconImage(112, 75, "/icons/system/logo_small.png"));
		initComponents();
		
	}

	/** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
	public int getReturnStatus() {
		return returnStatus;
	}

	
	/**
	 * 
	 * @return
	 */
	public Color getCustomColor() {
		Color color = null;
		if (colorSlider.isEnabled())
			color = colorLabel.getBackground();
		return color;
	}
	
	
	private void initComponents() {
		final JLabel tip = new JLabel("<html><i>"+Utils.msg.getString("colorlabeltooltip")+"</i></html>");
		tip.setIcon(new ImageIcon(Utils.createIconImage(12, 12, "/icons/system/information-balloon.png")));
		
		okButton      = new JButton();
		cancelButton  = new JButton();
		enableColor   = new JCheckBox();
		enablePreview = new JCheckBox();
		enableOpacity = new JCheckBox(); 
		colorSlider   = new JSlider(0, 16777216);
		opacitySlider = new JSlider(0, 100);
		colorLabel    = new JLabel();
		
		enablePreview.setToolTipText(Utils.msg.getString("enablepreviewtooltip"));
		enableColor.setToolTipText(Utils.msg.getString("enablecolortooltip"));
		enableOpacity.setToolTipText(Utils.msg.getString("enableopacitytooltip"));
		colorLabel.setToolTipText(Utils.msg.getString("colorlabeltooltip"));
		colorSlider.setToolTipText(Utils.msg.getString("colorslidertooltip"));
		
		enablePreview.setSelected(false);
		enablePreview.setText(Utils.msg.getString("enablepreview"));
		
		colorLabel.setOpaque(true);
		colorLabel.setEnabled(customColor);
		colorLabel.setBackground(defaultColor);
		colorLabel.addPropertyChangeListener("background", new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (enablePreview.isSelected()) {
					try {
						user.setCustomProperty(new PaintPropertyColor(colorLabel.getBackground()), AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
					} catch (NoSuchPropertyTypeException e) {
						e.printStackTrace();
					} catch (WrongPropertyTypeException e) {
						e.printStackTrace();
					}
				}
			}
		});
		colorLabel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if (enableColor.isSelected()) {
					final ColorSelector color = new ColorSelector(
							CustomPropertiesDialog.this,
							colorLabel.getBackground(),
							colorLabel.getLocationOnScreen().x + colorLabel.getWidth(),
							colorLabel.getLocationOnScreen().y + colorLabel.getHeight(),
							false
					);
					color.addComponentListener(new ComponentListener(){

						@Override
						public void componentHidden(ComponentEvent e) {
							colorLabel.setBackground(color.getSelectedColor());
						}

						@Override
						public void componentMoved(ComponentEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void componentResized(ComponentEvent e) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void componentShown(ComponentEvent e) {
							// TODO Auto-generated method stub
							
						}});
					
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				if (enableColor.isSelected()) {
					colorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				} else {
					colorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		
		colorSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				int color = (int)source.getValue();
				colorLabel.setBackground(
						new Color(
								color,
								false
						)
				);
			}
		});
		
		opacitySlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				int opacity = (int)source.getValue();
				Color color = colorLabel.getBackground();
				colorLabel.setBackground(
						new Color(
								color.getRed(),
								color.getGreen(),
								color.getBlue(),
								opacity
						)
				);
			}
		});
		enableColor.setSelected(customColor);
		colorSlider.setEnabled(customColor);
		opacitySlider.setEnabled(false);
		enableOpacity.setEnabled(false);
		tip.setVisible(customColor);
		colorSlider.setUI(new BasicSliderUI(colorSlider));
		opacitySlider.setUI(new BasicSliderUI(opacitySlider));
		enableColor.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean value = e.getStateChange() == ItemEvent.SELECTED;
				colorLabel.setEnabled(value);
				colorSlider.setEnabled(value);
				tip.setVisible(value);
				CustomPropertiesDialog.this.pack();
			}
		});
		enableOpacity.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				opacitySlider.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		
		setTitle("Set user custom properties...");
		setModal(true);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				closeDialog(evt);
			}
		});

		okButton.setText("OK");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});

		cancelButton.setText("Reset");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		enableColor.setText("Color");
		enableColor.setHorizontalTextPosition(SwingConstants.LEFT);
		enableColor.setVerticalAlignment(SwingConstants.BOTTOM);

		enableOpacity.setText("Opacity");
		enableOpacity.setHorizontalTextPosition(SwingConstants.LEFT);
		enableOpacity.setVerticalAlignment(SwingConstants.BOTTOM);

		colorLabel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, Color.white, Color.white, Color.darkGray, Color.lightGray));

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(tip, GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
										.addComponent(okButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(cancelButton))
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
														.addComponent(enableColor)
														.addComponent(enableOpacity))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
																.addComponent(opacitySlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
																.addComponent(colorSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																		.addComponent(enablePreview, javax.swing.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
																		.addComponent(colorLabel, GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE))))
																		.addContainerGap())
																
		);

		layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, okButton});

		layout.setVerticalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
								.addComponent(colorLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
												.addComponent(enableColor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(colorSlider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
														.addComponent(enableOpacity, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(opacitySlider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(enablePreview)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addComponent(tip)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 50, Short.MAX_VALUE)
														.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																.addComponent(cancelButton)
																.addComponent(okButton))
																.addContainerGap())
		);

		pack();
	}

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {                                         
		doClose(RET_OK);
	}                                        

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
		doClose(RET_CANCEL);
	}                                            

	/** Closes the dialog */
	private void closeDialog(java.awt.event.WindowEvent evt) {                             
		doClose(RET_CANCEL);
	}                            

	private void doClose(int retStatus) {
		returnStatus = retStatus;
		setVisible(false);
		dispose();
	}



	// Variables declaration - do not modify
	private JButton cancelButton;
	private JLabel colorLabel;
	private JSlider colorSlider;
	private JCheckBox enableColor;
	private JCheckBox enableOpacity;
	private JButton okButton;
	private JSlider opacitySlider;
	// End of variables declaration

	private int returnStatus = RET_CANCEL;
}