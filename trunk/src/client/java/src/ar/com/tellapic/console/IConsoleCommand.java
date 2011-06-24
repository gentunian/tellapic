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

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public interface IConsoleCommand {

	public IConsoleCommand executeCommand(String cmd, Object[] args) throws WrongCommandExecution;
	
	public String[] getCommandList();
	
//	public Class[] getArgumentTypesForCommand(String cmd);
	
	public String getReturnTypeForCommand(String cmd);
	
	public String[] getArgumentsTypesForCommand(String cmd);
	
	public String[] getArgumentsNamesForCommand(String cmd);
}
