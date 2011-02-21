package org.odata4j.core;

import org.core4j.Predicate1;

public class OPredicates {

	public static Predicate1<OEntity> entityPropertyValueEquals(final String propName, final Object value){
		return new Predicate1<OEntity>() {
			@Override
			public boolean apply(OEntity input) {
				Object pv = input.getProperty(propName).getValue();
				return (value==null)?pv==null:value.equals(pv);
			}
		};
	}
}
