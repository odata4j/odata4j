package org.odata4j.core;

import org.odata4j.edm.EdmBaseType;
import org.odata4j.edm.EdmType;

/**
 * Factory for creating OSimpleObjects
 *
 */
public class OSimpleObjects {

    public static <V> OSimpleObject<V> create(V value, EdmType type) {

        if (!type.isSimple()) {
            throw new RuntimeException("type is not simple");
        }

        return new SimpleObject<V>(OProperties.simple(null, type, value, true));
    }

    private static class SimpleObject<V> implements OSimpleObject<V> {

        public SimpleObject(OProperty<V> prop) {
            this.prop = prop;
        }

        @Override
        public V getValue() {
            return prop.getValue();
        }

        @Override
        public EdmBaseType getType() {
            return prop.getType();
        }

        final OProperty<V> prop;
    }
}
