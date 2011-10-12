package org.odata4j.edm;

import org.odata4j.core.ImmutableList;

public class EdmAssociationSet extends EdmItem {

  private final String name;
  private final EdmAssociation association;
  private final EdmAssociationSetEnd end1;
  private final EdmAssociationSetEnd end2;

  private EdmAssociationSet(String name, EdmAssociation association, EdmAssociationSetEnd end1, EdmAssociationSetEnd end2,
      EdmDocumentation doc, ImmutableList<EdmAnnotation<?>> annots) {
    super(doc, annots);
    this.name = name;
    this.association = association;
    this.end1 = end1;
    this.end2 = end2;
  }

  public String getName() {
    return name;
  }

  public EdmAssociation getAssociation() {
    return association;
  }

  public EdmAssociationSetEnd getEnd1() {
    return end1;
  }

  public EdmAssociationSetEnd getEnd2() {
    return end2;
  }

  public static Builder newBuilder() {
    return new Builder();
  }
  
  public static Builder newBuilder(EdmAssociationSet associationSet, BuilderContext context) {
    return context.newBuilder(associationSet, new Builder());
  }
  
  public static class Builder extends EdmItem.Builder<EdmAssociationSet, Builder> {

    private String name;
    private EdmAssociation.Builder association;
    private String associationName;
    private EdmAssociationSetEnd.Builder end1;
    private EdmAssociationSetEnd.Builder end2;

    @Override
    Builder newBuilder(EdmAssociationSet associationSet, BuilderContext context) {
      this.name = associationSet.name;
      this.association = EdmAssociation.newBuilder(associationSet.association, context);
      this.end1 = EdmAssociationSetEnd.newBuilder(associationSet.end1, context);
      this.end2 = EdmAssociationSetEnd.newBuilder(associationSet.end2, context);
      return this;
    }
    
    public EdmAssociationSet build() {
      return new EdmAssociationSet(name, association.build(), end1.build(), end2.build(), getDocumentation(), ImmutableList.copyOf(getAnnotations()));
    }

    public String getAssociationName() {
      return associationName;
    }

    public EdmAssociationSetEnd.Builder getEnd1() {
      return end1;
    }

    public EdmAssociationSetEnd.Builder getEnd2() {
      return end2;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setAssociationName(String associationName) {
      this.associationName = associationName;
      return this;
    }

    public Builder setAssociation(EdmAssociation.Builder association) {
      this.association = association;
      return this;
    }

    public Builder setEnds(EdmAssociationSetEnd.Builder end1, EdmAssociationSetEnd.Builder end2) {
      this.end1 = end1;
      this.end2 = end2;
      return this;
    }

  }

}
