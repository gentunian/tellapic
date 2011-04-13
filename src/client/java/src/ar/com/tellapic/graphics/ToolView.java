/**
 * 
 */
package ar.com.tellapic.graphics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import ar.com.tellapic.utils.Utils;

/**
 * 
 * @author Sebastian Treu, mailTo: sebastian.treu (at) gmail.com
 *
 */
public class ToolView extends JPanel implements Observer {
//	private static final int WIDTH    = 100;
//	private static final int HEIGHT   = 150;
	private static final int ICON_GAP = 5;
	private static final int ICON_SIZE = 18;
	private static final long serialVersionUID = 1L;
	
	public static final int NO_VALUE = -1;
	public static final int UPDATE_VIEW = 0;
	
	// icon path
	//TODO: use getResource()
//	public static final String ELLIPSE_ICON_PATH   = "/icons/ellipse.png";
//	public static final String LINE_ICON_PATH      = "/icons/line.png";
//	public static final String MARKER_ICON_PATH    = "/icons/pencil.png";
//	public static final String RECTANGLE_ICON_PATH = "/icons/rectangle.png";
//	public static final String TEXT_ICON_PATH      = "/icons/text.png";
//	public static final String ZOOM_ICON_PATH      = "/icons/zoom.png";
	
	
	// The toolbox controller interface
	private IToolBoxController controller;
	
	// ButtonGroup object that guarantees that only 1 button is selected
	private ButtonGroup         buttonGroup;
	private ActionListener      listener;
	
	
	public ToolView(IToolBoxState modelState) {
		Dimension iconDimension = new Dimension(ICON_SIZE, ICON_SIZE);
		Dimension minPanelDimension = new Dimension(iconDimension.width + ICON_GAP, Short.MAX_VALUE);
		setName("");
		//setLayout(new FlowLayout());//FlowLayout.CENTER, ICON_GAP, ICON_GAP));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMinimumSize(minPanelDimension);
		setMaximumSize(minPanelDimension);
		setPreferredSize(minPanelDimension);
		setBorder(new javax.swing.border.EmptyBorder(ICON_GAP, ICON_GAP, ICON_GAP, ICON_GAP));
//		setBorder(BorderFactory.createLineBorder(java.awt.Color.DARK_GRAY, 5));
		
		buttonGroup = new ButtonGroup();
		listener    = new MyActionListener();
		Utils.logMessage("ToolView instantiated");

//		addContainerListener(new ContainerListener() {
//			@Override
//			public void componentAdded(ContainerEvent arg0) {
//				Dimension size = ((FlowLayout)getLayout()).minimumLayoutSize(ToolView.this);
//				setMinimumSize(new Dimension(ICON_SIZE + 20, ICON_SIZE * getComponentCount() + 20));
//				setPreferredSize(new Dimension(ICON_SIZE * (getComponentCount() / 2), ICON_SIZE * (getComponentCount() / 2) + 10));
//				repaint();
//			}
//
//			@Override
//			public void componentRemoved(ContainerEvent arg0) {
//				Dimension size = ((FlowLayout)getLayout()).minimumLayoutSize(ToolView.this);
//				setMinimumSize(new Dimension(ICON_SIZE + 20, ICON_SIZE * getComponentCount() + 20));
//				setPreferredSize(size);
//				repaint();
//			}
//		});
		
		for(Map.Entry<String, Tool> tool : modelState.getTools().entrySet())
			addButton(tool.getValue());
		
//		
//		JLabel color = new JLabel();
//		color.setMinimumSize(iconDimension);
//		color.setMaximumSize(iconDimension);
//		color.setPreferredSize(iconDimension);
//		color.setOpaque(true);
//		color.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(209, 209, 209), 1, true), new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED)));
//		color.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//		color.setToolTipText(Utils.msg.getString("colorfieldtooltip"));
//		add(Box.createVerticalGlue());
		//add(color);
	}
	

	//TODO fix! reveer
	private void addButton(Tool tool) {
		JToggleButton  button = new JToggleButton(new ImageIcon(Utils.createIconImage(ICON_SIZE, ICON_SIZE, tool.getIconPath())));
		//JToggleButton  button = new JToggleButton(new ImageIcon(getClass().getResource(tool.getIconPath())));
		//button.setRolloverIcon(rolloverIcon);
		button.setFocusPainted(true);
//		button.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
//		button.setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));
//		button.setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
		button.setBorderPainted(false);
		button.setRolloverEnabled(true);
		//button.setMargin(new Insets(ICON_GAP, ICON_GAP, ICON_GAP, ICON_GAP));
		//button.setBorder(new EmptyBorder(ICON_GAP, ICON_GAP, ICON_GAP, ICON_GAP));
		button.setName(tool.getName());
		button.addActionListener(listener);
		button.setToolTipText(tool.getToolTipText());
		buttonGroup.add(button);
		add(button);
	}
	
	
	private void removeButton() {
		
	}
	
	

	public void setController(IToolBoxController c) {
		controller = c;
	}
	
	

	@Override
	public void update(Observable observable, Object arg) {
		if (arg instanceof ToolBoxModel.ActionData) {
			ToolBoxModel.ActionData data = (ToolBoxModel.ActionData) arg;
			int action = data.getAction();
			if (action == ToolBoxModel.ADD_TOOL) {
				Tool tool = data.getData();
				if (tool.isEnabled())
					addButton(tool);
				else
					removeButton();
			}
		}
	}
	

	
	
	private class MyActionListener implements ActionListener {
		
	
		@Override
		public void actionPerformed(ActionEvent e) {
			if (controller != null) {
				//Tool tool = ToolFactory.getTool(((JToggleButton) e.getSource()).getName());
				controller.selectToolByName(((JToggleButton) e.getSource()).getName());
			}
		}		
	}
}
