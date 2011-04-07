package org.odata4j.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.edm.EdmType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

public class OProperties {

    public static <T> OProperty<?> simple(String name, EdmType type, T value) {    	
    	return simple(name, type, value, false);
    }	
    	
    public static <T> OProperty<?> simple(String name, EdmType type, T value, boolean exceptionOnUnknownType) {    	
        if (type == EdmType.STRING) {
        	String sValue = "";
        	if (value instanceof String) {
        		sValue = (String) value;
        	} else if (value instanceof Character) {
        		sValue = ((Character)value).toString();
        	}
            return OProperties.string(name, sValue);
        } else if (type == EdmType.BOOLEAN) {
            Boolean bValue = (Boolean) value;
            return OProperties.boolean_(name, bValue);
        } else if (type == EdmType.INT16) {
            Short sValue = (Short) value;
            return OProperties.short_(name, sValue);
        } else if (type == EdmType.INT32) {
            Integer iValue = (Integer) value;
            return OProperties.int32(name, iValue);
        } else if (type == EdmType.INT64) {
            Long iValue = (Long) value;
            return OProperties.int64(name, iValue);
        } else if (type == EdmType.BYTE) {
            Byte bValue = (Byte) value;
            return OProperties.byte_(name, bValue);
        }  else if (type == EdmType.DECIMAL) {
            BigDecimal dValue = (BigDecimal) value;
            return OProperties.decimal(name, dValue);
        } else if (type == EdmType.DATETIME) {
        	if (value instanceof LocalDateTime)
        		return OProperties.datetime(name, (LocalDateTime)value);
        	else if (value instanceof Calendar)
        		return OProperties.datetime(name, (Date)((Calendar)value).getTime());
        	else 
        		return OProperties.datetime(name, (Date)value);
        } else if (type == EdmType.TIME) {
        	if (value instanceof LocalTime)
        		return OProperties.time(name, (LocalTime)value);
    		else if (value instanceof Calendar)
        		return OProperties.time(name, (Date)((Calendar)value).getTime());        		
    		else 
        		return OProperties.time(name, (Date)value);        		
        } else if (type == EdmType.BINARY) {
            byte[] bValue = (byte[]) value;
            return OProperties.binary(name, bValue);
        } else if (type == EdmType.DOUBLE) {
        	Double dValue = (Double) value;
            return OProperties.double_(name, dValue);
        } else if (type == EdmType.SINGLE) {
        	Float fValue = (Float) value;
            return OProperties.single(name, fValue);
        } else {
        	if (exceptionOnUnknownType) {
        		throw new UnsupportedOperationException("Implement " + type);
        	} else {
        		return new PropertyImpl<T>(name, type, value);
        	}
        }
    }

    public static OProperty<?> null_(String name, String type) {
        return new PropertyImpl<Object>(name, EdmType.get(type), null);
    }

    public static OProperty<List<OProperty<?>>> complex(String name, String type, List<OProperty<?>> value) {
        return new PropertyImpl<List<OProperty<?>>>(name, EdmType.get(type), value);
    }

    public static OProperty<?> parse(String name, String type, String value) {

        if (EdmType.GUID.toTypeString().equals(type)) {
            Guid uValue = value == null ? null : Guid.fromString(value);
            return OProperties.guid(name, uValue);
        } else if (EdmType.BOOLEAN.toTypeString().equals(type)) {
            Boolean bValue = value == null ? null : Boolean.parseBoolean(value);
            return OProperties.boolean_(name, bValue);
        } else if (EdmType.BYTE.toTypeString().equals(type)) {
            Byte bValue = value == null ? null : Byte.parseByte(value);
            return OProperties.byte_(name, bValue);
        } else if (EdmType.INT16.toTypeString().equals(type)) {
            Short sValue = value == null ? null : Short.parseShort(value);
            return OProperties.int16(name, sValue);
        } else if (EdmType.INT32.toTypeString().equals(type)) {
            Integer iValue = value == null ? null : Integer.parseInt(value);
            return OProperties.int32(name, iValue);
        } else if (EdmType.INT64.toTypeString().equals(type)) {
            Long lValue = value == null ? null : Long.parseLong(value);
            return OProperties.int64(name, lValue);
        } else if (EdmType.SINGLE.toTypeString().equals(type)) {
            Float fValue = value == null ? null : Float.parseFloat(value);
            return OProperties.single(name, fValue);
        } else if (EdmType.DOUBLE.toTypeString().equals(type)) {
            Double dValue = value == null ? null : Double.parseDouble(value);
            return OProperties.double_(name, dValue);
        } else if (EdmType.DECIMAL.toTypeString().equals(type)) {
            BigDecimal dValue = value == null ? null : new BigDecimal(value);
            return OProperties.decimal(name, dValue);
        } else if (EdmType.BINARY.toTypeString().equals(type)) {
            byte[] bValue = new Base64().decode(value);
            return OProperties.binary(name, bValue);
        } else if (EdmType.DATETIME.toTypeString().equals(type)) {
        	LocalDateTime dValue = value == null 
        		? null 
        		: new LocalDateTime(InternalUtil.parseDateTime(value));
            return OProperties.datetime(name, dValue);
        } else if (EdmType.TIME.toTypeString().equals(type)) {
            LocalTime tValue = value == null 
            	? null 
            	: InternalUtil.parseTime(value);
            return OProperties.time(name, tValue);
        } else if (EdmType.STRING.toTypeString().equals(type) || type == null) {
            return OProperties.string(name, value);
        }
        throw new UnsupportedOperationException("type:" + type);
    }

