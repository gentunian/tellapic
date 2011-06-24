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
package ar.com.tellapic.console;

import java.util.List;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IConsoleState {

	public String[] getCommandHistoryArray();
	
	public List<String> getCommandHistory();
	
	public String getLastCommand();
	
	public String getFirstCommand();
	
	public String getNextHistoryCommand();
	
	public String getPrevHistoryCommand();
	
	public String getCurrentHistoryCommand();
	
	public int getNextHistoryCommandIndex();
	
	public int getPrevHistoryCommandIndex();
	
	public int getCurrentHistoryCommandIndex();
}
