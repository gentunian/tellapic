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

import java.beans.PropertyChangeEvent;

import ar.com.tellapic.graphics.AbstractDrawing;
import ar.com.tellapic.graphics.DrawingShape;
import ar.com.tellapic.utils.Utils;


/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class TellapicRemoteUser extends TellapicAbstractUser {
	
	/**
	 * @param id
	 */
	public TellapicRemoteUser(int id, String name) {
		super(id, name);
		setRemote(true);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		AbstractDrawing drawing = (AbstractDrawing) evt.getSource();
		String property = evt.getPropertyName();
		Utils.logMessage("property changed: "+property+" to value: "+evt.getNewValue()+" on object: "+drawing.hashCode());
		if (property.endsWith(DrawingShape.PROPERTY_SELECTION)) {
			boolean selected = (Boolean) evt.getNewValue();
			if (selected) {
				for(AbstractDrawing d : getDrawings())
					if (!d.equals(drawing))
						d.setSelected(false);
			}
		}
		setChanged();
		notifyObservers(new Object[]{DRAWING_CHANGED, drawing, drawingList.indexOf(drawing)});
	}
}
