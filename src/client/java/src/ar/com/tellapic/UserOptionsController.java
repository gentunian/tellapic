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

import java.awt.Color;

import ar.com.tellapic.chat.ChatView;
import ar.com.tellapic.graphics.Drawing;
import ar.com.tellapic.graphics.PaintPropertyColor;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class UserOptionsController {
	
	private ChatView chatView;
	
	public UserOptionsController(ChatView view) {
		chatView = view;
	}
	
	
	public void initiateChat(AbstractUser user) {
		chatView.createNewChatTab(user.getName());
	}
	
	public void showCustomColorPopup(AbstractUser user, int x, int y) {
		CustomPropertiesDialog popup = null;
		PaintPropertyColor c = (PaintPropertyColor) user.getCustomColor();
		
		popup = new CustomPropertiesDialog(null, true, user, c);
		popup.setLocation(x - popup.getSize().width, y);
		popup.setVisible(true);
		if (popup.getReturnStatus() != CustomPropertiesDialog.RET_CANCEL) {
			try {
				Color color = popup.getCustomColor();
				if (color != null)
					user.setCustomProperty(new PaintPropertyColor(color), AbstractUser.CUSTOM_PAINT_PROPERTY_COLOR);
				else
					user.removeCustomColor();
			} catch (NoSuchPropertyTypeException e1) {
				e1.printStackTrace();
			} catch (WrongPropertyTypeException e1) {
				e1.printStackTrace();
			}
		} else {
			user.removeCustomColor();
		}
	}
	
	public void setUserVisible(AbstractUser user, boolean visible) {
		user.setVisible(visible);
	}


	/**
	 * @param user
	 */
	public void toggleUserVisibility(AbstractUser user) {
		boolean oldValue = user.isVisible();
		user.setVisible(!oldValue);
	}


	/**
	 * @param drawing
	 */
	public void toggleDrawingVisibility(Drawing drawing) {
		drawing.getUser().changeDrawingVisibility(drawing);
	}
}
