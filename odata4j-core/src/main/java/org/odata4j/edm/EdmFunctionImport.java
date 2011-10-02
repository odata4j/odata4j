package org.odata4j.edm;

import java.util.List;

public class EdmFunctionImport extends EdmItem {

  private final String name;
  private final EdmEntitySet entitySet;
  private final EdmType returnType;
  private final String httpMethod;
  private final List<EdmFunctionParameter> parameters;

  public EdmFunctionImport(String name, EdmEntitySet entitySet, EdmType returnType,
      String httpMethod, List<EdmFunctionParameter> parameters) {
    this(name, entitySet, returnType, httpMethod, parameters, null, null);
  }

  public EdmFunctionImport(String name, EdmEntitySet entitySet, EdmType returnType,
      String httpMethod, List<EdmFunctionParameter> parameters, EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
    super(null, null);
    this.name = name;
    this.entitySet = entitySet;
    this.returnType = returnType;
    this.httpMethod = httpMethod;
    this.parameters = parameters;
  }

  public String getName() {
    return name;
  }

  public EdmEntitySet getEntitySet() {
    return entitySet;
  }

  public EdmType getReturnType() {
    return returnType;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public List<EdmFunctionParameter> getParameters() {
    return parameters;
  }

}
