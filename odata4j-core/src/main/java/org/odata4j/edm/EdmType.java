package org.odata4j.edm;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.core4j.Enumerable;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;

public class EdmType {

    private static Map<String, EdmType> POOL = new HashMap<String, EdmType>();

    // http://msdn.microsoft.com/en-us/library/bb399213.aspx
    public static final EdmType BINARY = getInternal("Edm.Binary",byte[].class,Byte[].class);
    public static final EdmType BOOLEAN = getInternal("Edm.Boolean",boolean.class,Boolean.class);
    public static final EdmType BYTE = getInternal("Edm.Byte",byte.class,Byte.class);
    public static final EdmType DATETIME = getInternal("Edm.DateTime",LocalDateTime.class);
    public static final EdmType DATETIMEOFFSET = getInternal("Edm.DateTimeOffset",DateTime.class);
    public static final EdmType DECIMAL = getInternal("Edm.Decimal",BigDecimal.class);
    public static final EdmType DOUBLE = getInternal("Edm.Double",double.class,Double.class);
    public static final EdmType GUID = getInternal("Edm.Guid",Guid.class);
    public static final EdmType INT16 = getInternal("Edm.Int16",short.class,Short.class);
    public static final EdmType INT32 = getInternal("Edm.Int32",int.class,Integer.class);
    public static final EdmType INT64 = getInternal("Edm.Int64",long.class,Long.class);
    public static final EdmType SINGLE = getInternal("Edm.Single",float.class,Float.class);
    public static final EdmType STRING = getInternal("Edm.String",char.class,Character.class,String.class);
    public static final EdmType TIME = getInternal("Edm.Time",LocalTime.class);
    
    public static Set<EdmType> SIMPLE = Collections.unmodifiableSet(Enumerable.create(POOL.values()).toSet());
    
    private final String typeString;
    private final Set<Class<?>> javaTypes;

    private EdmType(String typeString,Set<Class<?>> javaTypes) {
    	this.typeString = typeString;
        this.javaTypes = Collections.unmodifiableSet(javaTypes);
    }

    public static EdmType get(String typeString) {
        return getInternal(typeString);
    }

    private static EdmType getInternal(String typeString,Class<?>... javaTypes) {
        if (typeString == null)
            return null;
        Set<Class<?>> javaTypeSet = Enumerable.create(javaTypes).toSet();
        if (!POOL.containsKey(typeString))
            POOL.put(typeString, new EdmType(typeString,javaTypeSet));
        return POOL.get(typeString);
    }

    public boolean isSimple() {
        return javaTypes.size()>0;
    }

    public String toTypeString() {
        return typeString;
    }
    
    public Set<Class<?>> getJavaTypes() {
		return javaTypes;
	}

    @Override
    public String toString() {
        return toTypeString();
    }

	public static EdmType forJavaType(Class<?> javaType) {
		for(EdmType simple : SIMPLE)
			if (simple.getJavaTypes().contains(javaType))
				return simple;
		return null;
	}
	
	@Override
	public int hashCode() {
		return typeString.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof EdmType && ((EdmType)other).typeString.equals(typeString);
	}
	
}
	
