
package org.odata4j.core;

/**
 * A generic annotation that lives in a namespace.
 * 
 * @author Tony Rozga
 */
public class Annotation implements IAnnotation {

  public Annotation(String namespaceURI, String namespacePrefix, String name, Object value) {
    this.namespaceURI = namespaceURI;
    this.localName = name;
    this.namespacePrefix = namespacePrefix;
    this.value = value;
  }

  public String getNamespaceURI() {
    return this.namespaceURI;
  }

  public String getLocalName() {
    return localName;
  }
  
  public Object getValue() {
    return value;
  }

  public String getNamespacePrefix() {
    return this.namespacePrefix;
  }

  /**
   * a simple annotation is one that can be represented with a simple datatype
   * like a string.
   * @return 
   */
  public boolean isSimple() {
    return value instanceof String;
  }
          
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Annotation)) {
      return false;
    }
    Annotation a = (Annotation) obj;
    return this.namespaceURI.equals(a.namespaceURI) && 
           // prefixes can differ across documents.s
           // this.namespacePrefix.equals(a.namespacePrefix) && 
           this.localName.equals(a.localName) ;
  }

  @Override
  public int hashCode() {
    return this.namespaceURI.hashCode() + this.localName.hashCode();
  }
  
  private String namespaceURI;
  private String namespacePrefix;
  private String localName;
  private Object value;

}
