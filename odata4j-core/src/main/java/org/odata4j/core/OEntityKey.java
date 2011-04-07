package org.odata4j.core;

import java.util.List;

import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.internal.InternalUtil;

public class OEntityKey {

	private final Object[] values;
	private OEntityKey(Object[] values){
		this.values = values;
	}
	
	@Override
	public String toString() {
		return InternalUtil.keyString(values);
	}

	public boolean isSingleValued() {
		return values.length==1;
	}

	public Object asSingleValue() {
		if (!isSingleValued())
			throw new RuntimeException("Compound key cannot be represented as a single value");
		return values[0];
	}

	public static OEntityKey create(Object... values) {
		validate(values);
		Object[] v = new Object[values.length];
		System.arraycopy(values, 0, v, 0, values.length);
		return new OEntityKey(v);
		
	}

	public static OEntityKey infer(EdmEntitySet entitySet, List<OProperty<?>> props) {
		if (entitySet==null) throw new IllegalArgumentException("EdmEntitySet cannot be null");
		if (props==null) throw new IllegalArgumentException("props cannot be null");
		EdmEntityType eet = entitySet.type;
		if (eet==null) throw new IllegalArgumentException("EdmEntityType cannot be null");
		Object[] v = new Object[eet.keys.size()];
		for(int i=0;i<v.length;i++)
			v[i] = eet.keys.get(i);
		validate(v);
		return new OEntityKey(v);
	}
	
	private static void validate(Object[] values){
		if (values==null) throw new IllegalArgumentException("Key values cannot be null");
		for(Object value : values)
			if (value==null) throw new IllegalArgumentException("Key values cannot be null");
		if (values.length==0)
			throw new IllegalArgumentException("Key values cannot be empty");
	}
}
