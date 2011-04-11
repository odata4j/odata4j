package org.odata4j.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NamedValues {

	public static <T> NamedValue<T> create(String name, T value){
		return new NamedValueImpl<T>(name,value);
	}
	public static Set<NamedValue<Object>> fromMap(Map<String,Object> values){
		Set<NamedValue<Object>> rt = new HashSet<NamedValue<Object>>();
		for(Map.Entry<String,Object> entry : values.entrySet())
			rt.add(new NamedValueImpl<Object>(entry.getKey(),entry.getValue()));
		return rt;
	}
	
	public static <T> NamedValue<T> copy(NamedValue<T> value){
		return new NamedValueImpl<T>(value);
	}
	
	private static class NamedValueImpl<T> implements NamedValue<T>{

		private final String name;
		private final T value;
		
		public NamedValueImpl(String name, T value){
			this.name = name;
			this.value = value;
		}
		public NamedValueImpl(NamedValue<T> namedValue){
			this.name = namedValue.getName();
			this.value = namedValue.getValue();
		}
		@Override
		public String getName() {
			return name;
		}

		@Override
		public T getValue() {
			return value;
		}
		
	}
}
