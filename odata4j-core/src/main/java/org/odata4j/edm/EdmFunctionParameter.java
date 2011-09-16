package org.odata4j.edm;

import java.util.List;

public class EdmFunctionParameter extends EdmItem {

  public final String name;
  public final EdmType type;
  public final String mode;

  public EdmFunctionParameter(String name, EdmType type, String mode) {
    this(name, type, mode, null, null);
  }
  
  public EdmFunctionParameter(String name, EdmType type, String mode, EdmDocumentation doc, List<EdmAnnotation> annots) {
    super(null, null); // TODO
    this.name = name;
    this.type = type;
    this.mode = mode;
  }
}
