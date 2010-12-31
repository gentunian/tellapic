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

import java.util.Locale;
import java.util.ResourceBundle;

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
}
