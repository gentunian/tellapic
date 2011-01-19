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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ar.com.tellapic.utils.Utils;

public class PaintPropertyView extends JPanel implements Observer {
	
	// Private constants resources
	private static final long   serialVersionUID = 1L;
	//private static final int    BUTTON_VGAP = 15;
	private static final int    BUTTON_HGAP = 5;
	private static final int    ICON_SIZE   = 24;
	private static final String BEVEL_JOIN_ICON_PATH  = "/icons/bevel_join.png";
	private static final String ROUND_JOIN_ICON_PATH  = "/icons/round_join.png";
	private static final String MITER_JOIN_ICON_PATH  = "/icons/miter_join.png";
	private static final String ROUND_END_ICON_PATH   = "/icons/round_cap.png";
	private static final String BUTT_END_ICON_PATH    = "/icons/butt_cap.png";
	private static final String SQUARE_END_ICON_PATH  = "/icons/square_cap.png";
	private static final String SET_CAP_ROUND_ACTION  = "capround";
	private static final String SET_CAP_SQUARE_ACTION = "capsquare";
	private static final String SET_CAP_BUTT_ACTION   = "capbutt";
	private static final String SET_JOIN_BEVEL_ACTION = "joinbevel";
	private static final String SET_JOIN_ROUND_ACTION = "joinround";
	private static final String SET_JOIN_MITER_ACTION = "joinmiter";
	private static final String SET_FONT_FACE_ACTION  = "face";
	private static final String SET_FONT_SIZE_ACTION  = "size";
	private static final String SET_FONT_STYLE_ACTION = "style";
	private static final String[]         FONT_STYLES = new String[] { "normal", "bold", "italic", "italic+bold" };
	
	//TODO: fix this font issue!
	private final Font               mainFont    = Font.decode("Droid-10");
	private final JPanel             strokePanel  = new JPanel();
	private final JPanel             fontPanel    = new JPanel();
	private final JPanel             colorPanel   = new JPanel();
	private final JPanel             opacityPanel = new JPanel();
	private Dimension                strokePanelDimension;
	private Dimension                fontPanelDimension;
	private Dimension                colorPanelDimension;
	private Dimension                opacityPanelDimension;
	private IPaintPropertyController controller;
	//private final Font[]             systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
	

	public PaintPropertyView() {
		setName("PropertyView");
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		strokePanelDimension  = createStrokePanel();
		fontPanelDimension    = createFontPanel();
		colorPanelDimension   = createColorPanel();
		opacityPanelDimension = createOpacityPanel();
		setBorder(BorderFactory.createTitledBorder(null, "title", TitledBorder.LEFT, TitledBorder.TOP, Font.decode("Droid-BOLD-10"), Color.white));
		printSizes();
	}
	
	private void printSizes() {
		Utils.logMessage("strokePanelDimension: ("+strokePanelDimension.width+","+strokePanelDimension.height+")");
		Utils.logMessage("colorPanelDimension: ("+colorPanelDimension.width+","+colorPanelDimension.height+")");
		Utils.logMessage("fontPanelDimension: ("+fontPanelDimension.width+","+fontPanelDimension.height+")");
		Utils.logMessage("opacityPanelDimension: ("+opacityPanelDimension.width+","+opacityPanelDimension.height+")");
		Utils.logMessage("this panel Dimension: ("+getSize().width+","+getSize().height+")");
	}
	
