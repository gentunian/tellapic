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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Observable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.ParameterizedCompletion.Parameter;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import ar.com.tellapic.ObjectCompletion;
import ar.com.tellapic.ObjectMethodCompletion;
import ar.com.tellapic.ObjectOrientedLanguageCompletionProvider;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ConsoleDialog extends JDialog implements IConsoleView{
	private static final long serialVersionUID = 1L;
	
	private final JPanel            contentPanel = new JPanel();
//	private JTextArea               thistextField;
	private final Action            cancelAction = new CancelAction();
	private final Action            okAction = new OkAction();
	private IConsoleModelController consoleController;
	private RSyntaxTextArea         textArea;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ConsoleDialog dialog = new ConsoleDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ConsoleDialog(JFrame parent, IConsoleModelController controller) {
		super(parent);
		
		textArea = new RSyntaxTextArea();
		RTextScrollPane areaScrollPane  = new RTextScrollPane(textArea);
		JLabel          lblNewLabel     = new JLabel("Try pressing Ctrl + Space. Compose command with ';'. Example:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel          lblNewLabel_1   = new JLabel("Rectangle.square(10,20,30).setColor(0xff00aa); Line.line(1,2,3,4);");
		GroupLayout     gl_contentPanel = new GroupLayout(contentPanel);
		Container       contentPane     = getContentPane();
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout());
		contentPane.add(contentPanel, BorderLayout.CENTER);
		areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		areaScrollPane.setAutoscrolls(true);
		areaScrollPane.setWheelScrollingEnabled(true);
		lblNewLabel.setFont(new Font("Dialog", Font.ITALIC, 11));
		lblNewLabel.setIcon(new ImageIcon(ConsoleDialog.class.getResource("/icons/system/information-balloon.png")));
		lblNewLabel_1.setBackground(Color.PINK);
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("SansSerif", Font.BOLD, 10));
		lblNewLabel_1.setOpaque(true);
		
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 412, Short.MAX_VALUE)
				.addComponent(areaScrollPane, GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
				.addComponent(lblNewLabel_1, GroupLayout.DEFAULT_SIZE, 448, Short.MAX_VALUE)
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNewLabel_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(areaScrollPane, GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
					.addContainerGap())
		);
		contentPanel.setLayout(gl_contentPanel);
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			contentPane.add(buttonPane, BorderLayout.SOUTH);
			
			JCheckBox shouldClose = new JCheckBox("Close this dialog after Run?");
			shouldClose.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					okAction.putValue("RUN_AND_CLOSE", e.getStateChange() == ItemEvent.SELECTED);
				}
			});
			buttonPane.add(shouldClose);
			{
				JButton okButton = new JButton("Run");
				okButton.setIcon(new ImageIcon(ConsoleDialog.class.getResource("/icons/system/ok.png")));
				okButton.setAction(okAction);
				okButton.setActionCommand("Run");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton();
				cancelButton.setIcon(new ImageIcon(ConsoleDialog.class.getResource("/icons/system/cancel.png")));
				cancelButton.setAction(cancelAction);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		setTitle("Execute...");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ConsoleDialog.class.getResource("/icons/tools/console.png")));
		setBounds(100, 100, 461, 230);
		InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel");
		ActionMap actionMap = getRootPane().getActionMap();
		actionMap.put("Cancel", cancelAction);
		
		consoleController = controller;
		controller.getAutocompletion().install(textArea);
	}
	
	/**
	 * 
	 */
	private void textInput() {
		consoleController.handleInput(textArea.getText().replaceAll("\n", ""));
		textArea.setText("");
	}

	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class CancelAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public CancelAction() {
			putValue(LARGE_ICON_KEY, new ImageIcon(ConsoleDialog.class.getResource("/icons/system/cancel.png")));
			putValue(NAME, "Cancel");
			putValue(SHORT_DESCRIPTION, "Cancel this dialog.");
		}
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
		}
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class OkAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public OkAction() {
			putValue(SMALL_ICON, null);
			putValue(LARGE_ICON_KEY, new ImageIcon(ConsoleDialog.class.getResource("/icons/system/ok.png")));
			putValue(NAME, "Run");
			putValue(SHORT_DESCRIPTION, "Execute the specified action.");
			putValue("RUN_AND_CLOSE", false);
		}
		public void actionPerformed(ActionEvent e) {
			textInput();
			if ((Boolean)getValue("RUN_AND_CLOSE"))
				dispose();
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
//		thistextField.setText((String) arg);
	}
	
	private JPanel negrada(int i) {
		JPanel cp = new JPanel(new BorderLayout());

		RSyntaxTextArea textArea = new RSyntaxTextArea();
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		RTextScrollPane sp = new RTextScrollPane(textArea);
		cp.add(sp);

		ObjectOrientedLanguageCompletionProvider oolp = new ObjectOrientedLanguageCompletionProvider();
		
		oolp.setAutoActivationRules(false, ".");
//		DefaultCompletionProvider oolp = new DefaultCompletionProvider();
		ObjectCompletion o1 = new ObjectCompletion(oolp, "Rectangle");
		ObjectCompletion o2 = new ObjectCompletion(oolp, "DrawingShape");
		
		ObjectMethodCompletion m1 = new ObjectMethodCompletion(oolp, "frame", o2.getInputText(), o2);
		ArrayList<Parameter> args = new ArrayList<Parameter>();
		args.add(new Parameter("String", "x"));
		args.add(new Parameter("String", "y"));
		args.add(new Parameter("String", "w"));
		args.add(new Parameter("String", "h"));
		m1.setParams(args);
		
		o1.addMethod(m1);
		
		ObjectMethodCompletion m2 = new ObjectMethodCompletion(oolp, "setAlpha", o2.getInputText(), o2);
		ArrayList<Parameter> args1 = new ArrayList<Parameter>();
		args1.add(new Parameter("String", "alpha"));
		m2.setParams(args1);
		
		o2.addMethod(m2);
		
		oolp.addCompletion(o1);

		AutoCompletion ac = new AutoCompletion(oolp);
		ac.setListCellRenderer(new CompletionCellRenderer());
		ac.setShowDescWindow(true);
		ac.setParameterAssistanceEnabled(true);
		ac.setAutoCompleteEnabled(true);
		ac.setAutoActivationEnabled(true);
		ac.setAutoActivationDelay(100);
		ac.setParameterAssistanceEnabled(true);

//		ac.setTriggerKey(KeyStroke.getKeyStroke('.'));
//		if (i == 0)
//			ac.install(thistextField);
//		else
			ac.install(textArea);
		
		return cp;
	}
}
