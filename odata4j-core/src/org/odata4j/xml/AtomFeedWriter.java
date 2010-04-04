package org.odata4j.xml;

import java.io.Writer;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;
import org.odata4j.xml.AtomFeedParser.DataServicesAtomEntry;

import core4j.Enumerable;
import core4j.Func1;

public class AtomFeedWriter extends BaseWriter {

    public static String generateResponseEntry(String baseUri, EntityResponse response, Writer w) {

        EdmEntitySet ees = response.getEntitySet();
        String entitySetName = ees.name;
        DateTime utc = new DateTime().withZone(DateTimeZone.UTC);
        String updated = toString(utc);

        XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
        writer.startDocument();

        writer.startElement(new QName2("entry"), atom);
        writer.writeNamespace("m", m);
        writer.writeNamespace("d", d);
        writer.writeAttribute("xml:base", baseUri);

        String absId = writeEntry(writer, getPropertyNames(response.getEntitySet()), response.getEntity().getProperties(), entitySetName, baseUri, updated, ees);
        writer.endDocument();
        return absId;
    }

    public static void generateRequestEntry(DataServicesAtomEntry request, Writer w) {

        DateTime utc = new DateTime().withZone(DateTimeZone.UTC);
        String updated = toString(utc);

        XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
        writer.startDocument();

        writer.startElement(new QName2("entry"), atom);
        writer.writeNamespace("d", d);
        writer.writeNamespace("m", m);

        writeEntry(writer, null, request.properties, null, null, updated, null);
        writer.endDocument();

    }

    public static void generateFeed(String baseUri, EntitiesResponse response, Writer w) {

        EdmEntitySet ees = response.getEntitySet();
        String entitySetName = ees.name;
        DateTime utc = new DateTime().withZone(DateTimeZone.UTC);
        String updated = toString(utc);

        XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
        writer.startDocument();

        writer.startElement(new QName2("feed"), atom);
        writer.writeNamespace("m", m);
        writer.writeNamespace("d", d);
        writer.writeAttribute("xml:base", baseUri);

        writeElement(writer, "title", entitySetName, "type", "text");
        writeElement(writer, "id", baseUri + entitySetName);

        writeElement(writer, "updated", updated);

        writeElement(writer, "link", null, "rel", "self", "title", entitySetName, "href", entitySetName);

        Integer inlineCount = response.getInlineCount();
        if (inlineCount != null) {
            writeElement(writer, "m:count", inlineCount.toString());
        }

        for(OEntity entity : response.getEntities()) {
            writer.startElement("entry");
            writeEntry(writer, getPropertyNames(response.getEntitySet()), entity.getProperties(), entitySetName, baseUri, updated, ees);
            writer.endElement("entry");
        }
        writer.endDocument();

    }

    
    private static List<String> getPropertyNames(EdmEntitySet ees){
        return Enumerable.create(ees.type.key).toList();    // TODO multiple key names?
    }
    
    private static String writeEntry(XMLWriter2 writer, List<String> keyPropertyNames, final List<OProperty<?>> entityProperties, String entitySetName, String baseUri, String updated, EdmEntitySet ees) {

        String key = null;
        if (keyPropertyNames != null) {
            Object[] keyProperties = Enumerable.create(keyPropertyNames).select(new Func1<String,OProperty<?>>(){
                public OProperty<?> apply(String input) {
                    for(OProperty<?> entityProperty : entityProperties)
                        if(entityProperty.getName().equals(input))
                            return entityProperty;
                        throw new IllegalArgumentException("Key property '" + input + "' is invalid");
                }}).cast(Object.class).toArray(Object.class);
            key = InternalUtil.keyString( keyProperties);
        }

        String relid = null;
        String absid = null;
        if (entitySetName != null) {
            relid = entitySetName + key;
            absid = baseUri + relid;
            writeElement(writer, "id", absid);
        }

        writeElement(writer, "title", null, "type", "text");
        writeElement(writer, "updated", updated);

        writer.startElement("author");
        writeElement(writer, "name", null);
        writer.endElement("author");

        if (entitySetName != null)
            writeElement(writer, "link", null, "rel", "edit", "title", entitySetName, "href", relid);

        if (ees != null) {
            for(EdmNavigationProperty np : ees.type.navigationProperties) {

                // <link rel="http://schemas.microsoft.com/ado/2007/08/dataservices/related/Products" type="application/atom+xml;type=feed" title="Products"
                // href="Suppliers(1)/Products" />

                String otherEntity = np.toRole.type.name;
                String rel = related + otherEntity;
                String type = "application/atom+xml;type=feed";
                String title = otherEntity;
                String href = relid + "/" + otherEntity;

                writeElement(writer, "link", null, "rel", rel, "type", type, "title", title, "href", href);

            }

            writeElement(writer, "category", null, "term", ees.type.getFQName(), "scheme", scheme);
        }

        writer.startElement("content");
        writer.writeAttribute("type", MediaType.APPLICATION_XML);

        writer.startElement(new QName2(m, "properties", "m"));

        for(OProperty<?> prop : entityProperties) {
            String name = prop.getName();
            EdmType type = prop.getType();
            Object value = prop.getValue();

            writer.startElement(new QName2(d, name, "d"));

            String sValue = null;

            if (type == EdmType.INT32) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.INT16) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.INT64) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.BOOLEAN) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.BYTE) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.DECIMAL) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.SINGLE) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.DOUBLE) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.STRING) {
                if (value != null)
                    sValue = value.toString();
            } else if (type == EdmType.DATETIME) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                LocalDateTime ldt = (LocalDateTime) value;
                DateTime dt = ldt.toDateTime(DateTimeZone.UTC);
                if (value != null)
                    sValue = toString(dt);
            } else if (type == EdmType.BINARY) {
                writer.writeAttribute(new QName2(m, "type", "m"), type.toTypeString());
                byte[] bValue = (byte[]) value;
                if (value != null)
                    sValue = Base64.encodeBase64String(bValue);
            } else {
                throw new UnsupportedOperationException("Implement " + type);
            }

            if (value == null) {
                writer.writeAttribute(new QName2(m, "null", "m"), "true");
            } else {
                writer.writeText(sValue);
            }
            writer.endElement(name);

        }

        writer.endElement("properties");
        writer.endElement("content");
        return absid;

    }

    private static void writeElement(XMLWriter2 writer, String elementName, String elementText, String... attributes) {
        writer.startElement(elementName);
        for(int i = 0; i < attributes.length; i += 2) {
            writer.writeAttribute(attributes[i], attributes[i + 1]);
        }
        if (elementText != null)
            writer.writeText(elementText);
        writer.endElement(elementName);
    }
}
