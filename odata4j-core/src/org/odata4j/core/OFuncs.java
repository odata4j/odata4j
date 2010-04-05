package org.odata4j.core;

import core4j.Func1;

public class OFuncs {

    public static <TProperty> Func1<OEntity,TProperty> entityPropertyValue(final String propName, final Class<TProperty> propClass){
        return new Func1<OEntity,TProperty>(){
            public TProperty apply(OEntity input) {
                return input.getProperty(propName, propClass).getValue();
            }};
    }
}
