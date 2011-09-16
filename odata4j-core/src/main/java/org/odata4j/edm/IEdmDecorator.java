
package org.odata4j.edm;

import java.util.List;
import org.odata4j.core.Namespace;

/**
 * Edm production can often be taken care of the framework ala JPAEdmGenerator.
 * However, some things are application specific like Documentation and Annotations
 * An IEdmDecorator is used in the context of a @see IEdmGenerator.
 * 
 * Sometimes the immutability of the Edm classes makes one have to sacrifice
 * simple interfaces....I would prefer something like:
 * 
 * void decorateEntityType(EdmEntityType type);
 * 
 * @author Tony Rozga
 */
public interface IEdmDecorator {
  
  List<Namespace> getNamespaces();
  
  EdmDocumentation getDocumentationForEntityType(String namespace, String typeName);
  List<EdmAnnotation> getAnnotationsForEntityType(String namespace, String typeName);
  
  EdmDocumentation getDocumentationForProperty(String namespace, String typename, String propName);
  List<EdmAnnotation> getAnnotationsForProperty(String namespace, String typename, String propName);
  
  // TODO: other EdmItem types here.
}
