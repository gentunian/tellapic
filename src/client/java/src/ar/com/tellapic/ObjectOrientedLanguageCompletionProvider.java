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
import java.util.Collections;
import java.util.List;

import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.Util;

import ar.com.tellapic.utils.Utils;

/**
 * @author 
 *          Sebastian Treu
 *          sebastian.treu(at)gmail.com
 *
 */
public class ObjectOrientedLanguageCompletionProvider extends DefaultCompletionProvider {
	
	
	public ObjectOrientedLanguageCompletionProvider() {
//		comparator = new MyComparator();
		super();
		this.setParameterizedCompletionParams('(', ",", ')');
	}
	
	@Override
	public String getAlreadyEnteredText(JTextComponent comp) {
//		String str = super.getAlreadyEnteredText(comp);
		String str = null;
		str = comp.getText();
		Utils.logMessage("getAlreadyEnteredText(): "+str);
		return str;
	}
	
//	@Override
//	public List getCompletionsAt(JTextComponent tc, Point p) {
//		Utils.logMessage("entering getCompletionsAt");
//		List list = super.getCompletionsAt(tc, p);
//		for(int i = 0; i < list.size(); i++) {
//			Utils.logMessage("getCompletionsAt: "+list.get(i));
//		}
//		return list;
//	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected List getCompletionsImpl(JTextComponent comp) {
		List retVal = new ArrayList();
		String text = getAlreadyEnteredText(comp);

		if (text!=null) {
			String newText = text;
			if (text.endsWith(".")) {
				String arrangeDecimals = text.replaceAll("([0-9]*)\\.([0-9]+)", "$1:$2");
				String[] split = arrangeDecimals.split("\\.");
				ObjectCompletion mainObjectCompletion = null;
//				ObjectCompletion lastObjectCompletion = null;
				
				for(int i = 0; i < split.length; i++) {
					Utils.logMessage("Split["+i+"]: "+split[i]);
					if (i == 0) {
						int index = Collections.binarySearch(completions, split[i], comparator);
						Utils.logMessage("indexresult: "+index);
						if (index >= 0) {
							mainObjectCompletion = (ObjectCompletion) completions.get(index);
							Utils.logMessage("c: "+mainObjectCompletion);
							for(ObjectMethodCompletion o : mainObjectCompletion.getMethodCompletions()) {
								o.setReplacementText(mainObjectCompletion.getReplacementText() +"."+ o.getInputText());
								retVal.add(o);
							}
						}
					} else if (mainObjectCompletion != null){
						String methodName = split[i].replaceAll("([a-zA-Z]+)\\([^)]*\\)", "$1");
						List<ObjectMethodCompletion> methods = mainObjectCompletion.getMethodCompletions();
						retVal = new ArrayList();
						for(ObjectMethodCompletion omc : methods) {
							if (omc.getInputText().startsWith(methodName)) {
								mainObjectCompletion = omc.getObjectCompletion();
								for(ObjectMethodCompletion c : omc.getObjectCompletion().getMethodCompletions()) {
									c.setReplacementText(getAlreadyEnteredText(comp)+ c.getInputText());
									retVal.add(c);
								}
							}
						}
					}
				}
				return retVal;
			}
			int index = Collections.binarySearch(completions, newText, comparator);
			if (index<0) { // No exact match
				index = -index - 1;
			}
			else {
				// If there are several overloads for the function being
				// completed, Collections.binarySearch() will return the index
				// of one of those overloads, but we must return all of them,
				// so search backward until we find the first one.
				int pos = index - 1;
				while (pos>0 && comparator.compare(completions.get(pos), newText)==0) {
					retVal.add(completions.get(pos));
					pos--;
				}
			}

			while (index<completions.size()) {
				Completion c = (Completion)completions.get(index);
				if (Util.startsWithIgnoreCase(c.getInputText(), newText)) {
					retVal.add(c);
					index++;
				}
				else {
					break;
				}
			}

		}

		return retVal;
	}
	
//	@Override
//	public List getCompletions(JTextComponent comp) {
//		Utils.logMessage("entering getCompletions");
//		List list = super.getCompletions(comp);
//		for(int i = 0; i < list.size(); i++) {
//			Utils.logMessage("getCompletions["+i+"]"+list.get(i));
//		}
//		return list;
//	}
//	@Override
//	public List getCompletionByInputText(String inputText) {
//		Utils.logMessage("entering getCompletionByInputText");
//		List list = super.getCompletionByInputText(inputText);
//		for(int i = 0; i < list.size(); i++) {
//			Utils.logMessage("getCompletionByInputText["+i+"]"+list.get(i));
//		}
//		return list;
//	}
}
