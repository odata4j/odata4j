package org.odata4j.edm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.core4j.Enumerable;

/**
 * A type in the EDM type system.
 * 
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399591.aspx">[msdn] Types (Metadata)</a>
 * @see 
 */
public abstract class EdmType {

  protected static Map<String, EdmType> POOL = new HashMap<String, EdmType>();
  
  private final String fullyQualifiedTypeName;
  
  public EdmType(String fullyQualifiedTypeName) {
    this.fullyQualifiedTypeName = fullyQualifiedTypeName;
  }

  /**
   * Gets the edm-type for a given type name.
   * 
   * @param typeString  the fully-qualified type name
   * @return the edm-type
   */
  public static EdmType get(String typeString) {
    return getInternal(typeString);
  }

  protected static EdmType getInternal(String typeString, Class<?>... javaTypes) {
    if (typeString == null)
      return null;
    Set<Class<?>> javaTypeSet = Enumerable.create(javaTypes).toSet();
    if (!POOL.containsKey(typeString)) {
      if (null == javaTypes || 0 == javaTypes.length) {
        POOL.put(typeString, EdmComplexType.create(typeString));
      } else {
        POOL.put(typeString, new EdmSimpleType(typeString, javaTypeSet));
      }
    }
    return POOL.get(typeString);
  }
  
  /**
   * Gets the fully-qualified type name for this edm-type.
   */
  public String getFullyQualifiedTypeName() {
    return this.fullyQualifiedTypeName;
  }

  @Override
  public String toString() {
    return getFullyQualifiedTypeName();
  }

  @Override
  public int hashCode() {
    return this.fullyQualifiedTypeName.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof EdmType && ((EdmType) other).fullyQualifiedTypeName.equals(this.fullyQualifiedTypeName);
  }
  
  public abstract boolean isSimple();
}
