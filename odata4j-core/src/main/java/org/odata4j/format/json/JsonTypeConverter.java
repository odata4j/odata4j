package org.odata4j.format.json;

import java.math.BigDecimal;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.repack.org.apache.commons.codec.DecoderException;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

public class JsonTypeConverter {
	
    public static OProperty<?> parse(String name, EdmType type, String value) {

        if (EdmType.GUID.equals(type)) {
            Guid uValue = value == null ? null : Guid.fromString(value.substring(5, value.length()-1));
            return OProperties.guid(name, uValue);
        } else if (EdmType.BOOLEAN.equals(type)) {
            Boolean bValue = value == null ? null : Boolean.parseBoolean(value);
            return OProperties.boolean_(name, bValue);
        } else if (EdmType.BYTE.equals(type)) {
            Byte bValue;
			try {
				bValue = value == null ? null : Hex.decodeHex(value.toCharArray())[0];
			} catch (DecoderException dex) {
				throw new IllegalArgumentException(dex);
			}
            return OProperties.byte_(name, bValue);
        } else if (EdmType.INT16.equals(type)) {
            Short sValue = value == null ? null : Short.parseShort(value);
            return OProperties.int16(name, sValue);
        } else if (EdmType.INT32.equals(type)) {
            Integer iValue = value == null ? null : Integer.parseInt(value);
            return OProperties.int32(name, iValue);
        } else if (EdmType.INT64.equals(type)) {
            Long lValue = value == null ? null : Long.parseLong(value);
            return OProperties.int64(name, lValue);
        } else if (EdmType.SINGLE.equals(type)) {
            Float fValue = value == null ? null : Float.parseFloat(value);
            return OProperties.single(name, fValue);
        } else if (EdmType.DOUBLE.equals(type)) {
            Double dValue = value == null ? null : Double.parseDouble(value);
            return OProperties.double_(name, dValue);
        } else if (EdmType.DECIMAL.equals(type)) {
            BigDecimal dValue = value == null ? null : new BigDecimal(value);
            return OProperties.decimal(name, dValue);
        } else if (EdmType.BINARY.equals(type)) {
            byte[] bValue = value == null ? null : new Base64().decode(value);
            return OProperties.binary(name, bValue);
        } else if (EdmType.DATETIME.equals(type)) {
			LocalDateTime dValue = null;
			if (value != null) {
				if (!value.startsWith("\\/Date(") || !value.endsWith(")\\/")) {
					throw new IllegalArgumentException("invalid date format");
				}
				String ticks = value.substring(7, value.length() - 3);
				String offset = null;
				int idx = ticks.indexOf('-');
				if (idx > 0) {
					offset = ticks.substring(idx + 1);
					ticks = ticks.substring(0, idx);
					dValue = new LocalDateTime(Long.parseLong(ticks),
							DateTimeZone.UTC);
					dValue = dValue.minusMinutes(Integer.valueOf(offset));
				} else if ((idx = ticks.indexOf('+')) > 0) {
					offset = ticks.substring(idx + 1);
					ticks = ticks.substring(0, idx);
					dValue = new LocalDateTime(Long.parseLong(ticks),
							DateTimeZone.UTC);
					dValue = dValue.plusMinutes(Integer.valueOf(offset));
				} else {
					dValue = new LocalDateTime(Long.parseLong(ticks),
							DateTimeZone.UTC);
				}
        	} 
            return OProperties.datetime(name, dValue);
        } else if (EdmType.TIME.equals(type)) {
            LocalTime tValue = value == null ? null : new LocalTime(value);
            return OProperties.time(name, tValue);
        } else if (EdmType.STRING.equals(type) || type == null) {
            return OProperties.string(name, value);
        }
        throw new UnsupportedOperationException("type:" + type);
    }

}
