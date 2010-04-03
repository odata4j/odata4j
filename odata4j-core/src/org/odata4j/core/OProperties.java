package org.odata4j.core;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import org.apache.commons.codec2.binary.Base64;
import org.apache.commons.codec2.binary.Hex;
import org.joda.time.LocalDateTime;
import org.odata4j.edm.EdmType;
import org.odata4j.expression.ExpressionParser;

public class OProperties {

	
	public static <T> OProperty<?> simple(String name, EdmType type, T value){
		return new PropertyImpl<T>(name,type,value);
	}
	public static OProperty<?> null_(String name, String type){
		return new PropertyImpl<Object>(name, EdmType.get(type), null);	
	}
	public static OProperty<List<OProperty<?>>> complex(String name, String type, List<OProperty<?>> value){
		return new PropertyImpl<List<OProperty<?>>>(name, EdmType.get(type), value);
	}
	
	
	
	public static OProperty<?> parse(String name, String type, String value){
		
		if (EdmType.GUID.toTypeString().equals(type)){
			UUID uValue = value==null?null:UUID.fromString(value);
			return OProperties.guid(name,uValue);
		} else if (EdmType.BOOLEAN.toTypeString().equals(type)){
			Boolean bValue = value==null?null:Boolean.parseBoolean(value);
			return OProperties.boolean_(name,bValue);
		} else if (EdmType.BYTE.toTypeString().equals(type)){
			Byte bValue = value==null?null:Byte.parseByte(value);
			return OProperties.byte_(name,bValue);
		} else if (EdmType.INT16.toTypeString().equals(type)){
			Short sValue = value==null?null:Short.parseShort(value);
			return OProperties.int16(name,sValue);
		} else if (EdmType.INT32.toTypeString().equals(type)){
			Integer iValue = value==null?null:Integer.parseInt(value);
			return OProperties.int32(name,iValue);
		} else if (EdmType.INT64.toTypeString().equals(type)){
			Long lValue = value==null?null:Long.parseLong(value);
			return OProperties.int64(name,lValue);
		} else if (EdmType.SINGLE.toTypeString().equals(type)){
			Float fValue =  value==null?null:Float.parseFloat(value);
			return OProperties.single(name,fValue);
		}  else if (EdmType.DOUBLE.toTypeString().equals(type)){
			Double dValue =  value==null?null:Double.parseDouble(value);
			return OProperties.double_(name,dValue);
		} else if (EdmType.DECIMAL.toTypeString().equals(type)){
			BigDecimal dValue =  value==null?null:new BigDecimal(value);
			return OProperties.decimal(name,dValue);
		} else if (EdmType.BINARY.toTypeString().equals(type)){
			byte[] bValue = new Base64().decode(value);
			return OProperties.binary(name, bValue);
		}else if (EdmType.DATETIME.toTypeString().equals(type)){
			if (value != null && value.matches(".*\\.\\d{1,7}Z?$")) {
				value= value.substring(0,value.lastIndexOf('.'));
			}
			if (value != null && value.endsWith("Z"))
				value = value.substring(0,value.length()-1);
			
			LocalDateTime dValue = value==null?null:new LocalDateTime(ExpressionParser.DATETIME_FORMATTER.parseDateTime(value));
			return OProperties.datetime(name,dValue);
		} else if (EdmType.STRING.toTypeString().equals(type) || type==null) {
			return OProperties.string(name,value);
		}
		throw new UnsupportedOperationException("type:" + type);
	}
	
	public static OProperty<Short> int16(String name, Short value){
		return new PropertyImpl<Short>(name, EdmType.INT16, value);
	}
	public static OProperty<Integer> int32(String name, Integer value){
		return new PropertyImpl<Integer>(name, EdmType.INT32, value);
	}
	public static OProperty<Long> int64(String name, Long value){
		return new PropertyImpl<Long>(name, EdmType.INT64, value);
	}
	
	public static OProperty<String> string(String name, String value){
		return new PropertyImpl<String>(name, EdmType.STRING, value);
	}
	
	public static OProperty<UUID> guid(String name, String value){
		return guid(name,UUID.fromString(value));
	}
	public static OProperty<UUID> guid(String name, UUID value){
		return new PropertyImpl<UUID>(name, EdmType.GUID, value);
	}
	public static OProperty<Boolean> boolean_(String name, Boolean value){
		return new PropertyImpl<Boolean>(name, EdmType.BOOLEAN, value);
	}
	public static OProperty<Float> single(String name, Float value){
		return new PropertyImpl<Float>(name, EdmType.SINGLE, value);
	}
	public static OProperty<Double> double_(String name, Double value){
		return new PropertyImpl<Double>(name, EdmType.DOUBLE, value);
	}
	public static OProperty<LocalDateTime> datetime(String name, LocalDateTime value){
		return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, value);
	}
	public static OProperty<LocalDateTime> datetime(String name, Date value){
		return new PropertyImpl<LocalDateTime>(name, EdmType.DATETIME, new LocalDateTime(value));
	}
	public static OProperty<Short> short_(String name, Short value){
		return new PropertyImpl<Short>(name, EdmType.INT16, value);
	}
	public static OProperty<BigDecimal> decimal(String name, BigDecimal value){
		return new PropertyImpl<BigDecimal>(name, EdmType.DECIMAL, value);
	}
	public static OProperty<byte[]> binary(String name, byte[] value){
		return new PropertyImpl<byte[]>(name, EdmType.BINARY, value);
	}
	public static OProperty<Byte> byte_(String name, byte value) {
		return new PropertyImpl<Byte>(name,EdmType.BYTE,value);
	}
	
	private static class PropertyImpl<T> implements OProperty<T> {

		private final String name;
		private final EdmType type;
		private final T value;
		
		public PropertyImpl(String name, EdmType type, T value){
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
			if (value instanceof byte[]){
				value = "0x"+Hex.encodeHexString((byte[])value);
			}
			return String.format("OProperty[%s,%s,%s]",name,type,value);
		}
		
	}


	
}
