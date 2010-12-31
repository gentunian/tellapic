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

import javax.swing.SwingUtilities;




/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class LocalUser extends AbstractUser {
	/*
	private ToolBoxModel            toolBox;
	private ToolViewController      toolBoxController;
	private DrawingLocalController  drawingController;
	private PaintPropertyController propertyController;
	*/
	public static final String LOCAL_NAME = "Local";
	//private DrawingLocalController  drawingController;
//	private ToolBoxModel            toolBox;
//	private PaintPropertyController propertyController;
//	private ToolViewController      toolController;
	
	private static class Holder {
		private static final LocalUser INSTANCE = new LocalUser(0, LOCAL_NAME);
	}
	
	private LocalUser(int id, String name) {
		super(id, name);
//		toolBox   = new ToolBoxModel();
		//drawingController  = new DrawingLocalController(toolBox); //, DrawingAreaModel.getInstance(), DrawingAreaView.getInstance());
//		propertyController = new PaintPropertyController(toolBox);
//		toolController     = new ToolViewController(toolBox);
		//UserGUIBuilder gui = 
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new UserGUIBuilder(Holder.INSTANCE);
            }
		});
	}
	
	public static LocalUser getInstance() {
		return Holder.INSTANCE;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isRemote()
	 */
	@Override
	public boolean isRemote() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isSelected()
	 */
	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isSpecial()
	 */
	@Override
	public boolean isSpecial() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return true;
	}
}