	private void setStrokeProperties(PaintPropertyStroke strokeProperty) {
		int capsValue  = strokeProperty.getEndCaps();
		int joinValue  = strokeProperty.getLineJoins();
		int widthValue = (int) strokeProperty.getWidth();
		for(Component item : strokePanel.getComponents()) {
			if (item instanceof JSlider) {
				JSlider slider = ((JSlider) item);
				if (slider.getName().equals("width"))
					slider.setValue(widthValue);
				
			} else if (item instanceof JToggleButton) {
				JToggleButton button = ((JToggleButton) item);
				if      (button.getActionCommand().equals(SET_CAP_BUTT_ACTION) && capsValue == BasicStroke.CAP_BUTT && !button.isSelected())
					button.doClick();
				else if (button.getActionCommand().equals(SET_CAP_ROUND_ACTION) && capsValue == BasicStroke.CAP_ROUND && !button.isSelected())
					button.doClick();
				else if (button.getActionCommand().equals(SET_CAP_SQUARE_ACTION) && capsValue == BasicStroke.CAP_SQUARE && !button.isSelected())
					button.doClick();
				else if (button.getActionCommand().equals(SET_JOIN_BEVEL_ACTION) && joinValue == BasicStroke.JOIN_BEVEL && !button.isSelected())
					button.doClick();
				else if (button.getActionCommand().equals(SET_JOIN_MITER_ACTION) && joinValue == BasicStroke.JOIN_MITER && !button.isSelected())
					button.doClick();
				else if (button.getActionCommand().equals(SET_JOIN_ROUND_ACTION) && joinValue == BasicStroke.JOIN_ROUND && !button.isSelected())
					button.doClick();
			}
		}
	}
	
	private void setFontProperties(PaintPropertyFont fontProperty) {
		
	}
	
	private void setColorProperties(PaintPropertyColor colorProperty) {
	
	}
	
	private void setAlphaProperties(PaintPropertyAlpha opacityProperty) {
	
	}
	
