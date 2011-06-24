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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;

import ar.com.tellapic.TellapicConsoleModelController;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class Main {

	public static void main(String[] args) {
		ConsoleModel model = new ConsoleModel();
//		ConsoleModelController modelController = new ConsoleModelController(model);
//		final JFrame consoleWindow = new JFrame("console test");
//		ConsolePanel consolePanel = new ConsolePanel(modelController);
//		model.addObserver(consolePanel);
//		consoleWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//		consoleWindow.addComponentListener(new ComponentListener() {
//			public void componentHidden(ComponentEvent e) {
//				consoleWindow.dispose();
//			}
//			public void componentMoved(ComponentEvent e) {}
//			public void componentResized(ComponentEvent e) {}
//			public void componentShown(ComponentEvent e) {}});
//		consoleWindow.add(consolePanel);
//		consoleWindow.pack();
//		consoleWindow.setVisible(true);
	}
}
