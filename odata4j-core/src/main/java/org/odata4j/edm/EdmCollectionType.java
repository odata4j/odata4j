package org.odata4j.edm;

/**
 * A homogeneous collection of OObjects
 */
public class EdmCollectionType extends EdmBaseType {

  public EdmCollectionType(String fqTypeName, EdmBaseType collectionType) {
    super(fqTypeName);
    this.collectionType = collectionType;
  }

  public EdmBaseType getCollectionType() {
    return this.collectionType;
  }
  
  private final EdmBaseType collectionType;
}
