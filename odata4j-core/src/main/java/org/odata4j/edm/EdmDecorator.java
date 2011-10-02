
package org.odata4j.edm;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.odata4j.core.Annotation;
import org.odata4j.core.Namespace;
import org.odata4j.core.OProperty;
import org.odata4j.producer.Path;

/**
 * Edm production can often be taken care of the framework ala JPAEdmGenerator.
 * However, some things are application specific like Documentation and Annotations
 * An EdmDecorator is used in the context of a @see EdmGenerator.
 *
 * Sometimes the immutability of the Edm classes makes one have to sacrifice
 * simple interfaces....I would prefer something like:
 *
 * void decorateEntityType(EdmEntityType type);
 */
public interface EdmDecorator {

  List<Namespace> getNamespaces();

  EdmDocumentation getDocumentationForSchema(String namespace, String typeName);
  List<EdmAnnotation<?>> getAnnotationsForSchema(String namespace, String typeName);

  EdmDocumentation getDocumentationForEntityType(String namespace, String typeName);
  List<EdmAnnotation<?>> getAnnotationsForEntityType(String namespace, String typeName);

  /**
   * try to resolve a custom property (i.e. Annotation) on a structural type
   * @param st - the type
   * @param path - the path to the property
   * @return - a property value (may be null) for the requested property if it exists.
   * @throws - IllegalArgumentException if the property does not exist.
   */
  Object resolveStructuralTypeProperty(EdmStructuralType st, Path path) throws IllegalArgumentException;

  EdmDocumentation getDocumentationForProperty(String namespace, String typename, String propName);
  List<EdmAnnotation<?>> getAnnotationsForProperty(String namespace, String typename, String propName);

   /**
   * try to resolve a custom property (i.e. Annotation) on a property type
   * @param st - the type
   * @param path - the path to the property
   * @return - a property value (may be null) for the requested property if it exists.
   * @throws - IllegalArgumentException if the property does not exist.
   */
  Object resolvePropertyProperty(EdmProperty st, Path path) throws IllegalArgumentException;

  // TODO: other EdmItem types here.

  /**
   * Get an annotation value that overrides the original annotation value.
   * This is an experiment that allows one to localize queryable metadata.
   * Say you have an annotation called LocalizedName on your item.  When
   * the metadata is queried, the caller can supply a custom locale parameter
   * in options and this method can override the original LocalizedName with
   * the one for the given locale.
   * @param item - the annotated item.
   * @param annot - the annotation
   * @param options - from query
   * @return - null if no override, an object with the value if there is one.
   */
  Object getAnnotationValueOverride(EdmItem item, Annotation<?> annot, boolean flatten, Locale locale, Map<String, String> options);

  /**
   * give the decorator a hook to modify outgoing metadata entities.
   * @param entitySet
   * @param props
   */
  void decorateEntity(EdmEntitySet entitySet, EdmItem item, EdmItem originalQueryItem,
      List<OProperty<?>> props, boolean flatten, Locale locale, Map<String, String> options);
}
