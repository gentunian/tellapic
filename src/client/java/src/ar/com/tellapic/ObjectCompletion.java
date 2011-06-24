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

import java.util.ArrayList;
import java.util.List;

import org.fife.ui.autocomplete.AbstractCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ObjectCompletion extends AbstractCompletion {

	private String                         name;
	private List<ObjectMethodCompletion>   methods;
	
	/**
	 * @param provider
	 */
	public ObjectCompletion(CompletionProvider provider, String name) {
		super(provider);
		this.name = name;
		methods = new ArrayList<ObjectMethodCompletion>();
	}

	/* (non-Javadoc)
	 * @see org.fife.ui.autocomplete.Completion#getReplacementText()
	 */
	@Override
	public String getReplacementText() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.fife.ui.autocomplete.Completion#getSummary()
	 */
	@Override
	public String getSummary() {
		return "Summary";
	}
	
	/**
	 * 
	 * @param m
	 */
	public void addMethod(ObjectMethodCompletion m) {
		if (m != null)
			methods.add(m);
	}

	/**
	 * 
	 */
	public List<ObjectMethodCompletion> getMethodCompletions() {
		return methods;
	}
}
