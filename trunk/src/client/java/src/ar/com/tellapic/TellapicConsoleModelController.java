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
import java.util.NoSuchElementException;

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
					String[] aDescs = tool.getArgumentsDescriptionsForCommand(cmd);
					String  summary = tool.getDescriptionForCommand(cmd);
					oc.addMethod(buildCompletionForType(oolp, cmd, rType, aTypes, aNames, aDescs, summary));
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
	private ObjectMethodCompletion buildCompletionForType(ObjectOrientedLanguageCompletionProvider oolp, String cmd, String rType, String[] aTypes, String[] aNames, String[] aDescs, String summary) {
		String[] rTypeWrap = rType.split("\\.");
		if (rType.equals("void")) {
		
			ObjectMethodCompletion omc = new ObjectMethodCompletion(oolp, cmd);
			if (aTypes != null)
				omc.setParams(buildParamList(aTypes, aNames, aDescs));
			omc.setSummary(summary);
			omc.setShortDescription(summary);
			return omc;
		}
		
		if (alreadyBuiltCompletion.containsKey(rType)) {
			ObjectMethodCompletion omc = new ObjectMethodCompletion(oolp, cmd, rTypeWrap[rTypeWrap.length - 1], alreadyBuiltCompletion.get(rType));
			if (aTypes != null)
				omc.setParams(buildParamList(aTypes, aNames, aDescs));
			omc.setSummary(summary);
			omc.setShortDescription(summary);
			return omc;
		}
		
		ObjectCompletion       ocForMethod = new ObjectCompletion(oolp, rTypeWrap[rTypeWrap.length - 1]);
		
		alreadyBuiltCompletion.put(rType, ocForMethod);

		try {
//			Class<?>   clazz    = Class.forName(rType.split(" ")[0]);
			Class<?>   clazz    = Class.forName(rType);
			Field      field    = clazz.getField("COMMANDS");
			String[][] COMMANDS = (String[][]) field.get(null);

			for(int i = 0; i < COMMANDS.length; i++) {
				for(int j = 0; j < COMMANDS[i].length; j++) {
					String[] split   = COMMANDS[i][j].split(" ", 3);
					String   type    = split[0].replace("ar.com.tellapic.graphics.AbstractDrawing", rType);
					String   cmdName = split[1].substring(0, split[1].indexOf('('));
					ArrayList<String> aTypes1 = new ArrayList<String>();
					ArrayList<String> aNames1 = new ArrayList<String>();
					ArrayList<String> aDescs1 = new ArrayList<String>();
					String[] aSplit = null;
					String argumentsLine = split[1].replaceAll("\\(([^)]+)\\)", "$1").replaceAll("_", " ");
					int end;
					do {
						int start = argumentsLine.indexOf('{');
						end   = argumentsLine.indexOf('}');
						aSplit = argumentsLine.substring(start+1, end).split(" ", 3);
						aTypes1.add(aSplit[0]);
						aNames1.add(aSplit[1]);
						aDescs1.add(aSplit[2]);
						argumentsLine = argumentsLine.substring(end, argumentsLine.length());
					} while(end < argumentsLine.length());

					ObjectMethodCompletion rTypeOC = buildCompletionForType(oolp, cmdName, type, aTypes1.toArray(new String[0]), aNames1.toArray(new String[0]), aDescs1.toArray(new String[0]), summary);

					ocForMethod.addMethod(rTypeOC);
				}
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
		ObjectMethodCompletion omc =  new ObjectMethodCompletion(oolp, cmd, rType, ocForMethod);
		if (aTypes != null)
			omc.setParams(buildParamList(aTypes, aNames, aDescs));
		omc.setSummary(summary);
		omc.setShortDescription(summary);
		return omc;
	}
	
	/**
	 * @param argumentsTypesForCommand
	 * @param argumentsNamesForCommand
	 * @return
	 */
	private List<Parameter> buildParamList(String[] argumentsTypesForCommand, String[] argumentsNamesForCommand, String[] argumentsDescriptions) {
		ArrayList<Parameter> paramList = new ArrayList<Parameter>();
		
		for (int i = 0; i < argumentsTypesForCommand.length; i++) {
			Parameter parameter = new Parameter(argumentsTypesForCommand[i], argumentsNamesForCommand[i]);
			parameter.setDescription(argumentsDescriptions[i]);
			paramList.add(parameter);
		}
		
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
		String[]        cmdArray  = cmd.split(";", 2);
		String          invoker   = getCommandInvoker(cmdArray[0]);
		
		if (invoker != null) {
			try {
				tbController.selectToolByName(invoker);
				command     = consoleUser.getToolBoxModel().getLastUsedTool();
				int current = 1;
				/* This will change number 3.4 to 3:4 */
				String cmdModified = cmdArray[0].replaceAll("([0-9]+)\\.([0-9]+)", "$1:$2");
				Utils.logMessage("cmd: "+cmd+" cmdmod: "+cmdModified);
				composeCommand(command, cmdModified.split("\\."), current);
				consoleModel.addCommandHistory(cmdArray[0]);
			} catch(IllegalArgumentException e) {
				Utils.logMessage("TODO: something went wrong while selecting command, aborting execution.");
				return;
			} catch(NoSuchElementException e) {
				Utils.logMessage("TODO: something went wrong while selecting command, aborting execution.");
				return;
			} catch (WrongCommandExecution e) {
				Utils.logMessage("TODO: something went wrong while executing command, aborting execution.");
				return;
			}
		}
		if (cmdArray.length > 1)
			handleInput(cmdArray[1]);
		
		// Do nasty thing: FIXME
		tbController.selectToolByName("Selector");
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
