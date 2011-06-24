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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
import javax.swing.border.BevelBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ConsoleDialog extends JDialog implements IConsoleView{
	private static final long serialVersionUID = 1L;
	
	private final JPanel            contentPanel = new JPanel();
	private JTextArea               thistextField;
	private final Action            cancelAction = new CancelAction();
	private final Action            okAction = new OkAction();
	private IConsoleModelController consoleController;
	
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
		consoleController = controller;
		setTitle("Execute...");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ConsoleDialog.class.getResource("/icons/tools/console.png")));
		setBounds(100, 100, 467, 202);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		thistextField = new JTextArea();
		thistextField.setCaretColor(Color.GREEN);
		thistextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					textInput(thistextField);
			}
		});
		thistextField.setBorder(new BevelBorder(BevelBorder.LOWERED, null, Color.WHITE, null, Color.WHITE));
		thistextField.setForeground(Color.GREEN);
		thistextField.setBackground(new Color(0, 0, 0));
		thistextField.setRows(1);
		thistextField.setToolTipText("Try hitting Ctrl+Space to get some hints.");
		thistextField.setFont(new Font("SansSerif", Font.BOLD, 14));
		thistextField.setColumns(10);
		JScrollPane areaScrollPane = new JScrollPane(thistextField);
		areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(250, 250));
		areaScrollPane.setAutoscrolls(true);
		areaScrollPane.setWheelScrollingEnabled(true);
		
		JLabel lblNewLabel = new JLabel("Tools can be used through this command line. Try something like:");
		lblNewLabel.setFont(new Font("Dialog", Font.ITALIC, 11));
		lblNewLabel.setIcon(new ImageIcon(ConsoleDialog.class.getResource("/icons/system/information-balloon.png")));
		JLabel lblNewLabel_1 = new JLabel("Rectangle.square(10,20,30).setColor(0xff00aa)");
		lblNewLabel_1.setBackground(Color.PINK);
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("SansSerif", Font.BOLD, 10));
		lblNewLabel_1.setOpaque(true);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
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
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("");
				okButton.setIcon(new ImageIcon(ConsoleDialog.class.getResource("/icons/system/ok.png")));
				okButton.setAction(okAction);
				okButton.setActionCommand("OK");
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
			Set<KeyStroke> newForwardKeys = new HashSet<KeyStroke>();
			Set<KeyStroke> newBackwardKeys = new HashSet<KeyStroke>();
			newForwardKeys.add(KeyStroke.getKeyStroke("control TAB"));
			newBackwardKeys.add(KeyStroke.getKeyStroke("control shift TAB"));
			setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);
			setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, newBackwardKeys);
//			int i = 0;
//			if (i ==1)
//				this.setContentPane(negrada(i));
//			else
//				negrada(i);
			
			controller.getAutocompletion().install(thistextField);
		}
	}
	
	private void textInput(JTextArea thistextField2) {
		consoleController.handleInput(thistextField.getText());
		thistextField.setText("");
	}
	
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
	
	private class OkAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public OkAction() {
			putValue(SMALL_ICON, null);
			putValue(LARGE_ICON_KEY, new ImageIcon(ConsoleDialog.class.getResource("/icons/system/ok.png")));
			putValue(NAME, "Ok");
			putValue(SHORT_DESCRIPTION, "Execute the specified action.");
		}
		public void actionPerformed(ActionEvent e) {
			textInput(thistextField);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		thistextField.setText((String) arg);
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
		if (i == 0)
			ac.install(thistextField);
		else
			ac.install(textArea);
		
		return cp;
	}
}
