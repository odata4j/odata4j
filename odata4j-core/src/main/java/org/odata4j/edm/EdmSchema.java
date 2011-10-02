package org.odata4j.edm;

import java.util.List;

import org.core4j.Enumerable;

public class EdmSchema extends EdmItem {

  private final String namespace;
  private final String alias;
  private final List<EdmEntityType> entityTypes;
  private final List<EdmComplexType> complexTypes;
  private final List<EdmAssociation> associations;
  private final List<EdmEntityContainer> entityContainers;

  public EdmSchema(String namespace, String alias, List<EdmEntityType> entityTypes,
      List<EdmComplexType> complexTypes, List<EdmAssociation> associations,
      List<EdmEntityContainer> entityContainers) {
    this(namespace, alias, entityTypes, complexTypes, associations, entityContainers, null, null);
  }

  public EdmSchema(String namespace, String alias, List<EdmEntityType> entityTypes,
      List<EdmComplexType> complexTypes, List<EdmAssociation> associations,
      List<EdmEntityContainer> entityContainers,
      EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
    super(doc, annots);
    this.namespace = namespace;
    this.alias = alias;
    this.entityTypes = entityTypes == null ? Enumerable.empty(EdmEntityType.class).toList() : entityTypes;
    this.complexTypes = complexTypes == null ? Enumerable.empty(EdmComplexType.class).toList() : complexTypes;
    this.associations = associations == null ? Enumerable.empty(EdmAssociation.class).toList() : associations;
    this.entityContainers = entityContainers == null ? Enumerable.empty(EdmEntityContainer.class).toList() : entityContainers;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getAlias() {
    return alias;
  }

  public List<EdmEntityType> getEntityTypes() {
    return entityTypes;
  }

  public List<EdmComplexType> getComplexTypes() {
    return complexTypes;
  }

  public List<EdmAssociation> getAssociations() {
    return associations;
  }

  public List<EdmEntityContainer> getEntityContainers() {
    return entityContainers;
  }

  public EdmEntityContainer findEntityContainer(String name) {
    for (EdmEntityContainer ec : entityContainers) {
      if (ec.getName().equals(name)) {
        return ec;
      }
    }
    return null;
  }

}
