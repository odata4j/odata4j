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

/**
 * A static factory to create immutable {@link OProperty} instances.
 */
public class OProperties {

	private OProperties() {}
	
	/**
	 * Creates a new OData property, inferring the edm-type from the value provided, which cannot be null.
	 * 
	 * @param <T>  the property value's java-type
	 * @param name  the property name
	 * @param value  the property value
	 * @return a new OData property instance
	 */
	public static <T> OProperty<T> simple(String name, T value) {
		if (value==null)
			throw new IllegalArgumentException("Cannot infer EdmType if value is null");
		EdmType type =EdmType.forJavaType(value.getClass());
		if (type==null)
			throw new IllegalArgumentException("Cannot infer EdmType for java type: " + value.getClass().getName());
	    return simple(name, type, value, false);
	}
	 
    /**
     * Creates a new OData property of the given edm-type.
     * 
     * @param <T>  the property value's java-type
     * @param name  the property name
     * @param type  the property edm-type
     * @param value  the property value
     * @return a new OData property instance
     */
    public static <T> OProperty<T> simple(String name, EdmType type, T value) {    	
    	return simple(name, type, value, false);
    }	
    	
    /**
     * Creates a new OData property of the given edm-type.
     * 
     * @param <T>  the property value's java-type
     * @param name  the property name
     * @param type  the property edm-type
     * @param value  the property value
     * @param exceptionOnUnknownType  if true, throw if the edm-type is unknown
     * @return a new OData property instance
     */
    @SuppressWarnings("unchecked")
	public static <T> OProperty<T> simple(String name, EdmType type, T value, boolean exceptionOnUnknownType) {    	
        if (type == EdmType.STRING) {
        	String sValue = null;
        	if (value != null) {
        		if (value instanceof Character) {
        			sValue = ((Character)value).toString();
        		} else {
        			sValue = (String) value;
        		}
        	}
            return (OProperty<T>)OProperties.string(name, sValue);
        } else if (type == EdmType.BOOLEAN) {
            Boolean bValue = (Boolean) value;
            return (OProperty<T>)OProperties.boolean_(name, bValue);
        } else if (type == EdmType.INT16) {
            Short sValue = (Short) value;
            return (OProperty<T>)OProperties.int16(name, sValue);
        } else if (type == EdmType.INT32) {
            Integer iValue = (Integer) value;
            return (OProperty<T>)OProperties.int32(name, iValue);
        } else if (type == EdmType.INT64) {
            Long iValue = (Long) value;
            return (OProperty<T>)OProperties.int64(name, iValue);
        } else if (type == EdmType.BYTE) {
            Byte bValue = (Byte) value;
            return (OProperty<T>)OProperties.byte_(name, bValue);
        }  else if (type == EdmType.DECIMAL) {
            BigDecimal dValue = (BigDecimal) value;
            return (OProperty<T>)OProperties.decimal(name, dValue);
        } else if (type == EdmType.DATETIME) {
        	if (value instanceof LocalDateTime)
        		return (OProperty<T>)OProperties.datetime(name, (LocalDateTime)value);
        	else if (value instanceof Calendar)
        		return (OProperty<T>)OProperties.datetime(name, (Date)((Calendar)value).getTime());
        	else 
        		return (OProperty<T>)OProperties.datetime(name, (Date)value);
        } else if (type == EdmType.TIME) {
        	if (value instanceof LocalTime)
        		return (OProperty<T>)OProperties.time(name, (LocalTime)value);
    		else if (value instanceof Calendar)
        		return (OProperty<T>)OProperties.time(name, (Date)((Calendar)value).getTime());        		
    		else 
        		return (OProperty<T>)OProperties.time(name, (Date)value);        		
        } else if (type == EdmType.BINARY) {
            byte[] bValue = (byte[]) value;
            return (OProperty<T>)OProperties.binary(name, bValue);
        } else if (type == EdmType.DOUBLE) {
        	Double dValue = (Double) value;
            return (OProperty<T>)OProperties.double_(name, dValue);
        } else if (type == EdmType.SINGLE) {
        	Float fValue = (Float) value;
            return (OProperty<T>)OProperties.single(name, fValue);
        } else if (type == EdmType.GUID) {
        	Guid gValue = (Guid) value;
            return (OProperty<T>)OProperties.guid(name, gValue);
        } else {
        	if (exceptionOnUnknownType) {
        		throw new UnsupportedOperationException("Implement " + type);
        	} else {
        		return new PropertyImpl<T>(name, type, value);
        	}
        }
    }

    /**
     * Creates a new OData property of the given edm-type with a null value.
     * 
     * @param name  the property name
     * @param type  the property edm-type
     * @return a new OData property instance
     */
    public static OProperty<?> null_(String name, String type) {
        return new PropertyImpl<Object>(name, EdmType.get(type), null);
    }

    /**
     * Creates a new complex-valued OData property of the given edm-type.
     * 
     * @param name  the property name
     * @param type  the property edm-type
     * @param value  the property values
     * @return a new OData property instance
     */
    public static OProperty<List<OProperty<?>>> complex(String name, String type, List<OProperty<?>> value) {
        return new PropertyImpl<List<OProperty<?>>>(name, EdmType.get(type), value);
    }

