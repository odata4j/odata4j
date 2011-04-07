package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

public class EdmComplexType {

    public final String namespace;
    public final String name;
   
    public final List<EdmProperty> properties;
  

    public EdmComplexType(String namespace, String name, List<EdmProperty> properties) {
        this.namespace = namespace;
        this.name = name;
        this.properties = properties == null ? new ArrayList<EdmProperty>() : properties;
    }

    public String getFQName() {
        return namespace + "." + name;
    }

}
