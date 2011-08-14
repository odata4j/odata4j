package org.odata4j.edm;

/**
 * Describes a homogeneous collection of instances of a specific type.
 */
public class EdmCollectionType extends EdmType {

  private final EdmType collectionType;
  
  public EdmCollectionType(String fqTypeName, EdmType collectionType) {
    super(fqTypeName);
    if (collectionType == null) throw new IllegalArgumentException("collectionType cannot be null");
    this.collectionType = collectionType;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  public EdmType getCollectionType() {
    return collectionType;
  }
}
