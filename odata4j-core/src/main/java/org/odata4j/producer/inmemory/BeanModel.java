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
import java.util.Map.Entry;
import java.util.Set;

import org.core4j.Enumerable;

/**
 * An abstract representation of the "bean" nature of a class.
 * This class caches up-front analysis of a class to locate the
 * getters and setters it will need in order to operate on instances.
 *
 * <p>Instances of this class can then be used in place of reflection.</p>
 */
public class BeanModel {

  private final Class<?> beanClass;
  private final Map<String, Method> getters;
  private final Map<String, Method> setters;
  private final Map<String, Class<?>> types;
  private final Map<String, Class<?>> collections;

  /**
   * Constructs the abstract bean representation of a class.
   *
   * @param beanClass  the class to introspect
   */
  public BeanModel(Class<?> beanClass) {
    this.beanClass = beanClass;
    this.getters = getBeanGetters(beanClass);
    this.setters = getBeanSetters(beanClass);
    this.types = computeTypes(getters, setters);
    this.collections = computeCollections(getters, setters);
  }

  /**
   * Recovers the original class on which this metadata is based.
   *
   * @return the original class
   */
  public Class<?> getBeanClass() {
    return beanClass;
  }

  /**
   * Returns the list of all properties identified on this class.
   *
   * <p>A property is any field that has a simple value type (i.e. not a collection type)
   * and either has a getter or a setter defined on it.
   *
   * @return the list of identified properties
   */
  public Iterable<String> getPropertyNames() {
    return types.keySet();
  }

  /**
   * Discovers the type of a property.
   *
   * @param propertyName  the property you are interested in
   * @return the type of the property
   */
  public Class<?> getPropertyType(String propertyName) {
    return types.get(propertyName);
  }

  /**
   * Returns the list of properties that have collection types.
   *
   * @return the list of properties
   */
  public Iterable<String> getCollectionNames() {
    return collections.keySet();
  }

  /**
   * For any given collection type, identifies the type of the elements of the collection.
   *
   * @param collectionName  the name of the collection
   * @return the type of the elements of the named collection
   */
  public Class<?> getCollectionElementType(String collectionName) {
    return collections.get(collectionName);
  }

  /**
   * Returns true if the property has a getter.
   */
  public boolean canRead(String propertyName) {
    return getters.containsKey(propertyName);
  }

  /**
   * Returns true if the property has a setter.
   */
  public boolean canWrite(String propertyName) {
    return setters.containsKey(propertyName);
  }

  /**
   * Interrogates an instance of the target class and discovers the value
   * of a given property.
   * This method is only intended to be used for simple properties.
   *
   * @param target  the instance of the class
   * @param propertyName  the name of the property to fetch
   * @return the value of the property in the given object
   */
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

  /**
   * Updates an instance to set a property to a given value
   * This method is only intended to be used for simple properties
   *
   * @param target the instance to update
   * @param propertyName the name of the property
   * @param propertyValue the value to set in the property
   */
  public void setPropertyValue(Object target, String propertyName, Object propertyValue) {
    Method method = getSetter(propertyName);
    if (!method.isAccessible())
      method.setAccessible(true);
    try {
      method.invoke(target, propertyValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns a collection from a property in an instance.
   *
   * @param target  the instance to look at
   * @param collectionName  the name of the property on the instance which holds the collection
   * @return an iterable containing the elements of the collection
   */
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
            ? Enumerable.create((Object[]) obj)
            : (Iterable<?>) obj;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Updates a collection property.
   *
   * @param target  the instance to look at
   * @param collectionName  the name of the property on the instance which holds the collection
   * @param collectionValue  the new collection
   */
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
              ? (List<T>) collectionValue
              : new ArrayList<T>(collectionValue);
        } else if (Set.class.isAssignableFrom(clazz)) {
          value = collectionValue instanceof Set
              ? (Set<T>) collectionValue
              : new HashSet<T>(collectionValue);
        } else
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

  private static Map<String, Class<?>> computeTypes(Map<String, Method> getters, Map<String, Method> setters) {
    Map<String, Class<?>> rt = new HashMap<String, Class<?>>();

    for (Entry<String, Method> getter : getters.entrySet()) {
      Class<?> getterType = getter.getValue().getReturnType();
      if (!isIterable(getterType))
        rt.put(getter.getKey(), getterType);
    }

    for (Entry<String, Method> setter : setters.entrySet()) {
      String propertyName = setter.getKey();
      Class<?> getterType = rt.get(propertyName);
      if (getterType != null) {
        Class<?> setterType = setter.getValue().getParameterTypes()[0];

        if (getterType != null && !getterType.equals(setterType))
          throw new RuntimeException(String.format("Inconsistent types for property %s.%s: getter type %s, setter type %s",
              setters.get(propertyName).getDeclaringClass().getName(),
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

    for (Entry<String, Method> getter : getters.entrySet()) {
      String propertyName = getter.getKey();
      Method method = getter.getValue();
      Class<?> getterType = method.getReturnType();
      if (isIterable(getterType)) {
        Class<?> setterType = setters.containsKey(propertyName)
            ? setters.get(propertyName).getParameterTypes()[0]
            : null;
        if (setterType != null) {
          if (!getterType.equals(setterType))
            throw new RuntimeException(String.format("Inconsistent types for association %s.%s: getter type %s, setter type %s",
                setters.get(propertyName).getDeclaringClass().getName(),
                propertyName,
                getterType.getName(),
                setterType.getName()));

          Class<?> elementClass;
          Type type = method.getGenericReturnType();
          if (type instanceof ParameterizedType) {
            Type[] actualTypes = ((ParameterizedType) type).getActualTypeArguments();
            elementClass = actualTypes.length > 0
                ? (Class<?>) actualTypes[0]
                : Object.class;
          } else
            elementClass = Object.class;

          rt.put(propertyName, elementClass);
        }
      }
    }

    return rt;
  }

  private static boolean isIterable(Class<?> clazz) {
    return clazz.isArray() || Iterable.class.isAssignableFrom(clazz);
  }

  private static Map<String, Method> getBeanGetters(Class<?> clazz) {

    Map<String, Method> rt = new HashMap<String, Method>();
    for (Method method : clazz.getMethods()) {
      String methodName = method.getName();
      if (methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3)) && method.getParameterTypes().length == 0 && !method.getReturnType().equals(Void.TYPE) && !Modifier.isStatic(method.getModifiers())) {
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
    for (Method method : clazz.getMethods()) {
      String methodName = method.getName();
      if (methodName.startsWith("set") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3)) && method.getParameterTypes().length == 1 && method.getReturnType().equals(Void.TYPE) && !Modifier.isStatic(method.getModifiers())) {
        String name = methodName.substring(3);
        rt.put(name, method);
      }
    }
    return rt;
  }

}
