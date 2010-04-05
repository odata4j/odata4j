package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

public class EdmEntityType {

    public final String namespace;
    public final String name;
    public final Boolean hasStream;
    public final List<String> keys;
    public final List<EdmProperty> properties;
    public final List<EdmNavigationProperty> navigationProperties;

    public EdmEntityType(String namespace, String name, Boolean hasStream, List<String> keys, List<EdmProperty> properties, List<EdmNavigationProperty> navigationProperties) {
        this.namespace = namespace;
        this.name = name;
        this.hasStream = hasStream;
        
        this.keys = keys;
        this.properties = properties == null ? new ArrayList<EdmProperty>() : properties;
        this.navigationProperties = navigationProperties == null ? new ArrayList<EdmNavigationProperty>() : navigationProperties;
    }

    public String getFQName() {
        return namespace + "." + name;
    }

}
