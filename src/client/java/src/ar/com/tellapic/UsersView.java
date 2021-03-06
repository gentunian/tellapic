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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.StringValue;

import ar.com.tellapic.adm.AbstractUser;
import ar.com.tellapic.chat.ChatViewController;
import ar.com.tellapic.graphics.AbstractDrawing;
import ar.com.tellapic.graphics.ControlToolSelector;
import ar.com.tellapic.graphics.DrawingAreaModel;
import ar.com.tellapic.graphics.DrawingPopupMenu;
import ar.com.tellapic.graphics.PaintPropertyColor;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UsersView extends JPanel {

	private static final long serialVersionUID = 1L;
	private ChatViewController     chatViewController;
	
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private static class Holder {
		private static final UsersView INSTANCE = new UsersView();
	}

	
	/**
	 * 
	 */
	private UsersView() {
		super(new GridLayout(1,0));
		setName(Utils.msg.getString("usersview"));
		JScrollPane treeView;
		final JXTreeTable tree = new JXTreeTable(TellapicUserManager.getInstance());;
		addKeyShortcuts(tree);
		tree.addMouseListener(new MouseAdapter(){
			/* This is a workarround to manage mouse events in the Tree View */
			@Override
			public void mouseClicked(MouseEvent e) {
				int colSel = tree.getColumnModel().getColumnIndexAtX(e.getX());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path == null)
					return;
				
				Object o = path.getLastPathComponent();
				if (chatViewController != null) {
					if (o instanceof TellapicAbstractUser) {
						TellapicAbstractUser user = (TellapicAbstractUser) o;
						switch(colSel) {
						case 1:
							boolean oldValue = user.isVisible();
							user.setVisible(!oldValue);
							break;
						case 2:
							showCustomColorPopup(user, e.getXOnScreen(), e.getYOnScreen());
							break;
						case 3:
							chatViewController.initiateChat(user);
							break;
						}
					} else if (o instanceof AbstractDrawing) {
						AbstractDrawing drawing = (AbstractDrawing) o;
						switch(colSel) {
						case 0:
							TellapicLocalUser luser = (TellapicLocalUser) TellapicUserManager.getInstance().getUser(SessionUtils.getUsername());
							luser.getToolboxController().selectToolByName("ControlToolSelector");
							ControlToolSelector t = (ControlToolSelector) luser.getToolBoxModel().getLastUsedTool();
							t.selectDrawing(drawing);
//							drawing.setSelected(true);
							break;
						case 1:
							drawing.setVisible(!drawing.isVisible());
							break;
						case 2:
							break;
						case 3:
							break;
						}
					}
				}
			}
			
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						tree.getTreeSelectionModel().addSelectionPath(path);
						Object data = path.getLastPathComponent();
						JPopupMenu popup = null;
						if (data instanceof AbstractDrawing) {
							popup = new DrawingPopupMenu(null, (AbstractDrawing) data);
						} else if (data instanceof TellapicAbstractUser) {
							popup = new UserPopupMenu((TellapicAbstractUser) data, chatViewController);
						}
						popup.show(UsersView.this, e.getX(), e.getY());
					}
				}
			}
			
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						tree.getTreeSelectionModel().addSelectionPath(path);
						Object data = path.getLastPathComponent();
						JPopupMenu popup = null;
						if (data instanceof AbstractDrawing) {
							popup = new DrawingPopupMenu(null, (AbstractDrawing) data);
						} else if (data instanceof TellapicAbstractUser) {
							popup = new UserPopupMenu((TellapicAbstractUser) data, chatViewController);
						}
						popup.show(UsersView.this, e.getX(), e.getY());
					}
				}
			}
		});
		tree.setRootVisible(true);
		tree.setBackground(Color.white);
		tree.setTableHeader(null);
		tree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tree.setSortable(false);
		tree.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		ComponentProvider<AbstractButton> visibilityProvider = new CheckBoxProvider("/icons/system/eye.png", "/icons/system/eye-close.png", "Enable/disable item's visibility.");
		ComponentProvider<JLabel> userProvider = new CustomLabelProvider(new UserValue(), (int)JLabel.CENTER_ALIGNMENT);
		tree.setDefaultRenderer(MyEyeCheckBox.class, new DefaultTableRenderer(visibilityProvider));
		tree.setDefaultRenderer(String.class, new DefaultTableRenderer(userProvider));
		tree.setTreeCellRenderer(new DefaultTreeRenderer(userProvider));
		tree.setAutoCreateColumnsFromModel(false);
		
		tree.getColumn(0).setMaxWidth(222);
		tree.getColumn(1).setMaxWidth(22);
		tree.getColumn(2).setMaxWidth(22);
		tree.getColumn(3).setMaxWidth(22);
		
		tree.getColumn(0).setMinWidth(190);
		tree.getColumn(1).setMinWidth(12);
		tree.getColumn(2).setMinWidth(12);
		tree.getColumn(3).setMinWidth(12);
		
		tree.getColumn(0).setPreferredWidth(190);
		tree.getColumn(1).setPreferredWidth(16);
		tree.getColumn(2).setPreferredWidth(16);
		tree.getColumn(3).setPreferredWidth(16);

