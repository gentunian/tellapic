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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.renderer.BooleanValue;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.JRendererCheckBox;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.StringValue;

import ar.com.tellapic.graphics.Drawing;
import ar.com.tellapic.graphics.PaintPropertyColor;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UserView extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
//	private JTree                  usersTree;
//	private DefaultMutableTreeNode rootNode;
//	private DefaultTreeModel       treeModel;
	private JScrollPane            treeView;
	private UserOptionsController  userOptionsController;
	private JXTreeTable            tree;
	
	private static class Holder {
		private static final UserView INSTANCE = new UserView();
	}

	
	private UserView() {
		super(new GridLayout(1,0));
		setName(Utils.msg.getString("userview"));
		
		tree = new JXTreeTable(UserManager.getInstance());
		//tree.getTreeTableModel().addTreeModelListener(new TreeModelListener(){});

		addKeyShortcuts();
		
		tree.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				int colSel = tree.getColumnModel().getColumnIndexAtX(e.getX());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path == null)
					return;
				
				Object o = path.getLastPathComponent();
				if (userOptionsController != null) {
					if (o instanceof AbstractUser) {
						AbstractUser user = (AbstractUser) o;
						switch(colSel) {
						case 1:
//							userOptionsController.toggleUserVisibility(user);
							boolean oldValue = user.isVisible();
							user.setVisible(!oldValue);
							break;
						case 2:
							showCustomColorPopup(user, e.getXOnScreen(), e.getYOnScreen());
							break;
						case 3:
							userOptionsController.initiateChat(user);
							break;
						}
					} else if (o instanceof Drawing) {
						Drawing drawing = (Drawing) o;
						switch(colSel) {
						case 1:
//							userOptionsController.toggleDrawingVisibility(drawing);
							drawing.getUser().changeDrawingVisibility(drawing);
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
				// TODO Auto-generated method stub
				if (e.isPopupTrigger()) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						tree.getTreeSelectionModel().addSelectionPath(path);
						TreePopupOptions popup = new TreePopupOptions(path.getLastPathComponent(), userOptionsController);
						popup.show(UserView.this, e.getX(), e.getY());
					}
				}
			}
		});
//		tree.addTreeSelectionListener(new TreeSelectionListener(){
//			@Override
//			public void valueChanged(TreeSelectionEvent e) {
//				System.out.println("valueChanged: "+e.getPath());
//				
//			}
//		});
		//tree.setDefaultCellRenderer(MyEyeCheckBox.class, new MyEyeCheckBoxRenderer());
		tree.setRootVisible(true);
		tree.setBackground(Color.white);
		tree.setTableHeader(null);
		tree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tree.setSortable(false);
		
