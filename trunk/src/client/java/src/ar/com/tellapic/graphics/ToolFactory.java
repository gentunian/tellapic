package ar.com.tellapic.graphics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ar.com.tellapic.utils.Utils;

public class ToolFactory {
	private static Map<Integer, String> registeredToolsClassNames = Collections.synchronizedMap(new HashMap<Integer,String>());
	
	public static boolean registerToolClassName(int id, String toolClassName) {
		Utils.logMessage("Adding tool name "+toolClassName);
		Integer toolId = Integer.valueOf(id);
		registeredToolsClassNames.put(toolId, toolClassName);
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
