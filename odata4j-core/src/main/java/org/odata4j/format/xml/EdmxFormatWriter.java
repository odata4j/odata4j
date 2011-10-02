package org.odata4j.format.xml;

import java.io.Writer;

import org.odata4j.core.Annotation;
import org.odata4j.core.Namespace;
import org.odata4j.edm.EdmAnnotationAttribute;
import org.odata4j.edm.EdmAnnotationElement;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmDocumentation;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmItem;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSchema;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;

public class EdmxFormatWriter extends XmlFormatWriter {

  public static void write(EdmDataServices services, Writer w) {

    XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
    writer.startDocument();

    writer.startElement(new QName2(edmx, "Edmx", "edmx"));
    writer.writeAttribute("Version", "1.0");
    writer.writeNamespace("edmx", edmx);
    writer.writeNamespace("d", d);
    writer.writeNamespace("m", m);
    writeExtensionNamespaces(services, writer);

    writer.startElement(new QName2(edmx, "DataServices", "edmx"));
    writer.writeAttribute(new QName2(m, "DataServiceVersion", "m"), "1.0");

    // Schema
    for (EdmSchema schema : services.getSchemas()) {

      writer.startElement(new QName2("Schema"), edm);
      writer.writeAttribute("Namespace", schema.namespace);
      writeAnnotationAttributes(schema, writer);
      writeDocumentation(schema, writer);

      // ComplexType
      for (EdmComplexType ect : schema.complexTypes) {
        writer.startElement(new QName2("ComplexType"));

        writer.writeAttribute("Name", ect.name);
        if (null != ect.isAbstract) {
          writer.writeAttribute("Abstract", ect.isAbstract.toString());
        }
        writeAnnotationAttributes(ect, writer);
        writeDocumentation(ect, writer);

        write(ect.properties, writer);
        writeAnnotationElements(ect, writer);
        writer.endElement("ComplexType");
      }
      // EntityType
      for (EdmEntityType eet : schema.entityTypes) {
        writer.startElement(new QName2("EntityType"));

        writer.writeAttribute("Name", eet.name);
        if (null != eet.isAbstract) {
          writer.writeAttribute("Abstract", eet.isAbstract.toString());
        }

        // keys only on base types
        if (eet.isRootType()) {
          writeAnnotationAttributes(eet, writer);
          writeDocumentation(eet, writer);
          writer.startElement(new QName2("Key"));
          for (String key : eet.getKeys()) {
            writer.startElement(new QName2("PropertyRef"));
            writer.writeAttribute("Name", key);
            writer.endElement("PropertyRef");
          }

          writer.endElement("Key");
        } else {
          writer.writeAttribute("BaseType", eet.getBaseType().getFullyQualifiedTypeName());
          writeAnnotationAttributes(eet, writer);
          writeDocumentation(eet, writer);
        }

        write(eet.getDeclaredProperties(), writer);

        for (EdmNavigationProperty np : eet.getDeclaredNavigationProperties()) {


          writer.startElement(new QName2("NavigationProperty"));
          writer.writeAttribute("Name", np.name);
          writer.writeAttribute("Relationship", np.relationship.getFQNamespaceName());
          writer.writeAttribute("FromRole", np.fromRole.role);
          writer.writeAttribute("ToRole", np.toRole.role);
          writeAnnotationAttributes(np, writer);
          writeDocumentation(np, writer);
          writeAnnotationElements(np, writer);
          writer.endElement("NavigationProperty");

        }

        writeAnnotationElements(eet, writer);
        writer.endElement("EntityType");

      }

      // Association
      for (EdmAssociation assoc : schema.associations) {
        writer.startElement(new QName2("Association"));

        writer.writeAttribute("Name", assoc.name);
        writeAnnotationAttributes(assoc, writer);
        writeDocumentation(assoc, writer);

        writer.startElement(new QName2("End"));
        writer.writeAttribute("Role", assoc.end1.role);
        writer.writeAttribute("Type", assoc.end1.type.getFullyQualifiedTypeName());
        writer.writeAttribute("Multiplicity", assoc.end1.multiplicity.getSymbolString());
        writer.endElement("End");

        writer.startElement(new QName2("End"));
        writer.writeAttribute("Role", assoc.end2.role);
        writer.writeAttribute("Type", assoc.end2.type.getFullyQualifiedTypeName());
        writer.writeAttribute("Multiplicity", assoc.end2.multiplicity.getSymbolString());
        writer.endElement("End");

        writeAnnotationElements(assoc, writer);
        writer.endElement("Association");
      }

      // EntityContainer
      for (EdmEntityContainer container : schema.entityContainers) {
        writer.startElement(new QName2("EntityContainer"));

        writer.writeAttribute("Name", container.name);
        writer.writeAttribute(new QName2(m, "IsDefaultEntityContainer", "m"), Boolean.toString(container.isDefault));
        writeAnnotationAttributes(container, writer);
        writeDocumentation(container, writer);

        for (EdmEntitySet ees : container.entitySets) {
          writer.startElement(new QName2("EntitySet"));
          writer.writeAttribute("Name", ees.name);
          writer.writeAttribute("EntityType", ees.type.getFullyQualifiedTypeName());
          writeAnnotationAttributes(ees, writer);
          writeDocumentation(ees, writer);
          writeAnnotationElements(ees, writer);
          writer.endElement("EntitySet");
        }

        for (EdmFunctionImport fi : container.functionImports) {
          writer.startElement(new QName2("FunctionImport"));
          writer.writeAttribute("Name", fi.name);
          if (null != fi.entitySet) {
            writer.writeAttribute("EntitySet", fi.entitySet.name);
          }
          // TODO: how to differentiate inline ReturnType vs embedded ReturnType?
          writer.writeAttribute("ReturnType", fi.returnType.getFullyQualifiedTypeName());
          writer.writeAttribute(new QName2(m, "HttpMethod", "m"), fi.httpMethod);
          writeAnnotationAttributes(fi, writer);
          writeDocumentation(fi, writer);

          for (EdmFunctionParameter param : fi.parameters) {
              writer.startElement(new QName2("Parameter"));
              writer.writeAttribute("Name", param.name);
              writer.writeAttribute("Type", param.type.getFullyQualifiedTypeName());
              if (param.mode != null)
                writer.writeAttribute("Mode", param.mode.toString());
              writeAnnotationAttributes(param, writer);
              writeDocumentation(param, writer);
              writeAnnotationElements(param, writer);
              writer.endElement("Parameter");
          }
          writeAnnotationElements(fi, writer);
          writer.endElement("FunctionImport");
        }

        for (EdmAssociationSet eas : container.associationSets) {
          writer.startElement(new QName2("AssociationSet"));
          writer.writeAttribute("Name", eas.name);
          writer.writeAttribute("Association", eas.association.getFQNamespaceName());
          writeAnnotationAttributes(eas, writer);
          writeDocumentation(eas, writer);

          writer.startElement(new QName2("End"));
          writer.writeAttribute("Role", eas.end1.role.role);
          writer.writeAttribute("EntitySet", eas.end1.entitySet.name);
          writer.endElement("End");

          writer.startElement(new QName2("End"));
          writer.writeAttribute("Role", eas.end2.role.role);
          writer.writeAttribute("EntitySet", eas.end2.entitySet.name);
          writer.endElement("End");

          writeAnnotationElements(eas, writer);
          writer.endElement("AssociationSet");
        }

        writeAnnotationElements(container, writer);
        writer.endElement("EntityContainer");
      }

      writeAnnotationElements(schema, writer);
      writer.endElement("Schema");

    }

    writer.endDocument();
  }

