package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

public class EdmEntityType {

	public final String namespace;
	public final String name;
	public final String key;
	public final List<EdmProperty> properties;
	public final List<EdmNavigationProperty> navigationProperties;
	
	public EdmEntityType(String namespace, String name, String key, List<EdmProperty> properties, List<EdmNavigationProperty> navigationProperties){
		this.namespace = namespace;
		this.name = name;
		this.key = key;
		this.properties = properties==null?new ArrayList<EdmProperty>():properties;
		this.navigationProperties = navigationProperties==null?new ArrayList<EdmNavigationProperty>():navigationProperties;
	}

	public String getFQName() {
		return namespace + "." + name;
	}
	
	
}
