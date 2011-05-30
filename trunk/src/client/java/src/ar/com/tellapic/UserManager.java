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

import ar.com.tellapic.graphics.AbstractDrawing;
import ar.com.tellapic.graphics.DrawingAreaView;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UserManager implements IUserManager, IUserManagerState, TreeTableModel, Observer, TreeModelListener {

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
	

	private ArrayList<AbstractUser> users;
	
	private TreeModelSupport tms;
	
	private static class Holder {
		private final static UserManager INSTANCE = new UserManager();
	}
	
	private UserManager() {
		users = new ArrayList<AbstractUser>();
		tms = new TreeModelSupport(this);
	}
	

	/**
	 * 
	 * @return
	 */
	public static UserManager getInstance() {
		return Holder.INSTANCE;
	}

	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#createUser(int, java.lang.String, boolean)
	 */
	@Override
	public AbstractUser createUser(int id, String name, boolean remote) {
		AbstractUser user = null;
		if (remote)
			user = new RemoteUser();
		else
			user = LocalUser.getInstance();
		
		user.setName(name);
		user.setUserId(id);
		
		if (addUser(user))
			return user;
		else
			return null; //TODO: Throw an exception?
	}
	
	
		
	
	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#addUser(ar.com.tellapic.AbstractUser)
	 */
	@Override
	public boolean addUser(AbstractUser user) {
		boolean userWasAdded = users.add(user);
		
		if (userWasAdded) {
			user.addObserver(this);
			tms.fireTreeStructureChanged(new TreePath(users));
		}
		
		return userWasAdded;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getLocalUser()
	 */
	@Override
	public LocalUser getLocalUser() {
		for(int i = 0 ; i < users.size(); i++)
			if (users.get(i) instanceof LocalUser)
				return (LocalUser) users.get(i);
		return null;
	}

	
	/*
	 * 
	 */
	private AbstractUser findUser(String userName) {
		AbstractUser user = null;
		boolean found = false;
		int i;
		for(i = 0; i < users.size() && !found; i++) {
			user = users.get(i);
			found = user.getName().equals(userName);
		}

		return user;
	}
	
	
	/*
	 * 
	 */
	private AbstractUser findUser(int id) {
		AbstractUser user = null;
		boolean found = false;
		int i;
		for(i = 0; i < users.size() && !found; i++) {
			user = users.get(i);
			found = (user.getUserId() == id);
		}
		
		return (found)? user : null;
	}
	
	
	/*
	 * 
	 */
	private void removeUser(AbstractUser user) {
		if (users.remove(user))
			user.cleanUp();
		tms.fireTreeStructureChanged(new TreePath(users));
	}


	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#delUser(java.lang.String)
	 */
	@Override
	public AbstractUser delUser(String name) {
		AbstractUser user = findUser(name);
	
		removeUser(user);
		
		return user;
	}
	
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.IUserManager#delUser(int)
	 */
	@Override
	public AbstractUser delUser(int id) {
		AbstractUser user = findUser(id);

		removeUser(user);
		
		return user;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getUsers()
	 */
	@Override
	public ArrayList<AbstractUser> getUsers() {
		return users;
	}
	


	/* (non-Javadoc)
	 * @see com.tellapic.IUserManager#requireDisconnection(int)
	 */
	@Override
	public boolean requireDisconnection(String name) {
		// TODO Auto-generated method stub
		return false;
	}


	/* (non-Javadoc)
	 * @see com.tellapic.IUserManagerState#getUser(int)
	 */
	@Override
	public AbstractUser getUser(String name) {
		return findUser(name);
	}

	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.IUserManagerState#getUserName(int)
	 */
	@Override
	public AbstractUser getUser(int id) {
		return findUser(id);
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
		} else if (node instanceof AbstractUser) {
			switch(column) {
			case 0:
				value = node;
				break;
			case 1:
				value = ((AbstractUser)node).isVisible();
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
		Utils.logMessage("set value "+value+" at node " +node+" for column "+column);
		if (node instanceof AbstractUser) {
			switch(column) {
			case 1:
				((AbstractUser)node).setVisible((Boolean)value);
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

			else if (parent instanceof AbstractUser && index >= 0)
				child = ((AbstractUser)parent).getDrawings().get(index);

			else 
				child = parent;

		Utils.logMessage("getChild: "+parent+ " at: "+index+ " is: "+child);
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
//			Utils.logMessage("getchildCount HASHMAP: "+count);
		} else if (parent instanceof AbstractUser) {
			AbstractUser user = (AbstractUser) parent;
			count = user.getDrawings().size();
//			Utils.logMessage("getchildCount USER: "+count);
			
		} else if (parent instanceof AbstractDrawing) {
//			Utils.logMessage("getchildCount DRAWING: "+count);
			count = 0;
		} else {
//			System.out.println("wadafak? "+parent);
		}
//		Utils.logMessage("getchildCount from "+parent+" was: "+count);
		return count;
	}


	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {

		int index = 0;
		
		if (parent instanceof AbstractUser) {
			index = ((AbstractUser)parent).getDrawings().indexOf(child);
		} else if (parent instanceof ArrayList<?>) {
			index = ((ArrayList<?>)parent).indexOf(child);
		}
//		Utils.logMessage("getIndexOf: "+parent+ " child: "+child+" was: "+index);
		return index;
	}


	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
//		System.out.println("GETROOT: "+users);
		return users;
	}


	/* (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		boolean value = true;
		if (node instanceof AbstractUser) {
			value = (((AbstractUser)node).getDrawings().size() == 0);
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
		System.out.println("WAFASDFASJFKSAFJASLKFJAS");

	}


	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		AbstractUser user = (AbstractUser) o;
		ArrayList<AbstractDrawing> childs = user.getDrawings();
		if (arg instanceof AbstractDrawing) {
			if (childs.contains(arg))
				tms.fireChildAdded(new TreePath(new Object[]{ getRoot(), user }), childs.indexOf(arg), arg);
			
		} else if (arg instanceof Boolean){
			 TreePath path = new TreePath(new Object[]{ getRoot() });
			tms.fireChildChanged(path, users.indexOf(user), user);
			int indices[] = new int[user.getDrawings().size()];
			for(int i = 0; i < indices.length; i++)
				indices[i] = i;
			tms.fireChildrenChanged(path.pathByAddingChild(user), indices, user.getDrawings().toArray());
		} else if (arg instanceof Integer) {
			int index = (Integer) arg;
			tms.fireChildChanged(new TreePath(new Object[]{getRoot(), user}), index, childs.get(index));
			
		} else if (arg instanceof Object[]) {
			int index = (Integer)((Object[]) arg)[1];
			AbstractDrawing drawing = (AbstractDrawing)((Object[])arg)[0];
			tms.fireChildRemoved(new TreePath(new Object[]{ getRoot(), user }), index, drawing);
		}
		
		DrawingAreaView.getInstance().update(null, null);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent e) {}


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