  /**
   * extensions to CSDL like Annotations appear in an application specific set
   * of namespaces.
   * @param services
   * @param writer
   */
  private static void writeExtensionNamespaces(EdmDataServices services, XMLWriter2 writer) {
    if (null != services.getNamespaces()) {
      for (Namespace ns : services.getNamespaces()) {
        writer.writeNamespace(ns.getPrefix(), ns.getUri());
      }
    }
  }

  private static void write(Iterable<EdmProperty> properties, XMLWriter2 writer) {
    for (EdmProperty prop : properties) {
      writer.startElement(new QName2("Property"));

      writer.writeAttribute("Name", prop.name);
      writer.writeAttribute("Type", prop.type.getFullyQualifiedTypeName());
      writer.writeAttribute("Nullable", Boolean.toString(prop.nullable));
      if (prop.maxLength != null) {
        writer.writeAttribute("MaxLength", Integer.toString(prop.maxLength));
      }
      if (!prop.collectionKind.equals(CollectionKind.None)) {
        writer.writeAttribute("CollectionKind", prop.collectionKind.toString());
      }
      if (prop.defaultValue != null) {
        writer.writeAttribute("DefaultValue", prop.defaultValue);
      }
      if (prop.precision != null) {
        writer.writeAttribute("Precision", Integer.toString(prop.precision));
      }
      if (prop.scale != null) {
        writer.writeAttribute("Scale", Integer.toString(prop.precision));
      }
      writeAnnotationAttributes(prop, writer);
      writeAnnotationElements(prop, writer);
      writer.endElement("Property");
    }
  }

  private static void writeAnnotationAttributes(EdmItem item, XMLWriter2 writer) {
    if (null != item.getAnnotations()) {
      for (Annotation<?> a : item.getAnnotations()) {
        if (a instanceof EdmAnnotationAttribute) {
          writer.writeAttribute(
                  new QName2(a.getNamespaceUri(), a.getLocalName(), a.getNamespacePrefix()),
                  null == a.getValue() ? "" : a.getValue().toString());
        }
      }
    }
  }

  private static void writeAnnotationElements(EdmItem item, XMLWriter2 writer) {
    if (null != item.getAnnotations()) {
      for (Annotation<?> a : item.getAnnotations()) {
        if (a instanceof EdmAnnotationElement) {
          // TODO
          throw new UnsupportedOperationException("Implement element annotations");
        }
      }
    }
  }

  private static void writeDocumentation(EdmItem item, XMLWriter2 writer) {
    EdmDocumentation doc = item.getDocumentation();
    if (null != doc && (null != doc.getSummary() || null != doc.getLongDescription())) {
      QName2 d = new QName2(edm, "Documentation");
      writer.startElement(d);
      {
        if (null != doc.getSummary()) {
          QName2 s = new QName2(edm, "Summary");
          writer.startElement(s);
          writer.writeText(doc.getSummary());
          writer.endElement(s.getLocalPart());
        }
        if (null != doc.getLongDescription()) {
          QName2 s = new QName2(edm, "LongDescription");
          writer.startElement(s);
          writer.writeText(doc.getLongDescription());
          writer.endElement(s.getLocalPart());
        }
      }
      writer.endElement(d.getLocalPart());
    }
  }


}
