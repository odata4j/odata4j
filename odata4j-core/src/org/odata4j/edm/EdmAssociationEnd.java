package org.odata4j.edm;

public class EdmAssociationEnd {

    public final String role;
    public final EdmEntityType type;
    public final EdmMultiplicity multiplicity;

    public EdmAssociationEnd(String role, EdmEntityType type, EdmMultiplicity multiplicity) {
        this.role = role;
        this.type = type;
        this.multiplicity = multiplicity;
    }
}
