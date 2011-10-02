package org.odata4j.edm;

import java.util.List;

public class EdmAssociation extends EdmItem {

  private final String namespace;
  private final String alias;
  private final String name;
  private final EdmAssociationEnd end1;
  private final EdmAssociationEnd end2;

  public EdmAssociation(String namespace, String alias, String name, EdmAssociationEnd end1, EdmAssociationEnd end2) {
    this(namespace, alias, name, end1, end2, null, null);
  }

  public EdmAssociation(String namespace, String alias, String name, EdmAssociationEnd end1, EdmAssociationEnd end2,
      EdmDocumentation doc, List<EdmAnnotation<?>> annots) {
    super(doc, annots);
    this.namespace = namespace;
    this.alias = alias;
    this.name = name;
    this.end1 = end1;
    this.end2 = end2;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getAlias() {
    return alias;
  }

  public String getName() {
    return name;
  }

  public EdmAssociationEnd getEnd1() {
    return end1;
  }

  public EdmAssociationEnd getEnd2() {
    return end2;
  }

  public String getFQNamespaceName() {
    return namespace + "." + name;
  }

  public String getFQAliasName() {
    return alias == null ? null : (alias + "." + name);
  }

  @Override
  public String toString() {
    StringBuilder rt = new StringBuilder();
    rt.append("EdmAssociation[");
    if (namespace != null)
      rt.append(namespace + ".");
    rt.append(name);
    if (alias != null)
      rt.append(",alias=" + alias);
    rt.append(",end1=" + end1);
    rt.append(",end2=" + end2);
    rt.append(']');
    return rt.toString();
  }

}
