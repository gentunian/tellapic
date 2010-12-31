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

import java.awt.GridLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UserView extends JPanel implements Observer {

	private static final long serialVersionUID = 1L;
	private JTree                  usersTree;
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel       treeModel;
	private JScrollPane            treeView;
	private final IUserManagerController controller = UserManagerController.getInstance();
	
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
		rootNode  = new DefaultMutableTreeNode(Utils.msg.getString("userlist"));
		treeModel = new DefaultTreeModel(rootNode);
		usersTree = new JTree(treeModel);

//		CheckTreeManager manager = new CheckTreeManager(usersTree, true, new TreePathSelectable() {
//
//			@Override
//			public boolean isSelectable(TreePath path) {
//				// TODO Auto-generated method stub
//				//return path.getPathCount() == 2;
//				return true;
//			}
//		});
//		CheckTreeSelectionModel selModel = manager.getSelectionModel();
//		selModel.addTreeSelectionListener(new TreeSelectionListener() {
//
//			@Override
//			public void valueChanged(TreeSelectionEvent arg0) {
//				// TODO Auto-generated method stub9
//				CheckTreeSelectionModel s = (CheckTreeSelectionModel) arg0.getSource();
//				Utils.logMessage("VALUE CHANGED! ");
//				Utils.logMessage("\tlead selection row: "+s.getLeadSelectionRow());
//				Utils.logMessage("\tmax selection row: "+s.getMaxSelectionRow());
//				Utils.logMessage("\tmin selection row: "+s.getMinSelectionRow());
//				Utils.logMessage("\tselection count: "+s.getSelectionCount());
//				Utils.logMessage("\tselection rows:"+s.getSelectionRows());
//				
//				for(Enumeration e = s.getAllSelectedPaths(); e.hasMoreElements();) {
//					//son treepaths
//					Utils.logMessage("queloque: "+e.nextElement().getClass().getName());
//				}
//				if (s.getSelectionCount() == 1)
//					Utils.logMessage("\tobject toString: "+s.getSelectionPath().getLastPathComponent());
//			
//			}});
		
		treeView = new JScrollPane(usersTree);
		treeView.setBorder(BorderFactory.createEmptyBorder());
		ToolTipManager.sharedInstance().registerComponent(usersTree);
		TreeSelectionModel treeSelectionModel = usersTree.getSelectionModel();
		treeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		treeSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				// TODO Auto-generated method stub
				Utils.logMessage("VALUE CHANGED! "+arg0.getSource());
			}});
		usersTree.expandRow(0);
		usersTree.setShowsRootHandles(true);
		usersTree.setRootVisible(true);
		usersTree.setEditable(false);
		
		usersTree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				// TODO Auto-generated method stub
				Utils.logMessage("VALUE CHANGED! "+arg0.getSource());
			}});
		
		add(treeView);
	}

	
	/**
	 * 
	 * @return
	 */
	public static UserView getInstance() {
		return Holder.INSTANCE;
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object data) {
		Utils.logMessage("Updating user view. Data received: "+data);
		
		if (data != null) {
			AbstractUser user = (AbstractUser) o;
			DefaultMutableTreeNode userNode = null;
			boolean found = false;
			for(int i = 0; i < rootNode.getChildCount() && !found; i++) {
				userNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
				if (userNode.getUserObject().equals(user))
					found = true;
			}
			
			if (found) {
				//The user already exist in the users list.
				if (user.isRemoved()) {
					treeModel.removeNodeFromParent(userNode);
				} else {
					if (data instanceof Integer) {
						DefaultMutableTreeNode   drawingNode = new DefaultMutableTreeNode(user.getDrawings().get((Integer)data));
						treeModel.insertNodeInto(drawingNode, userNode, userNode.getChildCount());
						usersTree.scrollPathToVisible(new TreePath(drawingNode.getPath()));
					}
				}
			} else {
				//The user doesn't exist in the users list. Add it.
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(user);
				treeModel.insertNodeInto(child, rootNode, rootNode.getChildCount());
				usersTree.scrollPathToVisible(new TreePath(child.getPath()));
			}
		}
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