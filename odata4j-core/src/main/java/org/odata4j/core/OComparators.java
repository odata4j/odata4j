package org.odata4j.core;

import java.util.Comparator;

public class OComparators {

	private OComparators() {}
	
	@SuppressWarnings("rawtypes")
	public static Comparator<NamedValue> namedValueByNameRaw(){
		return new Comparator<NamedValue>(){
			public int compare(NamedValue lhs, NamedValue rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}};
	}

	public static Comparator<OProperty<?>> propertyByName() {
		return new Comparator<OProperty<?>>(){
			public int compare(OProperty<?> lhs, OProperty<?> rhs) {
				return lhs.getName().compareTo(rhs.getName());
			}};
	}
}
