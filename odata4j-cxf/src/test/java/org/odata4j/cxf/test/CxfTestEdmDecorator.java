package org.odata4j.cxf.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.odata4j.core.NamespacedAnnotation;
import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.core.PrefixedNamespace;
import org.odata4j.edm.EdmAnnotation;
import org.odata4j.edm.EdmAnnotationAttribute;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDecorator;
import org.odata4j.edm.EdmDocumentation;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmItem;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmStructuralType;
import org.odata4j.producer.PropertyPath;

public class CxfTestEdmDecorator implements EdmDecorator {

  public static final String namespace = "http://test.cxf.odata4j.org";
  public static final String prefix = "inmem";

  private final List<PrefixedNamespace> namespaces = new ArrayList<PrefixedNamespace>(1);
  private final EdmComplexType schemaInfoType;

  public CxfTestEdmDecorator() {
    namespaces.add(new PrefixedNamespace(namespace, prefix));
    this.schemaInfoType = createSchemaInfoType().build();
  }

  @Override
  public List<PrefixedNamespace> getNamespaces() {
    return namespaces;
  }

  @Override
  public EdmDocumentation getDocumentationForSchema(String namespace) {
    return new EdmDocumentation("InMemoryProducerExample", "This schema exposes a few example types to demonstrate the InMemoryProducer");
  }

  private EdmComplexType.Builder createSchemaInfoType() {
    List<EdmProperty.Builder> props = new ArrayList<EdmProperty.Builder>();

    EdmProperty.Builder ep = EdmProperty.newBuilder("Author").setType(EdmSimpleType.STRING);
    props.add(ep);

    ep = EdmProperty.newBuilder("SeeAlso").setType(EdmSimpleType.STRING);
    props.add(ep);

    return EdmComplexType.newBuilder().setNamespace(namespace).setName("SchemaInfo").addProperties(props);

  }

  @Override
  public List<EdmAnnotation<?>> getAnnotationsForSchema(String namespace) {
    List<EdmAnnotation<?>> annots = new ArrayList<EdmAnnotation<?>>();
    annots.add(new EdmAnnotationAttribute(namespace, prefix, "Version", "1.0 early experience pre-alpha"));

    List<OProperty<?>> p = new ArrayList<OProperty<?>>();
    p.add(OProperties.string("Author", "Xavier S. Dumont"));
    p.add(OProperties.string("SeeAlso", "InMemoryProducerExample.java"));

    annots.add(EdmAnnotation.element(namespace, prefix, "SchemaInfo", OComplexObject.class,
        OComplexObjects.create(schemaInfoType, p)));

    annots.add(EdmAnnotation.element(namespace, prefix, "Tags", OCollection.class,
        OCollections.newBuilder(EdmSimpleType.STRING)
            .add(OSimpleObjects.create(EdmSimpleType.STRING, "tag1"))
            .add(OSimpleObjects.create(EdmSimpleType.STRING, "tag2"))
            .build()));
    return annots;
  }

  @Override
  public EdmDocumentation getDocumentationForEntityType(String namespace, String typeName) {
    return null;
  }

  @Override
  public List<EdmAnnotation<?>> getAnnotationsForEntityType(String namespace, String typeName) {
    return null;
  }

  @Override
  public Object resolveStructuralTypeProperty(EdmStructuralType st, PropertyPath path) throws IllegalArgumentException {
    return null;
  }

  @Override
  public EdmDocumentation getDocumentationForProperty(String namespace, String typename, String propName) {
    return null;
  }

  @Override
  public List<EdmAnnotation<?>> getAnnotationsForProperty(String namespace, String typename, String propName) {
    return null;
  }

  @Override
  public Object resolvePropertyProperty(EdmProperty st, PropertyPath path) throws IllegalArgumentException {
    return null;
  }

  @Override
  public Object getAnnotationValueOverride(EdmItem item, NamespacedAnnotation<?> annot, boolean flatten, Locale locale, Map<String, String> options) {
    return null;
  }

  @Override
  public void decorateEntity(EdmEntitySet entitySet, EdmItem item, EdmItem originalQueryItem, List<OProperty<?>> props, boolean flatten, Locale locale, Map<String, String> options) {
    // no-op
  }

}