package org.odata4j.edm;

import java.util.List;

import org.odata4j.core.Named;


public class EdmProperty extends EdmPropertyBase {

  public enum CollectionKind {
    NONE,
    LIST,
    BAG
  }

  private final EdmStructuralType declaringType;
  private final EdmType type;
  private final boolean nullable;
  private final Integer maxLength;
  private final Boolean unicode;
  private final Boolean fixedLength;
  private final String storeGeneratedPattern;
  private final CollectionKind collectionKind;
  private final String defaultValue;
  private final Integer precision;
  private final Integer scale;

  private final String fcTargetPath;
  private final String fcContentKind;
  private final String fcKeepInContent;
  private final String fcEpmContentKind;
  private final String fcEpmKeepInContent;

  public EdmProperty(EdmDocumentation documentation, List<EdmAnnotation<?>> annotations, String name,
      EdmStructuralType declaringType, EdmType type, boolean nullable, Integer maxLength, Boolean unicode, Boolean fixedLength,
      String storeGeneratedPattern,
      String fcTargetPath, String fcContentKind, String fcKeepInContent, String fcEpmContentKind, String fcEpmKeepInContent,
      CollectionKind collectionKind, String defaultValue, Integer precision, Integer scale) {
    super(documentation, annotations, name);
    this.declaringType = declaringType;
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
    return String.format("EdmProperty[%s,%s]", getName(), type);
  }

  public EdmStructuralType getDeclaringType() {
    return this.declaringType;
  }


  public static EdmProperty.Builder newBuilder(String name) {
    return new Builder(name);
  }

  public static class Builder extends EdmPropertyBase.Builder<Builder> implements Named {

    private EdmStructuralType declaringType;
    private EdmType type;
    private boolean nullable;
    private Integer maxLength;
    private Boolean unicode;
    private Boolean fixedLength;
    private String storeGeneratedPattern;
    private CollectionKind collectionKind;
    private String defaultValue;
    private Integer precision;
    private Integer scale;

    private String fcTargetPath;
    private String fcContentKind;
    private String fcKeepInContent;
    private String fcEpmContentKind;
    private String fcEpmKeepInContent;

    private Builder(String name) {
      super(name);
      this.collectionKind = CollectionKind.NONE;
    }

    public EdmProperty build() {
      return new EdmProperty(getDocumentation(), getAnnotations(), getName(), declaringType, type, nullable, maxLength, unicode, fixedLength, storeGeneratedPattern,
          fcTargetPath, fcContentKind, fcKeepInContent, fcEpmContentKind, fcEpmKeepInContent, collectionKind,
          defaultValue, precision, scale);
    }

    public Builder setType(EdmType type) {
      this.type = type;
      return this;
    }

    public Builder setNullable(boolean nullable) {
      this.nullable = nullable;
      return this;
    }

    public Builder setDeclaringType(EdmStructuralType declaringType) {
      this.declaringType = declaringType;
      return this;
    }

    public Builder setMaxLength(Integer maxLength) {
      this.maxLength = maxLength;
      return this;
    }

    public Builder setUnicode(Boolean unicode) {
      this.unicode = unicode;
      return this;
    }

    public Builder setFixedLength(Boolean fixedLength) {
      this.fixedLength = fixedLength;
      return this;
    }

    public Builder setStoreGeneratedPattern(String storeGeneratedPattern) {
      this.storeGeneratedPattern = storeGeneratedPattern;
      return this;
    }

    public Builder setFcTargetPath(String fcTargetPath) {
      this.fcTargetPath = fcTargetPath;
      return this;
    }

    public Builder setFcContentKind(String fcContentKind) {
      this.fcContentKind = fcContentKind;
      return this;
    }

    public Builder setFcKeepInContent(String fcKeepInContent) {
      this.fcKeepInContent = fcKeepInContent;
      return this;
    }

    public Builder setFcEpmContentKind(String fcEpmContentKind) {
      this.fcEpmContentKind = fcEpmContentKind;
      return this;
    }

    public Builder setFcEpmKeepInContent(String fcEpmKeepInContent) {
      this.fcEpmKeepInContent = fcEpmKeepInContent;
      return this;
    }

    public Builder setCollectionKind(CollectionKind collectionKind) {
      this.collectionKind = collectionKind;
      return this;
    }

    public Builder setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    public Builder setPrecision(Integer precision) {
      this.precision = precision;
      return this;
    }

    public Builder setScale(Integer scale) {
      this.scale = scale;
      return this;
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

  }

}
