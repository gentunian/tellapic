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

import ar.com.tellapic.lib.stream_t;
import ar.com.tellapic.lib.tellapic;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class NetManager {
	
	private int fd;
	private int id;
	private boolean connected;
	private String  username;
	
	private static class Holder {
		private final static NetManager INSTANCE = new NetManager();
	}
	
	private NetManager() {
		connected = false;
		fd = 0;
		id = 0;
		username = null;
	}
	
	/**
	 * 
	 * @return
	 */
	public static NetManager getInstance() {
		return Holder.INSTANCE;
	}

	
	/**
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @param password
	 * @return
	 */
	public int connect(String host, int port, String name, String password) {
		System.out.println("Connecting to "+host+":"+port);
		fd = tellapic.tellapic_connect_to(host, port);
		if (fd <= 0)
			return fd;
		
		stream_t stream = tellapic.tellapic_read_stream_b(fd);
		if (stream.getHeader().getCbyte() == tellapic.CTL_FAIL) {
			tellapic.tellapic_close_fd(fd);
			return -1;
		}
		System.out.println("Authing with host. Cbyte read: "+stream.getHeader().getCbyte());
		
		if (stream.getHeader().getCbyte() == tellapic.CTL_SV_ID) {
			id = stream.getData().getControl().getIdfrom();
			
			System.out.println("Sending password: "+password);
			tellapic.tellapic_send_ctle(fd, id, tellapic.CTL_CL_PWD, password.length(), password);
			
			stream = tellapic.tellapic_read_stream_b(fd);
			System.out.println("Response read: "+stream.getHeader().getCbyte());
			
			while(stream.getHeader().getCbyte() == tellapic.CTL_SV_PWDFAIL) {
				//Retry password dialog here
				System.out.println("Password was wrong.");
			}
			
			if (stream.getHeader().getCbyte() == tellapic.CTL_SV_PWDOK) {
				System.out.println("Password was ok. Sending name: "+name);
				tellapic.tellapic_send_ctle(fd, stream.getData().getControl().getIdfrom(), tellapic.CTL_CL_NAME, name.length(), name);
			} else {
				tellapic.tellapic_close_fd(fd);
				return -1;
			}
		} else {
			tellapic.tellapic_close_fd(fd);
			return -1;
		}
		connected = true;
		
		username = name; //TODO: Server must inform about the availability of the 'name'.
		
		ReceiverThread r = new ReceiverThread(fd);
		r.start();
		
		return fd;
	}
	
	
	/**
	 * 
	 * @param port
	 * @param filename
	 * @param name
	 * @param password
	 */
	public void startServer(int port, String filename, String name, String password) {
		
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return connected;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public int getSocketFd() {
		return fd;
	}
	
	public int getServerId() {
		return id;
	}

	/**
	 * @return
	 */
	public String getServerUsername() {
		return username;
	}
}