    /**
     * Creates a new OData property of the given edm-type with a value parsed from a string.
     * 
     * @param name  the property name
     * @param type  the property edm-type
     * @param value  the property value
     * @return a new OData property instance
     */
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

    /**
     * Creates a new short-valued OData property with {@link EdmType#INT16}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Short> int16(String name, Short value) {
        return new PropertyImpl<Short>(name, EdmType.INT16, value);
    }

    /**
     * Creates a new integer-valued OData property with {@link EdmType#INT32}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Integer> int32(String name, Integer value) {
        return new PropertyImpl<Integer>(name, EdmType.INT32, value);
    }

    /**
     * Creates a new long-valued OData property with {@link EdmType#INT64}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Long> int64(String name, Long value) {
        return new PropertyImpl<Long>(name, EdmType.INT64, value);
    }

    /**
     * Creates a new String-valued OData property with {@link EdmType#STRING}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<String> string(String name, String value) {
        return new PropertyImpl<String>(name, EdmType.STRING, value);
    }

    /**
     * Creates a new String-valued OData property with {@link EdmType#STRING}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<String> string(String name, char value) {
        return new PropertyImpl<String>(name, EdmType.STRING, Character.toString(value));
    }

    /**
     * Creates a new String-valued OData property with {@link EdmType#STRING}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Character> character(String name, Character value) {
        return new PropertyImpl<Character>(name, EdmType.STRING, value);
    }

    /**
     * Creates a new Guid-valued OData property with {@link EdmType#GUID}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Guid> guid(String name, String value) {
        return guid(name, Guid.fromString(value));
    }

    /**
     * Creates a new Guid-valued OData property with {@link EdmType#GUID}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Guid> guid(String name, Guid value) {
        return new PropertyImpl<Guid>(name, EdmType.GUID, value);
    }

    /**
     * Creates a new boolean-valued OData property with {@link EdmType#BOOLEAN}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Boolean> boolean_(String name, Boolean value) {
        return new PropertyImpl<Boolean>(name, EdmType.BOOLEAN, value);
    }

    /**
     * Creates a new single-precision-valued OData property with {@link EdmType#SINGLE}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Float> single(String name, Float value) {
        return new PropertyImpl<Float>(name, EdmType.SINGLE, value);
    }

    /**
     * Creates a new double-precision-valued OData property with {@link EdmType#DOUBLE}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Double> double_(String name, Double value) {
        return new PropertyImpl<Double>(name, EdmType.DOUBLE, value);
    }

    /**
     * Creates a new LocalDateTime-valued OData property with {@link EdmType#DATETIME}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<LocalDateTime> datetime(String name, LocalDateTime value) {
        return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, value);
    }

    /**
     * Creates a new LocalDateTime-valued OData property with {@link EdmType#DATETIME}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<LocalDateTime> datetime(String name, Date value) {
        return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, new LocalDateTime(value));
    }

    /**
     * Creates a new LocalDateTime-valued OData property with {@link EdmType#DATETIME}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<LocalDateTime> datetime(String name, Calendar value) {
        return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, new LocalDateTime(value));
    }

    /**
     * Creates a new LocalTime-valued OData property with {@link EdmType#TIME}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<LocalTime> time(String name, LocalTime value) {
        return new PropertyImpl<LocalTime>(name, EdmType.TIME, value);
    }
    
    /**
     * Creates a new LocalTime-valued OData property with {@link EdmType#TIME}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<LocalTime> time(String name, Date value) {
        return new PropertyImpl<LocalTime>(name, EdmType.TIME, new LocalTime(value));
    }    

    /**
     * Creates a new BigDecimal-valued OData property with {@link EdmType#DECIMAL}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<BigDecimal> decimal(String name, BigDecimal value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, value);
    }

    /**
     * Creates a new BigDecimal-valued OData property with {@link EdmType#DECIMAL}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<BigDecimal> decimal(String name, BigInteger value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, BigDecimal.valueOf(value.longValue()));
    }

    /**
     * Creates a new BigDecimal-valued OData property with {@link EdmType#DECIMAL}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<BigDecimal> decimal(String name, long value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, BigDecimal.valueOf(value));
    }

    /**
     * Creates a new BigDecimal-valued OData property with {@link EdmType#DECIMAL}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<BigDecimal> decimal(String name, double value) {
        return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, BigDecimal.valueOf(value));
    }

    /**
     * Creates a new byte-array-valued OData property with {@link EdmType#BINARY}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<byte[]> binary(String name, byte[] value) {
        return new PropertyImpl<byte[]>(name, EdmType.BINARY, value);
    }

    /**
     * Creates a new byte-array-valued OData property with {@link EdmType#BINARY}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
    public static OProperty<Byte[]> binary(String name, Byte[] value) {
        return new PropertyImpl<Byte[]>(name, EdmType.BINARY, value);
    }

    /**
     * Creates a new byte-valued OData property with {@link EdmType#BYTE}
     * 
     * @param name  the property name
     * @param value  the property value
     * @return a new OData property instance
     */
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
