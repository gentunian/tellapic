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
	
	private LocalUser(int id, String name) {
		super(id, name);
	}
	
	public static LocalUser getInstance() {
		return Holder.INSTANCE;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isRemote()
	 */
	@Override
	public boolean isRemote() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isSelected()
	 */
	@Override
	public boolean isSelected() {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.tellapic.AbstractUser#isSpecial()
	 */
	@Override
	public boolean isSpecial() {
		return false;
	}
}