//		tree.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		ComponentProvider<AbstractButton> visibilityProvider = new CheckBoxProvider("/icons/system/eye.png", "/icons/system/eye-close.png");
		ComponentProvider<JLabel> userProvider = new LabelProvider(new UserValue(), (int)JLabel.CENTER_ALIGNMENT);
		tree.setDefaultRenderer(MyEyeCheckBox.class, new DefaultTableRenderer(visibilityProvider));
		tree.setDefaultRenderer(String.class, new DefaultTableRenderer(userProvider));
		tree.setTreeCellRenderer(new DefaultTreeRenderer(userProvider));
		tree.setAutoCreateColumnsFromModel(false);
		
		tree.getColumn(1).setMaxWidth(22);
		tree.getColumn(2).setMaxWidth(22);
		tree.getColumn(3).setMaxWidth(22);
		
		tree.getColumn(1).setMinWidth(12);
		tree.getColumn(2).setMinWidth(12);
		tree.getColumn(3).setMinWidth(12);
		
		tree.getColumn(0).setPreferredWidth(50);
		tree.getColumn(1).setPreferredWidth(16);
		tree.getColumn(2).setPreferredWidth(16);
		tree.getColumn(3).setPreferredWidth(16);

		tree.packTable(2);
		treeView = new JScrollPane(tree);
		treeView.setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.white);
		add(treeView);
	}

	
	/**
	 * 
	 */
	private void addKeyShortcuts() {
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
				if (value instanceof Drawing) {
					Drawing drawing = (Drawing) value;
					drawing.getUser().changeDrawingVisibility(drawing);
				} else if (value instanceof AbstractUser)
					((AbstractUser)value).changeVisibility();
			}
		});
		
		actionMap.put("chat", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tree.getSelectedRow();
				Object value = tree.getValueAt(row, tree.getHierarchicalColumn());
				if (value instanceof AbstractUser)
					userOptionsController.initiateChat((AbstractUser) value);
			}
			
		});
		
		actionMap.put("color", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tree.getSelectedRow();
				Object value = tree.getValueAt(row, tree.getHierarchicalColumn());
				if (value instanceof AbstractUser) {
					showCustomColorPopup((AbstractUser) value, getLocationOnScreen().x, getLocationOnScreen().y);
				}
			}
		});
		
		actionMap.put("deleteDrawing", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = tree.getSelectedRow();
				Object value = tree.getValueAt(row, 0);
				if (value instanceof Drawing) {
					AbstractUser user = ((Drawing)value).getUser();
					user.removeDrawing((Drawing) value);
				}
			}
		});
	}


	/**
	 * 
	 * @param c
	 */
	public void setUserOptionsController(UserOptionsController c) {
		userOptionsController = c;
	}
	
	
	
	private class CheckBoxProvider extends ComponentProvider<AbstractButton> {
		private static final long serialVersionUID = 1L;
		private Icon selectedIcon;
		private Icon deselectedIcon;

		CheckBoxProvider(String selIconPath, String deselIconPath) {
			selectedIcon   = new ImageIcon(Utils.createIconImage(12, 12, selIconPath));
			deselectedIcon = new ImageIcon(Utils.createIconImage(12, 12, deselIconPath));
		}
		
//		CheckBoxProvider(Icon selIcon, Icon deselIcon) {
//			selectedIcon = selIcon;
//			deselectedIcon = deselIcon;
//		}
		
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
			rendererComponent.setToolTipText("Enable/Disable item visibility");
//			rendererComponent.setText(getValueAsString(context));
		}
	}
	
	
	/**
	 * 
	 * @param user
	 */
	private void showCustomColorPopup(AbstractUser user, int x, int y) {
		CustomPropertiesDialog popup = null;
		PaintPropertyColor c = (PaintPropertyColor) user.getCustomColor();
		popup = new CustomPropertiesDialog(null, true, user, c);
		popup.setLocation(x - popup.getSize().width, y);
		popup.setVisible(true);
		if (popup.getReturnStatus() != CustomPropertiesDialog.RET_CANCEL) {
			try {
				Color color = popup.getCustomColor();
				if (color != null)
					user.setCustomProperty(new PaintPropertyColor(color), AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
				else
					user.removeCustomColor();
			} catch (NoSuchPropertyTypeException e1) {
				e1.printStackTrace();
			} catch (WrongPropertyTypeException e1) {
				e1.printStackTrace();
			}
		} else
			user.removeCustomColor();
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
			else if (value instanceof Drawing)
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
			else if (value instanceof Drawing)
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
	public static UserView getInstance() {
		return Holder.INSTANCE;
	}
	

	@Override
	public void update(Observable o, Object data) {
		//Utils.logMessage("Updating user view. Data received: "+data);
//		
//		if (data != null) {
//			AbstractUser user = (AbstractUser) o;
//			DefaultMutableTreeNode userNode = null;
//			
//			boolean found = false;
//			for(int i = 0; i < rootNode.getChildCount() && !found; i++) {
//				userNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
//				if (userNode.getUserObject().equals(user))
//					found = true;
//			}
//			
//			if (found) {
				//The user already exist in the users list.
//				if (user.isRemoved()) {
//					treeModel.removeNodeFromParent(userNode);
//				} else {
//					if (data instanceof Integer) {
//						DefaultMutableTreeNode   drawingNode = new DefaultMutableTreeNode(user.getDrawings().get((Integer)data));
//						treeModel.insertNodeInto(drawingNode, userNode, userNode.getChildCount());
						//usersTree.scrollPathToVisible(new TreePath(drawingNode.getPath()));
//					}
//				}
//			} else {
				//The user doesn't exist in the users list. Add it.
//				DefaultMutableTreeNode child = new DefaultMutableTreeNode(user);
//				treeModel.insertNodeInto(child, rootNode, rootNode.getChildCount());
//				usersTree.scrollPathToVisible(new TreePath(child.getPath()));
//			}
//		}
	}
	

	
//	private class ToggleVisibilityCellRenderer extends JCheckBox implements TableCellRenderer {
//		private static final long serialVersionUID = 1L;
//		private final Icon showIcon = new ImageIcon(getClass().getResource("/icons/new/eye.png"));
//		private final Icon hideIcon = new ImageIcon(getClass().getResource("/icons/new/eye-close.png"));
//		private boolean show;
//		
//		public ToggleVisibilityCellRenderer() {
//			show = true;		
////			setSelectedIcon(eyeIcon);
////			setIcon(eyeIcon);
////			setDisabledIcon(closedEyeIcon);
////			setDisabledSelectedIcon(closedEyeIcon);
////			setSelected(true);
//		}
//		
//		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//			setSelected(isSelected);
//			return this;
//		}
//	}
	
//	private class CheckBoxEditor extends AbstractCellEditor  implements TableCellEditor {
//
//		/* (non-Javadoc)
//		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
//		 */
//		@Override
//		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
//			return r;
//		} 
//		
//		public boolean isCellEditable(EventObject e) { 
//			if (e instanceof MouseEvent) {
//				Utils.logMessage("isCellEditable: mouse event");
//				MouseEvent me = (MouseEvent)e;
////				MouseEvent newME = new MouseEvent(r, me.getID(),
////						me.getWhen(), me.getModifiers(),
////						me.getX(),
////						me.getY(), me.getClickCount(),
////						me.isPopupTrigger());
//				r.dispatchEvent(me);
//			} else
//				Utils.logMessage("isCellEditable: not a mouse event");
//			return false;
//		}
//	}
}