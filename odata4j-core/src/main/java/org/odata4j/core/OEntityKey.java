package org.odata4j.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;

public class OEntityKey {

	public enum KeyType{
		SINGLE,
		COMPLEX
	}
	
	private final Object[] values;
	private final String keyString;

	private OEntityKey(Object[] values) {
		this.values = values;
		this.keyString = keyString(values);
	}
		
	@SuppressWarnings("unchecked")
	public static OEntityKey create(Object... values) {
		if (values!=null&&values.length==1&&values[0] instanceof Iterable<?>){
			return create(Enumerable.create((Iterable<Object>)values[0]).toArray(Object.class));
		}
		Object[] v = validate(values);
		return new OEntityKey(v);
	}
	
	public static OEntityKey create(Map<String,Object> values) {
		return create(NamedValues.fromMap(values));
	}

	public static OEntityKey infer(EdmEntitySet entitySet, List<OProperty<?>> props) {
		if (entitySet==null) throw new IllegalArgumentException("EdmEntitySet cannot be null");
		if (props==null) throw new IllegalArgumentException("props cannot be null");
		EdmEntityType eet = entitySet.type;
		if (eet==null) throw new IllegalArgumentException("EdmEntityType cannot be null");
		
		Object[] v = new Object[eet.keys.size()];
		for(int i=0;i<v.length;i++){
			String keyPropertyName = eet.keys.get(i);
			v[i] = getProp(props,keyPropertyName);
		}
		v = validate(v);
		return new OEntityKey(v);
	}
	
	 public static OEntityKey parse(String keyString) {
	        String ks = null;
	        if (keyString != null && keyString.length() > 0) {
	            if (keyString.startsWith("(") && keyString.endsWith(")")) {
	                ks = keyString.substring(1, keyString.length() - 1);
	            } else {
	            	ks = keyString;
	            }
	        }
	        if (ks == null) 
	            throw new RuntimeException("Unable to parse keyString");

	        Object idObject;

	        if (ks.startsWith("'") && ks.endsWith("'")) {
	            idObject = ks.substring(1, ks.length() - 1);
	        } else if (ks.endsWith("L")) {
	            idObject = Long.parseLong(ks.substring(0, ks.length() - 1));
	        } else {
	            idObject = Integer.parseInt(ks);
	        }

	        return OEntityKey.create(idObject);
	    }
	
	
		
		@Override
		public String toString() {
			return toKeyString();
		}
		
		public String toKeyString() {
			return keyString;
		}
		
		@Override
		public int hashCode() {
			return keyString.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof OEntityKey) && ((OEntityKey)obj).keyString.equals(keyString);
		}
		
		public Object asSingleValue() {
			if (values.length>1)
				throw new RuntimeException("Complex key cannot be represented as a single value");
			return values[0];
		}

		@SuppressWarnings("unchecked")
		public Set<NamedValue<?>> asComplexValue(){
			if (values.length==1)
				throw new RuntimeException("Single-valued key cannot be represented as a complex value");
			return (Set<NamedValue<?>>)(Object)Enumerable.create(values).toSet();
		}
		
		public KeyType getKeyType() {
			return values.length==1?KeyType.SINGLE:KeyType.COMPLEX;
		}

	
	
	
	
	private static OProperty<?> getProp(List<OProperty<?>> props, String name){
		for(OProperty<?> prop : props)
			if (prop.getName().equals(name))
				return prop;
		throw new IllegalArgumentException(String.format("Property %s not found in %s",name,props));
		
	}
	
	private static Object[] validate(Object[] values){
		if (values==null) 
			throw new IllegalArgumentException("Key values cannot be null");
		for(Object value : values)
			if (value==null) 
				throw new IllegalArgumentException("Key values cannot be null");
		if (values.length==0)
			throw new IllegalArgumentException("Key values cannot be empty");
		
		if (values.length==1){
			Object o = values[0];
			if (o instanceof NamedValue)
				o = ((NamedValue<?>)o).getValue();
			assertSimple(o);
			return new Object[]{o};
		}
		
		Object[] v = new Object[values.length];
		for(int i=0;i<values.length;i++){
			Object o = values[i];
			if (!(o instanceof NamedValue<?>))
				throw new IllegalArgumentException("Complex key values must be named");
			NamedValue<?> nv = (NamedValue<?>)o;
			if (nv.getName()==null||nv.getName().length()==0)
				throw new IllegalArgumentException("Complex key values must be named");
			if (nv.getValue()==null)
				throw new IllegalArgumentException("Complex key values cannot be null");
			assertSimple(nv.getValue());
			v[i] = NamedValues.copy(nv);          
		}
		return v;
		
	}
	private static void assertSimple(Object o){
		if (!EDM_SIMPLE_TYPES.contains(o.getClass()))
			throw new IllegalArgumentException("Key value must be a simple type, found: " + o.getClass().getName());
	}
	
	
	@SuppressWarnings("unchecked")
	private static final Set<Object> INTEGRAL_TYPES = 
			Enumerable.create(Integer.class, Integer.TYPE, Long.class, Long.TYPE,Short.class, Short.TYPE).cast(Object.class).toSet();
	
	@SuppressWarnings("unchecked")
	private static final Set<Object> CHAR_TYPES = 
			Enumerable.create(Character.class,Character.TYPE).cast(Object.class).toSet();
	
	@SuppressWarnings("unchecked")
	private static final Set<Object> EDM_SIMPLE_TYPES = 
			Enumerable.create(
					Guid.class,
					Boolean.class,Boolean.TYPE,
					Byte.class,Byte.TYPE,
					Short.class,Short.TYPE,
					Integer.class,Integer.TYPE,
					Long.class,Long.TYPE,
					Float.class,Float.TYPE,
					Double.class,Double.TYPE,
					BigDecimal.class,
					Byte[].class,byte[].class,
					LocalDateTime.class,
					LocalTime.class,
					Character.class,Character.TYPE,
					String.class
					).cast(Object.class).toSet();
	
	private static String keyString(Object[] values) {

		String keyValue;
		if (values.length == 1) {
			keyValue = keyString(values[0], false);
		} else {
			keyValue = Enumerable.create(values)
					.select(new Func1<Object, String>() {
						public String apply(Object input) {
							return keyString(input, true);
						}
					}).orderBy().join(",");
		}

		return "(" + keyValue + ")";
	}
	
	
	private static String keyString(Object key, boolean includePropName) {
		if (key instanceof Guid) {
			return "guid'" + key + "'";
		} else if (key instanceof String || CHAR_TYPES.contains(key.getClass())) {
			return "'" + key.toString().replace("'", "''") + "'";
		} else if (key instanceof Long) {
			return key.toString() + "L";
		} else if (INTEGRAL_TYPES.contains(key.getClass())) {
			return key.toString();
		} else if (key instanceof NamedValue<?>) {
			NamedValue<?> namedValue = (NamedValue<?>) key;
			String value = keyString(namedValue.getValue(), false);

			if (includePropName)
				return namedValue.getName() + "=" + value;
			else
				return value;
		} else {
			return key.toString();
		}
	}
}
