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
package ar.com.tellapic.utils;

import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import ar.com.tellapic.graphics.RemoteMouseEvent;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public final class Utils {
	public static boolean debug = true;
	public static Locale currentLocale = Locale.getDefault();
	public static ResourceBundle msg = ResourceBundle.getBundle("Words");
	
	public static void logMessage(String msg) {
		if (debug)
			System.out.println(msg);
	}
	
	public static void changeLocale() {
		//TODO: fucking work to do
	}
	
	/**
	 * 
	 * @param event
	 */
	public static void printEventInfo(MouseEvent event) {
		Utils.logMessage("Mouse event:");
		Utils.logMessage("\tbutton: "+event.getButton());
		Utils.logMessage("\tclick count: "+event.getClickCount());
		Utils.logMessage("\tid: "+event.getID());
		Utils.logMessage("\tmodifiers: "+event.getModifiers());
		Utils.logMessage("\tmodifiers ext: "+event.getModifiersEx());
		Utils.logMessage("\twhen: "+event.getWhen());
		Utils.logMessage("\tx: "+event.getX());
		Utils.logMessage("\ty: "+event.getY());
		Utils.logMessage("\tsource: "+event.getSource());
		Utils.logMessage("\tinstanceof RemoteMouseEvent?: "+ (event instanceof RemoteMouseEvent));
	}
}