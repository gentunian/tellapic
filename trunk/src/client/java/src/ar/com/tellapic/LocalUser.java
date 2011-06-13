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

import java.util.Observable;

import ar.com.tellapic.graphics.AbstractDrawing;
import ar.com.tellapic.lib.tellapic;
import ar.com.tellapic.lib.tellapicConstants;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class LocalUser extends AbstractUser {
	
	public static final String LOCAL_NAME = "Local";
	
	private static class Holder {
		private static final LocalUser INSTANCE = new LocalUser(0, LOCAL_NAME);
	}
	
	/**
	 * 
	 * @param id
	 * @param name
	 */
	private LocalUser(int id, String name) {
		super(id, name);
		setRemote(false);
	}
	
	/**
	 * 
	 * @return
	 */
	public static LocalUser getInstance() {
		return Holder.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * @see ar.com.tellapic.AbstractUser#removeDrawing(ar.com.tellapic.graphics.AbstractDrawing)
	 */
	@Override
	public synchronized boolean removeDrawing(AbstractDrawing drawing) {
		boolean removed = super.removeDrawing(drawing);
		
		if (removed) {
			String number = String.valueOf(drawing.getNumber());
			tellapic.tellapic_send_ctle(NetManager.getInstance().getSocket(), SessionUtils.getId(), tellapicConstants.CTL_CL_RMFIG, number.length(), number);
		}
		
		return removed;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		AbstractDrawing drawing = (AbstractDrawing) o;
		setChanged();
		notifyObservers(drawingList.indexOf(drawing));
	}
}
