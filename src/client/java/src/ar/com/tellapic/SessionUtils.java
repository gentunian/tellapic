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

import java.awt.image.BufferedImage;
import java.util.Observable;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class SessionUtils {

	private static BufferedImage  sharedImage;
	private static String         username;
	private static String         server;
	private static String         password;
	private static int            id;
	private static int            port;
	
	/**
	 * @param sharedImage the sharedImage to set
	 */
	public static void setSharedImage(BufferedImage sharedImage) {
		SessionUtils.sharedImage = sharedImage;
//		DrawingAreaView.getInstance().setImage(sharedImage);
	}

	/**
	 * @return the sharedImage
	 */
	public static BufferedImage getSharedImage() {
		return sharedImage;
	}


	/**
	 * @param username the username to set
	 */
	public static void setUsername(String username) {
		SessionUtils.username = username;
	}


	/**
	 * @return the username
	 */
	public static String getUsername() {
		return username;
	}


	/**
	 * @param id
	 */
	public static void setId(int id) {
		SessionUtils.id = id;
	}
	
	
	/**
	 * 
	 */
	public static int getId() {
		return id;
	}

	/**
	 * @param server the server to set
	 */
	public static void setServer(String server) {
		SessionUtils.server = server;
	}

	/**
	 * @return the server
	 */
	public static String getServer() {
		return server;
	}

	/**
	 * @param port the port to set
	 */
	public static void setPort(int port) {
		SessionUtils.port = port;
	}

	/**
	 * @return the port
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * @param password the password to set
	 */
	public static void setPassword(String password) {
		SessionUtils.password = password;
	}

	/**
	 * @return the password
	 */
	public static String getPassword() {
		return password;
	}
}
