package org.odata4j.core;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.LocalDateTime;

/**
 * A consumer-side function-request builder.  Call {@link #execute()} or simply iterate to issue the request.
 * 
 * @param <T>  the entity representation as a java type
 */
public interface OFunctionRequest<T> extends OQueryRequest<T> {

  /** Add a generic parameter */
  OFunctionRequest<T> parameter(String name, OObject value);

  /** Add a boolean parameter */
  OFunctionRequest<T> pBoolean(String name, boolean value);

  /** Add a byte parameter */
  OFunctionRequest<T> pByte(String name, byte value);

  /** Add a datetime parameter */
  OFunctionRequest<T> pDateTime(String name, Calendar value);

  /** Add a datetime parameter */
  OFunctionRequest<T> pDateTime(String name, Date value);

  /** Add a datetime parameter */
  OFunctionRequest<T> pDateTime(String name, LocalDateTime value);

  /** Add a decimal  parameter */
  OFunctionRequest<T> pDecimal(String name, BigDecimal value);

  /** Add a double parameter */
  OFunctionRequest<T> pDouble(String name, double value);

  /** Add a guid parameter */
  OFunctionRequest<T> pGuid(String name, Guid value);

  /** Add a 16-bit integer parameter */
  OFunctionRequest<T> pInt16(String name, short value);

  /** Add a 32-bit integer parameter */
  OFunctionRequest<T> pInt32(String name, int value);

  /** Add a 64-bit integer parameter */
  OFunctionRequest<T> pInt64(String name, long value);

  /** Add a single parameter */
  OFunctionRequest<T> pSingle(String name, float value);

  /** Add a time parameter */
  OFunctionRequest<T> pTime(String name, Calendar value);

  /** Add a time parameter */
  OFunctionRequest<T> pTime(String name, Date value);

  /** Add a time parameter */
  OFunctionRequest<T> pTime(String name, LocalDateTime value);
  
  /** Add a string parameter */
  OFunctionRequest<T> pString(String name, String value);
}
