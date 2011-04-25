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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
	
//	private Some treeTableModel;
//	private JTreeTable treeTable;
//	private final Icon eyeIcon = new ImageIcon(getClass().getResource("/icons/new/eye.png"));
//	private final Icon closedEyeIcon = new ImageIcon(getClass().getResource("/icons/new/eye-close.png"));
	//private JCheckBox showHide;
//	ToggleVisibilityCellRenderer r;
	
	private static class Holder {
		private static final UserView INSTANCE = new UserView();
	}

	
	private UserView() {
		super(new GridLayout(1,0));
		setName(Utils.msg.getString("userview"));
		
		tree = new JXTreeTable(UserManager.getInstance());
//		tree.getTreeTableModel().addTreeModelListener(UserManager.getInstance());
		tree.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {
				int colSel = tree.getColumnModel().getColumnIndexAtX(e.getX());
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if (path == null)
					return;
				
				Object o = path.getLastPathComponent();
//				System.out.println("Clicked: "+colSel+" component: "+tree.getComponentAt(e.getX(), e.getY()).getComponentAt(e.getX(), e.getY())+" count: "+tree.getSelectedColumnCount());
				
				if (userOptionsController != null) {
					if (o instanceof AbstractUser) {
						AbstractUser user = (AbstractUser) o;
						switch(colSel) {
						case 1:
							userOptionsController.toggleUserVisibility(user);
							break;
						case 2:
							userOptionsController.showCustomColorPopup(user, e.getXOnScreen(), e.getYOnScreen());
							break;
						case 3:
							userOptionsController.initiateChat(user);
							break;
						}
					} else if (o instanceof Drawing) {
						Drawing drawing = (Drawing) o;
						switch(colSel) {
						case 1:
							userOptionsController.toggleDrawingVisibility(drawing);
							break;
						case 2:
							break;
						case 3:
							break;
						}
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
			
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				if (e.isPopupTrigger()) {
					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					
					System.out.println("Clicked: "+path+" col: "+tree.getSelectedColumn());
					if (path != null) {
						tree.getTreeSelectionModel().addSelectionPath(path);
						TreePopupOptions popup = new TreePopupOptions(path.getLastPathComponent(), userOptionsController);
						
						popup.show(UserView.this, e.getX(), e.getY());
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
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
		
		/*********************************************************************/
		/* This is the renderer for the tree (the first column in the table) */
		/*********************************************************************/
//		tree.setTreeCellRenderer(new TreeCellRenderer() {
//			private MyNameLabel    description = new MyNameLabel();
//			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//				if (value instanceof AbstractUser) {
//					description.setText(value.toString());
//					if (value instanceof LocalUser) {
//						String text = description.getText();
//						description.setText("<html><b>"+text+"</b> <i>(you)</i></html>");
//					}
//					description.setUserIcon();
//				} else if (value instanceof Drawing) {
//					description.setDrawingIcon();
//					description.setText(value.toString());
//				} else {
//					description.setUsersIcon();
//					String text = Utils.msg.getString("userlist");
//					description.setText("<html>"+text+" <b>("+((ArrayList<?>)value).size()+")</b></html>");
//				}
//				return description;
//			}
//		});
		
		
		/****************************************************************/
		/* This will render the column for showing the hide/show option */
		/****************************************************************/
//		tree.setDefaultRenderer(MyEyeCheckBox.class, new TableCellRenderer(){
//			private MyEyeCheckBox visibility = new MyEyeCheckBox();
//			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//				visibility.setSelected((Boolean)value);
//
//				if (isSelected)
//					visibility.setBackground(Color.LIGHT_GRAY);
//				else
//					visibility.setBackground(Color.white);
//				int w = visibility.getPreferredSize().width + 10;
//				TableColumn tc = table.getColumnModel().getColumn(column);
//				tc.setPreferredWidth(w);
//				tc.setMaxWidth(w);
//				return visibility;
//			}
//		});


		/****************************************************************/
		/* This will render the column for showing the CHAT option      */
		/****************************************************************/
//		tree.setDefaultRenderer(MyChatLabel.class, new TableCellRenderer() {
//			private MyChatLabel chat = new MyChatLabel();
//			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//				if (isSelected)
//					chat.setBackground(Color.LIGHT_GRAY);
//				else
//					chat.setBackground(Color.white);
//				chat.useIcon((Boolean)value);
//				int w = chat.getPreferredSize().width + 10;
//				TableColumn tc = table.getColumnModel().getColumn(column);
//				tc.setPreferredWidth(w);
//				tc.setMaxWidth(w);
//				return chat;
//			}
//		});
		
		
		/****************************************************************/
		/* This will render the column for showing the custom properties*/
		/****************************************************************/
//		tree.setDefaultRenderer(MyPropertiesLabel.class, new TableCellRenderer(){
//			private MyPropertiesLabel properties = new MyPropertiesLabel();
//			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//				if (isSelected)
//					properties.setBackground(Color.LIGHT_GRAY);
//				else
//					properties.setBackground(Color.white);
//				properties.useIcon((Boolean)value);
//				int w = properties.getPreferredSize().width + 10;
//				TableColumn tc = table.getColumnModel().getColumn(column);
//				tc.setPreferredWidth(w);
//				tc.setMaxWidth(w);
//				return properties;
//			}
//		});
//		
//		tree.setDefaultEditor(MyEyeCheckBox.class, new MyEyeCheckBoxEditor());
//		tree.setDefaultEditor(MyChatLabel.class, new MyChatLabelEditor());
//		tree.setDefaultEditor(MyPropertiesLabel.class, new MyPropertiesLabelEditor());
		
//		tree.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		ComponentProvider<AbstractButton> visibilityProvider = new CheckBoxProvider("/icons/system/eye.png", "/icons/system/eye-close.png");
		ComponentProvider<JLabel> userProvider = new LabelProvider(new UserValue(), (int)JLabel.CENTER_ALIGNMENT);
		
		tree.getColumn(1).setCellRenderer(new DefaultTableRenderer(visibilityProvider));
		tree.getColumn(2).setCellRenderer(new DefaultTableRenderer(userProvider));
		tree.getColumn(3).setCellRenderer(new DefaultTableRenderer(userProvider));
		tree.getColumn(1).setMaxWidth(22);
		tree.getColumn(2).setMaxWidth(22);
		tree.getColumn(3).setMaxWidth(22);
		tree.getColumn(1).setMinWidth(12);
		tree.getColumn(2).setMinWidth(12);
		tree.getColumn(3).setMinWidth(12);
		
//		tree.setShowGrid(true);
//		HighlightPredicate customPredicate = new HipghlightPredicate() {
//			@Override
//			public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
//				return true;
//			}
//		};
//		Highlighter hl = new ColorHighlighter(customPredicate, Color.GREEN, Color.BLUE);
//		tree.addHighlighter(hl);
		tree.setTreeCellRenderer(new DefaultTreeRenderer(userProvider));
		tree.packAll();
		treeView = new JScrollPane(tree);
		treeView.setBorder(BorderFactory.createEmptyBorder());
		setBackground(Color.white);
		add(treeView);
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
			else if (value.toString().startsWith("[chat]"))
				icon = chatIcon;
			else  if (value.toString().startsWith("[properties]"))
				icon = propertiesIcon;

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