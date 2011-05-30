package ar.com.tellapic.graphics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ar.com.tellapic.utils.Utils;

public class ToolFactory {
	private static Map<Integer, String> registeredToolsClassNames = Collections.synchronizedMap(new HashMap<Integer,String>());
	
	public static boolean registerToolClassName(int id, String toolClassName) {
		Utils.logMessage("Registering tool name "+toolClassName);
//		Integer toolId = Integer.valueOf(id);
		registeredToolsClassNames.put(id, toolClassName);
		//TODO: WTF?
		return true;
	}
	
	
	public static Map<Integer, String> getRegisteredToolsClassNames() {
		return registeredToolsClassNames;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Tool createTool(String toolClassName) {
		Tool tool = null;		
		if (registeredToolsClassNames.containsValue(toolClassName)) {
			Class<Tool> toolClass = null;
			//Method instanceMethod = null;
			try {
				toolClass      = (Class<Tool>) Class.forName(toolClassName);
				//instanceMethod = toolClass.getMethod("getInstance");
				//tool           = (Tool) instanceMethod.invoke((Object[])null, (Object[])null);
				tool = toolClass.newInstance();
			} catch (ClassNotFoundException    e) {
				e.printStackTrace();
			} catch (IllegalAccessException    e) {
				Method instanceMethod = null;
				try {
					instanceMethod = toolClass.getMethod("getInstance");
					tool           = (Tool) instanceMethod.invoke((Object[])null, (Object[])null);
				} catch (SecurityException e1) {
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return tool;
	}
}
