package ar.com.tellapic.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.RenderingHints.Key;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import ar.com.tellapic.utils.Utils;

public class PaintPropertyView extends JPanel implements Observer {
	
	private static final long   serialVersionUID      = 1L;
	private static final int    GAP                   = 8;
	private static final int    SEPARATOR_HEIGHT      = ToolView.ICON_SIZE;
	private static final int    SEPARATOR_WIDTH       = 24;
	private static final String BEVEL_JOIN_ICON_PATH  = "/icons/tools/joinbevel.png";
	private static final String ROUND_JOIN_ICON_PATH  = "/icons/tools/joinround.png";
	private static final String MITER_JOIN_ICON_PATH  = "/icons/tools/joinmiter.png";
	private static final String ROUND_END_ICON_PATH   = "/icons/tools/capround.png";
	private static final String BUTT_END_ICON_PATH    = "/icons/tools/capbutt.png";
	private static final String SQUARE_END_ICON_PATH  = "/icons/tools/capsquare.png";
//	private static final String ZOOM_IN_ICON_PATH     = "/icons/zoomin.png";
//	private static final String ZOOM_OUT_ICON_PATH    = "/icons/zoomout.png";
//	private static final String SET_CAP_ROUND_ACTION  = "capround";
//	private static final String SET_CAP_SQUARE_ACTION = "capsquare";
//	private static final String SET_CAP_BUTT_ACTION   = "capbutt";
	private static final String SET_CAPS_ACTION       = "setcap";
	private static final String SET_JOINS_ACTION      = "setjoin";
	private static final String SET_FONT_FACE_ACTION  = "face";
	private static final String SET_FONT_SIZE_ACTION  = "size";
	private static final String SET_FONT_STYLE_ACTION = "style";
	private static final String SET_ZOOM_IN_ACTION    = "zoomin";
	private static final String SET_ZOOM_OUT_ACTION   = "zoomout";
	private static final String SET_ZOOM_ACTION       = "zoom";
	private static final String SET_ZOOM_TOSIZE_ACTION = "zoomtosize";
	private static final String SET_ZOOM_TOFIT_ACTION = "zoomtofit";
	
	private static final String[] FONT_STYLES         = new String[] { 
		"normal",
		"<html><b>bold</b></html>",
		"<html><i>italic</i></html>",
		"<html><b><i>italic+bold</b></i></html>"
	};
	private static final String[] END_CAPS_TYPES  = new String[] {
		"Miter",
		"Round",
		"Bevel"
	};
	private static final String[] LINE_JOIN_TYPES = new String[] {
			"Butt",
			"Round",
			"Square"
	};
	private static final Color    DEFAULT_COLOR    = Color.white;
	@SuppressWarnings("unused")
	private static final int      DEFAULT_END_CAPS = BasicStroke.CAP_SQUARE;
	@SuppressWarnings("unused")
	private static final int      DEFAULT_LINE_JOIN = BasicStroke.JOIN_MITER;
	private static final double   DEFAULT_OPACITY = 100;
	private static final double   DEFAULT_WIDTH = 5.0;
	
	
	
	
	private final Font               defaultTitleFont = Font.decode("Droid-bold-10");
	private final Font               defaultValueFont = Font.decode("Droid-10");
	private JSpinner                 widthSpinner;
	private JSpinner                 opacitySpinner;
	private JComboBox                dashCombo;
	private JComboBox                capsCombo;
	private JComboBox                joinCombo;
	private PopupButton              toolIcon;
	private JLabel                   widthLabel;
	private JLabel                   opacityLabel;
	private JLabel                   dashLabel;
	private JLabel                   capsLabel;
	private JLabel                   joinLabel;
	private JLabel                   fontFaceLabel;
	private JLabel                   fontSizeLabel;
	private JLabel                   fontStyleLabel;
	private JLabel                   textLabel;
	private JLabel                   colorLabel;
	private JLabel                   colorField;
	private JSeparator               jSeparator1;
	private JSeparator               jSeparator2;
	private JSeparator               jSeparator3;
	private JSeparator               jSeparator4;
	private JSeparator               jSeparator5;
	private GroupLayout              layout;
	private JComboBox                fontFaceCombo;
	private JComboBox                fontSizeCombo;
	private JComboBox                fontStyleCombo;
	private JTextField               textField;
	private IPaintPropertyController controller;
	private MyActionListener         actionListener;
	private JSeparator               jSeparator6;
	private JSeparator               jSeparator7;
	private JToggleButton            zoomInButton;
	private JToggleButton            zoomOutButton;
	private JComboBox                zoomCombo;
	private JSeparator               jSeparator8;
	private JButton                  zoomToFitButton;
	private JButton                  zoomToSizeButton;
	@SuppressWarnings("unused")
	private boolean                  useDefaultValues;
	
	
	/** Creates new form ToolView */
	public PaintPropertyView() {
		setName(Utils.msg.getString("propertyview"));
		useDefaultValues = true;
		layout           = new GroupLayout(this);
		actionListener   = new MyActionListener();

		createSeparators();
		
		Dimension iconDimension = new Dimension(ToolView.ICON_SIZE + 8, ToolView.ICON_SIZE + 8);
		toolIcon = new PopupButton();
		toolIcon.setMaximumSize(iconDimension);
		toolIcon.setMinimumSize(iconDimension);
		toolIcon.setPreferredSize(iconDimension);
		
		/* Creates the options for customize stroked shapes */
		createStrokeOptions();
		
		/* Creates the options for customize text */
		createFontOptions();
		
		/**/
		createColorOptions();

		/**/
		createZoomOptions();
		
		setMinimumSize(new Dimension(3200, ToolView.ICON_SIZE));
		setMaximumSize(new Dimension(3200, ToolView.ICON_SIZE + 10));
		setPreferredSize(new Dimension(3200, ToolView.ICON_SIZE + GAP));
		setLayout(layout);
	}