	/**
	 * 
	 * @param c the controller to set
	 */
	public void setController(IPaintPropertyController c) {
		controller = c;
		controller.handleColorChange(Color.black);
	}
	
	
	/**
	 * 
	 * @param enabled
	 */
	public void setStrokePanelEnabled(boolean enabled) {
//		Utils.logMessage("Enabling stroke panel: "+enabled+" current size: ("+getSize().width+","+getSize().height+")");
//		int h = getSize().height + (strokePanelDimension.height * (enabled? 1 : -1));
//		Utils.logMessage("new height calculated: "+h);
		if (enabled)
			add(strokePanel);

		else
			remove(strokePanel);
//		Utils.logMessage("new size after adding item: ("+getSize().width+","+getSize().height+")");
//		setMinimumSize(new Dimension(getSize().width,  h));
//		printSizes();
		this.validate();
	}
	
	
	/**
	 * 
	 * @param enabled
	 */
	public void setOpacityPanelEnabled(boolean enabled) {
//		Utils.logMessage("Enabling opacity panel: "+enabled+" current size: ("+getSize().width+","+getSize().height+")");
//		int h = getSize().height + (opacityPanelDimension.height * (enabled? 1 : -1) );
//		Utils.logMessage("new height calculated: "+h);
		if (enabled)
			add(opacityPanel);
		else
			remove(opacityPanel);
//		Utils.logMessage("new size after adding item: ("+getSize().width+","+getSize().height+")");
//		
//		setMinimumSize(new Dimension(getSize().width, h));	
//		printSizes();
		this.validate();
	}
	
	
	/**
	 * 
	 * @param enabled
	 */
	public void setFontPanelEnabled(boolean enabled) {
		if (enabled)
			add(fontPanel);
		else
			remove(fontPanel);
//		int h = getSize().height + (fontPanelDimension.height * (enabled? 1 : -1) );
//		Utils.logMessage("new h: "+h);
//		setMinimumSize(new Dimension(getSize().width, h));
//		printSizes();
		this.validate();
	}
	
	
	/**
	 * 
	 * @param enabled
	 */
	public void setColorPanelEnabled(boolean enabled) {
//		Utils.logMessage("Enabling color panel: "+enabled+" current size: ("+getSize().width+","+getSize().height+")");
//		int h = getSize().height + (colorPanelDimension.height * (enabled? 1 : -1) );
//		Utils.logMessage("new height calculated: "+h);
		if (enabled)
			add(colorPanel);
		else
			remove(colorPanel);
//		Utils.logMessage("new size after adding item: ("+getSize().width+","+getSize().height+")");
//		
//		setMinimumSize(new Dimension(getSize().width, h));
//		printSizes();
		this.validate();
	}
	
	
	private Dimension createOpacityPanel() {
		JLabel        opacityLabel = new JLabel("Opacity:");
		JSlider       opacity      = new JSlider();
		FontMetrics   metrics = opacityLabel.getFontMetrics(mainFont);
		SliderChangeListener listener    = new SliderChangeListener();
		
		opacity.setPaintTicks(true);
		opacity.setName("opacity");
		opacity.addChangeListener(listener);
		opacity.setMinorTickSpacing(10);
		opacity.setValue(100);
		opacityLabel.setLabelFor(opacity);
		opacityLabel.setFont(mainFont);
		opacityLabel.setMinimumSize(new Dimension(metrics.stringWidth(opacityLabel.getText()) + 8, metrics.getHeight()));
		
		GroupLayout layout = new GroupLayout(opacityPanel);
		opacityPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(opacityLabel)
						.addComponent(opacity)
				)
		);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(opacityLabel)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(opacity)
				)
		);
		
		opacityPanel.setMinimumSize(layout.minimumLayoutSize(opacityPanel));
		return opacityPanel.getMinimumSize();
	}
	 
	
	private Dimension createColorPanel() {
		JLabel  foregroundColor = new JLabel();
		JLabel  backgroundColor = new JLabel();
		Dimension minPanelSize  = new Dimension(50, 50);
		Dimension labelSize     = new Dimension(40, 30);	
		
		foregroundColor.setMinimumSize(labelSize);
		foregroundColor.setPreferredSize(labelSize);
		foregroundColor.setBackground(Color.black);
		foregroundColor.setOpaque(true);
		foregroundColor.setBorder(BorderFactory.createLoweredBevelBorder());
		backgroundColor.setMinimumSize(labelSize);
		backgroundColor.setPreferredSize(labelSize);
		backgroundColor.setBackground(Color.white);
		backgroundColor.setOpaque(true);
		backgroundColor.setBorder(BorderFactory.createLoweredBevelBorder());
		colorPanel.add(foregroundColor);
		colorPanel.add(backgroundColor);
		
		foregroundColor.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		foregroundColor.addMouseListener(new ColorChangeListener());
		
		return minPanelSize;
	}

	
	private Dimension createFontPanel() {
		//TODO: make constants
		Integer[] fontSizes = new Integer[140 - 8];
		for(int i = 8; i < 140; i++)
			fontSizes[i - 8] = i;
		JLabel       sizeLabel      = new JLabel("Size:");
		JLabel       faceLabel      = new JLabel("Face:");
		JLabel       styleLabel     = new JLabel("Style:");
		JLabel       textLabel      = new JLabel("Text:");
		JComboBox    fontFaceCombo  = new JComboBox(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
		JComboBox    fontStyleCombo = new JComboBox(FONT_STYLES);
		JComboBox    fontSizeCombo  = new JComboBox(fontSizes);
		JTextField   text           = new JTextField();
		FontListener fontListener   = new FontListener();
		
		for(String item : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {			
			FontMetrics metrics = fontFaceCombo.getFontMetrics(mainFont);
			fontFaceCombo.setMinimumSize(new Dimension(metrics.stringWidth(item) + 8, metrics.getHeight()));
		}
		
		for(JLabel item : new JLabel[] { sizeLabel, faceLabel, styleLabel, textLabel}) {
			String txt = item.getText();
			FontMetrics metrics = item.getFontMetrics(mainFont);
			item.setMinimumSize(new Dimension(metrics.stringWidth(txt) + 8, metrics.getHeight()));
		}
		
		fontFaceCombo.setActionCommand(SET_FONT_FACE_ACTION);
		fontSizeCombo.setActionCommand(SET_FONT_SIZE_ACTION);
		fontStyleCombo.setActionCommand(SET_FONT_STYLE_ACTION);
		fontFaceCombo.addActionListener(fontListener);
		fontSizeCombo.addActionListener(fontListener);
		fontStyleCombo.addActionListener(fontListener);
		sizeLabel.setLabelFor(fontSizeCombo);
		faceLabel.setLabelFor(fontFaceCombo);
		styleLabel.setLabelFor(fontStyleCombo);
		textLabel.setLabelFor(text);
		textLabel.setFont(mainFont);
		sizeLabel.setFont(mainFont);
		faceLabel.setFont(mainFont);
		styleLabel.setFont(mainFont);
		fontFaceCombo.setFont(mainFont);
		fontStyleCombo.setFont(mainFont);
		fontSizeCombo.setFont(mainFont);
		fontSizeCombo.setMinimumSize(fontFaceCombo.getMinimumSize());
		fontStyleCombo.setMinimumSize(fontFaceCombo.getMinimumSize());
		text.addCaretListener(new CaretListener(){
			@Override
			public void caretUpdate(CaretEvent arg0) {
				if (controller != null)
					controller.handleTextChange(((JTextField) arg0.getSource()).getText());
			}
		});
		
		GroupLayout layout = new GroupLayout(fontPanel);
		fontPanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(faceLabel)
						.addComponent(fontFaceCombo)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(sizeLabel)
						.addComponent(fontSizeCombo)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(styleLabel)
						.addComponent(fontStyleCombo)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(textLabel)
						.addComponent(text)
				)
		);
		
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(faceLabel)
						.addComponent(sizeLabel)
						.addComponent(styleLabel)
						.addComponent(textLabel)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(fontFaceCombo)
						.addComponent(fontSizeCombo)
						.addComponent(fontStyleCombo)
						.addComponent(text)
				)
		);
		
		fontPanel.setMinimumSize(layout.minimumLayoutSize(fontPanel));
		return fontPanel.getMinimumSize();
	}
	
	
	private Image createIconImage(int w, int h, String path) {
		Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource(path));
		return image.getScaledInstance(w, h, Image.SCALE_SMOOTH);	 
	}
	
	
	private Dimension createStrokePanel() {
		Dimension     minButtonDimension = new Dimension(ICON_SIZE + BUTTON_HGAP, ICON_SIZE + BUTTON_HGAP);
		Dimension     minSliderDimension = new Dimension(minButtonDimension.width * 3, minButtonDimension.height - BUTTON_HGAP);
		JSlider       widthSlider        = new JSlider(0, 20);
		ButtonGroup   endCapsGroup       = new ButtonGroup();
		ButtonGroup   lineJoinGroup      = new ButtonGroup();
		ImageIcon     bevelJoinIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, BEVEL_JOIN_ICON_PATH)); 
		ImageIcon     miterJoinIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, MITER_JOIN_ICON_PATH));
		ImageIcon     roundJoinIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, ROUND_JOIN_ICON_PATH));
		ImageIcon     buttEndIcon        = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, BUTT_END_ICON_PATH));
		ImageIcon     squareEndIcon      = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, SQUARE_END_ICON_PATH));
		ImageIcon     roundEndIcon       = new ImageIcon(createIconImage(ICON_SIZE, ICON_SIZE, ROUND_END_ICON_PATH));
		JToggleButton endCapsButt        = new JToggleButton(buttEndIcon);
		JToggleButton endCapsRound       = new JToggleButton(roundEndIcon);
		JToggleButton endCapsSquare      = new JToggleButton(squareEndIcon);
		JToggleButton lineJoinBevel      = new JToggleButton(bevelJoinIcon);
		JToggleButton lineJoinMiter      = new JToggleButton(miterJoinIcon);
		JToggleButton lineJoinRound      = new JToggleButton(roundJoinIcon);
		JLabel        widthLabel         = new JLabel("Width:");
		JLabel        joinLabel          = new JLabel("Join:");
		JLabel        capLabel           = new JLabel("Cap:");
		JLabel        dashLabel          = new JLabel("Dash:");
		JComboBox     miterLimit         = new JComboBox();
		SliderChangeListener listener    = new SliderChangeListener();
		ButtonListener       buttonListener = new ButtonListener();
		
		endCapsButt.setActionCommand(SET_CAP_BUTT_ACTION);
		endCapsRound.setActionCommand(SET_CAP_ROUND_ACTION);
		endCapsSquare.setActionCommand(SET_CAP_SQUARE_ACTION);
		lineJoinBevel.setActionCommand(SET_JOIN_BEVEL_ACTION);
		lineJoinMiter.setActionCommand(SET_JOIN_MITER_ACTION);
		lineJoinRound.setActionCommand(SET_JOIN_ROUND_ACTION);
		
		// Listeners setup
		endCapsButt.addActionListener(buttonListener);
		endCapsRound.addActionListener(buttonListener);
		endCapsSquare.addActionListener(buttonListener);
		lineJoinBevel.addActionListener(buttonListener);
		lineJoinMiter.addActionListener(buttonListener);
		lineJoinRound.addActionListener(buttonListener);
		
		widthSlider.addChangeListener(listener);
		widthSlider.setName("width");
		widthSlider.setPaintTicks(true);
		widthSlider.setMinorTickSpacing(1);
		widthSlider.setValue(1);
		
		widthLabel.setLabelFor(widthSlider);
		dashLabel.setLabelFor(miterLimit);
		
		widthLabel.setFont(mainFont);
		joinLabel.setFont(mainFont);
		capLabel.setFont(mainFont);
		dashLabel.setFont(mainFont);
		
		endCapsGroup.add(endCapsButt);
		endCapsGroup.add(endCapsRound);
		endCapsGroup.add(endCapsSquare);
		lineJoinGroup.add(lineJoinBevel);
		lineJoinGroup.add(lineJoinMiter);
		lineJoinGroup.add(lineJoinRound);
		
		widthSlider.setMinimumSize(minSliderDimension);
		miterLimit.setMinimumSize(minSliderDimension);
		
		endCapsRound.setMinimumSize(minButtonDimension);
		endCapsRound.setPreferredSize(minButtonDimension);
		endCapsRound.setMaximumSize(minButtonDimension);
		
		endCapsButt.setMinimumSize(minButtonDimension);
		endCapsButt.setPreferredSize(minButtonDimension);
		endCapsButt.setMaximumSize(minButtonDimension);
		
		endCapsSquare.setMinimumSize(minButtonDimension);
		endCapsSquare.setPreferredSize(minButtonDimension);
		endCapsSquare.setMaximumSize(minButtonDimension);
		endCapsSquare.doClick();
		
		lineJoinBevel.setMinimumSize(minButtonDimension);
		lineJoinBevel.setPreferredSize(minButtonDimension);
		lineJoinBevel.setMaximumSize(minButtonDimension);
		
		lineJoinRound.setMinimumSize(minButtonDimension);
		lineJoinRound.setPreferredSize(minButtonDimension);
		lineJoinRound.setMaximumSize(minButtonDimension);
		
		lineJoinMiter.setMinimumSize(minButtonDimension);
		lineJoinMiter.setMaximumSize(minButtonDimension);
		lineJoinMiter.setPreferredSize(minButtonDimension);
		lineJoinMiter.doClick();
		
		for(JLabel item : new JLabel[] { dashLabel, widthLabel, joinLabel, capLabel}) {
			String txt = item.getText();
			FontMetrics metrics = item.getFontMetrics(mainFont);
			item.setMinimumSize(new Dimension(metrics.stringWidth(txt) + 8, metrics.getHeight()));
		}
		
		GroupLayout layout = new GroupLayout(strokePanel);
		strokePanel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(widthLabel)
						.addComponent(widthSlider)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(joinLabel)
						.addComponent(lineJoinBevel)
						.addComponent(lineJoinRound)
						.addComponent(lineJoinMiter)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(capLabel)
						.addComponent(endCapsRound)
						.addComponent(endCapsButt)
						.addComponent(endCapsSquare)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(dashLabel)
						.addComponent(miterLimit)
				)
		);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(widthLabel)
						.addComponent(joinLabel)
						.addComponent(capLabel)
						.addComponent(dashLabel)
				)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(widthSlider)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(lineJoinBevel)
										.addComponent(endCapsRound)
								)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(lineJoinRound)
										.addComponent(endCapsButt)
								)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(lineJoinMiter)
										.addComponent(endCapsSquare)
								)
						)
						.addComponent(miterLimit)
				)
		);
		strokePanel.setMinimumSize(layout.minimumLayoutSize(strokePanel));
		return strokePanel.getMinimumSize();
	}
	
		
	@Override	
	public void update(Observable o, Object arg) {
		ToolBoxModel box = (ToolBoxModel) o;		
		if (arg instanceof ToolBoxModel.ActionData) {
			ToolBoxModel.ActionData data = (ToolBoxModel.ActionData) arg;
			int action = data.getAction();
			Tool  tool = data.getData();
			if (action == ToolBoxModel.SHOW_TOOL) {
				
				setStrokePanelEnabled(tool.hasStrokeProperties());
				setFontPanelEnabled(tool.hasFontProperties());
				setOpacityPanelEnabled(tool.hasAlphaProperties());
				setColorPanelEnabled(tool.hasColorProperties());
				((TitledBorder) getBorder()).setTitle(tool.getName());
				
			} else if (action == ToolBoxModel.UPDATE_TOOL) {
				
				if (tool.hasStrokeProperties())
					setStrokeProperties(box.getStrokeProperty());
				else if (tool.hasFontProperties())
					setFontProperties(box.getFontProperty());
				else if (tool.hasAlphaProperties())
					setAlphaProperties(box.getOpacityProperty());
				else if (tool.hasColorProperties())
					setColorProperties(box.getColorProperty());
			}
		}
		repaint();
	}

	
	private class FontListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String action = arg0.getActionCommand();
			JComboBox src = (JComboBox) arg0.getSource();
			
			if (action != null) {
				
				if (action.equals(SET_FONT_FACE_ACTION))
					controller.handleFontFaceChange((String)src.getSelectedItem());
				
				else if (action.equals(SET_FONT_SIZE_ACTION))
					controller.handleFontSizeChange(((Integer)src.getSelectedItem()).intValue());
				
				else if (action.equals(SET_FONT_STYLE_ACTION))
					controller.handleFontStyleChange(src.getSelectedIndex());
				
			}
		}
	}
	
	
	private class ButtonListener implements ActionListener {		

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String action = arg0.getActionCommand();
			
			if (action != null) {				
				if (action.equals(SET_CAP_SQUARE_ACTION) && controller != null)
					controller.handleEndCapsChange(BasicStroke.CAP_SQUARE);
				
				else if (action.equals(SET_CAP_ROUND_ACTION) && controller != null)
					controller.handleEndCapsChange(BasicStroke.CAP_ROUND);
				
				else if (action.equals(SET_CAP_BUTT_ACTION) && controller != null)
					controller.handleEndCapsChange(BasicStroke.CAP_BUTT);
				
				else if (action.equals(SET_JOIN_BEVEL_ACTION) && controller != null)
					controller.handleLineJoinsChange(BasicStroke.JOIN_BEVEL);
				
				else if (action.equals(SET_JOIN_ROUND_ACTION) && controller != null)
					controller.handleLineJoinsChange(BasicStroke.JOIN_ROUND);
				
				else if (action.equals(SET_JOIN_MITER_ACTION) && controller != null)
					controller.handleLineJoinsChange(BasicStroke.JOIN_MITER);
			}
		}
	}
	
	
	private class ColorChangeListener extends MouseAdapter {
		/*
		 * (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			Color c = JColorChooser.showDialog(PaintPropertyView.this, "Pick a Color", ((JLabel) e.getSource()).getBackground()); 
			if (c != null) {
				controller.handleColorChange(c);
				((JLabel) e.getSource()).setBackground(c);
			}
		}
	}
	
	private class SliderChangeListener implements ChangeListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
		 */
		@Override
		public void stateChanged(ChangeEvent arg0) {
			JSlider s = (JSlider) arg0.getSource();
			
			if (!s.getValueIsAdjusting() && controller != null) {
				int value = s.getValue();
				
				if (s.getName().equals("width"))
					controller.handleWidthChange(value);
				
				if (s.getName().equals("opacity"))
					controller.handleOpacityChange((value)/100f);

			}
		}
	}
}
