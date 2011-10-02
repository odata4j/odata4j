package org.odata4j.edm;

import java.util.List;

public class EdmFunctionParameter extends EdmItem {

  public enum Mode {
    IN, OUT, IN_OUT;
  };

  private final String name;
  private final EdmType type;
  private final Mode mode;

  public EdmFunctionParameter(String name, EdmType type, Mode mode) {
    this(name, type, mode, null, null);
  }

  public EdmFunctionParameter(String name, EdmType type, Mode mode, EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
    super(null, null);
    this.name = name;
    this.type = type;
    this.mode = mode;
  }

  public String getName() {
    return name;
  }

  public EdmType getType() {
    return type;
  }

  public Mode getMode() {
    return mode;
  }

}
