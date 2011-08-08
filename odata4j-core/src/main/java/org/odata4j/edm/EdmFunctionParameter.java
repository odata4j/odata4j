package org.odata4j.edm;

public class EdmFunctionParameter {

  public final String name;
  public final EdmBaseType type;
  public final String mode;

  public EdmFunctionParameter(String name, EdmBaseType type, String mode) {
    this.name = name;
    this.type = type;
    this.mode = mode;
  }
}
