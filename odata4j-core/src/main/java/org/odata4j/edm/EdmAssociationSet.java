package org.odata4j.edm;

import java.util.List;

public class EdmAssociationSet extends EdmItem {

  public final String name;
  public final EdmAssociation association;
  public final EdmAssociationSetEnd end1;
  public final EdmAssociationSetEnd end2;

  public EdmAssociationSet(String name, EdmAssociation association, EdmAssociationSetEnd end1, EdmAssociationSetEnd end2) {
    this(name, association, end1, end2, null, null);
  }    
  
  public EdmAssociationSet(String name, EdmAssociation association, EdmAssociationSetEnd end1, EdmAssociationSetEnd end2, 
          EdmDocumentation doc, List<EdmAnnotation> annots) {
    super(doc, annots); // TODO
    this.name = name;
    this.association = association;
    this.end1 = end1;
    this.end2 = end2;
  }

}
