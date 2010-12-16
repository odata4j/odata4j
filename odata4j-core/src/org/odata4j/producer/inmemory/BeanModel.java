package org.odata4j.producer.inmemory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.core4j.Enumerable;

public class BeanModel {

    private final Class<?> beanClass;
    private final Map<String, Method> getters;
    private final Map<String, Method> setters;
    private final Map<String, Class<?>> types;
    private final Map<String, Class<?>> collections;
    
    public BeanModel(Class<?> beanClass){
        this.beanClass = beanClass;
        this.getters = getBeanGetters(beanClass);
        this.setters = getBeanSetters(beanClass);
        this.types = computeTypes(getters,setters);
        this.collections = computeCollections(getters,setters);
    }
    
	public Class<?> getBeanClass() {
        return beanClass;
    }
    
    public Iterable<String> getPropertyNames() {
        return types.keySet();
    }

    public Class<?> getPropertyType(String propertyName) {
        return types.get(propertyName);
    }

    public Iterable<String> getCollectionNames() {
        return collections.keySet();
    }
    
    public Class<?> getCollectionElementType(String collectionName) {
    	return collections.get(collectionName);
    }

    public boolean canRead(String propertyName){
        return getters.containsKey(propertyName);
    }
    
    public boolean canWrite(String propertyName){
        return setters.containsKey(propertyName);
    }
  
  
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

    public void setPropertyValue(Object target, String propertyName, Object propertyValue) {
        Method method = getSetter(propertyName);
        if (!method.isAccessible())
            method.setAccessible(true);
        try {
            method.invoke(target,propertyValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public Iterable<?> getCollectionValue(Object target, String collectionName) {
        Method method = getGetter(collectionName);
        if (!method.isAccessible())
            method.setAccessible(true);
        try {
        	Object obj = method.invoke(target);
        	if (obj == null)
        		return null;
        	else
	        	return obj.getClass().isArray()
	        		? Enumerable.create((Object[])obj)
	        		: (Iterable<?>)obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public <T> void setCollectionValue(Object target, String collectionName, Collection<T> collectionValue) {
        Method method = getSetter(collectionName);
        if (!method.isAccessible())
            method.setAccessible(true);
        try {
        	Object value = null;
        	
        	if (collectionValue != null) {
	        	Class<?> clazz = method.getParameterTypes()[0];
	        	if (List.class.isAssignableFrom(clazz)) {
					value = collectionValue instanceof List
						? (List<T>)collectionValue
						:  new ArrayList<T>(collectionValue);
	        	} else if (Set.class.isAssignableFrom(clazz)) {
	        		value = collectionValue instanceof Set
	        			? (Set<T>)collectionValue
	        			: new HashSet<T>(collectionValue);
	        	}
	        	else
	        		throw new RuntimeException("Unsupported collection type " + collectionValue.getClass());        	
        	}
        	
        	method.invoke(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
        
    private Method getGetter(String propertyName) {
        Method method = getters.get(propertyName);
        if (method == null)
            throw new IllegalArgumentException("No getter found for propertyName " + propertyName);
        return method;
    }
    private Method getSetter(String propertyName) {
      Method method = setters.get(propertyName);
      if (method == null)
          throw new IllegalArgumentException("No setter found for propertyName " + propertyName);
      return method;
    }
    

    
    
    
    private static Map<String, Class<?>> computeTypes(Map<String, Method> getters,Map<String, Method> setters){
        Map<String, Class<?>> rt = new HashMap<String, Class<?>>();
        
        for(String propertyName : getters.keySet()) {
        	Class<?> getterType = getters.get(propertyName).getReturnType();
        	if (!isCollection(getterType))
        		rt.put(propertyName, getterType);
        }
        
        for(String propertyName : setters.keySet()) {
        	Class<?> getterType = rt.get(propertyName);
        	if (getterType != null) {
	            Class<?> setterType = setters.get(propertyName).getParameterTypes()[0];
	            
	            if (getterType != null && !getterType.equals(setterType))
	                throw new RuntimeException(String.format("Inconsistent types for property %s: getter type %s, setter type %s",
	                        propertyName,
	                        getterType.getName(),
	                        setterType.getName()));
	            
	            rt.put(propertyName, setterType);
        	}
        }
        
        return rt;
    }
    
    private Map<String, Class<?>> computeCollections(
			Map<String, Method> getters2, Map<String, Method> setters2) {
    	Map<String, Class<?>> rt = new HashMap<String, Class<?>>();
    	
		for(String propertyName : getters.keySet()) {
			Class<?> getterType = getters.get(propertyName).getReturnType();
			if (isCollection(getterType)) {
				Class<?> setterType = setters.containsKey(propertyName)
						? setters.get(propertyName).getParameterTypes()[0]
						: null;
				if (setterType != null) {
    		        if (!getterType.equals(setterType))
    		            throw new RuntimeException(String.format("Inconsistent types for association %s: getter type %s, setter type %s",
    		                    propertyName,
    		                    getterType.getName(),
    		                    setterType.getName()));
    
    		        Class<?> elementClass;
    		        Type type = getters.get(propertyName).getGenericReturnType();
    		        if (type instanceof ParameterizedType) {
    		        	Type[] actualTypes = ((ParameterizedType)type).getActualTypeArguments();
    		        	elementClass = actualTypes.length > 0
    		        		? (Class<?>)actualTypes[0]
    		        		: Object.class;
    		        }
    		        else
    		        	elementClass = Object.class;
    		        
    		        rt.put(propertyName, elementClass);
				}
			}
		}
    	
		return rt;
	}

    private static boolean isCollection(Class<?> clazz) {
    	return clazz.isArray() || Collection.class.isAssignableFrom(clazz);
    }
    
    private static Map<String, Method> getBeanGetters(Class<?> clazz) {

        Map<String, Method> rt = new HashMap<String, Method>();
        for(Method method : clazz.getMethods()) {
            String methodName = method.getName();
            if (methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3)) && method.getParameterTypes().length == 0 && !method.getReturnType().equals(Void.TYPE) && !Modifier.isStatic(method.getModifiers())
            ) {
                String name = methodName.substring(3);
                rt.put(name, method);

            }
            if (methodName.startsWith("is") && methodName.length() > 2 && Character.isUpperCase(methodName.charAt(2)) && method.getParameterTypes().length == 0 && (method.getReturnType().equals(Boolean.class) || method.getReturnType().equals(Boolean.TYPE)) && !Modifier.isStatic(method.getModifiers())) {
                String name = methodName.substring(2);
                rt.put(name, method);

            }
        }
        return rt;
    }
    
    
   
      
      
      private static Map<String, Method> getBeanSetters(Class<?> clazz) {

          Map<String, Method> rt = new HashMap<String, Method>();
          for(Method method : clazz.getMethods()) {
              String methodName = method.getName();
              if (methodName.startsWith("set") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3)) && method.getParameterTypes().length == 1 && method.getReturnType().equals(Void.TYPE) && !Modifier.isStatic(method.getModifiers())
              ) {
                  String name = methodName.substring(3);
                  rt.put(name, method);

              }
              
          }
          return rt;
      }
      
      

}
