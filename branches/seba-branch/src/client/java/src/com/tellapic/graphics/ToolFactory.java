package com.tellapic.graphics;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.tellapic.Utils;

public class ToolFactory {
	private static Set<String> registeredTools = Collections.synchronizedSet(new HashSet<String>());
	
	public static boolean registerTool(String className) {
		Utils.logMessage("Adding tool name "+className);
		return registeredTools.add(className);
	}
	
	
	public static Set<String> getRegisteredToolNames() {
		return registeredTools;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Tool createTool(String toolName) {
		Tool tool = null;		
		if (registeredTools.contains(toolName)) {
			Class<Tool> toolClass = null;
			//Method instanceMethod = null;
			try {
				toolClass      = (Class<Tool>) Class.forName(toolName);
				//instanceMethod = toolClass.getMethod("getInstance");
				//tool           = (Tool) instanceMethod.invoke((Object[])null, (Object[])null);
				tool = toolClass.newInstance();
			} catch (ClassNotFoundException    e) {
				e.printStackTrace();
			} catch (SecurityException         e) {
				e.printStackTrace();
			} catch (IllegalAccessException    e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tool;
	}
}
