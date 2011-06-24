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

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;

import ar.com.tellapic.console.ConsoleModel;
import ar.com.tellapic.console.IConsoleCommand;
import ar.com.tellapic.console.IConsoleModelController;
import ar.com.tellapic.console.WrongCommandExecution;
import ar.com.tellapic.graphics.IToolBoxController;
import ar.com.tellapic.graphics.Tool;
import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class TellapicConsoleModelController implements IConsoleModelController {
	private ConsoleModel                      consoleModel;
	private IToolBoxController                tbController;
	private TellapicAbstractUser              consoleUser;
	private AutoCompletion                    ac;
	private HashMap<String, ObjectCompletion> alreadyBuiltCompletion;
	
	/**
	 * 
	 * @param console
	 * @param toolBoxController
	 * @param user
	 */
	public TellapicConsoleModelController(ConsoleModel console, IToolBoxController toolBoxController, TellapicAbstractUser user) {
		consoleModel = console;
		tbController = toolBoxController;
		consoleUser  = user;
		alreadyBuiltCompletion = new HashMap<String, ObjectCompletion>();

		ObjectOrientedLanguageCompletionProvider oolp = new ObjectOrientedLanguageCompletionProvider();
		oolp.setAutoActivationRules(false, ".");
		
		for(Tool tool : user.getToolBoxModel().getTools().values()) {
			ObjectCompletion oc = new ObjectCompletion(oolp, tool.getAlias());
			
			if (tool.getCommandList() != null) { 

				for (String cmd : tool.getCommandList()) {
//					ObjectMethodCompletion omc = null;
					String   rType  = tool.getReturnTypeForCommand(cmd);
					String[] aTypes = tool.getArgumentsTypesForCommand(cmd);
					String[] aNames = tool.getArgumentsNamesForCommand(cmd);
					oc.addMethod(buildCompletionForType(oolp, cmd, rType, aTypes, aNames));
				}
				oolp.addCompletion(oc);
			}
		}
		
		ac = new AutoCompletion(oolp);
		ac.setListCellRenderer(new CompletionCellRenderer());
		ac.setShowDescWindow(true);
		ac.setParameterAssistanceEnabled(true);
		ac.setAutoCompleteEnabled(true);
		ac.setAutoActivationEnabled(true);
		ac.setAutoActivationDelay(100);
		ac.setParameterAssistanceEnabled(true);
	}
	
	/**
	 * 
	 * @param currentType
	 */
	private ObjectMethodCompletion buildCompletionForType(ObjectOrientedLanguageCompletionProvider oolp, String cmd, String rType, String[] aTypes, String[] aNames) {
		if (rType.equals("void")) {
			ObjectMethodCompletion omc = new ObjectMethodCompletion(oolp, cmd);
			if (aTypes != null)
				omc.setParams(buildParamList(aTypes, aNames));
			return omc;
		}
		
		if (alreadyBuiltCompletion.containsKey(rType)) {
			ObjectMethodCompletion omc = new ObjectMethodCompletion(oolp, cmd, rType, alreadyBuiltCompletion.get(rType));
			if (aTypes != null)
				omc.setParams(buildParamList(aTypes, aNames));
			return omc;
		}
		
		ObjectCompletion       ocForMethod = new ObjectCompletion(oolp, rType);
//		ObjectMethodCompletion omc         = new ObjectMethodCompletion(oolp, cmd, rType, ocForMethod);
		
		alreadyBuiltCompletion.put(rType, ocForMethod);

		try {
			Class<?>   clazz    = Class.forName(rType);
			Field      field    = clazz.getField("COMMANDS");
			String[][] COMMANDS = (String[][]) field.get(null);

			for(int i = 0; i < COMMANDS[0].length; i++) {
				String   type = COMMANDS[i+1][0];
				String[] aTypes1 = null;
				String[] aNames1 = null;
				
				if (COMMANDS[i+1].length - 1 > 0) {
					aTypes1 = new String[COMMANDS[i+1].length - 1];
					aNames1 = new String[COMMANDS[i+1].length - 1];

					for (int j = 1; j < COMMANDS[i+1].length; j++) {
						aTypes1[j-1] = COMMANDS[i+1][j].split(" ")[0];
						aNames1[j-1] = COMMANDS[i+1][j].split(" ")[1];
					}
				}
				
				ObjectMethodCompletion rTypeOC = buildCompletionForType(oolp, COMMANDS[0][i], type, aTypes1, aNames1);
				
				ocForMethod.addMethod(rTypeOC);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		
		return new ObjectMethodCompletion(oolp, cmd, rType, ocForMethod);
	}
	
	/**
	 * @param argumentsTypesForCommand
	 * @param argumentsNamesForCommand
	 * @return
	 */
	private List<Parameter> buildParamList(String[] argumentsTypesForCommand, String[] argumentsNamesForCommand) {
		ArrayList<Parameter> paramList = new ArrayList<Parameter>();
		
		for (int i = 0; i < argumentsTypesForCommand.length; i++)
			paramList.add(new Parameter(argumentsTypesForCommand[i], argumentsNamesForCommand[i]));
		
		return paramList;
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleModelController#handleKeyEvent(ar.com.tellapic.console.KeyEvent)
	 */
	@Override
	public void handleKeyEvent(KeyEvent keyEvent) {
		int commandIndex = 0;
		
		switch(keyEvent.getKeyCode()) {
		
		case KeyEvent.VK_UP:
			commandIndex = consoleModel.getPrevHistoryCommandIndex();
			consoleModel.setCommand(commandIndex);
			break;
			
		case KeyEvent.VK_DOWN:
			commandIndex = consoleModel.getNextHistoryCommandIndex();
			consoleModel.setCommand(commandIndex);
			break;
			
//		case KeyEvent.VK_PERIOD:
//			JTextField textField = (JTextField) keyEvent.getSource();
//			String currentText = textField.getText();
//			
//			break;
		}
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleModelController#handleInput(java.lang.String)
	 */
	@Override
	public void handleInput(String cmd) {
		IConsoleCommand command   = null;
		String          invoker   = getCommandInvoker(cmd);
		
		if (invoker != null) {
			try {
				tbController.selectToolByName(invoker);
				command     = consoleUser.getToolBoxModel().getLastUsedTool();
				int current = 1;
				/* This will change number 3.4 to 3:4 */
				String cmdModified = cmd.replaceAll("([0-9]+)\\.([0-9]+)", "$1:$2");
				composeCommand(command, cmdModified.split("\\."), current);
				consoleModel.addCommandHistory(cmd);
			} catch(IllegalArgumentException e) {
				Utils.logMessage("TODO: something went wrong while selecting command");
			} catch (WrongCommandExecution e) {
				Utils.logMessage("TODO: something went wrong while executing command");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param command
	 * @param methods
	 * @param current
	 * @return
	 * @throws WrongCommandExecution
	 */
	private IConsoleCommand composeCommand(IConsoleCommand command, String[] methods, int current) throws WrongCommandExecution {
		IConsoleCommand cCommand  = command;
		String          method    = null;
		String          arguments = null;
		
		if (current < methods.length) {
			System.out.println("method: "+methods[current]);
			method    = methods[current].split("\\(")[0];
			arguments = methods[current].split("\\(|\\)")[1];
			Utils.logMessage("command arguments: "+arguments);
			
			cCommand  = command.executeCommand(method, trimArguments(arguments.split(",")));
			current++;
			composeCommand(cCommand, methods, current);
		}
		
		return cCommand;
	}
	
	/**
	 * 
	 * @param arguments
	 * @return
	 */
	private String[] trimArguments(String[] arguments) {
		String[] argsTrimmed = new String[arguments.length];
		
		for(int i = 0; i < arguments.length; i++) {
			argsTrimmed[i] = arguments[i].replaceAll(":", ".").trim();
		}
		
		return argsTrimmed;
	}

	/**
	 * 
	 * @param cmd
	 * @return
	 */
	private String getCommandInvoker(String cmd) {
		return cmd.split("\\.")[0];
	}

	/* (non-Javadoc)
	 * @see ar.com.tellapic.console.IConsoleModelController#getAutocompletionList()
	 */
	@Override
	public AutoCompletion getAutocompletion() {
		return ac;
	}
}
