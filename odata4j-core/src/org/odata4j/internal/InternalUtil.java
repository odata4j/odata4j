package org.odata4j.internal;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.core4j.Funcs;
import org.core4j.ThrowingFunc1;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.producer.inmemory.BeanModel;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLInputFactory2;

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

    @SuppressWarnings("unchecked")
    public static <T> T toEntity(Class<T> entityType, DataServicesAtomEntry dsae, FeedCustomizationMapping fcMapping){
        OEntity oe = InternalUtil.toOEntity(dsae,fcMapping);
        if (entityType.equals(OEntity.class))
          return (T)oe;
        else
          return (T)InternalUtil.toPojo(entityType, oe);
    }
    
    public static OEntity toOEntity(DataServicesAtomEntry dsae, FeedCustomizationMapping mapping) {
        if (mapping==null)
            return OEntities.create(dsae.properties);
        
        Enumerable<OProperty<?>> properties = Enumerable.create(dsae.properties);
        if (mapping.titlePropName != null)
            properties = properties.concat(OProperties.string(mapping.titlePropName, dsae.title));
        if (mapping.summaryPropName != null)
            properties = properties.concat(OProperties.string(mapping.summaryPropName, dsae.summary));
        
        return OEntities.create(properties.toList());
       
    }
    
    public static <T> T toPojo(Class<T> pojoClass, OEntity oe){
       
        try {
            
            Constructor<T> defaultCtor = findDefaultDeclaredConstructor(pojoClass);
            if (defaultCtor==null)
                throw new RuntimeException("Unable to find a default constructor for " + pojoClass.getName());
            
            if (!defaultCtor.isAccessible())
                defaultCtor.setAccessible(true);
            
            T rt = defaultCtor.newInstance();
            
            BeanModel beanModel = new BeanModel(pojoClass);
            
            for(OProperty<?> op : oe.getProperties()){
                if (beanModel.canWrite(op.getName()))
                    beanModel.setPropertyValue(rt,op.getName(),op.getValue());
            }
            
            return rt;
        } catch (Exception e) {
           throw new RuntimeException(e);
        } 
        
    }
    
    
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> findDefaultDeclaredConstructor(Class<T> pojoClass){
        for(Constructor<?> ctor : pojoClass.getDeclaredConstructors()){
            if (ctor.getParameterTypes().length==0)
                return (Constructor<T>)ctor;
        }
        return null;
    }
    
    
    public static String getEntityRelId(List<String> keyPropertyNames, final List<OProperty<?>> entityProperties, String entitySetName){
        String key = null;
        if (keyPropertyNames != null) {
            Object[] keyProperties = Enumerable.create(keyPropertyNames).select(new Func1<String,OProperty<?>>(){
                public OProperty<?> apply(String input) {
                    for(OProperty<?> entityProperty : entityProperties)
                        if(entityProperty.getName().equals(input))
                            return entityProperty;
                        throw new IllegalArgumentException("Key property '" + input + "' is invalid");
                }}).cast(Object.class).toArray(Object.class);
            key = InternalUtil.keyString( keyProperties);
        }
        
        return entitySetName + key;

    }
    


}
