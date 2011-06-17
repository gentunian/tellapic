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

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.tree.TreeModelSupport;
import org.jdesktop.swingx.treetable.TreeTableModel;

import ar.com.tellapic.adm.AbstractUser;
import ar.com.tellapic.adm.UserManager;
import ar.com.tellapic.graphics.AbstractDrawing;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class TellapicUserManager extends UserManager implements TreeTableModel, TreeModelListener, Observer {
	private static final int COLUMN_COUNT = 4;
	private static final String[] COLUMN_NAME = new String[] {
		"User" ,
		"Visibility",
		"Chat",
		"Custom Properties"
	};
	private static final Class<?>[] COLUMN_CLASS = new Class<?>[] { 
		String.class,
		MyEyeCheckBox.class,
		String.class,
		String.class
	};
	
	private TreeModelSupport        tms;
	
	private static class Holder {
		private final static TellapicUserManager INSTANCE = new TellapicUserManager();
	}
	
	/**
	 * 
	 * @return
	 */
	public static TellapicUserManager getInstance() {
		return Holder.INSTANCE;
	}
	
	/**
	 * 
	 */
	private TellapicUserManager() {
		super();
		tms = new TreeModelSupport(this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.UserManager#addUser(ar.com.tellapic.AbstractUser)
	 */
	@Override
	public boolean addUser(AbstractUser user) {
		boolean added = super.addUser(user);
		
		if (added) {
			user.addObserver(this);
			tms.fireTreeStructureChanged(new TreePath(getUsers()));
		}
		
		return added;
	}
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.UserManager#delUser(ar.com.tellapic.AbstractUser)
	 */
	@Override
	public boolean delUser(AbstractUser user) {
		boolean removed = super.delUser(user);
		
		if (removed) {
			((TellapicAbstractUser)user).cleanUp();
			tms.fireTreeStructureChanged(new TreePath(getUsers()));
		}
		
		return removed;
	}
	

	
	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.TreeTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return COLUMN_CLASS[columnIndex];
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.TreeTableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.TreeTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAME[column];
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.TreeTableModel#getHierarchicalColumn()
	 */
	@Override
	public int getHierarchicalColumn() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.TreeTableModel#getValueAt(java.lang.Object, int)
	 */
	@Override
	public Object getValueAt(Object node, int column) {
		
		Object value = "DEFAULT";
		if (node instanceof ArrayList<?>) {
			switch(column) {
			case 0:
				value = Utils.msg.getString("userlist") + ((ArrayList<?>)node).size();
				break;
			default:
				value = false;
			}
		} else if (node instanceof TellapicAbstractUser) {
			switch(column) {
			case 0:
				value = node;
				break;
			case 1:
				value = ((TellapicAbstractUser)node).isVisible();
				break;
			case 2:
				value = "[properties]";
				break;
			case 3:
				value = "[chat]";
				break;
			}
		} else if( node instanceof AbstractDrawing) {
			switch(column) {
			case 0:
				value = node;
				break;
			case 1:
				value = ((AbstractDrawing)node).isVisible();
				break;
			default:
				value = false;
				break;
			}
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.TreeTableModel#isCellEditable(java.lang.Object, int)
	 */
	@Override
	public boolean isCellEditable(Object node, int column) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingx.treetable.TreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	@Override
	public void setValueAt(Object value, Object node, int column) {
		if (node instanceof TellapicAbstractUser) {
			switch(column) {
			case 1:
				((TellapicAbstractUser)node).setVisible((Boolean)value);
				break;
			case 2:
				break;
			}
		}
		else if (node instanceof AbstractDrawing) {
			switch(column) {
			case 1: 
				((AbstractDrawing)node).setVisible((Boolean)value);
				break;
			case 2:
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		tms.addTreeModelListener(l);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		Object child = null;
		if (index >= 0 && index < getChildCount(parent))
			if (parent instanceof ArrayList<?> )
				child = ((ArrayList<?>)parent).get(index);
			else if (parent instanceof TellapicAbstractUser && index >= 0)
				child = ((TellapicAbstractUser)parent).getDrawings().get(index);
			else 
				child = parent;
		return child;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		int count = 0;
		if (parent instanceof ArrayList<?>) {
			count = ((ArrayList<?>)parent).size();
		} else if (parent instanceof TellapicAbstractUser) {
			TellapicAbstractUser user = (TellapicAbstractUser) parent;
			count = user.getDrawings().size();
		} else if (parent instanceof AbstractDrawing) {
			count = 0;
		} else {

		}
		return count;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		int index = 0;
		
		if (parent instanceof TellapicAbstractUser) {
			index = ((TellapicAbstractUser)parent).getDrawings().indexOf(child);
		} else if (parent instanceof ArrayList<?>) {
			index = ((ArrayList<?>)parent).indexOf(child);
		}
		return index;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
//		System.out.println("GETROOT: "+users);
		return getUsers();
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		boolean value = true;
		if (node instanceof TellapicAbstractUser) {
			value = (((TellapicAbstractUser)node).getDrawings().size() == 0);
		} else 
			value = !(node instanceof ArrayList<?>);
		
		return value;
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO Auto-generated method stub
		tms.removeTreeModelListener(l);
	}

	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		TellapicAbstractUser       user   = (TellapicAbstractUser) o;
		ArrayList<AbstractDrawing> childs = user.getDrawings();
		
		if (arg instanceof Object[]) {
			
			/* The first mandatory argument */
			int action = (Integer)((Object[]) arg)[0];
			
			/* The second optional argument */
			AbstractDrawing drawing = null;
			
			/* The thierd optional argument */
			int extra = 0;
			
			switch(action) {
			
			case TellapicAbstractUser.ADD_DRAWING:
				drawing = (AbstractDrawing) ((Object[]) arg)[1];
				tms.fireChildAdded(new TreePath(new Object[]{ getRoot(), user }), childs.indexOf(drawing), drawing);
				break;
				
			case TellapicAbstractUser.REMOVE_DRAWING:
				drawing = (AbstractDrawing) ((Object[]) arg)[1];
				extra   = (Integer)((Object[]) arg)[2];
				tms.fireChildRemoved(new TreePath(new Object[]{ getRoot(), user }), extra, drawing);
				break;
				
			case TellapicAbstractUser.CLEANUP:
				break;
				
			case TellapicAbstractUser.DRAWING_NUMBER_SET:
				break;
				
			case TellapicAbstractUser.PROPERTY_REMOVE:
				break;
				
			case TellapicAbstractUser.PROPERTY_SET:
				break;
				
			case TellapicAbstractUser.NAME_SET:
				break;
				
			case TellapicAbstractUser.REMOTE_CHANGED:
				break;
				
			case TellapicAbstractUser.SELECTION_CHANGED:
				break;
				
			case TellapicAbstractUser.USER_ID_SET:
				break;
				
			case TellapicAbstractUser.VISIBILITY_CHANGED:
				TreePath path = new TreePath(new Object[]{getRoot()});
				tms.fireChildChanged(path, getUsers().indexOf(user), user);
				int indices[] = new int[user.getDrawings().size()];
				for(int i = 0; i < indices.length; i++)
					indices[i] = i;
				tms.fireChildrenChanged(path.pathByAddingChild(user), indices, user.getDrawings().toArray());
				break;
				
			case TellapicAbstractUser.DRAWING_CHANGED:
				drawing = (AbstractDrawing) ((Object[]) arg)[1];
				extra = (Integer)((Object[])arg)[2];
				if (childs.contains(drawing))
					tms.fireChildChanged(new TreePath(new Object[]{getRoot(), user}), extra, drawing);
				break;
			}
		}
		
		DrawingAreaView.getInstance().update(null, null);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent e) {}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent e) {}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent e) {}
}
