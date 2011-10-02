package org.odata4j.edm;

import java.util.List;


public class EdmProperty extends EdmPropertyBase {

  private final EdmType type;
  private final boolean nullable;
  private final Integer maxLength;
  private final Boolean unicode;
  private final Boolean fixedLength;
  private final String storeGeneratedPattern;
  private final CollectionKind collectionKind;
  private final String defaultValue;
  private EdmStructuralType structuralType;
  private final Integer precision;
  private final Integer scale;

  private final String fcTargetPath;
  private final String fcContentKind;
  private final String fcKeepInContent;
  private final String fcEpmContentKind;
  private final String fcEpmKeepInContent;

  public enum CollectionKind {
    NONE,
    LIST,
    BAG
  }

  public EdmProperty(String name, EdmType type, boolean nullable) {
    this(name, type, nullable, null, null, null, null, null, null, null, null, null, CollectionKind.NONE, null, null);
  }

  public EdmProperty(String name, EdmType type, boolean nullable, CollectionKind collectionKind, EdmDocumentation documentation, List<EdmAnnotation<?>> annotations) {
    this(name, type, nullable, null, null, null, null, null, null, null, null, null, collectionKind, documentation, annotations);
  }

  public EdmProperty(String name, EdmType type, boolean nullable, Integer maxLength, Boolean unicode, Boolean fixedLength,
      String storeGeneratedPattern,
      String fcTargetPath, String fcContentKind, String fcKeepInContent, String fcEpmContentKind, String fcEpmKeepInContent,
      CollectionKind collectionKind, EdmDocumentation documentation, List<EdmAnnotation<?>> annotations) {
    this(name, type, nullable, maxLength, unicode, fixedLength, storeGeneratedPattern,
        fcTargetPath, fcContentKind, fcKeepInContent, fcEpmContentKind,
        fcEpmKeepInContent, collectionKind, documentation, annotations, null, null, null);
  }

  public EdmProperty(String name, EdmType type, boolean nullable, Integer maxLength, Boolean unicode, Boolean fixedLength,
      String storeGeneratedPattern,
      String fcTargetPath, String fcContentKind, String fcKeepInContent, String fcEpmContentKind, String fcEpmKeepInContent,
      CollectionKind collectionKind, EdmDocumentation documentation, List<EdmAnnotation<?>> annotations, String defaultValue,
      Integer precision, Integer scale) {

    super(name, documentation, annotations);
    this.type = type;
    this.nullable = nullable;
    this.maxLength = maxLength;
    this.unicode = unicode;
    this.fixedLength = fixedLength;
    this.storeGeneratedPattern = storeGeneratedPattern;
    this.collectionKind = collectionKind;
    this.defaultValue = defaultValue;
    this.precision = precision;
    this.scale = scale;

    this.fcTargetPath = fcTargetPath;
    this.fcContentKind = fcContentKind;
    this.fcKeepInContent = fcKeepInContent;
    this.fcEpmContentKind = fcEpmContentKind;
    this.fcEpmKeepInContent = fcEpmKeepInContent;
  }

  public EdmType getType() {
    return type;
  }

  public boolean isNullable() {
    return nullable;
  }

  public Integer getMaxLength() {
    return maxLength;
  }

  public Boolean getUnicode() {
    return unicode;
  }

  public Boolean getFixedLength() {
    return fixedLength;
  }

  public String getStoreGeneratedPattern() {
    return storeGeneratedPattern;
  }

  public CollectionKind getCollectionKind() {
    return collectionKind;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public Integer getPrecision() {
    return precision;
  }

  public Integer getScale() {
    return scale;
  }

  public String getFcTargetPath() {
    return fcTargetPath;
  }

  public String getFcContentKind() {
    return fcContentKind;
  }

  public String getFcKeepInContent() {
    return fcKeepInContent;
  }

  public String getFcEpmContentKind() {
    return fcEpmContentKind;
  }

  public String getFcEpmKeepInContent() {
    return fcEpmKeepInContent;
  }

  @Override
  public String toString() {
    return String.format("EdmProperty[%s,%s]", name, type);
  }

  /**
   * Design Note (Tony Rozga):
   * The stucturalType field breaks immutability but for a very good reason:  In
   * the world of queryable metadata, a property's structural type name and the
   * namespace it lives in form the key of the EdmProperty queryable item.  We don't
   * know the structural type instance when we create the property instance so we
   * add a setter.
   */
  // TODO remove!
  public void setStructuralType(EdmStructuralType value) {
    this.structuralType = value;
  }

  public EdmStructuralType getStructuralType() {
    return this.structuralType;
  }

}
