package org.odata4j.core;

import org.core4j.Predicate1;

public class OPredicates {

	private OPredicates(){}
	
	public static Predicate1<OEntity> entityPropertyValueEquals(final String propName, final Object value){
		return new Predicate1<OEntity>() {
			@Override
			public boolean apply(OEntity input) {
				Object pv = input.getProperty(propName).getValue();
				return (value==null)?pv==null:value.equals(pv);
			}
		};
	}
	
	public static Predicate1<OLink> linkTitleEquals(final String title){
		return new Predicate1<OLink>() {
			@Override
			public boolean apply(OLink input) {
				String lt = input.getTitle();
				return (title==null)?lt==null:title.equals(lt);
			}
		};
	}
	
	public static Predicate1<OProperty<?>> propertyNameEquals(final String propName){
		return new Predicate1<OProperty<?>>() {
            public boolean apply(OProperty<?> input) {
                return input.getName().equals(propName);
            }
        };
	}
}