    public static OProperty<Short> int16(String name, Short value) {
        return new PropertyImpl<Short>(name, EdmType.INT16, value);
    }

    public static OProperty<Integer> int32(String name, Integer value) {
        return new PropertyImpl<Integer>(name, EdmType.INT32, value);
    }

    public static OProperty<Long> int64(String name, Long value) {
        return new PropertyImpl<Long>(name, EdmType.INT64, value);
    }

    public static OProperty<String> string(String name, String value) {
        return new PropertyImpl<String>(name, EdmType.STRING, value);
    }

    public static OProperty<String> string(String name, char value) {
        return new PropertyImpl<String>(name, EdmType.STRING, Character.toString(value));
    }

    public static OProperty<Character> character(String name, Character value) {
        return new PropertyImpl<Character>(name, EdmType.STRING, value);
    }

    public static OProperty<Guid> guid(String name, String value) {
        return guid(name, Guid.fromString(value));
    }

    public static OProperty<Guid> guid(String name, Guid value) {
        return new PropertyImpl<Guid>(name, EdmType.GUID, value);
    }

    public static OProperty<Boolean> boolean_(String name, Boolean value) {
        return new PropertyImpl<Boolean>(name, EdmType.BOOLEAN, value);
    }

    public static OProperty<Float> single(String name, Float value) {
        return new PropertyImpl<Float>(name, EdmType.SINGLE, value);
    }

    public static OProperty<Double> double_(String name, Double value) {
        return new PropertyImpl<Double>(name, EdmType.DOUBLE, value);
    }

    public static OProperty<LocalDateTime> datetime(String name, LocalDateTime value) {
        return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, value);
    }

    public static OProperty<LocalDateTime> datetime(String name, Date value) {
        return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, new LocalDateTime(value));
    }

    public static OProperty<LocalDateTime> datetime(String name, Calendar value) {
        return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, new LocalDateTime(value));
    }

    public static OProperty<LocalTime> time(String name, LocalTime value) {
        return new PropertyImpl<LocalTime>(name, EdmType.TIME, value);
    }
    
    public static OProperty<LocalTime> time(String name, Date value) {
        return new PropertyImpl<LocalTime>(name, EdmType.TIME, new LocalTime(value));
    }    

    public static OProperty<Short> short_(String name, Short value) {
        return new PropertyImpl<Short>(name, EdmType.INT16, value);
    }

    public static OProperty<BigDecimal> decimal(String name, BigDecimal value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, value);
    }

    public static OProperty<BigDecimal> decimal(String name, BigInteger value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, BigDecimal.valueOf(value.longValue()));
    }

    public static OProperty<BigDecimal> decimal(String name, long value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, BigDecimal.valueOf(value));
    }

    public static OProperty<BigDecimal> decimal(String name, double value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, BigDecimal.valueOf(value));
    }

    public static OProperty<byte[]> binary(String name, byte[] value) {
        return new PropertyImpl<byte[]>(name, EdmType.BINARY, value);
    }

    public static OProperty<Byte[]> binary(String name, Byte[] value) {
        return new PropertyImpl<Byte[]>(name, EdmType.BINARY, value);
    }

    public static OProperty<Byte> byte_(String name, byte value) {
        return new PropertyImpl<Byte>(name, EdmType.BYTE, value);
    }

    private static class PropertyImpl<T> implements OProperty<T> {

        private final String name;
        private final EdmType type;
        private final T value;

        public PropertyImpl(String name, EdmType type, T value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public EdmType getType() {
            return type;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            Object value = this.value;
            if (value instanceof byte[]) {
                value = "0x" + Hex.encodeHexString((byte[]) value);
            }
            return String.format("OProperty[%s,%s,%s]", name, type, value);
        }
    }
}
