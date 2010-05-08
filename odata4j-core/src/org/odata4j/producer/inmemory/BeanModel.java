package org.odata4j.producer.inmemory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class BeanModel {

    private final Class<?> beanClass;
    private final Map<String, Method> getters;
    private final Map<String, Method> setters;
    private final Map<String, Class<?>> types;
    
    public BeanModel(Class<?> beanClass){
        this.beanClass = beanClass;
        this.getters = getBeanGetters(beanClass);
        this.setters = getBeanSetters(beanClass);
        this.types = computeTypes(getters,setters);
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
        
        for(String propertyName : getters.keySet())
            rt.put(propertyName, getters.get(propertyName).getReturnType());
        
        for(String propertyName : setters.keySet()) {
            Class<?> setterType = setters.get(propertyName).getParameterTypes()[0];
            Class<?> getterType = rt.get(propertyName);
            
            if (getterType != null && !getterType.equals(setterType))
                throw new RuntimeException(String.format("Inconsistent types for property %s: getter type %s, setter type %s",
                        propertyName,
                        getterType.getName(),
                        setterType.getName()));
            
            rt.put(propertyName, setterType);
        }
        
        return rt;
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
