package org.odata4j.edm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.core4j.Enumerable;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;

/**
 * A type in the EDM type system.  
 * Simple types are exposed as constants and associated with one or more java-types.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399213.aspx">[msdn] Simple Types (EDM)</a>
 *
 * Refactoring Note:
 * - this class was originally used for simple and complex types.  The full
 *   semantics are now:  if your type is an EdmType, you can be a property of an Entity.
 *   This does not preclude you from being a another thing...like a FunctionParameter for example.
 
 * @see EdmSimpleType
 * @see EdmComplexType
 */
public abstract class EdmType extends EdmBaseType {

  private static Map<String, EdmType> POOL = new HashMap<String, EdmType>();

  // http://msdn.microsoft.com/en-us/library/bb399213.aspx
  public static final EdmType BINARY = getInternal("Edm.Binary", byte[].class, Byte[].class);
  public static final EdmType BOOLEAN = getInternal("Edm.Boolean", boolean.class, Boolean.class);
  public static final EdmType BYTE = getInternal("Edm.Byte", byte.class, Byte.class);
  public static final EdmType DATETIME = getInternal("Edm.DateTime", LocalDateTime.class);
  public static final EdmType DATETIMEOFFSET = getInternal("Edm.DateTimeOffset", DateTime.class);
  public static final EdmType DECIMAL = getInternal("Edm.Decimal", BigDecimal.class);
  public static final EdmType DOUBLE = getInternal("Edm.Double", double.class, Double.class);
  public static final EdmType GUID = getInternal("Edm.Guid", Guid.class);
  public static final EdmType INT16 = getInternal("Edm.Int16", short.class, Short.class);
  public static final EdmType INT32 = getInternal("Edm.Int32", int.class, Integer.class);
  public static final EdmType INT64 = getInternal("Edm.Int64", long.class, Long.class);
  public static final EdmType SINGLE = getInternal("Edm.Single", float.class, Float.class);
  public static final EdmType STRING = getInternal("Edm.String", char.class, Character.class, String.class);
  public static final EdmType TIME = getInternal("Edm.Time", LocalTime.class);

  /**
   * Set of all edm simple types.
   */
  public static Set<EdmType> SIMPLE = Collections.unmodifiableSet(Enumerable.create(POOL.values()).toSet());

  protected EdmType(String typeString) {
    super(typeString);
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

  private static EdmType getInternal(String typeString, Class<?>... javaTypes) {
    if (typeString == null)
      return null;
    Set<Class<?>> javaTypeSet = Enumerable.create(javaTypes).toSet();
    if (!POOL.containsKey(typeString)) {
      if (null == javaTypes || 0 == javaTypes.length) {
        POOL.put(typeString, new EdmComplexType(typeString));
      } else {
        POOL.put(typeString, new EdmSimpleType(typeString, javaTypeSet));
      }
    }
    return POOL.get(typeString);
  }

  /**
   * Whether or not this is an edm simple type.
   * 
   * @return true or false
   */
  public abstract boolean isSimple();

  public abstract Set<Class<?>> getJavaTypes();

  /**
   * Finds the edm simple type for a given java-type.
   * 
   * @param javaType  the java-type
   * @return the associated edm simple type, else null
   */
  public static EdmType forJavaType(Class<?> javaType) {
    for (EdmType simple : SIMPLE)
      if (simple.getJavaTypes().contains(javaType))
        return simple;
    return null;
  }

}
