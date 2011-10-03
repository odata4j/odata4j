package org.odata4j.edm;

import java.util.List;

import org.odata4j.core.Named;

public abstract class EdmPropertyBase extends EdmItem implements Named {

  private final String name;

  protected EdmPropertyBase(EdmDocumentation documentation, List<EdmAnnotation<?>> annotations, String name) {
    super(documentation, annotations);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static class Builder<T> extends EdmItem.Builder<T> implements Named {

    private String name;

    Builder(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @SuppressWarnings("unchecked")
    public T setName(String name) {
      this.name = name;
      return (T) this;
    }

  }

}
