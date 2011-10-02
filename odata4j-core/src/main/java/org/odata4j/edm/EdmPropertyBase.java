package org.odata4j.edm;

import java.util.List;

public abstract class EdmPropertyBase extends EdmItem {

  private final String name;

  protected EdmPropertyBase(String name, EdmDocumentation documentation, List<EdmAnnotation<?>> annotations) {
    super(documentation, annotations);
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
