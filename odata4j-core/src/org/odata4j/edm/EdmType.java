package org.odata4j.edm;

import java.util.HashMap;
import java.util.Map;

public class EdmType {

	private static Map<String,EdmType> POOL = new HashMap<String,EdmType>();
	
	public static final EdmType BINARY = get(true,"Edm.Binary");
	public static final EdmType BOOLEAN = get(true,"Edm.Boolean");
	public static final EdmType DATETIME = get(true,"Edm.DateTime");
	public static final EdmType DATETIMEOFFSET = get(true,"Edm.DateTimeOffset");
	public static final EdmType TIME = get(true,"Edm.Time");
	public static final EdmType DECIMAL = get(true,"Edm.Decimal");
	public static final EdmType SINGLE = get(true,"Edm.Single");
	public static final EdmType DOUBLE = get(true,"Edm.Double");
	public static final EdmType GUID = get(true,"Edm.Guid");
	public static final EdmType INT16 = get(true,"Edm.Int16");
	public static final EdmType INT32 = get(true,"Edm.Int32");
	public static final EdmType INT64 = get(true,"Edm.Int64");
	public static final EdmType BYTE = get(true,"Edm.Byte");
	public static final EdmType STRING = get(true,"Edm.String");
	public static final EdmType FACETS = get(true,"Edm.Facets");
	
	private final boolean isPrimitive;
	private final String typeString;
	
	private EdmType(boolean isPrimitive, String typeString){
		this.isPrimitive = isPrimitive;
		this.typeString = typeString;
	}
	
	
	
	public static EdmType get(String typeString){
		return get(false,typeString);
	}
	private static EdmType get(boolean isPrimitive, String typeString){
		if (typeString==null)
			return null;
		
		if (!POOL.containsKey(typeString))
			POOL.put(typeString, new EdmType(isPrimitive,typeString));
		return POOL.get(typeString);
	}
	

	
	public boolean isPrimitive() {
		return isPrimitive;
	}
	public String toTypeString(){
		return typeString;
	}
	
	@Override
	public String toString() {
		return toTypeString();
	}
//	public static EdmType fromTypeString(String value){
//		for(EdmType et : values()){
//			if (et.toTypeString().equals(value))
//				return et;
//		}
//		return null;
//	}





	
}