//		tree.packTable(2);
		tree.setHorizontalScrollEnabled(true);
		treeView = new JScrollPane(tree);
		treeView.setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.white);
		add(treeView);
	}

	
	/**
	 * @param tree 
	 * 
	 */
	private void addKeyShortcuts(final JXTreeTable tree) {
		InputMap  inputMap  = tree.getInputMap();
		ActionMap actionMap = tree.getActionMap();
		inputMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteDrawing");
		inputMap.put(KeyStroke.getKeyStroke("H"), "hide");
		inputMap.put(KeyStroke.getKeyStroke("ENTER"), "chat");
		inputMap.put(KeyStroke.getKeyStroke("C"), "color");
		
		actionMap.put("hide", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tree.getSelectedRow();
				Object value = tree.getValueAt(row, tree.getHierarchicalColumn());
				if (value instanceof AbstractDrawing) {
					AbstractDrawing drawing = (AbstractDrawing) value;
					drawing.setVisible(!drawing.isVisible());
				} else if (value instanceof AbstractUser) {
					AbstractUser user = (AbstractUser) value;
					user.setVisible(!user.isVisible());
				}
			}
		});
		
		actionMap.put("chat", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tree.getSelectedRow();
				Object value = tree.getValueAt(row, tree.getHierarchicalColumn());
				if (value instanceof AbstractUser)
					chatViewController.initiateChat((AbstractUser) value);
			}
			
		});
		
		actionMap.put("color", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tree.getSelectedRow();
				Object value = tree.getValueAt(row, tree.getHierarchicalColumn());
				if (value instanceof AbstractUser) {
					showCustomColorPopup((TellapicAbstractUser) value, getLocationOnScreen().x, getLocationOnScreen().y);
				}
			}
		});
		
		actionMap.put("deleteDrawing", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tree.getSelectedRow();
				Object value = tree.getValueAt(row, 0);
				if (value instanceof AbstractDrawing)
					DrawingAreaModel.getInstance().removeDrawing((AbstractDrawing)value);
			}
		});
	}


	/**
	 * 
	 * @param c
	 */
	public void setChatViewController(ChatViewController c) {
		chatViewController = c;
	}
	
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class CustomLabelProvider extends LabelProvider {
		private static final long serialVersionUID = 1L;
		
		/**
		 * @param userValue
		 * @param centerAlignment
		 */
		public CustomLabelProvider(UserValue userValue, int centerAlignment) {
			super(userValue, centerAlignment);
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.renderer.ComponentProvider#format(org.jdesktop.swingx.renderer.CellContext)
		 */
		@Override
		protected void format(CellContext context) {
			super.format(context);
			Object value = context.getValue();
			if (value instanceof TellapicAbstractUser) {
				TellapicAbstractUser user = (TellapicAbstractUser) value;
				PaintPropertyColor customColor = null;
				Font usedFont = null;
				try {
					customColor = (PaintPropertyColor) user.getCustomProperty(TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
					if (customColor != null) {
						rendererComponent.setForeground(customColor.getColor());
						usedFont = rendererComponent.getFont().deriveFont(Font.BOLD);
					} else {
						usedFont = rendererComponent.getFont().deriveFont(Font.PLAIN); 
					}
				} catch (NoSuchPropertyTypeException e) {
					usedFont = rendererComponent.getFont().deriveFont(Font.PLAIN); 
				}
				rendererComponent.setFont(usedFont);
				String text = rendererComponent.getText();
				if (text != null) {
					int newWidth  = rendererComponent.getFontMetrics(usedFont).stringWidth(text);
					int iconWidth = rendererComponent.getIcon().getIconWidth();
					rendererComponent.setPreferredSize(new Dimension(newWidth + iconWidth + 10, 22));
				}
			}
		}
	}
	
	/**
	 * 
	 * @param user
	 */
	private void showCustomColorPopup(TellapicAbstractUser user, int x, int y) {
		CustomPropertiesDialog popup = null;
		PaintPropertyColor c = null;
		try {
			c = (PaintPropertyColor) user.getCustomProperty(TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
		} catch (NoSuchPropertyTypeException e) {
			e.printStackTrace();
		}
		popup = new CustomPropertiesDialog(null, true, user, c);
		popup.setLocation(x - popup.getSize().width, y);
		popup.setVisible(true);
		if (popup.getReturnStatus() != CustomPropertiesDialog.RET_CANCEL) {
			try {
				Color color = popup.getCustomColor();
				if (color != null)
					user.setCustomProperty(new PaintPropertyColor(color), TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
				else
					user.removeCustomProperty(TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
			} catch (NoSuchPropertyTypeException e1) {
				e1.printStackTrace();
			} catch (WrongPropertyTypeException e1) {
				e1.printStackTrace();
			}
		} else
			try {
				user.removeCustomProperty(TellapicAbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
			} catch (NoSuchPropertyTypeException e) {
				e.printStackTrace();
			}
	}
	
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class UserValue implements IconValue, StringValue {
		private static final long serialVersionUID = 1L;
		private Icon userIcon = (Icon)(new ImageIcon(Utils.createIconImage(12, 12, "/icons/system/user.png")));
		private Icon usersIcon = (Icon)(new ImageIcon(Utils.createIconImage(12, 12, "/icons/system/users.png")));
		private Icon drawingIcon = (Icon)(new ImageIcon(Utils.createIconImage(12, 12, "/icons/tools/drawings.png")));
		private Icon chatIcon = (Icon)(new ImageIcon(Utils.createIconImage(12, 12, "/icons/system/balloons-white.png")));
		private Icon propertiesIcon = (Icon)(new ImageIcon(Utils.createIconImage(12, 12, "/icons/tools/color1.png")));
		
		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.renderer.StringValue#getString(java.lang.Object)
		 */
		@Override
		public String getString(Object value) {
//			System.out.println("StringValue: "+value);
			String str = null;
			
			if (value instanceof ArrayList<?>)
				str = Utils.msg.getString("userlist");
			else if (value instanceof AbstractUser)
				str = value.toString();
			else if (value instanceof AbstractDrawing)
				str = value.toString();
			
			return str;
		}

		/* (non-Javadoc)
		 * @see org.jdesktop.swingx.renderer.IconValue#getIcon(java.lang.Object)
		 */
		@Override
		public Icon getIcon(Object value) {
//			System.out.println("IconValue: "+value);
			Icon icon = null;
			
			if (value instanceof ArrayList<?>)
				icon = usersIcon;
			else if (value instanceof AbstractUser)
				icon = userIcon;
			else if (value instanceof AbstractDrawing)
				icon = drawingIcon;
			else if (value instanceof String) {
				if (value.toString().startsWith("[chat]"))
					icon = chatIcon;
				else  if (value.toString().startsWith("[properties]"))
					icon = propertiesIcon;
			}
			return icon;
		}
		
	}
	
	/**
	 * 
	 * @return
	 */
	public static UsersView getInstance() {
		return Holder.INSTANCE;
	}
	
}