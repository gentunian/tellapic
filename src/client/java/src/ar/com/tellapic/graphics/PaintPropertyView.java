package ar.com.tellapic.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ar.com.tellapic.utils.Utils;

public class PaintPropertyView extends JPanel implements Observer {
	
	private static final long serialVersionUID = 1L;
	private static final int ICON_SIZE = 18;
	private static final int SEPARATOR_HEIGHT = ICON_SIZE;
	private static final int SEPARATOR_WIDTH  = 3;
	private static final String BEVEL_JOIN_ICON_PATH  = "/icons/joinbevel.png";
	private static final String ROUND_JOIN_ICON_PATH  = "/icons/joinround.png";
	private static final String MITER_JOIN_ICON_PATH  = "/icons/joinmiter.png";
	private static final String ROUND_END_ICON_PATH   = "/icons/capround.png";
	private static final String BUTT_END_ICON_PATH    = "/icons/capbutt.png";
	private static final String SQUARE_END_ICON_PATH  = "/icons/capsquare.png";
	private static final String SET_CAP_ROUND_ACTION  = "capround";
	private static final String SET_CAP_SQUARE_ACTION = "capsquare";
	private static final String SET_CAP_BUTT_ACTION   = "capbutt";
	private static final String SET_CAPS_ACTION   = "setcap";
	private static final String SET_JOINS_ACTION = "setjoin";
	private static final String SET_JOIN_BEVEL_ACTION = "joinbevel";
	private static final String SET_JOIN_ROUND_ACTION = "joinround";
	private static final String SET_JOIN_MITER_ACTION = "joinmiter";
	private static final String SET_FONT_FACE_ACTION  = "face";
	private static final String SET_FONT_SIZE_ACTION  = "size";
	private static final String SET_FONT_STYLE_ACTION = "style";
	private static final String[] FONT_STYLES = new String[] { "normal", "<html><b>bold</b></html>", "<html><i>italic</i></html>", "<html><b><i>italic+bold</b></i></html>" };
	
	private static final Color    DEFAULT_COLOR = Color.black;
	private static final int      DEFAULT_END_CAPS = BasicStroke.CAP_SQUARE;
	private static final String   DEFAULT_FONT_FACE = "Serif";
	private static final int      DEFAULT_FONT_SIZE = 10;
	private static final int      DEFAULT_FONT_STYLE = Font.PLAIN;
	private static final int      DEFAULT_LINE_JOIN = BasicStroke.JOIN_MITER;
	private static final double   DEFAULT_OPACITY = 100;
	private static final double   DEFAULT_WIDTH = 5.0;
	
	private final Font            defaultFont = Font.decode("Droid-10");
	
//	private JComboBox widthCombo;
	private JSpinner  widthSpinner;
	
//	private JComboBox opacityCombo;
	private JSpinner opacitySpinner;
	private JComboBox dashCombo;
	private JComboBox capsCombo;
	private JComboBox joinCombo;
	private JLabel toolIcon;
	private JLabel widthLabel;
	private JLabel opacityLabel;
	private JLabel dashLabel;
	private JLabel capsLabel;
	private JLabel joinLabel;
	private JLabel fontFaceLabel;
	private JLabel fontSizeLabel;
	private JLabel fontStyleLabel;
	private JLabel textLabel;
	private JLabel colorLabel;
	private JLabel colorField;
	private JSeparator jSeparator1;
	private JSeparator jSeparator2;
	private JSeparator jSeparator3;
	private JSeparator jSeparator4;
	private JSeparator jSeparator5;
	private GroupLayout layout;
	private JComboBox fontFaceCombo;
	private JComboBox fontSizeCombo;
	private JComboBox fontStyleCombo;
	private JTextField textField;
	private IPaintPropertyController controller;
	private ComboListener comboListener;

	
	/** Creates new form ToolView */
	public PaintPropertyView() {
		setName(Utils.msg.getString("propertyview"));
		
		layout      = new GroupLayout(this);
		comboListener = new ComboListener();

		createSeparators();
		
		Dimension iconDimension = new Dimension(ICON_SIZE, ICON_SIZE);
		toolIcon = new JLabel();
		toolIcon.setMaximumSize(iconDimension);
		toolIcon.setMinimumSize(iconDimension);
		toolIcon.setPreferredSize(iconDimension);
		
		/* Creates the options for customize stroked shapes */
		createStrokeOptions();
		
		/* Creates the options for customize text */
		createFontOptions();
		
		/**/
		createColorOptions();
		
		setPreferredSize(new Dimension(3200, ICON_SIZE*2));
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	}


