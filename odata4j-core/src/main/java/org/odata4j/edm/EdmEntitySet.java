package org.odata4j.edm;

/** Metadata describing a set of entities.
 *  This is a simple struct giving the user-facing "name" of the set,
 *  along with an internal schema definition of the entity type
 */
public class EdmEntitySet {

    public final String name;
    public final EdmEntityType type;

    /** Create an EdmEntitySet with a given name and type
     * 
     * @param name the user-facing name of the set
     * @param type the schema entry for this type
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
