package org.odata4j.core;

import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;

public class OEntities {

    public static OEntity create(final List<OProperty<?>> properties){
        return new OEntity(){

            @Override
            public String toString() {
                return "OEntity[" + Enumerable.create(getProperties()).join(",") + "]";
            }
            
            @Override
            public List<OProperty<?>> getProperties() {
                return properties;
            }

            @Override
            public OProperty<?> getProperty(final String propName) {
                return Enumerable.create(properties).first(new Predicate1<OProperty<?>>(){
                    public boolean apply(OProperty<?> input) {
                       return input.getName().equals(propName);
                    }});
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> OProperty<T> getProperty(String propName, Class<T> propClass) {
                return (OProperty<T>)getProperty(propName);
            }};
    }
}
