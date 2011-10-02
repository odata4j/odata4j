package org.odata4j.edm;

import java.util.List;

import org.core4j.Enumerable;

public class EdmEntityContainer extends EdmItem {

  private final String name;
  private final boolean isDefault;
  private final Boolean lazyLoadingEnabled;
  private final List<EdmEntitySet> entitySets;
  private final List<EdmAssociationSet> associationSets;
  private final List<EdmFunctionImport> functionImports;

  public EdmEntityContainer(String name, boolean isDefault, Boolean lazyLoadingEnabled,
      List<EdmEntitySet> entitySets, List<EdmAssociationSet> associationSets,
      List<EdmFunctionImport> functionImports) {
    this(name, isDefault, lazyLoadingEnabled, entitySets, associationSets, functionImports, null, null);
  }

  public EdmEntityContainer(String name, boolean isDefault, Boolean lazyLoadingEnabled,
     List<EdmEntitySet> entitySets, List<EdmAssociationSet> associationSets,
     List<EdmFunctionImport> functionImports, EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
    super(doc, annots);
    this.name = name;
    this.isDefault = isDefault;
    this.lazyLoadingEnabled = lazyLoadingEnabled;
    this.entitySets = entitySets == null ? Enumerable.empty(EdmEntitySet.class).toList() : entitySets;
    this.associationSets = associationSets == null ? Enumerable.empty(EdmAssociationSet.class).toList() : associationSets;
    this.functionImports = functionImports == null ? Enumerable.empty(EdmFunctionImport.class).toList() : functionImports;
  }

  public String getName() {
    return name;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public Boolean getLazyLoadingEnabled() {
    return lazyLoadingEnabled;
  }

  public List<EdmEntitySet> getEntitySets() {
    return entitySets;
  }

  public List<EdmAssociationSet> getAssociationSets() {
    return associationSets;
  }

  public List<EdmFunctionImport> getFunctionImports() {
    return functionImports;
  }

}