	/**
	 * 
	 * @return
	 */
	private JPopupMenu buildToolPopupOptions(Tool tool) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem  tips = new JMenuItem("Tips");
		
		if (tool instanceof DrawingTool) {
			final DrawingTool drawingTool = (DrawingTool) tool;
			JCheckBoxMenuItem symmetric = new JCheckBoxMenuItem("Symmetric Mode");
			symmetric.addItemListener(new ItemListener(){
				@Override
				public void itemStateChanged(ItemEvent e) {
					drawingTool.setSymmetricMode(e.getStateChange() == ItemEvent.SELECTED);
				}
			});
			symmetric.setSelected(drawingTool.isSymmetricModeEnabled());
			popup.add(buildQualityMenu(tool));
			popup.addSeparator();
			popup.add(symmetric);
			popup.addSeparator();
		}
		popup.add(tips);
		return popup;
	}
	
	/**
	 * 
	 */
	private void createZoomOptions() {
		zoomToFitButton  = new JButton();
		zoomToSizeButton = new JButton();
		zoomInButton     = new JToggleButton();
		zoomOutButton    = new JToggleButton();
		zoomCombo        = new JComboBox(ControlToolZoom.getInstance());
		
		ButtonGroup group = new ButtonGroup();
		group.add(zoomInButton);
		group.add(zoomOutButton);
		
		zoomInButton.setIcon(new ImageIcon(Utils.createIconImage(ToolView.ICON_SIZE, ToolView.ICON_SIZE, ControlToolZoom.ZOOMIN_ICON_PATH)));
		zoomOutButton.setIcon(new ImageIcon(Utils.createIconImage(ToolView.ICON_SIZE, ToolView.ICON_SIZE, ControlToolZoom.ZOOMOUT_ICON_PATH)));
		zoomToFitButton.setIcon(new ImageIcon(Utils.createIconImage(ToolView.ICON_SIZE, ToolView.ICON_SIZE, ControlToolZoom.ZOOMTOFIT_ICON_PATH)));
		zoomToSizeButton.setIcon(new ImageIcon(Utils.createIconImage(ToolView.ICON_SIZE, ToolView.ICON_SIZE, ControlToolZoom.ZOOMTOSIZE_ICON_PATH)));
		Dimension iconDimension = new Dimension(ToolView.ICON_SIZE + 8, ToolView.ICON_SIZE + 8);
		zoomInButton.setPreferredSize(iconDimension);
		zoomOutButton.setPreferredSize(iconDimension);
		zoomToFitButton.setPreferredSize(iconDimension);
		zoomToSizeButton.setPreferredSize(iconDimension);
		
		zoomCombo.setFont(defaultValueFont);
		zoomCombo.setActionCommand(SET_ZOOM_ACTION);
		zoomToSizeButton.setActionCommand(SET_ZOOM_TOSIZE_ACTION);
		zoomToFitButton.setActionCommand(SET_ZOOM_TOFIT_ACTION);
		zoomInButton.setActionCommand(SET_ZOOM_IN_ACTION);
		zoomOutButton.setActionCommand(SET_ZOOM_OUT_ACTION);
		
		zoomInButton.addActionListener(actionListener);
		zoomOutButton.addActionListener(actionListener);
		zoomToSizeButton.addActionListener(actionListener);
		zoomToFitButton.addActionListener(actionListener);
		zoomCombo.addActionListener(actionListener);
		zoomInButton.setSelected(true);
	}


	/**
	 * 
	 */
	private void createFontOptions() {
		Dimension comboDimension = new Dimension(100, ToolView.ICON_SIZE + GAP);
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
		
		Dimension faceComboDimension = new Dimension(getMaxTextWidth(fontFaceCombo, GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()), ToolView.ICON_SIZE + GAP);

		textField.setFocusable(true);
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				if (textField.getText().length() == 0) {
					textField.setText("< Your text here >");
//					textField.setFont(Font.decode("Droid-italic-10"));
					textField.setSelectedTextColor(Color.black);
					textField.selectAll();
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		textField.setPreferredSize(new Dimension(Short.MAX_VALUE, ToolView.ICON_SIZE + GAP));
		textField.setFont(defaultValueFont);
		textLabel.setFont(defaultTitleFont);
		fontFaceLabel.setFont(defaultTitleFont);
		fontStyleLabel.setFont(defaultTitleFont);
		fontSizeLabel.setFont(defaultTitleFont);
		fontFaceCombo.setPreferredSize(faceComboDimension);
		fontStyleCombo.setPreferredSize(comboDimension);
		fontSizeCombo.setPreferredSize(new Dimension(50, ToolView.ICON_SIZE + GAP));
		fontFaceCombo.setFont(defaultValueFont);
		fontStyleCombo.setFont(defaultValueFont);
		fontSizeCombo.setFont(defaultValueFont);
		fontFaceCombo.setActionCommand(SET_FONT_FACE_ACTION);
		fontSizeCombo.setActionCommand(SET_FONT_SIZE_ACTION);
		fontStyleCombo.setActionCommand(SET_FONT_STYLE_ACTION);
		fontFaceCombo.addActionListener(actionListener);
		fontSizeCombo.addActionListener(actionListener);
		fontStyleCombo.addActionListener(actionListener);
		textField.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent arg0) {
				if (controller != null)
					controller.handleTextChange(((JTextField) arg0.getSource()).getText());
			}
		});
		
		ComboWheelListener l1 = new ComboWheelListener(fontFaceCombo);
		ComboWheelListener l2 = new ComboWheelListener(fontStyleCombo);
		ComboWheelListener l3 = new ComboWheelListener(fontSizeCombo);
		fontFaceCombo.addMouseWheelListener(l1);
		fontStyleCombo.addMouseWheelListener(l2);
		fontSizeCombo.addMouseWheelListener(l3);
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
		widthSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_WIDTH, 0.1, 70.0, 0.1));
		opacitySpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_OPACITY, 0.0, 100.0, 0.5));
		joinCombo    = new JComboBox(new Integer[] {0,1,2});
		capsCombo    = new JComboBox(new Integer[] {0,1,2});
		dashCombo    = new JComboBox();
		
		SpinnerTranslucentEditor opacityEditor = new SpinnerTranslucentEditor(opacitySpinner); 
		WidthSpinnerEditor       widthEditor   = new WidthSpinnerEditor(widthSpinner);
		
		opacitySpinner.setEditor(opacityEditor);
		opacitySpinner.addChangeListener(opacityEditor);
		
		widthSpinner.setEditor(widthEditor);
		widthSpinner.addChangeListener(widthEditor);
		
		
		LabelComboBoxRenderer capsRenderer = new LabelComboBoxRenderer(
				capsCombo.getBorder(),
				new String[] {
						BUTT_END_ICON_PATH,
						ROUND_END_ICON_PATH,
						SQUARE_END_ICON_PATH
				},
				LINE_JOIN_TYPES
		);
		
		LabelComboBoxRenderer joinRenderer = new LabelComboBoxRenderer(
				joinCombo.getBorder(),
				new String[] {
						MITER_JOIN_ICON_PATH,
						ROUND_JOIN_ICON_PATH,
						BEVEL_JOIN_ICON_PATH
				},
				END_CAPS_TYPES
		);
		capsCombo.setRenderer(capsRenderer);
		joinCombo.setRenderer(joinRenderer);
		opacitySpinner.setFont(defaultValueFont);
		widthSpinner.setFont(defaultValueFont);
		widthLabel.setFont(defaultTitleFont);
		opacityLabel.setFont(defaultTitleFont);
		joinLabel.setFont(defaultTitleFont);
		capsLabel.setFont(defaultTitleFont);
		dashLabel.setFont(defaultTitleFont);
		capsCombo.setFont(defaultValueFont);
		joinCombo.setFont(defaultValueFont);
		
		int gap = + LabelComboBoxRenderer.ICON_GAP*3  + ToolView.ICON_SIZE * 2;
		Dimension capsComboDimension = new Dimension(getMaxTextWidth(capsCombo, END_CAPS_TYPES) + gap, ToolView.ICON_SIZE + GAP);
		Dimension joinComboDimension = new Dimension(getMaxTextWidth(joinCombo, LINE_JOIN_TYPES)+ gap, ToolView.ICON_SIZE + GAP);
		Dimension dashComboDimension = new Dimension(100, ToolView.ICON_SIZE + GAP);
		
		capsCombo.setEditable(false);
		joinCombo.setEditable(false);
		capsCombo.setPreferredSize(capsComboDimension);
		joinCombo.setPreferredSize(joinComboDimension);
		widthSpinner.setPreferredSize(new Dimension(50, ToolView.ICON_SIZE + GAP));
		opacitySpinner.setPreferredSize(new Dimension(50, ToolView.ICON_SIZE + GAP));
		dashCombo.setPreferredSize(dashComboDimension);
		capsCombo.setActionCommand(SET_CAPS_ACTION);
		joinCombo.setActionCommand(SET_JOINS_ACTION);
		joinCombo.addActionListener(actionListener);
		capsCombo.addActionListener(actionListener);
		dashCombo.addActionListener(actionListener);

		SpinnerWheelListener l1 = new SpinnerWheelListener(opacitySpinner);
		SpinnerWheelListener l2 = new SpinnerWheelListener(widthSpinner);
		ComboWheelListener l3 = new ComboWheelListener(capsCombo);
		ComboWheelListener l4 = new ComboWheelListener(joinCombo);
		
		capsCombo.addMouseWheelListener(l3);
		joinCombo.addMouseWheelListener(l4);
		opacitySpinner.addMouseWheelListener(l1);
		widthSpinner.addMouseWheelListener(l2);
	}

	
	/**
	 * 
	 */
	private void createColorOptions() {
		Dimension iconDimension = new Dimension(ToolView.ICON_SIZE + 8, ToolView.ICON_SIZE + 8);
		
		colorLabel = new JLabel(Utils.msg.getString("color")+":");
		colorField = new JLabel();
		
		colorLabel.setIcon(new ImageIcon(Utils.createIconImage(ToolView.ICON_SIZE, ToolView.ICON_SIZE, "/icons/tools/color1.png")));
		colorLabel.setLabelFor(colorField);
		colorLabel.setFont(defaultTitleFont);
		colorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		colorLabel.addMouseListener(new ColorSelectionListener(colorLabel));
		
		colorField.setPreferredSize(iconDimension);
		colorField.setOpaque(true);
		colorField.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(209, 209, 209), 1, true), new SoftBevelBorder(BevelBorder.LOWERED)));
		colorField.setBackground(DEFAULT_COLOR);
		colorField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		colorField.setToolTipText(Utils.msg.getString("colorfieldtooltip"));
		colorField.addMouseListener(new ColorSelectionListener(colorField));
	}
	
	
	/**
	 * 
	 * @param colorSelector
	 * @param component
	 */
	private void manageColorChange(ColorSelector colorSelector, JComponent component) {
		/* Get the selected color */
		Color color = colorSelector.getSelectedColor();
		
		/* Set the current color in the toolbox */
		controller.handleColorChange(color);
		
		/* Set the current color in the component view */
		component.setBackground(color);
		
		/* We don't need it anymore */
		colorSelector.dispose();
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
		jSeparator6 = new JSeparator();
		jSeparator7 = new JSeparator();
		jSeparator8 = new JSeparator();
		jSeparator1.setOrientation(SwingConstants.VERTICAL);
		jSeparator1.setMaximumSize(separatorDimension);
		jSeparator1.setMinimumSize(separatorDimension);
		jSeparator1.setPreferredSize(separatorDimension);
		jSeparator2.setOrientation(SwingConstants.VERTICAL);
		jSeparator2.setMaximumSize(separatorDimension);
		jSeparator2.setMinimumSize(separatorDimension);
		jSeparator2.setPreferredSize(separatorDimension);
		jSeparator3.setOrientation(SwingConstants.VERTICAL);
		jSeparator3.setMaximumSize(separatorDimension);
		jSeparator3.setMinimumSize(separatorDimension);
		jSeparator3.setPreferredSize(separatorDimension);
		jSeparator4.setOrientation(SwingConstants.VERTICAL);
		jSeparator4.setMaximumSize(separatorDimension);
		jSeparator4.setMinimumSize(separatorDimension);
		jSeparator4.setPreferredSize(separatorDimension);
		jSeparator5.setOrientation(SwingConstants.VERTICAL);
		jSeparator5.setMaximumSize(separatorDimension);
		jSeparator5.setMinimumSize(separatorDimension);
		jSeparator5.setPreferredSize(separatorDimension);
		jSeparator6.setOrientation(SwingConstants.VERTICAL);
		jSeparator6.setMaximumSize(separatorDimension);
		jSeparator6.setMinimumSize(separatorDimension);
		jSeparator6.setPreferredSize(separatorDimension);
		jSeparator7.setOrientation(SwingConstants.VERTICAL);
		jSeparator7.setMaximumSize(separatorDimension);
		jSeparator7.setMinimumSize(separatorDimension);
		jSeparator7.setPreferredSize(separatorDimension);
		jSeparator8.setOrientation(SwingConstants.VERTICAL);
		jSeparator8.setMaximumSize(separatorDimension);
		jSeparator8.setMinimumSize(separatorDimension);
		jSeparator8.setPreferredSize(separatorDimension);
	}


	/**
	 * 
	 * @param c
	 * @return
	 */
	private int getMaxTextWidth(JComponent c, String[] items) {
		int max = 0;
		FontMetrics  metrics = c.getFontMetrics(defaultTitleFont);
		for(String item : items)
			if (metrics.stringWidth(item) > max)
				max = metrics.stringWidth(item);
		
		return max;
	}
	
	
	/**
	 * Show panel for tool tool.
	 * @param tool
	 */
	public void showPanel(Tool tool) {
		GroupLayout.ParallelGroup   vParallelGroup    = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
		GroupLayout.SequentialGroup hSequentialGroup  = layout.createSequentialGroup();
		ToolIconActionListener       toolIconListener = new ToolIconActionListener(buildToolPopupOptions(tool) );
		ActionListener[] l =toolIcon.getListeners(ActionListener.class);
		
		if (l.length > 0) {
			for(int i = 0; i < l.length; i++)
				toolIcon.removeActionListener(l[i]);
		}
		
		toolIcon.setIcon(new ImageIcon(createIconImage(ToolView.ICON_SIZE, ToolView.ICON_SIZE, tool.getIconPath())));
		toolIcon.setToolTipText(tool.getToolTipText());
		toolIcon.addActionListener(toolIconListener);
		
		removeAll();
		
		hSequentialGroup.addContainerGap()
		.addComponent(toolIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		.addGap(5,5,5)
		.addComponent(jSeparator1, 5,5,5)
		;
		vParallelGroup
		.addComponent(toolIcon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		.addComponent(jSeparator1, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
		;
		
		if (tool instanceof DrawingTool && ((DrawingTool)tool).hasColorCapability()) {
			hSequentialGroup
			.addGap(5,5,5)
			.addComponent(colorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(colorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(jSeparator2, 5,5,5)
			;
			
			vParallelGroup
			.addComponent(colorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(colorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator2, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
		}
		
		if (tool instanceof DrawingTool && ((DrawingTool)tool).hasAlphaCapability()) {
			hSequentialGroup
			.addGap(5,5,5)
			.addComponent(opacityLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(opacitySpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(jSeparator3, 5,5,5)
			;
			
			vParallelGroup
			.addComponent(opacityLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(opacitySpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator3, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
		}

		if (tool instanceof DrawingTool && ((DrawingTool)tool).hasStrokeCapability()) {
			hSequentialGroup
			.addGap(5,5,5)
			.addComponent(widthLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(widthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(jSeparator4, 5,5,5)
			.addGap(5,5,5)
			.addComponent(joinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(joinCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(jSeparator5, 5,5,5)
			.addGap(5,5,5)
			.addComponent(capsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(capsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(jSeparator6, 5,5,5)
			.addGap(5,5,5)
			.addComponent(dashLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(dashCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(jSeparator7, 5,5,5)
			;
			
			vParallelGroup
			.addComponent(widthLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(widthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator4, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator5, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(capsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(capsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator6, GroupLayout.PREFERRED_SIZE, SEPARATOR_WIDTH, GroupLayout.PREFERRED_SIZE)
			.addComponent(dashLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(dashCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator7, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			;
		}
		
		
		if (tool instanceof DrawingTool && ((DrawingTool)tool).hasFontCapability()) {
			hSequentialGroup
			.addGap(5,5,5)
			.addComponent(fontFaceLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(fontFaceCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(fontSizeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(fontSizeCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(fontStyleLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(fontStyleCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
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
			textField.requestFocus();
		}
		
		if (tool instanceof ControlTool && ((ControlTool)tool).hasZoomCapability()) {
			hSequentialGroup
			.addGap(5,5,5)
			.addComponent(zoomToFitButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(zoomToSizeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(jSeparator7, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			.addGap(5,5,5)
			.addComponent(zoomInButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(zoomOutButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap(5,5,5)
			.addComponent(zoomCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addContainerGap()
			;
			
			vParallelGroup
			.addComponent(zoomToFitButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(zoomToSizeButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(jSeparator7, SEPARATOR_WIDTH, SEPARATOR_WIDTH, SEPARATOR_WIDTH)
			.addComponent(zoomInButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(zoomOutButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(zoomCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			;
			
		}
		
		layout.setHorizontalGroup(hSequentialGroup);
		layout.setVerticalGroup(vParallelGroup);
	}
	

	/**
	 * 
	 * @param strokeProperty
	 */
	private void setStrokeProperties(PaintPropertyStroke strokeProperty) {
		int    caps  = strokeProperty.getEndCaps();
		int    join  = strokeProperty.getLineJoins();
		double width = strokeProperty.getWidth();
		
		capsCombo.setSelectedItem(caps);
		joinCombo.setSelectedItem(join);
		widthSpinner.setValue(width);
	}
	
	
	/**
	 * 
	 * @param fontProperty
	 */
	private void setFontProperties(PaintPropertyFont fontProperty) {
		textField.requestFocus();
	}
	
	
	/**
	 * 
	 * @param colorProperty
	 */
	private void setColorProperties(PaintPropertyColor colorProperty) {
		colorField.setBackground(colorProperty.getColor());
	}
	
	
	/**
	 * 
	 * @param opacityProperty
	 */
	private void setAlphaProperties(PaintPropertyAlpha opacityProperty) {
		double opacity = opacityProperty.alpha*100;
		opacitySpinner.setValue(opacity);
	}
	
	
	/**
	 * 
	 * @param c the controller to set
	 */
	public void setPaintPropertyController(IPaintPropertyController c) {
		controller = c;
		setDefaultProperties();
	}
	
	
	/**
	 * 
	 */
	private void setDefaultProperties() {
//		capsCombo.setSelectedIndex(DEFAULT_END_CAPS);
//		joinCombo.setSelectedIndex(DEFAULT_LINE_JOIN);
//		widthSpinner.setValue(DEFAULT_WIDTH);
//		opacitySpinner.setValue(DEFAULT_OPACITY);
		controller.handleColorChange(DEFAULT_COLOR);
//		controller.handleEndCapsChange(DEFAULT_END_CAPS);
//		controller.handleFontFaceChange(DEFAULT_FONT_FACE);
//		controller.handleFontSizeChange(DEFAULT_FONT_SIZE);
//		controller.handleFontStyleChange(DEFAULT_FONT_STYLE);
//		controller.handleLineJoinsChange(DEFAULT_LINE_JOIN);
		controller.handleOpacityChange(DEFAULT_OPACITY);
		controller.handleWidthChange(DEFAULT_WIDTH);
	}
	
	
	private Image createIconImage(int w, int h, String path) {
		URL url = getClass().getResource(path);
		if (url == null)
			return null;
		Image image = Toolkit.getDefaultToolkit().createImage(url);
		return image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override	
	public void update(Observable o, Object arg) {
		ToolBoxModel box = (ToolBoxModel) o;
		if (arg instanceof ToolBoxModel.ActionData) {
			ToolBoxModel.ActionData data = (ToolBoxModel.ActionData) arg;
			int action = data.getAction();
			Tool  tool = data.getData();
			if (action == ToolBoxModel.SHOW_TOOL && tool != null) {
				
				showPanel(tool);
//				if (useDefaultValues)
//					box.setCurrentToolDefaultValues();
				
//				setStrokePanelEnabled(tool.hasStrokeProperties());
//				setFontPanelEnabled(tool.hasFontProperties());
//				setOpacityPanelEnabled(tool.hasAlphaProperties());
//				setColorPanelEnabled(tool.hasColorProperties());
//				((TitledBorder) getBorder()).setTitle(tool.getName());
//				
			} else if (action == ToolBoxModel.UPDATE_TOOL && tool != null) {
				if (tool instanceof DrawingTool && ((DrawingTool)tool).hasStrokeCapability())
					setStrokeProperties(box.getStrokeProperty());
				
				if (tool instanceof DrawingTool && ((DrawingTool)tool).hasFontCapability())
					setFontProperties(box.getFontProperty());
				
				if (tool instanceof DrawingTool && ((DrawingTool)tool).hasAlphaCapability())
					setAlphaProperties(box.getOpacityProperty());
				
				if (tool instanceof DrawingTool && ((DrawingTool)tool).hasColorCapability())
					setColorProperties(box.getColorProperty());
			}
		repaint();
		}
	}


	
		/*
	 * Build the quality menu:
	 * 
	 *  Quality -> Text Antialising -> Antialias on
	 *                                 Antialias off
	 *                                 Antialias default
	 *                                 Antialias GASP
	 *                                 Antialias LCD HRGB
	 *                                 Antialias LCD HBGR
	 *                                 Antialias LCD VRGB
	 *                                 Antialias LCD VBGR
	 *                                 
	 *          -> Shape Antialiasing -> Antialias ON
	 *                                -> Antialias DEFAULT
	 *                                
	 *          -> Color Rendering -> Quality
	 *                             -> Speed
	 *                             -> DEFAULT
	 *                             
	 *          -> Dithering -> Disable
	 *                       -> Enable
	 *                       -> DEFAULT
	 *                       
	 *          -> Image Interpolation -> Bicubic
	 *                                 -> Bilinear
	 *                                 -> Near neighbor
	 *                                 
	 *          -> Rendering Quality -> Quality
	 *                               -> Speed
	 *                               -> Default
	 *                               
	 *          -> Stroke Normalization Control -> DEFAULT
	 *                                          -> Normalize
	 *                                          -> Pure
	 */
	private JMenu buildQualityMenu(Tool tool) {
		JMenu root                = new JMenu(Utils.msg.getString("quality"));
		JMenu aaText              = new JMenu("Text Antialising");
		JMenu aaShape             = new JMenu("Shape Antialising");
		JMenu colorRendering      = new JMenu("Color Rendering");
		JMenu dithering           = new JMenu("Dithering");
		JMenu imageInterpolation  = new JMenu("Image Interpolation");
		JMenu renderingQuality    = new JMenu("Rendering Quality");
		JMenu strokeNormalization = new JMenu("Stroke Normalization Control");

		if (tool instanceof DrawingTool) {
			DrawingTool drawingTool = (DrawingTool) tool;
			if (drawingTool.hasFontCapability()) {
				root.add(
						buildMenuCheckBoxes(
								aaText,
								RenderingHints.KEY_TEXT_ANTIALIASING,
								new Object[] {RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
										RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,
										RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT,
										RenderingHints.VALUE_TEXT_ANTIALIAS_GASP,
										RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB,
										RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR,
										RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB,
										RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR
								}
						)
				);
			}
			if (drawingTool.hasStrokeCapability()) {
				root.add(
						buildMenuCheckBoxes(
								aaShape,
								RenderingHints.KEY_ANTIALIASING,
								new Object[] { 
										RenderingHints.VALUE_ANTIALIAS_ON,
										RenderingHints.VALUE_ANTIALIAS_OFF,
										RenderingHints.VALUE_ANTIALIAS_DEFAULT
								}
						)
				);
				root.add(
						buildMenuCheckBoxes(
								dithering,
								RenderingHints.KEY_DITHERING,
								new Object[] {
										RenderingHints.VALUE_DITHER_DISABLE,
										RenderingHints.VALUE_DITHER_ENABLE,
										RenderingHints.VALUE_DITHER_DEFAULT
								}
						)
				);
				root.add(
						buildMenuCheckBoxes(
								imageInterpolation,
								RenderingHints.KEY_INTERPOLATION,
								new Object[] {
										RenderingHints.VALUE_INTERPOLATION_BICUBIC,
										RenderingHints.VALUE_INTERPOLATION_BILINEAR,
										RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
								}
						)
				);
				root.add(
						buildMenuCheckBoxes(
								renderingQuality,
								RenderingHints.KEY_RENDERING,
								new Object[]  {
										RenderingHints.VALUE_RENDER_QUALITY,
										RenderingHints.VALUE_RENDER_SPEED,
										RenderingHints.VALUE_RENDER_DEFAULT
								}
						)
				);

				root.add(
						buildMenuCheckBoxes(
								strokeNormalization, 
								RenderingHints.KEY_STROKE_CONTROL, 
								new Object[] {
										RenderingHints.VALUE_STROKE_DEFAULT,
										RenderingHints.VALUE_STROKE_NORMALIZE,
										RenderingHints.VALUE_STROKE_PURE
								}
						)
				);
			}
			if (drawingTool.hasColorCapability()) {
				root.add(
						buildMenuCheckBoxes(
								colorRendering,
								RenderingHints.KEY_COLOR_RENDERING,
								new Object[] { 
										RenderingHints.VALUE_COLOR_RENDER_QUALITY,
										RenderingHints.VALUE_COLOR_RENDER_SPEED,
										RenderingHints.VALUE_COLOR_RENDER_DEFAULT
								}
						)
				);
			}
		}
		return root;
	}
	
	
	/*
	 * 
	 */
	private JMenu buildMenuCheckBoxes(JMenu root, Key key, Object[] values) {
		ButtonGroup       group = new ButtonGroup();
		JCheckBoxMenuItem item  = null;
		
		for(int i = 0; i < values.length; i++) {
			item = new JCheckBoxMenuItem(values[i].toString());
			group.add(item);
			item.addItemListener(new MenuItemListener(key, values[i]));
			root.add(item);
			
			if (item.getText().matches(".*default.*|.*Default.*|.*DEFAULT.*"))
				item.setSelected(true);
		}

		return root;
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class MyActionListener implements ActionListener {
		/* (non-Javadoc)
		 * @see event.ActionListener#actionPerformed(event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String action = arg0.getActionCommand();
			JComboBox src = null;
			
			if (action != null) {
				
				if (action.equals(SET_FONT_FACE_ACTION)) {
					src = (JComboBox) arg0.getSource();
					controller.handleFontFaceChange((String)src.getSelectedItem());
					textField.setFont(Font.decode((String)src.getSelectedItem()+" 10"));
					
				} else if (action.equals(SET_FONT_SIZE_ACTION)) {
					src = (JComboBox) arg0.getSource();
					controller.handleFontSizeChange(((Integer)src.getSelectedItem()).intValue());
					
				} else if (action.equals(SET_FONT_STYLE_ACTION)){
					src = (JComboBox) arg0.getSource();
					controller.handleFontStyleChange(src.getSelectedIndex());
					
				} else if (action.equals(SET_CAPS_ACTION)){
					src = (JComboBox) arg0.getSource();
					controller.handleEndCapsChange(((Integer)src.getSelectedItem()).intValue());
					
				} else if (action.equals(SET_JOINS_ACTION)){
					src = (JComboBox) arg0.getSource();
					controller.handleLineJoinsChange(((Integer)src.getSelectedItem()).intValue());
					
				} else if (action.equals(SET_ZOOM_IN_ACTION)) {
					controller.handleZoomChange(true);
					
				} else if (action.equals(SET_ZOOM_OUT_ACTION)) {
					controller.handleZoomChange(false);
					
				}
//					
//				else if (action.equals(SET_ZOOM_ACTION)) {
//					src = (JComboBox) arg0.getSource();
//					String str = (String) src.getSelectedItem();
//					float zoom = Float.valueOf(str.substring(0, str.length() - 1)) / 100;
//					controller.handleZoomChange(zoom);
//				}
//				
//				else if (action.equals(SET_ZOOM_TOFIT_ACTION)) {
//					
//				}
//				
//				else if (action.equals(SET_ZOOM_TOSIZE_ACTION))
//					controller.handleZoomChange(1);
			}
		}
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class MenuItemListener implements ItemListener {
		private Object value;
		private Key    key;
		
		public MenuItemListener(Key key, Object v) {
			this.value = v;
			this.key   = key;
		}
		/* (non-Javadoc)
		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {
			controller.handleRenderingHint(key, (e.getStateChange() == ItemEvent.SELECTED)? value : null);
		}
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	class LabelComboBoxRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 1L;
		private Icon[]   icons;
		private String[] descriptions;
		
		public static final int ICON_GAP  = 3;
		
		public LabelComboBoxRenderer(Border b, String[] iconPaths, String[] descriptions) {
			super();
			setOpaque(true);
			setFont(defaultValueFont);
			setVerticalAlignment(CENTER);
			setHorizontalAlignment(SwingConstants.LEFT);
			setIconTextGap(ICON_GAP);
			icons = new Icon[iconPaths.length];
			for(int i = 0; i < iconPaths.length; i++) {
				Image image = createIconImage(ToolView.ICON_SIZE, ToolView.ICON_SIZE, iconPaths[i]);
				if (image != null)
					icons[i] = new ImageIcon(image);
			}
			this.descriptions = descriptions;
			setBorder(new EmptyBorder(ICON_GAP, 0, ICON_GAP, 0));
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
				setBackground(Color.white);
			}
			
			setIcon(icons[selectedIndex]);
			setText(descriptions[selectedIndex]);
//			setFont(defaultFont);
			
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
	private class WidthSpinnerEditor extends JTextField implements ChangeListener {
		private static final long serialVersionUID = 1L;

		public WidthSpinnerEditor(JSpinner spinner) {
			setFont(Utils.MAIN_FONT);
			setOpaque(true);
			setBackground(Color.white);
			setEditable(false);
			setText("  "+new DecimalFormat("#.##").format(spinner.getModel().getValue()));
			addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e) {
					JPopupMenu popup = new JPopupMenu();
					JSlider slider = new JSlider(0, 100, (int)(double)(Double)widthSpinner.getModel().getValue());
					slider.addChangeListener(new ChangeListener(){
						@Override
						public void stateChanged(ChangeEvent e) {
							JSlider s = (JSlider) e.getSource();
							double value = s.getModel().getValue();
							widthSpinner.setValue(value);
						}
					});
					slider.setUI(new BasicSliderUI(slider));
					popup.add(slider);
					popup.show(PaintPropertyView.this, widthSpinner.getX(), widthSpinner.getHeight());
				}
			});
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			JSpinner spinner = (JSpinner) e.getSource();
			SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
			double value = (Double)model.getValue();
			setText("  "+String.valueOf(new DecimalFormat("#.##").format(value)));
			if (controller != null) {
				controller.handleWidthChange(value);
			}
		}
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class SpinnerTranslucentEditor extends JTextField implements ChangeListener {
		private static final long serialVersionUID = 1L;
		private double alpha = 1;
		private String textValue;
		
		public SpinnerTranslucentEditor(JSpinner spinner) {
			textValue = String.valueOf(alpha*100);
			setForeground(Color.black);
			setFont(Utils.MAIN_FONT);
			setPreferredSize(new Dimension(24, 18));
			setEditable(false);
			setOpaque(false);
			addMouseListener(new MouseAdapter(){
				public void mouseClicked(MouseEvent e) {
					JPopupMenu popup = new JPopupMenu();
					JSlider slider = new JSlider(0, 100, (int)(double)(Double)opacitySpinner.getModel().getValue());
					slider.addChangeListener(new ChangeListener(){
						@Override
						public void stateChanged(ChangeEvent e) {
							JSlider s = (JSlider) e.getSource();
							double value = s.getModel().getValue();
							opacitySpinner.setValue(value);
						}
					});
					slider.setUI(new BasicSliderUI(slider));
					popup.add(slider);
					popup.show(PaintPropertyView.this, opacitySpinner.getX(), opacitySpinner.getHeight());
				}
			});
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent e) {
			JSpinner spinner = (JSpinner) e.getSource();
			SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
			alpha = (Double) model.getValue()/100;
			textValue = String.valueOf(new DecimalFormat("#.#").format(alpha*100));
			if (controller != null) {
				controller.handleOpacityChange(alpha);
			}
			repaint();
		}
		
		/*
		 * 
		 */
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			super.paintComponent(g);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha)); 
			g2.setColor(colorField.getBackground());
			g2.fillRect(getBounds().x + 3, getBounds().y + 3, getBounds().x + getBounds().width -6, getBounds().y + getBounds().height -6);
			g2.setFont(Utils.MAIN_FONT);
			g2.setColor(Color.black);
			g2.drawString(textValue, 5, getHeight()/2 + 3);
		}
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class ComboWheelListener implements MouseWheelListener {
		private JComboBox source;

		
		public ComboWheelListener(JComboBox source) {
			this.source = source;
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int amount = e.getWheelRotation() > 0? 1 : - 1;
			int index = source.getSelectedIndex() + amount;
			
			if (index < source.getModel().getSize() && index >= 0)
				source.setSelectedIndex(index);
		}
	}
	
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class SpinnerWheelListener implements MouseWheelListener {
		
		private JSpinner source;
		private SpinnerNumberModel model;
		private Double  max;
		private Double min;
		private Double step;
		
		public SpinnerWheelListener(JSpinner source) {
			this.source = source;
			model = (SpinnerNumberModel) source.getModel();
			max = (Double) model.getMaximum();
			min = (Double) model.getMinimum();
			step = (Double) model.getStepSize();
		}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			double amount = -1 * e.getWheelRotation() * step;
			double value = (Double)source.getValue() + amount;
			
			if (value + amount < max && value + amount > min)
				source.setValue(value+amount);
		}
	}
	
	/**
	 * 
	 * @author 
	 *          Sebastian Treu
	 *          sebastian.treu(at)gmail.com
	 *
	 */
	private class ColorSelectionListener extends MouseAdapter {
		private JComponent    component;
		
		public ColorSelectionListener(JComponent c) {
			component     = c;
		}
		
		public void mouseClicked(MouseEvent e) {
			final ColorSelector colorSelector = new ColorSelector(
					component.getBackground(),
					component.getLocationOnScreen().x,
					component.getLocationOnScreen().y + 24,
					true
			);
			colorSelector.addComponentListener(new ComponentListener(){
				public void componentHidden(ComponentEvent e) {
					manageColorChange(colorSelector, component);
				}
				public void componentMoved(ComponentEvent e) {}
				public void componentResized(ComponentEvent e) {}
				public void componentShown(ComponentEvent e) {}
			});
		}
	}
}
