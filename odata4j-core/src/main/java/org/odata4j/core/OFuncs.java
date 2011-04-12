package org.odata4j.core;

import org.core4j.Func1;

public class OFuncs {

    public static <TProperty> Func1<OEntity,TProperty> entityPropertyValue(final String propName, final Class<TProperty> propClass){
        return new Func1<OEntity,TProperty>(){
            public TProperty apply(OEntity input) {
                return input.getProperty(propName, propClass).getValue();
            }};
    }
    
    public static <T> Func1<NamedValue<T>,OProperty<T>> namedValueToProperty(){
    	return new Func1<NamedValue<T>,OProperty<T>>(){
			public OProperty<T> apply(NamedValue<T> input) {
				return OProperties.simple(input.getName(), input.getValue());
			}};
    }
    
	@SuppressWarnings("rawtypes")
	public static Func1<NamedValue,OProperty<?>> namedValueToPropertyRaw(){
    	return new Func1<NamedValue,OProperty<?>>(){
			public OProperty<?> apply(NamedValue input) {
				return OProperties.simple(input.getName(), input.getValue());
			}};
    }
}
