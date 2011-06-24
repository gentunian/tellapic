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

import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.FunctionCompletion;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ObjectMethodCompletion extends FunctionCompletion {

	private ObjectCompletion objectCompletion;
	private String           replacementText;
	private String           name;
	
	/**
	 * 
	 * @param provider
	 * @param name
	 * @param returnType
	 * @param o
	 */
	public ObjectMethodCompletion(CompletionProvider provider, String name, String returnType, ObjectCompletion o) {
		super(provider, name, returnType);
		
		if (o != null)
			setObjectCompletion(o);
		
		replacementText = name;
		this.name = name;
	}

	/**
	 * 
	 * @param provider
	 * @param name
	 */
	public ObjectMethodCompletion(CompletionProvider provider, String name) {
		this(provider, name, "void", null);
	}

	/**
	 * @param objectCompletion the objectCompletion to set
	 */
	public void setObjectCompletion(ObjectCompletion objectCompletion) {
		this.objectCompletion = objectCompletion;
	}

	/**
	 * @return the objectCompletion
	 */
	public ObjectCompletion getObjectCompletion() {
		return objectCompletion;
	}

	/**
	 * @param replacementText the replacementText to set
	 */
	public void setReplacementText(String replacementText) {
		this.replacementText = replacementText;
	}

	/**
	 * @return the replacementText
	 */
	@Override
	public String getReplacementText() {
		return replacementText;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.fife.ui.autocomplete.AbstractCompletion#getInputText()
	 */
	@Override
	public String getInputText() {
		return name;
	}
}
