package org.odata4j.producer.inmemory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.edm.EdmSimpleType;

public class InMemoryTypeMapping {

  private static final Map<Class<?>, EdmSimpleType<?>> SUPPORTED_TYPES = new HashMap<Class<?>, EdmSimpleType<?>>();

  public static final InMemoryTypeMapping DEFAULT = new InMemoryTypeMapping();

  static {
    SUPPORTED_TYPES.put(byte[].class, EdmSimpleType.BINARY);
    SUPPORTED_TYPES.put(Boolean.class, EdmSimpleType.BOOLEAN);
    SUPPORTED_TYPES.put(Boolean.TYPE, EdmSimpleType.BOOLEAN);
    SUPPORTED_TYPES.put(Byte.class, EdmSimpleType.SBYTE);
    SUPPORTED_TYPES.put(Byte.TYPE, EdmSimpleType.SBYTE);
    SUPPORTED_TYPES.put(LocalDateTime.class, EdmSimpleType.DATETIME);
    SUPPORTED_TYPES.put(BigDecimal.class, EdmSimpleType.DECIMAL);
    SUPPORTED_TYPES.put(Double.class, EdmSimpleType.DOUBLE);
    SUPPORTED_TYPES.put(Double.TYPE, EdmSimpleType.DOUBLE);
    SUPPORTED_TYPES.put(Guid.class, EdmSimpleType.GUID);
    SUPPORTED_TYPES.put(Short.class, EdmSimpleType.INT16);
    SUPPORTED_TYPES.put(Short.TYPE, EdmSimpleType.INT16);
    SUPPORTED_TYPES.put(Integer.class, EdmSimpleType.INT32);
    SUPPORTED_TYPES.put(Integer.TYPE, EdmSimpleType.INT32);
    SUPPORTED_TYPES.put(Long.class, EdmSimpleType.INT64);
    SUPPORTED_TYPES.put(Long.TYPE, EdmSimpleType.INT64);
    SUPPORTED_TYPES.put(Float.class, EdmSimpleType.SINGLE);
    SUPPORTED_TYPES.put(Float.TYPE, EdmSimpleType.SINGLE);
    SUPPORTED_TYPES.put(String.class, EdmSimpleType.STRING);
    SUPPORTED_TYPES.put(LocalTime.class, EdmSimpleType.TIME);
    SUPPORTED_TYPES.put(DateTime.class, EdmSimpleType.DATETIMEOFFSET);
    SUPPORTED_TYPES.put(Date.class, EdmSimpleType.DATETIME);

    SUPPORTED_TYPES.put(Object.class, EdmSimpleType.STRING);
  }

  public EdmSimpleType<?> findEdmType(Class<?> clazz) {
    EdmSimpleType<?> type = SUPPORTED_TYPES.get(clazz);
    if (type != null) return type;
    return null;
  }

}
