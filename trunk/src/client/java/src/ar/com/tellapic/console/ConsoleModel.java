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

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ConsoleModel extends Observable implements IConsoleManager, IConsoleState{

	private ArrayList<String> commandHistory;
	private int               lastCommand;
	
	public ConsoleModel() {
		lastCommand = -1;
		commandHistory = new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.ConsoleManager#addCommandHistory(java.lang.String)
	 */
	@Override
	public void addCommandHistory(String cmd) {
		if (cmd == null)
			throw new NullPointerException("cmd cannot be null");
		
		if (!commandHistory.contains(cmd)) {
			commandHistory.add(cmd);
			lastCommand = commandHistory.indexOf(cmd);
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.ConsoleState#getCommandHistory()
	 */
	@Override
	public String[] getCommandHistoryArray() {
		return commandHistory.toArray(new String[0]);
	}

	public List<String> getCommandHistory() {
		return commandHistory;
	}
	
	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.ConsoleState#getFirstCommand()
	 */
	@Override
	public String getFirstCommand() {
		if (!commandHistory.isEmpty())
			return commandHistory.get(0);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.ConsoleState#getLastCommand()
	 */
	@Override
	public String getLastCommand() {
		if (!commandHistory.isEmpty())
			return commandHistory.get(commandHistory.size()-1);
		
		return null;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.ConsoleState#getNextHistoryCommand()
	 */
	@Override
	public String getNextHistoryCommand() {
		if (lastCommand < commandHistory.size() - 1)
			lastCommand++;
		
		return getCurrentHistoryCommand();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.ConsoleState#getPrevHistoryCommand()
	 */
	@Override
	public String getPrevHistoryCommand() {
		if (lastCommand > 0)
			lastCommand--;
		
		return getCurrentHistoryCommand();
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.ConsoleState#getCurrentHistoryCommand()
	 */
	@Override
	public String getCurrentHistoryCommand() {
		if (commandHistory.isEmpty())
			return null;
		
		return commandHistory.get(lastCommand);
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleManager#setCommand(java.lang.String)
	 */
	@Override
	public void setCommand(int index) {
		if (index >= 0 && index < commandHistory.size()) {
			lastCommand = index;
			setChanged();
			notifyObservers(commandHistory.get(lastCommand));
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleState#getCurrentHistoryCommandIndex()
	 */
	@Override
	public int getCurrentHistoryCommandIndex() {
		return lastCommand;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleState#getNextHistoryCommandIndex()
	 */
	@Override
	public int getNextHistoryCommandIndex() {
		return commandHistory.indexOf(getNextHistoryCommand());
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleState#getPrevHistoryCommandIndex()
	 */
	@Override
	public int getPrevHistoryCommandIndex() {
		return commandHistory.indexOf(getPrevHistoryCommand());
	}
}
