package org.odata4j.edm;

import java.util.List;

import core4j.Enumerable;

public class EdmSchema {

    public final String namespace;
    public final List<EdmEntityType> entityTypes;
    public final List<EdmComplexType> complexTypes;
    public final List<EdmAssociation> associations;
    public final List<EdmEntityContainer> entityContainers;

    public EdmSchema(String namespace, List<EdmEntityType> entityTypes, List<EdmComplexType> complexTypes, List<EdmAssociation> associations, List<EdmEntityContainer> entityContainers) {
        this.namespace = namespace;
        this.entityTypes = entityTypes == null ? Enumerable.empty(EdmEntityType.class).toList() : entityTypes;
        this.complexTypes = complexTypes == null ? Enumerable.empty(EdmComplexType.class).toList() : complexTypes;
        this.associations = associations == null ? Enumerable.empty(EdmAssociation.class).toList() : associations;
        this.entityContainers = entityContainers == null ? Enumerable.empty(EdmEntityContainer.class).toList() : entityContainers;

    }
}
