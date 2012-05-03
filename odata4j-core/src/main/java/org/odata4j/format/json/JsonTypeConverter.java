package org.odata4j.format.json;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Boole;
import org.odata4j.core.Guid;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.UnsignedByte;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;

public class JsonTypeConverter {

  public static OProperty<?> parse(String name, EdmSimpleType<?> type, String value) {

    if (EdmSimpleType.GUID.equals(type)) {
      Guid uValue = value == null ? null : Guid.fromString(value.substring(5, value.length() - 1));
      return OProperties.guid(name, uValue);
    } else if (EdmSimpleType.BOOLEAN.equals(type)) {
      Boolean bValue = value == null ? null : Boole.fromString(value).toBoolean();
      return OProperties.boolean_(name, bValue);
    } else if (EdmSimpleType.BYTE.equals(type)) {
      UnsignedByte bValue = value == null ? null : UnsignedByte.parseUnsignedByte(value);
      return OProperties.byte_(name, bValue);
    } else if (EdmSimpleType.SBYTE.equals(type)) {
      Byte bValue = value == null ? null : Byte.parseByte(value);
      return OProperties.sbyte_(name, bValue);
    } else if (EdmSimpleType.INT16.equals(type)) {
      Short sValue = value == null ? null : Short.parseShort(value);
      return OProperties.int16(name, sValue);
    } else if (EdmSimpleType.INT32.equals(type)) {
      Integer iValue = value == null ? null : Integer.parseInt(value);
      return OProperties.int32(name, iValue);
    } else if (EdmSimpleType.INT64.equals(type)) {
      Long lValue = value == null ? null : Long.parseLong(value);
      return OProperties.int64(name, lValue);
    } else if (EdmSimpleType.SINGLE.equals(type)) {
      Float fValue = value == null ? null : Float.parseFloat(value);
      return OProperties.single(name, fValue);
    } else if (EdmSimpleType.DOUBLE.equals(type)) {
      Double dValue = value == null ? null : Double.parseDouble(value);
      return OProperties.double_(name, dValue);
    } else if (EdmSimpleType.DECIMAL.equals(type)) {
      BigDecimal dValue = value == null ? null : new BigDecimal(value);
      return OProperties.decimal(name, dValue);
    } else if (EdmSimpleType.BINARY.equals(type)) {
      byte[] bValue = value == null ? null : new Base64().decode(value);
      return OProperties.binary(name, bValue);
    } else if (EdmSimpleType.DATETIME.equals(type)) {
      LocalDateTime ldValue = value == null ? null : InternalUtil.parseDateTimeFromJson(value);
      return OProperties.datetime(name, ldValue);
    } else if (EdmSimpleType.DATETIMEOFFSET.equals(type)) {
      DateTime dValue = value == null ? null : InternalUtil.parseDateTimeOffsetFromJson(value);
      return OProperties.datetimeOffset(name, dValue);
    } else if (EdmSimpleType.TIME.equals(type)) {
      LocalTime tValue = value == null ? null : InternalUtil.parseTime(value);
      return OProperties.time(name, tValue);
    } else if (EdmSimpleType.STRING.equals(type) || type == null) {
      return OProperties.string(name, value);
    }
    throw new UnsupportedOperationException("type:" + type);
  }

}
