package org.odata4j.edm;

public class EdmEntitySet {

    public final String name;
    public final EdmEntityType type;

    public EdmEntitySet(String name, EdmEntityType type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public String toString() {
    	return String.format("EdmEntitySet[%s,%s]",name,type);
    }
}
