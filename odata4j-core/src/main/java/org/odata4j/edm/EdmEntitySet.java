package org.odata4j.edm;

/**
 * The EntitySet element in conceptual schema definition language is a logical container for instances of an entity type and instances of any type that is derived from that entity type. 
 *
 * @see http://msdn.microsoft.com/en-us/library/bb386874.aspx
 */
public class EdmEntitySet {

    /**
     * The name of the entity set.
     */
    public final String name;
    
    /**
     * The entity type for which the entity set contains instances.
     */
    public final EdmEntityType type;

    /**
     * Creates a new EdmEntitySet.
     * 
     * @param name  the name of the entity set
     * @param type  the entity type for which the entity set contains instances
     */
    public EdmEntitySet(String name, EdmEntityType type) {
        this.name = name;
        this.type = type;
    }
    
    @Override
    public String toString() {
    	return String.format("EdmEntitySet[%s,%s]",name,type);
    }
}
