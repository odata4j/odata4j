
package org.odata4j.edm;

/**
 * A CSDL Documenation element
 * 
 * @author Tony Rozga
 */
public class EdmDocumentation {
  
  public EdmDocumentation(String summary, String longDescription) {
    this.summary = summary;
    this.longDescription = longDescription;
  }
  
  public String getSummary() {
    return this.summary;
  }
  
  public String getLongDescription() {
    return this.longDescription;
  }
  
  private final String summary;
  private final String longDescription;
}
