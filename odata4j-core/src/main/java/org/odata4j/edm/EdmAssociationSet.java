package org.odata4j.edm;

import java.util.List;

public class EdmAssociationSet extends EdmItem {

  private final String name;
  private final EdmAssociation association;
  private final EdmAssociationSetEnd end1;
  private final EdmAssociationSetEnd end2;

  public EdmAssociationSet(String name, EdmAssociation association, EdmAssociationSetEnd end1, EdmAssociationSetEnd end2) {
    this(name, association, end1, end2, null, null);
  }

  public EdmAssociationSet(String name, EdmAssociation association, EdmAssociationSetEnd end1, EdmAssociationSetEnd end2,
      EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
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

}
