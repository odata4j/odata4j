package org.odata4j.internal;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLInputFactory2;
import org.odata4j.xml.AtomFeedParser.DataServicesAtomEntry;

import core4j.Enumerable;
import core4j.Func1;
import core4j.Funcs;
import core4j.ThrowingFunc1;

public class InternalUtil {

    public static XMLEventReader2 newXMLEventReader(Reader reader) {
        XMLInputFactory2 f = XMLFactoryProvider2.getInstance().newXMLInputFactory2();
        return f.createXMLEventReader(reader);
    }

    public static String reflectionToString(final Object obj) {
        StringBuilder rt = new StringBuilder();
        Class<?> objClass = obj.getClass();
        rt.append(objClass.getSimpleName());
        rt.append('[');

        String content = Enumerable.create(objClass.getFields()).select(Funcs.wrap(new ThrowingFunc1<Field, String>() {
            public String apply(Field f) throws Exception {
                Object fValue = f.get(obj);
                return f.getName() + ":" + fValue;
            }
        })).join(",");

        rt.append(content);

        rt.append(']');
        return rt.toString();
    }

    public static String keyString(Object[] key) {

        String keyValue;
        if (key.length == 1) {
            keyValue = keyString(key[0], false);
        } else {
            keyValue = Enumerable.create(key).select(new Func1<Object, String>() {
                public String apply(Object input) {
                    return keyString(input, true);
                }
            }).join(",");
        }

        return "(" + keyValue + ")";
    }

    @SuppressWarnings("unchecked")
    private static final Set<Object> INTEGRAL_TYPES = Enumerable.create(Integer.class, Integer.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE).cast(Object.class).toSet();

    private static String keyString(Object key, boolean includePropName) {
        if (key instanceof UUID) {
            return "guid'" + key + "'";
        } else if (key instanceof String) {
            return "'" + ((String) key).replace("'", "''") + "'";

        } else if (INTEGRAL_TYPES.contains(key.getClass())) {
            return key.toString();
        } else if (key instanceof OProperty<?>) {
            OProperty<?> oprop = (OProperty<?>) key;
            String value = keyString(oprop.getValue(), false);

            if (includePropName)
                return oprop.getName() + "=" + value;
            else
                return value;
        } else {
            return key.toString();
        }
    }

    public static OEntity toEntity(DataServicesAtomEntry dsae, FeedCustomizationMapping mapping) {
        if (mapping==null)
            return OEntities.create(dsae.properties);
        
        Enumerable<OProperty<?>> properties = Enumerable.create(dsae.properties);
        if (mapping.titlePropName != null)
            properties = properties.concat(OProperties.string(mapping.titlePropName, dsae.title));
        if (mapping.summaryPropName != null)
            properties = properties.concat(OProperties.string(mapping.summaryPropName, dsae.summary));
        
        return OEntities.create(properties.toList());
       
    }

}
