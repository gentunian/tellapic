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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.swingx.JXTable;

import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class MainDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public static final int CREATE_TAB    = 0;
	public static final int FAVOURITE_TAB = 1;	
	public static final int JOIN_TAB      = 2;

	private boolean userInput;
	private boolean fileSet;
	private boolean hostSet;
	private boolean portSet;
	private boolean userSet;
	private String  host;
	private String  name;
	private String  password;
	private String  port;
	private ActionListener deleteFavouriteListener;
	private ActionListener createPortFieldListener;
	private ActionListener favouriteJoinListener;
	private ActionListener favouriteExitListener;
	private ActionListener joinExitListener;
	private ActionListener joinJoinListener;
	private ActionListener joinAddFavouriteListener;

	
	public MainDialog(JFrame parent) {
		super(parent);
		createListeners();
		initComponents();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(Utils.msg.getString("MainDialog.0")); //$NON-NLS-1$
		setLocationByPlatform(true);
		setModal(true);
		setResizable(true);
		setModal(true);
		pack();
		setLocationRelativeTo(null);
//		setUndecorated(true);
		userInput = false;
		hostSet = false;
		fileSet = false;
		portSet = false;
		userSet = false;
	}
	
	
	/**
	 * 
	 */
	protected JRootPane createRootPane() {
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		Action actionListener = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent actionEvent) {
				MainDialog.this.dispose();
				Utils.shutdown();
			}
		};
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", actionListener);
		
		return rootPane;
	}
	
	
	/**
	 * 
	 */
	private void createListeners() {
		joinJoinListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				joinJoinButtonActionPerformed(evt);
			}
		};
		
		joinAddFavouriteListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (joinAddFavouriteButtonActionPerformed(evt)) {
					joinAddOkLabel.setText(Utils.msg.getString("favadded"));
					joinAddOkLabel.setVisible(true);
				} else {
					
				}
					
			}
		};
		
		deleteFavouriteListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				FavouritesTableModel model = (FavouritesTableModel) favouriteTable.getModel();
				int[] index = favouriteTable.getSelectedRows();
				Arrays.sort(index);
				for(int i = index.length - 1; i >= 0; i--)
					model.removeFavourite(index[i]);
			}
		};
		
		createPortFieldListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				createPortFieldActionPerformed(evt);
			}
		};
		
		favouriteExitListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				favouriteExitButtonActionPerformed(evt);
			}
		};
		
		favouriteJoinListener = new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				FavouritesTableModel model = (FavouritesTableModel) favouriteTable.getModel();
				host = (String) model.getValueAt(favouriteTable.getSelectedRow(), 0);
				port = (String) model.getValueAt(favouriteTable.getSelectedRow(), 1);
				name = (String) model.getValueAt(favouriteTable.getSelectedRow(), 2);
				password = (String) model.getValueAt(favouriteTable.getSelectedRow(), 3);
				userInput = true;
				MainDialog.this.dispose();
			}
		};
		
		joinExitListener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				joinExitButtonexit(evt);
			}
		};
	}
	
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	private void initComponents() {
		jTabbedPane1 = new JTabbedPane();
		jPanel1 = new JPanel();
		createImageArea = new JLabel();
		createCreateButton = new JButton();
		createExitButton = new JButton();
		createFileField = new JTextField();
		createFileLabel = new JLabel();
		createPasswordLabel = new JLabel();
		createPasswordField = new JPasswordField();
		createUsernameField = new JTextField();
		createUsernameLabel = new JLabel();
		createPortLabel = new JLabel();
		createPortField = new JTextField();
		jPanel3 = new JPanel();
		jScrollPane1 = new JScrollPane();
		favouriteTable = new JXTable();
		favouriteJoinButton = new JButton();
		favouriteExitButton = new JButton();
		favouriteDeleteButton = new JButton();
		jPanel2 = new JPanel();
		joinPasswordField = new JPasswordField();
		joinPasswordLabel = new JLabel();
		joinUsernameLabel = new JLabel();
		joinUsernameField = new JTextField();
		joinPortField = new JTextField();
		joinPortLabel = new JLabel();
		joinHostLabel = new JLabel();
		joinHostField = new JTextField();
		joinExitButton = new JButton();
		joinJoinButton = new JButton();
		joinAddFavouriteButton = new JButton();
		joinAddOkLabel = new JLabel();
		
		joinAddOkLabel.setIcon(new ImageIcon(Utils.createIconImage(16, 16, "/icons/system/information-balloon.png")));
		joinAddOkLabel.setVisible(false);
		
		/* Listeners */
		favouriteDeleteButton.addActionListener(deleteFavouriteListener);
		createPortField.addActionListener(createPortFieldListener);
		
		jTabbedPane1.setBackground(SystemColor.inactiveCaption);
		jTabbedPane1.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		jTabbedPane1.setFocusable(false);
		jTabbedPane1.setMinimumSize(new Dimension(221, 390));

		jPanel1.setMaximumSize(new Dimension(221, 390));
		jPanel1.setMinimumSize(new Dimension(221, 390));

		createImageArea.setToolTipText(Utils.msg.getString("MainDialog.1")); //$NON-NLS-1$
		createImageArea.setBorder(BorderFactory.createEtchedBorder());
		createImageArea.setCursor(new Cursor(Cursor.HAND_CURSOR));
		createImageArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				createImageAreaMouseClicked(evt);
			}
		});

		createCreateButton.setText(Utils.msg.getString("MainDialog.2")); //$NON-NLS-1$
		createCreateButton.setEnabled(false);
		createExitButton.setText(Utils.msg.getString("MainDialog.3")); //$NON-NLS-1$
		createExitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				exit(evt);
			}
		});

		createFileField.setEditable(false);
		createFileField.setHorizontalAlignment(SwingConstants.LEFT);
		createFileField.setToolTipText(Utils.msg.getString("MainDialog.4")); //$NON-NLS-1$
		createFileField.setCursor(new Cursor(Cursor.TEXT_CURSOR));
		createFileField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				createFileFieldMouseClicked(evt);
			}
		});

		createFileLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		createFileLabel.setText(Utils.msg.getString("MainDialog.5")); //$NON-NLS-1$
		createPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		createPasswordLabel.setText(Utils.msg.getString("MainDialog.6")); //$NON-NLS-1$
		createPasswordField.setToolTipText(Utils.msg.getString("MainDialog.7")); //$NON-NLS-1$
		createUsernameField.setToolTipText(Utils.msg.getString("MainDialog.8")); //$NON-NLS-1$
		createUsernameField.setInputVerifier(new NonEmptyVerifier());
		createUsernameField.setName(Utils.msg.getString("MainDialog.9")); // NOI18N //$NON-NLS-1$
		createUsernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		createUsernameLabel.setText(Utils.msg.getString("MainDialog.10")); //$NON-NLS-1$
		createPortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		createPortLabel.setText(Utils.msg.getString("MainDialog.11")); //$NON-NLS-1$
		createPortField.setToolTipText(Utils.msg.getString("MainDialog.12")); //$NON-NLS-1$
		createPortField.setInputVerifier(new PortVerifier());
		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(
				jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(createImageArea, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 318, GroupLayout.PREFERRED_SIZE)
								.addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
										.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
												.addComponent(createPortLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
												.addComponent(createFileLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
												.addComponent(createPasswordLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
												.addComponent(createUsernameLabel, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
														.addComponent(createPasswordField)
														.addComponent(createPortField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
														.addComponent(createUsernameField, GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
														.addComponent(createFileField, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)))
														.addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
																.addContainerGap()
																.addComponent(createExitButton, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(createCreateButton, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)))
																.addContainerGap())
		);
		jPanel1Layout.setVerticalGroup(
				jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel1Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(createPortField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(createPortLabel))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(createUsernameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(createUsernameLabel))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
												.addComponent(createPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(createPasswordLabel))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
														.addComponent(createFileField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(createFileLabel, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(createImageArea, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
														.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																.addComponent(createExitButton, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
																.addComponent(createCreateButton, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
																.addContainerGap())
		);

		jTabbedPane1.addTab(Utils.msg.getString("MainDialog.13"), jPanel1); //$NON-NLS-1$
		jPanel3.setMinimumSize(new Dimension(221, 390));
		favouriteTable.setModel(new FavouritesTableModel());
		favouriteTable.setEditable(true);
		favouriteTable.setColumnSelectionAllowed(false);
		favouriteTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		favouriteTable.getTableHeader().setReorderingAllowed(false);
		favouriteTable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		favouriteTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int count = favouriteTable.getSelectedRowCount();
				if (count > 1) {
					favouriteJoinButton.setEnabled(false);
				} else 
					favouriteJoinButton.setEnabled(true);
			}
		});
		
		jScrollPane1.setViewportView(favouriteTable);
		favouriteJoinButton.setText(Utils.msg.getString("MainDialog.18")); //$NON-NLS-1$
		favouriteJoinButton.setEnabled(false);
		favouriteJoinButton.addActionListener(favouriteJoinListener);
		favouriteExitButton.setText(Utils.msg.getString("MainDialog.19")); //$NON-NLS-1$
		favouriteExitButton.addActionListener(favouriteExitListener);
		favouriteDeleteButton.setText(Utils.msg.getString("MainDialog.20")); //$NON-NLS-1$

		GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
		jPanel3.setLayout(jPanel3Layout);
		jPanel3Layout.setHorizontalGroup(
				jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel3Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
										.addComponent(favouriteExitButton, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
										.addComponent(favouriteDeleteButton, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(favouriteJoinButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
										.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE))
										.addContainerGap())
		);
		jPanel3Layout.setVerticalGroup(
				jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
						.addContainerGap()
						.addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 308, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
						.addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(favouriteJoinButton)
								.addComponent(favouriteExitButton)
								.addComponent(favouriteDeleteButton))
								.addContainerGap())
		);

		jTabbedPane1.addTab(Utils.msg.getString("MainDialog.21"), jPanel3); //$NON-NLS-1$
		jPanel2.setMinimumSize(new Dimension(221, 390));
		joinPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		joinPasswordLabel.setText(Utils.msg.getString("MainDialog.22")); //$NON-NLS-1$
		joinUsernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		joinUsernameLabel.setText(Utils.msg.getString("MainDialog.23")); //$NON-NLS-1$
		joinUsernameField.setInputVerifier(new NonEmptyVerifier());
		joinUsernameField.setName(Utils.msg.getString("MainDialog.24")); // NOI18N //$NON-NLS-1$
		joinPortField.setInputVerifier(new PortVerifier());
		joinPortLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		joinPortLabel.setText(Utils.msg.getString("MainDialog.25")); //$NON-NLS-1$
		joinHostLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		joinHostLabel.setText(Utils.msg.getString("MainDialog.26")); //$NON-NLS-1$
		joinHostField.setInputVerifier(new NonEmptyVerifier());
		joinHostField.setName(Utils.msg.getString("MainDialog.27")); // NOI18N //$NON-NLS-1$
		joinHostField.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent e) {
				joinAddOkLabel.setVisible(false);
			}
		});
		joinExitButton.setText(Utils.msg.getString("MainDialog.28")); //$NON-NLS-1$
		joinExitButton.addActionListener(joinExitListener);
		joinPortField.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent e) {
				joinAddOkLabel.setVisible(false);
			}
		});
		joinUsernameField.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent e) {
				joinAddOkLabel.setVisible(false);
			}
		});
		joinPasswordField.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent e) {
				joinAddOkLabel.setVisible(false);
			}
		});
		joinJoinButton.setText(Utils.msg.getString("MainDialog.29")); //$NON-NLS-1$
		joinJoinButton.setEnabled(false);
		joinJoinButton.addActionListener(joinJoinListener);
		joinAddFavouriteButton.setText(Utils.msg.getString("MainDialog.30")); //$NON-NLS-1$
		joinAddFavouriteButton.setEnabled(false);
		joinAddFavouriteButton.addActionListener(joinAddFavouriteListener);

		GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
		jPanel2.setLayout(jPanel2Layout);
		jPanel2Layout.setHorizontalGroup(
				jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addGroup(jPanel2Layout.createSequentialGroup()
										.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
												.addComponent(joinUsernameLabel, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
												.addComponent(joinPasswordLabel, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
												.addComponent(joinPortLabel, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE)
												.addComponent(joinHostLabel, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
												.addGap(22, 22, 22)
												.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
														.addComponent(joinAddOkLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
														.addComponent(joinAddFavouriteButton, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
														.addComponent(joinHostField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
														.addComponent(joinUsernameField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
														.addComponent(joinPasswordField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
														.addComponent(joinPortField, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)))
														.addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
																.addComponent(joinExitButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
																.addComponent(joinJoinButton, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE)))
																.addContainerGap())
		);
		jPanel2Layout.setVerticalGroup(
				jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(jPanel2Layout.createSequentialGroup()
						.addGap(16, 16, 16)
						.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(joinHostField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(joinHostLabel, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(joinPortField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(joinPortLabel))
										.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
												.addComponent(joinUsernameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(joinUsernameLabel))
												.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
												.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
														.addComponent(joinPasswordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
														.addComponent(joinPasswordLabel))
														.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(joinAddFavouriteButton)
														.addComponent(joinAddOkLabel)
														.addGap(257, 257, 257)
														.addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
																.addComponent(joinExitButton)
																.addComponent(joinJoinButton)))
		);

		jTabbedPane1.addTab(Utils.msg.getString("MainDialog.31"), jPanel2); //$NON-NLS-1$
		jTabbedPane1.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				int currentTabIndex = ((JTabbedPane)e.getSource()).getSelectedIndex();

				switch(currentTabIndex) {
				
				case CREATE_TAB:
					createPortField.requestFocus(true);
					break;
					
				case JOIN_TAB:
					joinHostField.requestFocus(true);
					break;
				}
			}
		});
		getContentPane().add(jTabbedPane1, BorderLayout.CENTER);
	}

	
	/*
	 * 
	 */
	private void createPortFieldActionPerformed(ActionEvent evt) {
		// TODO add your handling code here:
	}

	
	/*
	 * 
	 */
	private void createFileFieldMouseClicked(MouseEvent evt) {
		createImageAreaMouseClicked(evt);
	}

	
	/**
	 * 
	 * @param evt
	 */
	private void exit(ActionEvent evt) {
		MainDialog.this.dispose();
	}

	
	/*
	 * 
	 */
	private void joinExitButtonexit(ActionEvent evt) {
		MainDialog.this.dispose();
	}

	
	/*
	 * 
	 */
	private void joinJoinButtonActionPerformed(ActionEvent evt) {
		host = joinHostField.getText();
		name = joinUsernameField.getText();
		password = String.valueOf(joinPasswordField.getPassword());
		port = joinPortField.getText();
		userInput = true;
		MainDialog.this.dispose();
		
	}

	
	/*
	 * 
	 */
	private void favouriteExitButtonActionPerformed(ActionEvent evt) {
		MainDialog.this.dispose();
	}

	
	/*
	 * 
	 */
	private void createImageAreaMouseClicked(MouseEvent evt) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(Utils.msg.getString("MainDialog.32"), Utils.msg.getString("MainDialog.33"), Utils.msg.getString("MainDialog.34")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println(Utils.msg.getString("MainDialog.35")+chooser.getSelectedFile().getName()); //$NON-NLS-1$
			createFileField.setText(chooser.getSelectedFile().getAbsolutePath());
			fileSet = true;
			ImageIcon icon = new ImageIcon(chooser.getSelectedFile().getAbsolutePath());
			Image     img  = icon.getImage().getScaledInstance(createImageArea.getWidth(), createImageArea.getHeight(), Image.SCALE_SMOOTH);
			icon.setImage(img);
			createImageArea.setIcon(icon);
		} else if ( returnVal == JFileChooser.CANCEL_OPTION) {
			fileSet = false;
		}
		enableButton(jTabbedPane1.getSelectedIndex());
	}

	
	/*
	 * 
	 */
	private boolean joinAddFavouriteButtonActionPerformed(ActionEvent evt) {
		FavouritesTableModel model = (FavouritesTableModel) favouriteTable.getModel();
		return model.addFavourite(
				joinHostField.getText(),
				Integer.parseInt(joinPortField.getText()),
				joinUsernameField.getText(),
				String.valueOf(joinPasswordField.getPassword())
		);
		
	}



	private JButton createCreateButton;
	private JButton createExitButton;
	private JTextField createFileField;
	private JLabel createFileLabel;
	private JLabel createImageArea;
	private JPasswordField createPasswordField;
	private JLabel createPasswordLabel;
	private JTextField createPortField;
	private JLabel createPortLabel;
	private JTextField createUsernameField;
	private JLabel createUsernameLabel;
	private JButton favouriteDeleteButton;
	private JButton favouriteExitButton;
	private JButton favouriteJoinButton;
	private JXTable favouriteTable;
	private JPanel jPanel1;
	private JPanel jPanel2;
	private JPanel jPanel3;
	private JScrollPane jScrollPane1;
	private JTabbedPane jTabbedPane1;
	private JButton joinAddFavouriteButton;
	private JButton joinExitButton;
	private JTextField joinHostField;
	private JLabel joinHostLabel;
	private JButton joinJoinButton;
	private JPasswordField joinPasswordField;
	private JLabel joinPasswordLabel;
	private JTextField joinPortField;
	private JLabel joinPortLabel;
	private JTextField joinUsernameField;
	private JLabel joinUsernameLabel;
	private JLabel joinAddOkLabel;

	/*
	 * 
	 */
	class PortVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent input) {
			JTextField tf = (JTextField) input;
			int port = 0;
			if (isNumeric(tf.getText())) {
				port = Integer.parseInt(tf.getText());
				if (port <= 1024) {
					tf.setBackground(Color.orange); //TODO: this shouldn't be done here. implemente shouldYieldFocus() instead
					portSet = false;
				}
				else {
					tf.setBackground(Color.white); //TODO: this shouldn't be done here. implemente shouldYieldFocus() instead
					portSet = true;
				}
			}
			else {
				tf.setBackground(Color.orange); //TODO: this shouldn't be done here. implemente shouldYieldFocus() instead
				portSet = false;
			}
			enableButton(jTabbedPane1.getSelectedIndex());  //TODO: this shouldn't be done here. implemente shouldYieldFocus() instead
			return portSet;
		}
	}

	
	/*
	 * 
	 */
	private void enableButton(int tabSelected) {
		switch (tabSelected) {
		case CREATE_TAB:
			createCreateButton.setEnabled((portSet && userSet && fileSet));
			break;

		case FAVOURITE_TAB:

			break;

		case JOIN_TAB:
			boolean value = (hostSet && portSet && userSet);
			joinAddFavouriteButton.setEnabled(value);
			joinJoinButton.setEnabled(value);
		}
	}

	/*
	 * 
	 */
	class NonEmptyVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent input) {
			JTextField tf = (JTextField) input;
			boolean ret = false;
			if (tf.getText().length() == 0) {
				tf.setBackground(Color.orange); //TODO: this shouldn't be done here. implemente shouldYieldFocus() instead
				if (tf.getName().equals(Utils.msg.getString("MainDialog.36")) || tf.getName().equals(Utils.msg.getString("MainDialog.37"))) //$NON-NLS-1$ //$NON-NLS-2$
					userSet = false;
				else
					hostSet = false;
			} else {
				tf.setBackground(Color.white); //TODO: this shouldn't be done here. implemente shouldYieldFocus() instead
				ret = true;
				if (tf.getName().equals(Utils.msg.getString("MainDialog.38")) || tf.getName().equals(Utils.msg.getString("MainDialog.39"))) //$NON-NLS-1$ //$NON-NLS-2$
					userSet = true;
				else
					hostSet = true;
			}
			enableButton(jTabbedPane1.getSelectedIndex()); //TODO: this shouldn't be done here. implemente shouldYieldFocus() instead
			return ret;
		}
	}

	
	/*
	 * 
	 */
	private static boolean isNumeric(String aStringValue) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher matcher = pattern.matcher(aStringValue);
		return matcher.matches();
	}

	
	/**
	 * @return
	 */
	public boolean isUserInput() {
		return userInput;
	}

	/**
	 * @return
	 */
	public String getRemoteHost() {
		return host;
	}

	/**
	 * @return
	 */
	public String getRemotePort() {
		return port;
	}

	/**
	 * @return
	 */
	public String getUsername() {
		return name;
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 */
	public int getOption() {
		return jTabbedPane1.getSelectedIndex();
	}
}