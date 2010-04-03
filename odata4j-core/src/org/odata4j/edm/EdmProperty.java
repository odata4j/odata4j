package org.odata4j.edm;

public class EdmProperty {

    public final String name;
    public final EdmType type;
    public final boolean nullable;
    public final Integer maxLength;

    public EdmProperty(String name, EdmType type, boolean nullable, Integer maxLength) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.maxLength = maxLength;
    }

}