	/**
	 * 
	 */
	private void createFontOptions() {
		Dimension comboDimension = new Dimension(100, ICON_SIZE);
		Integer[]    fontSizes = new Integer[140 - 8];
		
		for(int i = 8; i < 140; i++)
			fontSizes[i - 8] = i;
		
		fontFaceCombo  = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		fontSizeCombo  = new JComboBox(fontSizes);
		fontStyleCombo = new JComboBox(FONT_STYLES);
		textField      = new JTextField();
		textLabel      = new JLabel(Utils.msg.getString("text")+":");
		fontFaceLabel  = new JLabel(Utils.msg.getString("fontface")+":");
		fontSizeLabel  = new JLabel(Utils.msg.getString("fontsize")+":");
		fontStyleLabel = new JLabel(Utils.msg.getString("fontstyle")+":");

		
		int max = 0;
		FontMetrics  metrics = fontFaceCombo.getFontMetrics(defaultFont);
		for(String item : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
			if (metrics.stringWidth(item) > max)
				max = metrics.stringWidth(item);
		}
		Dimension faceComboDimension = new Dimension(max, ICON_SIZE);
		
		textField.setPreferredSize(new Dimension(Short.MAX_VALUE, ICON_SIZE));
		textField.setFont(defaultFont);
		textLabel.setFont(defaultFont);
		fontFaceLabel.setFont(defaultFont);
		fontStyleLabel.setFont(defaultFont);
		fontSizeLabel.setFont(defaultFont);
		fontFaceCombo.setPreferredSize(faceComboDimension);
		fontStyleCombo.setPreferredSize(comboDimension);
		fontSizeCombo.setPreferredSize(new Dimension(50, ICON_SIZE));
		fontFaceCombo.setFont(defaultFont);
		fontStyleCombo.setFont(defaultFont);
		fontSizeCombo.setFont(defaultFont);
		fontFaceCombo.setActionCommand(SET_FONT_FACE_ACTION);
		fontSizeCombo.setActionCommand(SET_FONT_SIZE_ACTION);
		fontStyleCombo.setActionCommand(SET_FONT_STYLE_ACTION);
		fontFaceCombo.addActionListener(comboListener);
		fontSizeCombo.addActionListener(comboListener);
		fontStyleCombo.addActionListener(comboListener);
		textField.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent arg0) {
				if (controller != null)
					controller.handleTextChange(((JTextField) arg0.getSource()).getText());
			}
		});
	}


	/**
	 * 
	 */
	private void createStrokeOptions() {
		joinLabel    = new JLabel(Utils.msg.getString("join")+":");
		widthLabel   = new JLabel(Utils.msg.getString("width")+":");
		opacityLabel = new JLabel(Utils.msg.getString("opacity")+":");
		capsLabel    = new JLabel(Utils.msg.getString("caps")+":");
		dashLabel    = new JLabel(Utils.msg.getString("dash")+":");
		widthSpinner = new JSpinner(new SpinnerNumberModel((double)DEFAULT_WIDTH, 0.1, 70.0, 0.1));
		opacitySpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_OPACITY, 1, 100, 0.1));
		joinCombo    = new JComboBox(new Integer[] {0,1,2});
		capsCombo    = new JComboBox(new Integer[] {0,1,2});
		dashCombo    = new JComboBox();
		
		
		LabelComboBoxRenderer capsRenderer = new LabelComboBoxRenderer(
				capsCombo.getBorder(),
				new String[] {
						BUTT_END_ICON_PATH,
						ROUND_END_ICON_PATH,
						SQUARE_END_ICON_PATH
				},
				new String[] {
						"Butt",
						"Round",
						"Square"
				});
		LabelComboBoxRenderer joinRenderer = new LabelComboBoxRenderer(
				joinCombo.getBorder(),
				new String[] {
						MITER_JOIN_ICON_PATH,
						ROUND_JOIN_ICON_PATH,
						BEVEL_JOIN_ICON_PATH
				},
				new String[] {
						"Miter",
						"Round",
						"Bevel"
				});

		Dimension comboDimension = new Dimension(100, ICON_SIZE);
		
		capsCombo.setRenderer(capsRenderer);
		joinCombo.setRenderer(joinRenderer);
		capsCombo.setEditable(false);
		joinCombo.setEditable(false);
		widthSpinner.setPreferredSize(new Dimension(50, ICON_SIZE));
		opacitySpinner.setPreferredSize(new Dimension(50, ICON_SIZE));
		dashCombo.setPreferredSize(comboDimension);
		opacitySpinner.setFont(defaultFont);
		widthSpinner.setFont(defaultFont);
		widthLabel.setFont(defaultFont);
		opacityLabel.setFont(defaultFont);
		joinLabel.setFont(defaultFont);
		capsLabel.setFont(defaultFont);
		dashLabel.setFont(defaultFont);
		
		capsCombo.setActionCommand(SET_CAPS_ACTION);
		joinCombo.setActionCommand(SET_JOINS_ACTION);
		joinCombo.addActionListener(comboListener);
		capsCombo.addActionListener(comboListener);
		dashCombo.addActionListener(comboListener);
		widthSpinner.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				if (controller != null)
					controller.handleWidthChange((Double)((JSpinner)e.getSource()).getValue());
			}
		});
		
		opacitySpinner.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				if (controller != null) {
					double value = (Double)((JSpinner)e.getSource()).getValue();
					controller.handleOpacityChange(value/100);
				}
			}
		});
	}


	/**
	 * 
	 */
	private void createColorOptions(){
		colorLabel = new JLabel(Utils.msg.getString("color")+":");
		colorField = new JLabel();
		
		colorLabel.setLabelFor(colorField);
		colorLabel.setFont(defaultFont);
		colorField.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		colorField.setOpaque(true);
		colorField.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(209, 209, 209), 1, true), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
		colorField.setBackground(Color.black);
		colorField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		colorField.setToolTipText(Utils.msg.getString("colorfieldtooltip"));
		colorField.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JLabel label = (JLabel)e.getSource();
				Color c = JColorChooser.showDialog(PaintPropertyView.this, "Pick a Color", label.getBackground()); 
				if (c != null) {
					controller.handleColorChange(c);
					label.setBackground(c);
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	
	/**
	 * 
	 */
	private void createSeparators() {
		Dimension separatorDimension = new Dimension(SEPARATOR_WIDTH, SEPARATOR_HEIGHT);
		jSeparator1 = new JSeparator();
		jSeparator2 = new JSeparator();
		jSeparator3 = new JSeparator();
		jSeparator4 = new JSeparator();
		jSeparator5 = new JSeparator();
		
		jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
		jSeparator1.setMaximumSize(separatorDimension);
		jSeparator1.setMinimumSize(separatorDimension);
		jSeparator1.setPreferredSize(separatorDimension);
		jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
		jSeparator2.setMaximumSize(separatorDimension);
		jSeparator2.setMinimumSize(separatorDimension);
		jSeparator2.setPreferredSize(separatorDimension);
		jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
		jSeparator3.setMaximumSize(separatorDimension);
		jSeparator3.setMinimumSize(separatorDimension);
		jSeparator3.setPreferredSize(separatorDimension);
		jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
		jSeparator4.setMaximumSize(separatorDimension);
		jSeparator4.setMinimumSize(separatorDimension);
		jSeparator4.setPreferredSize(separatorDimension);
		jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
		jSeparator5.setMaximumSize(separatorDimension);
		jSeparator5.setMinimumSize(separatorDimension);
		jSeparator5.setPreferredSize(separatorDimension);
	}


	/**
	 * Show panel for tool tool.
	 * @param tool
	 */
	public void showPanel(Tool tool) {
		remove(toolIcon);
		remove(jSeparator1);
		remove(jSeparator2);
		remove(jSeparator3);
		remove(jSeparator4);
		remove(jSeparator5);
		remove(widthLabel);
		remove(opacityLabel);
		remove(capsLabel);
		remove(dashLabel);
		remove(joinLabel);
		remove(fontFaceLabel);
		remove(fontSizeLabel);
		remove(fontStyleLabel);
		remove(textLabel);
		remove(widthSpinner);
		remove(capsCombo);
		remove(joinCombo);
		remove(opacitySpinner);
		remove(dashCombo);
		remove(fontFaceCombo);
		remove(fontSizeCombo);
		remove(fontStyleCombo);
		remove(textField);
		remove(colorLabel);
		remove(colorField);
		
//		GroupLayout.ParallelGroup   hParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		GroupLayout.ParallelGroup   vParallelGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		GroupLayout.SequentialGroup hSequentialGroup = layout.createSequentialGroup();
//		GroupLayout.SequentialGroup vSequentialGroup = layout.createSequentialGroup();
		createIconImage(ICON_SIZE, ICON_SIZE, tool.getIconPath());
		toolIcon.setIcon(new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, tool.getIconPath())));
		
		hSequentialGroup.addContainerGap()
		.addComponent(toolIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		.addComponent(jSeparator1, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
		;
		vParallelGroup
		.addComponent(toolIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		.addComponent(jSeparator1, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
		;
		
		if (tool.hasColorProperties()) {
			hSequentialGroup
			.addComponent(colorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(colorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator2, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
			
			vParallelGroup
			.addComponent(colorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(colorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator2, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
		}
		if (tool.hasAlphaProperties()) {
			hSequentialGroup
			.addComponent(opacityLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(opacitySpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator3, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
			
			vParallelGroup
			.addComponent(opacityLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(opacitySpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator3, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
		}

		if (tool.hasStrokeProperties()) {
			hSequentialGroup
			.addComponent(widthLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(widthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator3, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(capsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(capsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator4, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(dashLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(dashCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator4, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
			
			vParallelGroup
			.addComponent(widthLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(widthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator3, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(capsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(capsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator4, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(dashLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(dashCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator4, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
		}
		
		
		if (tool.hasFontProperties()) {
			hSequentialGroup
			.addComponent(fontFaceLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontFaceCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontSizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontSizeCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontStyleLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontStyleCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(textField, GroupLayout.PREFERRED_SIZE, 157, Short.MAX_VALUE)
			.addContainerGap()
			;
			
			vParallelGroup
			.addComponent(fontFaceLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontFaceCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontSizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontSizeCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontStyleLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(fontStyleCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			;
		}
		
		layout.setHorizontalGroup(hSequentialGroup);
		layout.setVerticalGroup(vParallelGroup);
	}
	
//	
//	private void setStrokeProperties(PaintPropertyStroke strokeProperty) {
//		int capsValue  = strokeProperty.getEndCaps();
//		int joinValue  = strokeProperty.getLineJoins();
//		int widthValue = (int) strokeProperty.getWidth();
//		for(Component item : strokePanel.getComponents()) {
//			if (item instanceof JSlider) {
//				JSlider slider = ((JSlider) item);
//				if (slider.getName().equals("width"))
//					slider.setValue(widthValue);
//				
//			} else if (item instanceof JToggleButton) {
//				JToggleButton button = ((JToggleButton) item);
//				if      (button.getActionCommand().equals(SET_CAP_BUTT_ACTION) && capsValue == BasicStroke.CAP_BUTT && !button.isSelected())
//					button.doClick();
//				else if (button.getActionCommand().equals(SET_CAP_ROUND_ACTION) && capsValue == BasicStroke.CAP_ROUND && !button.isSelected())
//					button.doClick();
//				else if (button.getActionCommand().equals(SET_CAP_SQUARE_ACTION) && capsValue == BasicStroke.CAP_SQUARE && !button.isSelected())
//					button.doClick();
//				else if (button.getActionCommand().equals(SET_JOIN_BEVEL_ACTION) && joinValue == BasicStroke.JOIN_BEVEL && !button.isSelected())
//					button.doClick();
//				else if (button.getActionCommand().equals(SET_JOIN_MITER_ACTION) && joinValue == BasicStroke.JOIN_MITER && !button.isSelected())
//					button.doClick();
//				else if (button.getActionCommand().equals(SET_JOIN_ROUND_ACTION) && joinValue == BasicStroke.JOIN_ROUND && !button.isSelected())
//					button.doClick();
//			}
//		}
//	}
//	
//	private void setFontProperties(PaintPropertyFont fontProperty) {
//		
//	}
//	
//	private void setColorProperties(PaintPropertyColor colorProperty) {
//	
//	}
//	
//	private void setAlphaProperties(PaintPropertyAlpha opacityProperty) {
//	
//	}
	
	/**
	 * 
	 * @param c the controller to set
	 */
	public void setController(IPaintPropertyController c) {
		controller = c;
		
		setDefaultProperties();
	}
	

	private void setDefaultProperties() {
		capsCombo.setSelectedIndex(DEFAULT_END_CAPS);
		joinCombo.setSelectedIndex(DEFAULT_LINE_JOIN);
		widthSpinner.setValue(DEFAULT_WIDTH);
		opacitySpinner.setValue(DEFAULT_OPACITY);
		controller.handleColorChange(DEFAULT_COLOR);
//		controller.handleEndCapsChange(DEFAULT_END_CAPS);
//		controller.handleFontFaceChange(DEFAULT_FONT_FACE);
//		controller.handleFontSizeChange(DEFAULT_FONT_SIZE);
//		controller.handleFontStyleChange(DEFAULT_FONT_STYLE);
//		controller.handleLineJoinsChange(DEFAULT_LINE_JOIN);
		controller.handleOpacityChange(DEFAULT_OPACITY);
		controller.handleWidthChange(DEFAULT_WIDTH);
	}
	
	
	 
	public void setColorPanelEnabled(boolean enabled) {
//		Utils.logMessage("Enabling color panel: "+enabled+" current size: ("+getSize().width+","+getSize().height+")");
//		int h = getSize().height + (colorPanelDimension.height * (enabled? 1 : -1) );
//		Utils.logMessage("new height calculated: "+h);
//		if (enabled)
//			add(colorPanel);
//		else
//			remove(colorPanel);
//		Utils.logMessage("new size after adding item: ("+getSize().width+","+getSize().height+")");
//		
//		setMinimumSize(new Dimension(getSize().width, h));
//		printSizes();
//		this.validate();
	}
	
	
//	private Dimension createOpacityPanel() {
//		JLabel        opacityLabel = new JLabel("Opacity:");
//		JSlider       opacity      = new JSlider();
//		FontMetrics   metrics = opacityLabel.getFontMetrics(mainFont);
//		SliderChangeListener listener    = new SliderChangeListener();
//		
//		opacity.setPaintTicks(true);
//		opacity.setName("opacity");
//		opacity.addChangeListener(listener);
//		opacity.setMinorTickSpacing(10);
//		opacity.setValue(100);
//		opacityLabel.setLabelFor(opacity);
//		opacityLabel.setFont(mainFont);
//		opacityLabel.setMinimumSize(new Dimension(metrics.stringWidth(opacityLabel.getText()) + 8, metrics.getHeight()));
//		
//		GroupLayout layout = new GroupLayout(opacityPanel);
//		opacityPanel.setLayout(layout);
//		layout.setAutoCreateGaps(true);
//		layout.setAutoCreateContainerGaps(true);
//		layout.setVerticalGroup(
//				layout.createSequentialGroup()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//						.addComponent(opacityLabel)
//						.addComponent(opacity)
//				)
//		);
//		layout.setHorizontalGroup(
//				layout.createSequentialGroup()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//						.addComponent(opacityLabel)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//						.addComponent(opacity)
//				)
//		);
//		
//		opacityPanel.setMinimumSize(layout.minimumLayoutSize(opacityPanel));
//		return opacityPanel.getMinimumSize();
//	}
//	 
//	
//	private Dimension createColorPanel() {
//		JLabel  foregroundColor = new JLabel();
//		JLabel  backgroundColor = new JLabel();
//		Dimension minPanelSize  = new Dimension(50, 50);
//		Dimension labelSize     = new Dimension(40, 30);	
//		
//		foregroundColor.setMinimumSize(labelSize);
//		foregroundColor.setPreferredSize(labelSize);
//		foregroundColor.setBackground(Color.black);
//		foregroundColor.setOpaque(true);
//		foregroundColor.setBorder(BorderFactory.createLoweredBevelBorder());
//		backgroundColor.setMinimumSize(labelSize);
//		backgroundColor.setPreferredSize(labelSize);
//		backgroundColor.setBackground(Color.white);
//		backgroundColor.setOpaque(true);
//		backgroundColor.setBorder(BorderFactory.createLoweredBevelBorder());
//		colorPanel.add(foregroundColor);
//		colorPanel.add(backgroundColor);
//		
//		foregroundColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//		foregroundColor.addMouseListener(new ColorChangeListener());
//		
//		return minPanelSize;
//	}
//
//	
//	private Dimension createFontPanel() {
//		//TODO: make constants
//		Integer[] fontSizes = new Integer[140 - 8];
//		for(int i = 8; i < 140; i++)
//			fontSizes[i - 8] = i;
//		JLabel       sizeLabel      = new JLabel("Size:");
//		JLabel       faceLabel      = new JLabel("Face:");
//		JLabel       styleLabel     = new JLabel("Style:");
//		JLabel       textLabel      = new JLabel("Text:");
//		JComboBox    fontFaceCombo  = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
//		JComboBox    fontStyleCombo = new JComboBox(FONT_STYLES);
//		JComboBox    fontSizeCombo  = new JComboBox(fontSizes);
//		JTextField   text           = new JTextField();
//		FontListener fontListener   = new FontListener();
//		
//		for(String item : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {			
//			FontMetrics metrics = fontFaceCombo.getFontMetrics(mainFont);
//			fontFaceCombo.setMinimumSize(new Dimension(metrics.stringWidth(item) + 8, metrics.getHeight()));
//		}
//		
//		for(JLabel item : new JLabel[] { sizeLabel, faceLabel, styleLabel, textLabel}) {
//			String txt = item.getText();
//			FontMetrics metrics = item.getFontMetrics(mainFont);
//			item.setMinimumSize(new Dimension(metrics.stringWidth(txt) + 8, metrics.getHeight()));
//		}
//		
//		fontFaceCombo.setActionCommand(SET_FONT_FACE_ACTION);
//		fontSizeCombo.setActionCommand(SET_FONT_SIZE_ACTION);
//		fontStyleCombo.setActionCommand(SET_FONT_STYLE_ACTION);
//		fontFaceCombo.addActionListener(fontListener);
//		fontSizeCombo.addActionListener(fontListener);
//		fontStyleCombo.addActionListener(fontListener);
//		sizeLabel.setLabelFor(fontSizeCombo);
//		faceLabel.setLabelFor(fontFaceCombo);
//		styleLabel.setLabelFor(fontStyleCombo);
//		textLabel.setLabelFor(text);
//		textLabel.setFont(mainFont);
//		sizeLabel.setFont(mainFont);
//		faceLabel.setFont(mainFont);
//		styleLabel.setFont(mainFont);
//		fontFaceCombo.setFont(mainFont);
//		fontStyleCombo.setFont(mainFont);
//		fontSizeCombo.setFont(mainFont);
//		fontSizeCombo.setMinimumSize(fontFaceCombo.getMinimumSize());
//		fontStyleCombo.setMinimumSize(fontFaceCombo.getMinimumSize());
//		text.addCaretListener(new CaretListener(){
//			@Override
//			public void caretUpdate(CaretEvent arg0) {
//				if (controller != null)
//					controller.handleTextChange(((JTextField) arg0.getSource()).getText());
//			}
//		});
//		
//		GroupLayout layout = new GroupLayout(fontPanel);
//		fontPanel.setLayout(layout);
//		layout.setAutoCreateGaps(true);
//		layout.setAutoCreateContainerGaps(true);
//
//		layout.setVerticalGroup(
//				layout.createSequentialGroup()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(faceLabel)
//						.addComponent(fontFaceCombo)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(sizeLabel)
//						.addComponent(fontSizeCombo)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(styleLabel)
//						.addComponent(fontStyleCombo)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(textLabel)
//						.addComponent(text)
//				)
//		);
//		
//		layout.setHorizontalGroup(
//				layout.createSequentialGroup()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//						.addComponent(faceLabel)
//						.addComponent(sizeLabel)
//						.addComponent(styleLabel)
//						.addComponent(textLabel)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//						.addComponent(fontFaceCombo)
//						.addComponent(fontSizeCombo)
//						.addComponent(fontStyleCombo)
//						.addComponent(text)
//				)
//		);
//		
//		fontPanel.setMinimumSize(layout.minimumLayoutSize(fontPanel));
//		return fontPanel.getMinimumSize();
//	}
	
	
	private Image createIconImage(int w, int h, String path) {
		URL url = getClass().getResource(path);
		if (url == null)
			return null;
		Image image = Toolkit.getDefaultToolkit().createImage(url);
		return image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
	}
	
	
//	private Dimension createStrokePanel() {
//		Dimension     minButtonDimension = new Dimension(ICON_SIZE + BUTTON_HGAP, ICON_SIZE + BUTTON_HGAP);
//		Dimension     minSliderDimension = new Dimension(minButtonDimension.width * 3, minButtonDimension.height - BUTTON_HGAP);
//		JSlider       widthSlider        = new JSlider(0, 20);
//		ButtonGroup   endCapsGroup       = new ButtonGroup();
//		ButtonGroup   lineJoinGroup      = new ButtonGroup();
//		ImageIcon     bevelJoinIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, BEVEL_JOIN_ICON_PATH)); 
//		ImageIcon     miterJoinIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, MITER_JOIN_ICON_PATH));
//		ImageIcon     roundJoinIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, ROUND_JOIN_ICON_PATH));
//		ImageIcon     buttEndIcon        = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, BUTT_END_ICON_PATH));
//		ImageIcon     squareEndIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, SQUARE_END_ICON_PATH));
//		ImageIcon     roundEndIcon       = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, ROUND_END_ICON_PATH));
//		JToggleButton endCapsButt        = new JToggleButton(buttEndIcon);
//		JToggleButton endCapsRound       = new JToggleButton(roundEndIcon);
//		JToggleButton endCapsSquare      = new JToggleButton(squareEndIcon);
//		JToggleButton lineJoinBevel      = new JToggleButton(bevelJoinIcon);
//		JToggleButton lineJoinMiter      = new JToggleButton(miterJoinIcon);
//		JToggleButton lineJoinRound      = new JToggleButton(roundJoinIcon);
//		JLabel        widthLabel         = new JLabel("Width:");
//		JLabel        joinLabel          = new JLabel("Join:");
//		JLabel        capLabel           = new JLabel("Cap:");
//		JLabel        dashLabel          = new JLabel("Dash:");
//		JComboBox     miterLimit         = new JComboBox();
//		SliderChangeListener listener    = new SliderChangeListener();
//		ButtonListener       buttonListener = new ButtonListener();
//		
//		endCapsButt.setActionCommand(SET_CAP_BUTT_ACTION);
//		endCapsRound.setActionCommand(SET_CAP_ROUND_ACTION);
//		endCapsSquare.setActionCommand(SET_CAP_SQUARE_ACTION);
//		lineJoinBevel.setActionCommand(SET_JOIN_BEVEL_ACTION);
//		lineJoinMiter.setActionCommand(SET_JOIN_MITER_ACTION);
//		lineJoinRound.setActionCommand(SET_JOIN_ROUND_ACTION);
//		
//		// Listeners setup
//		endCapsButt.addActionListener(buttonListener);
//		endCapsRound.addActionListener(buttonListener);
//		endCapsSquare.addActionListener(buttonListener);
//		lineJoinBevel.addActionListener(buttonListener);
//		lineJoinMiter.addActionListener(buttonListener);
//		lineJoinRound.addActionListener(buttonListener);
//		
//		widthSlider.addChangeListener(listener);
//		widthSlider.setName("width");
//		widthSlider.setPaintTicks(true);
//		widthSlider.setMinorTickSpacing(1);
//		widthSlider.setValue(1);
//		
//		widthLabel.setLabelFor(widthSlider);
//		dashLabel.setLabelFor(miterLimit);
//		
//		widthLabel.setFont(mainFont);
//		joinLabel.setFont(mainFont);
//		capLabel.setFont(mainFont);
//		dashLabel.setFont(mainFont);
//		
//		endCapsGroup.add(endCapsButt);
//		endCapsGroup.add(endCapsRound);
//		endCapsGroup.add(endCapsSquare);
//		lineJoinGroup.add(lineJoinBevel);
//		lineJoinGroup.add(lineJoinMiter);
//		lineJoinGroup.add(lineJoinRound);
//		
//		widthSlider.setMinimumSize(minSliderDimension);
//		miterLimit.setMinimumSize(minSliderDimension);
//		
//		endCapsRound.setMinimumSize(minButtonDimension);
//		endCapsRound.setPreferredSize(minButtonDimension);
//		endCapsRound.setMaximumSize(minButtonDimension);
//		
//		endCapsButt.setMinimumSize(minButtonDimension);
//		endCapsButt.setPreferredSize(minButtonDimension);
//		endCapsButt.setMaximumSize(minButtonDimension);
//		
//		endCapsSquare.setMinimumSize(minButtonDimension);
//		endCapsSquare.setPreferredSize(minButtonDimension);
//		endCapsSquare.setMaximumSize(minButtonDimension);
//		endCapsSquare.doClick();
//		
//		lineJoinBevel.setMinimumSize(minButtonDimension);
//		lineJoinBevel.setPreferredSize(minButtonDimension);
//		lineJoinBevel.setMaximumSize(minButtonDimension);
//		
//		lineJoinRound.setMinimumSize(minButtonDimension);
//		lineJoinRound.setPreferredSize(minButtonDimension);
//		lineJoinRound.setMaximumSize(minButtonDimension);
//		
//		lineJoinMiter.setMinimumSize(minButtonDimension);
//		lineJoinMiter.setMaximumSize(minButtonDimension);
//		lineJoinMiter.setPreferredSize(minButtonDimension);
//		lineJoinMiter.doClick();
//		
//		for(JLabel item : new JLabel[] { dashLabel, widthLabel, joinLabel, capLabel}) {
//			String txt = item.getText();
//			FontMetrics metrics = item.getFontMetrics(mainFont);
//			item.setMinimumSize(new Dimension(metrics.stringWidth(txt) + 8, metrics.getHeight()));
//		}
//		
//		GroupLayout layout = new GroupLayout(strokePanel);
//		strokePanel.setLayout(layout);
//		layout.setAutoCreateGaps(true);
//		layout.setAutoCreateContainerGaps(true);
//		layout.setVerticalGroup(
//				layout.createSequentialGroup()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//						.addComponent(widthLabel)
//						.addComponent(widthSlider)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//						.addComponent(joinLabel)
//						.addComponent(lineJoinBevel)
//						.addComponent(lineJoinRound)
//						.addComponent(lineJoinMiter)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//						.addComponent(capLabel)
//						.addComponent(endCapsRound)
//						.addComponent(endCapsButt)
//						.addComponent(endCapsSquare)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//						.addComponent(dashLabel)
//						.addComponent(miterLimit)
//				)
//		);
//		layout.setHorizontalGroup(
//				layout.createSequentialGroup()
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//						.addComponent(widthLabel)
//						.addComponent(joinLabel)
//						.addComponent(capLabel)
//						.addComponent(dashLabel)
//				)
//				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//						.addComponent(widthSlider)
//						.addGroup(layout.createSequentialGroup()
//								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//										.addComponent(lineJoinBevel)
//										.addComponent(endCapsRound)
//								)
//								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//										.addComponent(lineJoinRound)
//										.addComponent(endCapsButt)
//								)
//								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//										.addComponent(lineJoinMiter)
//										.addComponent(endCapsSquare)
//								)
//						)
//						.addComponent(miterLimit)
//				)
//		);
//		strokePanel.setMinimumSize(layout.minimumLayoutSize(strokePanel));
//		return strokePanel.getMinimumSize();
//	}
	
		
	@Override	
	public void update(Observable o, Object arg) {
//		ToolBoxModel box = (ToolBoxModel) o;
		if (arg instanceof ToolBoxModel.ActionData) {
			ToolBoxModel.ActionData data = (ToolBoxModel.ActionData) arg;
			int action = data.getAction();
			Tool  tool = data.getData();
			if (action == ToolBoxModel.SHOW_TOOL) {
				
				showPanel(tool);
//				setStrokePanelEnabled(tool.hasStrokeProperties());
//				setFontPanelEnabled(tool.hasFontProperties());
//				setOpacityPanelEnabled(tool.hasAlphaProperties());
//				setColorPanelEnabled(tool.hasColorProperties());
//				((TitledBorder) getBorder()).setTitle(tool.getName());
//				
			} else if (action == ToolBoxModel.UPDATE_TOOL) {
//				
//				if (tool.hasStrokeProperties())
//					setStrokeProperties(box.getStrokeProperty());
//				else if (tool.hasFontProperties())
//					setFontProperties(box.getFontProperty());
//				else if (tool.hasAlphaProperties())
//					setAlphaProperties(box.getOpacityProperty());
//				else if (tool.hasColorProperties())
//					setColorProperties(box.getColorProperty());
			}
//		}
		repaint();
	
		}
	}

	
	class LabelComboBoxRenderer extends JLabel implements ListCellRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Icon[]   icons;
		private String[] descriptions;
		
		public LabelComboBoxRenderer(Border b, String[] iconPaths, String[] descriptions) {
			super();
			setOpaque(true);
			setVerticalAlignment(CENTER);
			setHorizontalAlignment(SwingConstants.LEFT);
			setIconTextGap(5);
			setBorder(new EmptyBorder(2,2,2,2));
			
			icons = new Icon[iconPaths.length];
			
			for(int i = 0; i < iconPaths.length; i++) {
				Image image = createIconImage(16, 16, iconPaths[i]);
				if (image != null)
					icons[i] = new ImageIcon(image);
			}
				
			
			this.descriptions = descriptions;
//			setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED, null, null, java.awt.Color.darkGray, java.awt.Color.lightGray));
			//setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(209, 209, 209), 1, true), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
			setBorder(b);
		}

		/*
		 * This method finds the image and text corresponding
		 * to the selected value and returns the label, set up
		 * to display the text and image.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			//Get the selected index. (The index param isn't
			//always valid, so just use the value.)
			int selectedIndex = ((Integer)value).intValue();

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
				this.setBackground(Color.white);
			}
			
			setIcon(icons[selectedIndex]);
			setText(descriptions[selectedIndex]);
			setFont(list.getFont());
			
			return this;
		}
	}


	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class ComboListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String action = arg0.getActionCommand();
			JComboBox src = (JComboBox) arg0.getSource();
			
			if (action != null) {
				
				if (action.equals(SET_FONT_FACE_ACTION)) {
					controller.handleFontFaceChange((String)src.getSelectedItem());
					textField.setFont(Font.decode((String)src.getSelectedItem()+" 10"));
				}
				else if (action.equals(SET_FONT_SIZE_ACTION))
					controller.handleFontSizeChange(((Integer)src.getSelectedItem()).intValue());
				
				else if (action.equals(SET_FONT_STYLE_ACTION))
					controller.handleFontStyleChange(src.getSelectedIndex());
				
				if (action.equals(SET_CAPS_ACTION))
					controller.handleEndCapsChange(((Integer)src.getSelectedItem()).intValue());
				
				if (action.equals(SET_JOINS_ACTION))
					controller.handleLineJoinsChange(((Integer)src.getSelectedItem()).intValue());
			}
		}
	}
	
}
//	
//	
//	private class ButtonListener implements ActionListener {		
//
//		/* (non-Javadoc)
//		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//		 */
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			String action = arg0.getActionCommand();
//			
//			if (action != null) {				
//				if (action.equals(SET_CAP_SQUARE_ACTION) && controller != null)
//					controller.handleEndCapsChange(BasicStroke.CAP_SQUARE);
//				
//				else if (action.equals(SET_CAP_ROUND_ACTION) && controller != null)
//					controller.handleEndCapsChange(BasicStroke.CAP_ROUND);
//				
//				else if (action.equals(SET_CAP_BUTT_ACTION) && controller != null)
//					controller.handleEndCapsChange(BasicStroke.CAP_BUTT);
//				
//				else if (action.equals(SET_JOIN_BEVEL_ACTION) && controller != null)
//					controller.handleLineJoinsChange(BasicStroke.JOIN_BEVEL);
//				
//				else if (action.equals(SET_JOIN_ROUND_ACTION) && controller != null)
//					controller.handleLineJoinsChange(BasicStroke.JOIN_ROUND);
//				
//				else if (action.equals(SET_JOIN_MITER_ACTION) && controller != null)
//					controller.handleLineJoinsChange(BasicStroke.JOIN_MITER);
//			}
//		}
//	}
//	
//	
//	private class ColorChangeListener extends MouseAdapter {
//		/*
//		 * (non-Javadoc)
//		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
//		 */
//		@Override
//		public void mouseClicked(MouseEvent e) {
//			Color c = JColorChooser.showDialog(PaintPropertyView.this, "Pick a Color", ((JLabel) e.getSource()).getBackground()); 
//			if (c != null) {
//				controller.handleColorChange(c);
//				((JLabel) e.getSource()).setBackground(c);
//			}
//		}
//	}
//	
//	private class SliderChangeListener implements ChangeListener {
//
//		/* (non-Javadoc)
//		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
//		 */
//		@Override
//		public void stateChanged(ChangeEvent arg0) {
//			JSlider s = (JSlider) arg0.getSource();
//			
//			if (!s.getValueIsAdjusting() && controller != null) {
//				int value = s.getValue();
//				
//				if (s.getName().equals("width"))
//					controller.handleWidthChange(value);
//				
//				if (s.getName().equals("opacity"))
//					controller.handleOpacityChange((value)/100f);
//
//			}
//		}
//	}

