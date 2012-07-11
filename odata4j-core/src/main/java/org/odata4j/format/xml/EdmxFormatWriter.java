package org.odata4j.format.xml;

import static org.odata4j.format.xml.XmlFormatParser.NS_EDM2008_9;

import java.io.Writer;

import org.odata4j.core.NamespacedAnnotation;
import org.odata4j.core.PrefixedNamespace;
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
    //writer.writeNamespace("d", d);
    
    writeExtensionNamespaces(services, writer);

    writer.startElement(new QName2(edmx, "DataServices", "edmx"));
    writer.writeAttribute(new QName2(m, "DataServiceVersion", "m"), "2.0");
    writer.writeNamespace("m", m);


    // Schema
    for (EdmSchema schema : services.getSchemas()) {

      writer.startElement(new QName2("Schema"));
      writer.writeAttribute("Namespace", schema.getNamespace());
      writer.writeNamespace("xmlns", NS_EDM2008_9);
      writeAnnotationAttributes(schema, writer);
      writeDocumentation(schema, writer);

      // EntityType
      for (EdmEntityType eet : schema.getEntityTypes()) {
        writer.startElement(new QName2("EntityType"));

        writer.writeAttribute("Name", eet.getName());
        if (eet.getIsAbstract() != null) {
          writer.writeAttribute("Abstract", eet.getIsAbstract().toString());
        }

        if (Boolean.TRUE.equals(eet.getHasStream())) {
          writer.writeAttribute(new QName2(m, "HasStream", "m"), "true");
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

        writeProperties(eet.getDeclaredProperties(), writer);

        for (EdmNavigationProperty np : eet.getDeclaredNavigationProperties()) {

          writer.startElement(new QName2("NavigationProperty"));
          writer.writeAttribute("Name", np.getName());
          writer.writeAttribute("Relationship", np.getRelationship().getFQNamespaceName());
          writer.writeAttribute("FromRole", np.getFromRole().getRole());
          writer.writeAttribute("ToRole", np.getToRole().getRole());
          writeAnnotationAttributes(np, writer);
          writeDocumentation(np, writer);
          writeAnnotationElements(np, writer);
          writer.endElement("NavigationProperty");

        }

        writeAnnotationElements(eet, writer);
        writer.endElement("EntityType");

      }
      // ComplexType
      for (EdmComplexType ect : schema.getComplexTypes()) {
        writer.startElement(new QName2("ComplexType"));

        writer.writeAttribute("Name", ect.getName());
        if (ect.getIsAbstract() != null) {
          writer.writeAttribute("Abstract", ect.getIsAbstract().toString());
        }
        writeAnnotationAttributes(ect, writer);
        writeDocumentation(ect, writer);

        writeProperties(ect.getProperties(), writer);
        writeAnnotationElements(ect, writer);
        writer.endElement("ComplexType");
      }

      // Association
      for (EdmAssociation assoc : schema.getAssociations()) {
        writer.startElement(new QName2("Association"));

        writer.writeAttribute("Name", assoc.getName());
        writeAnnotationAttributes(assoc, writer);
        writeDocumentation(assoc, writer);
        
        writer.startElement(new QName2("End"));
        writer.writeAttribute("Type", assoc.getEnd1().getType().getFullyQualifiedTypeName());
        writer.writeAttribute("Multiplicity", assoc.getEnd1().getMultiplicity().getSymbolString());
        writer.writeAttribute("Role", assoc.getEnd1().getRole());
        writer.endElement("End");

        writer.startElement(new QName2("End"));
        writer.writeAttribute("Type", assoc.getEnd2().getType().getFullyQualifiedTypeName());
        writer.writeAttribute("Multiplicity", assoc.getEnd2().getMultiplicity().getSymbolString());
        writer.writeAttribute("Role", assoc.getEnd2().getRole());
        writer.endElement("End");

        writeAnnotationElements(assoc, writer);
        writer.endElement("Association");
        
      }

      // EntityContainer
      for (EdmEntityContainer container : schema.getEntityContainers()) {
        writer.startElement(new QName2("EntityContainer"));

        writer.writeAttribute("Name", container.getName());
        writer.writeAttribute(new QName2(m, "IsDefaultEntityContainer", "m"), Boolean.toString(container.isDefault()));
        writeAnnotationAttributes(container, writer);
        writeDocumentation(container, writer);

        for (EdmEntitySet ees : container.getEntitySets()) {
          writer.startElement(new QName2("EntitySet"));
          writer.writeAttribute("Name", ees.getName());
          writer.writeAttribute("EntityType", ees.getType().getFullyQualifiedTypeName());
          writeAnnotationAttributes(ees, writer);
          writeDocumentation(ees, writer);
          writeAnnotationElements(ees, writer);
          writer.endElement("EntitySet");
        }
        
        for (EdmAssociationSet eas : container.getAssociationSets()) {
          writer.startElement(new QName2("AssociationSet"));
          writer.writeAttribute("Name", eas.getName());
          writer.writeAttribute("Association", eas.getAssociation().getFQNamespaceName());
          writeAnnotationAttributes(eas, writer);
          writeDocumentation(eas, writer);

          writer.startElement(new QName2("End"));
          writer.writeAttribute("EntitySet", eas.getEnd1().getEntitySet().getName());
          writer.writeAttribute("Role", eas.getEnd1().getRole().getRole());
          writer.endElement("End");

          writer.startElement(new QName2("End"));
          writer.writeAttribute("EntitySet", eas.getEnd2().getEntitySet().getName());
          writer.writeAttribute("Role", eas.getEnd2().getRole().getRole());
          writer.endElement("End");

          writeAnnotationElements(eas, writer);
          writer.endElement("AssociationSet");
        }

        for (EdmFunctionImport fi : container.getFunctionImports()) {
          writer.startElement(new QName2("FunctionImport"));
          writer.writeAttribute("Name", fi.getName());
          if (fi.getReturnType() != null) {
            // TODO: how to differentiate inline ReturnType vs embedded ReturnType?
            writer.writeAttribute("ReturnType", fi.getReturnType().getFullyQualifiedTypeName());
          }
          if (fi.getEntitySet() != null) {
            writer.writeAttribute("EntitySet", fi.getEntitySet().getName());
          }
          writer.writeAttribute(new QName2(m, "HttpMethod", "m"), fi.getHttpMethod());
          writeAnnotationAttributes(fi, writer);
          writeDocumentation(fi, writer);

          for (EdmFunctionParameter param : fi.getParameters()) {
            writer.startElement(new QName2("Parameter"));
            writer.writeAttribute("Name", param.getName());
            writer.writeAttribute("Type", param.getType().getFullyQualifiedTypeName());
            if (param.getMode() != null)
              writer.writeAttribute("Mode", param.getMode().toString());
            if (param.isNullable()!=null)
              writer.writeAttribute("Nullable", param.isNullable().toString());
            writeAnnotationAttributes(param, writer);
            writeDocumentation(param, writer);
            writeAnnotationElements(param, writer);
            writer.endElement("Parameter");
          }
          writeAnnotationElements(fi, writer);
          writer.endElement("FunctionImport");
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
   * Extensions to CSDL like Annotations appear in an application specific set
   * of namespaces.
   */
  private static void writeExtensionNamespaces(EdmDataServices services, XMLWriter2 writer) {
    if (services.getNamespaces() != null) {
      for (PrefixedNamespace ns : services.getNamespaces()) {
        writer.writeNamespace(ns.getPrefix(), ns.getUri());
      }
    }
  }

  private static void writeProperties(Iterable<EdmProperty> properties, XMLWriter2 writer) {
    for (EdmProperty prop : properties) {
      writer.startElement(new QName2("Property"));

      writer.writeAttribute("Name", prop.getName());
      writer.writeAttribute("Type", prop.getType().getFullyQualifiedTypeName());
      writer.writeAttribute("Nullable", Boolean.toString(prop.isNullable()));
      if (prop.getMaxLength() != null) {
        writer.writeAttribute("MaxLength", Integer.toString(prop.getMaxLength()));
      }
      if (!prop.getCollectionKind().equals(CollectionKind.NONE)) {
        writer.writeAttribute("CollectionKind", prop.getCollectionKind().toString());
      }
      if (prop.getDefaultValue() != null) {
        writer.writeAttribute("DefaultValue", prop.getDefaultValue());
      }
      if (prop.getPrecision() != null) {
        writer.writeAttribute("Precision", Integer.toString(prop.getPrecision()));
      }
      if (prop.getScale() != null) {
        writer.writeAttribute("Scale", Integer.toString(prop.getScale()));
      }
      if (prop.getConcurrencyMode()!=null){
        writer.writeAttribute("ConcurrencyMode", prop.getConcurrencyMode());
      }
      if (prop.getMimeType()!=null){
        writer.writeAttribute(new QName2(m, "MimeType", "m"), prop.getMimeType());
      }
      if (prop.getFcTargetPath() != null) {
        writer.writeAttribute(new QName2(m, "FC_TargetPath", "m"), prop.getFcTargetPath());
      }
      if (prop.getFcKeepInContent() != null) {
        writer.writeAttribute(new QName2(m, "FC_KeepInContent", "m"), prop.getFcKeepInContent());
      }
      if(prop.getFcNsPrefix()!=null){
        writer.writeAttribute(new QName2(m, "FC_NsPrefix", "m"), prop.getFcNsPrefix());
      }
      if(prop.getFcNsUri()!=null){
        writer.writeAttribute(new QName2(m, "FC_NsUri", "m"), prop.getFcNsUri());
      }
      writeAnnotationAttributes(prop, writer);
      writeAnnotationElements(prop, writer);
      writer.endElement("Property");
    }
  }

  private static void writeAnnotationAttributes(EdmItem item, XMLWriter2 writer) {
    if (item.getAnnotations() != null) {
      for (NamespacedAnnotation<?> a : item.getAnnotations()) {
        if (a instanceof EdmAnnotationAttribute) {
          writer.writeAttribute(
              new QName2(a.getNamespace().getUri(), a.getName(), a.getNamespace().getPrefix()),
              a.getValue() == null ? "" : a.getValue().toString());
        }
      }
    }
  }

  private static void writeAnnotationElements(EdmItem item, XMLWriter2 writer) {
    if (item.getAnnotations() != null) {
      for (NamespacedAnnotation<?> a : item.getAnnotations()) {
        if (a instanceof EdmAnnotationElement) {
          // TODO: please don't throw an exception here.
          // this totally breaks ODataConsumer even thought it doesn't rely
          // on annotations.  A no-op is a interim approach that allows work
          // to proceed by those using queryable metadata to access annotations.
          // throw new UnsupportedOperationException("Implement element annotations");
        }
      }
    }
  }

  private static void writeDocumentation(EdmItem item, XMLWriter2 writer) {
    EdmDocumentation doc = item.getDocumentation();
    if (doc != null && (doc.getSummary() != null || doc.getLongDescription() != null)) {
      QName2 d = new QName2(edm, "Documentation");
      writer.startElement(d);
      {
        if (doc.getSummary() != null) {
          QName2 s = new QName2(edm, "Summary");
          writer.startElement(s);
          writer.writeText(doc.getSummary());
          writer.endElement(s.getLocalPart());
        }
        if (doc.getLongDescription() != null) {
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
