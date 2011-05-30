package org.odata4j.stax2;

public class QName2 {

  private final String namespaceURI;
  private final String localPart;
  private final String prefix;
  private final String fqName;

  public QName2(String localPart) {
    this(null, localPart);
  }

  public QName2(String namespaceURI, String localPart) {
    this(namespaceURI, localPart, null);
  }

  public QName2(String namespaceURI, String localPart, String prefix) {
    this.namespaceURI = namespaceURI;
    this.localPart = localPart;
    this.prefix = prefix;
    this.fqName = "{" + namespaceURI + "}" + "{" + prefix + "}" + localPart;
  }

  public String getLocalPart() {
    return localPart;
  }

  public String getNamespaceURI() {
    return namespaceURI;
  }

  public String getPrefix() {
    return prefix;
  }

  @Override
  public int hashCode() {
    return fqName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof QName2))
      return false;

    return fqName.equals(((QName2) obj).fqName);
  }

  @Override
  public String toString() {
    return fqName;
  }
}
