package org.odata4j.producer.inmemory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class BeanBasedPropertyModel implements PropertyModel {

	private final Class<?> clazz;
	private final Map<String,Method> getters;
	
	
	public BeanBasedPropertyModel(Class<?> clazz){
		this.clazz = clazz;
		this.getters = getBeanGetters(clazz);
	}
	
	@Override
	public Iterable<String> getPropertyNames() {
		return getters.keySet();
	}



	@Override
	public Class<?> getPropertyType(String propertyName) {
		return getGetter(propertyName).getReturnType();
	}
	
	private Method getGetter(String propertyName){
		Method method = getters.get(propertyName);
		if (method==null)
			throw new IllegalArgumentException("No getter found for propertyName " + propertyName);
		return method;
	}
	
	@Override
	public Object getPropertyValue(Object target, String propertyName) {
		
		Method method = getGetter(propertyName);
		if (!method.isAccessible())
			method.setAccessible(true);
		
		try {
			return method.invoke(target);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	
	
	
	private static Map<String,Method> getBeanGetters(Class<?> clazz){
		
		 Map<String,Method> rt = new HashMap<String,Method>();
		 for(Method method : clazz.getMethods()){
			String methodName = method.getName();
			if (
					methodName.startsWith("get") 
					&& methodName.length()>3 
					&& Character.isUpperCase(methodName.charAt(3))
					&& method.getParameterTypes().length==0
					&& !method.getReturnType().equals(Void.TYPE)
					&& !Modifier.isStatic(method.getModifiers())
					
					){
				String name = methodName.substring(3);
				rt.put(name, method);
				
			}
			if (
					methodName.startsWith("is") 
					&& methodName.length()>2
					&& Character.isUpperCase(methodName.charAt(2))
					&& method.getParameterTypes().length==0
					&& (method.getReturnType().equals(Boolean.class) || method.getReturnType().equals(Boolean.TYPE))
					&& !Modifier.isStatic(method.getModifiers())
					){
				String name = methodName.substring(2);
				rt.put(name, method);
				
			}
		 }
		 return rt;
	}



	

}
