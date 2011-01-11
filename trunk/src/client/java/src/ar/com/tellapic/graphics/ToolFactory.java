package ar.com.tellapic.graphics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ar.com.tellapic.Utils;

public class ToolFactory {
	private static Map<Integer, String> registeredTools = Collections.synchronizedMap(new HashMap<Integer,String>());
	
	public static boolean registerTool(int id, String toolClassName) {
		Utils.logMessage("Adding tool name "+toolClassName);
		Integer toolId = Integer.valueOf(id);
		registeredTools.put(toolId, toolClassName);
		//TODO: WTF?
		return true;
	}
	
	
	public static Map<Integer, String> getRegisteredToolNames() {
		return registeredTools;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Tool createTool(String toolClassName) {
		Tool tool = null;		
		if (registeredTools.containsValue(toolClassName)) {
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
