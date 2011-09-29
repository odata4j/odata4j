package org.odata4j.edm;

import java.util.List;

public class EdmFunctionParameter extends EdmItem {

  public enum Mode {
    In, Out, InOut;
  };

  public final String name;
  public final EdmType type;
  public final Mode mode;

  public EdmFunctionParameter(String name, EdmType type, Mode mode) {
    this(name, type, mode, null, null);
  }
  
  public EdmFunctionParameter(String name, EdmType type, Mode mode, EdmDocumentation doc, List<EdmAnnotation> annots) {
    super(null, null); // TODO
    this.name = name;
    this.type = type;
    this.mode = mode;
  }
}
