/**
 * 
 */
package ar.com.tellapic.graphics;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

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
	private static final int ICON_SIZE = 32;
	private static final long serialVersionUID = 1L;
	
	public static final int NO_VALUE = -1;
	public static final int UPDATE_VIEW = 0;
	
	// icon path
	//TODO: use getResource()
	public static final String ELLIPSE_ICON_PATH   = "/icons/ellipse.png";
	public static final String LINE_ICON_PATH      = "/icons/line.png";
	public static final String MARKER_ICON_PATH    = "/icons/pencil.png";
	public static final String RECTANGLE_ICON_PATH = "/icons/rectangle.png";
	public static final String TEXT_ICON_PATH      = "/icons/text.png";
	public static final String ZOOM_ICON_PATH      = "/icons/zoom.png";
	
	
	// The toolbox controller interface
	private IToolBoxController controller;
	
	// ButtonGroup object that guarantees that only 1 button is selected
	private ButtonGroup         buttonGroup;
	private ActionListener      listener;
	
	
	public ToolView(IToolBoxState modelState) {
		setName("Tool");
		//setLayout(new FlowLayout());//FlowLayout.CENTER, ICON_GAP, ICON_GAP));
		setMinimumSize(new Dimension(ICON_SIZE, ICON_SIZE));
		setMaximumSize(new Dimension(ICON_SIZE, ICON_SIZE));
		setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		buttonGroup = new ButtonGroup();
		listener    = new MyActionListener();
		Utils.logMessage("ToolView instantiated");

		addContainerListener(new ContainerListener() {
			@Override
			public void componentAdded(ContainerEvent arg0) {
//				Dimension size = ((FlowLayout)getLayout()).minimumLayoutSize(ToolView.this);
//				setMinimumSize(new Dimension(ICON_SIZE + 20, ICON_SIZE * getComponentCount() + 20));
//				setPreferredSize(new Dimension(ICON_SIZE * (getComponentCount() / 2), ICON_SIZE * (getComponentCount() / 2) + 10));
				repaint();
			}

			@Override
			public void componentRemoved(ContainerEvent arg0) {
//				Dimension size = ((FlowLayout)getLayout()).minimumLayoutSize(ToolView.this);
//				setMinimumSize(new Dimension(ICON_SIZE + 20, ICON_SIZE * getComponentCount() + 20));
//				setPreferredSize(size);
				repaint();
			}
		});
		
		for(Map.Entry<String, Tool> tool : modelState.getTools().entrySet()) {
			addButton(tool.getValue());
		}
	}
	

	//TODO fix! reveer
	private void addButton(Tool tool) {
		JToggleButton  button = new JToggleButton(new ImageIcon(getClass().getResource(tool.getIconPath())));
		//button.setRolloverIcon(rolloverIcon);
		button.setFocusPainted(true);
		button.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		button.setBorderPainted(false);
		button.setRolloverEnabled(true);
		button.setMargin(new Insets(3, 3, 3, 3));
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
