package org.odata4j.format.xml;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLWriter2;

public class XmlFormatWriter {

    protected static String edmx = "http://schemas.microsoft.com/ado/2007/06/edmx";
    protected static String d = "http://schemas.microsoft.com/ado/2007/08/dataservices";
    protected static String m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
    protected static String edm = "http://schemas.microsoft.com/ado/2006/04/edm";

    protected static String atom = "http://www.w3.org/2005/Atom";
    protected static String app = "http://www.w3.org/2007/app";

    protected static final String scheme = "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme";
    protected static final String related = "http://schemas.microsoft.com/ado/2007/08/dataservices/related/";

    protected static String toString(DateTime utc) {
        return utc.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");
    }
    
    
    
    protected String writeEntry(XMLWriter2 writer, List<String> keyPropertyNames, List<OProperty<?>> entityProperties, String entitySetName, String baseUri, String updated, EdmEntitySet ees) {

        String relid = null;
        String absid = null;
        if (entitySetName != null) {
            relid = InternalUtil.getEntityRelId(keyPropertyNames,entityProperties,entitySetName);
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
                if (value != null) {
                    LocalDateTime ldt = (LocalDateTime) value;
                    DateTime dt = ldt.toDateTime(DateTimeZone.UTC);
                    sValue = toString(dt);
                }
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

    protected void writeElement(XMLWriter2 writer, String elementName, String elementText, String... attributes) {
        writer.startElement(elementName);
        for(int i = 0; i < attributes.length; i += 2) {
            writer.writeAttribute(attributes[i], attributes[i + 1]);
        }
        if (elementText != null)
            writer.writeText(elementText);
        writer.endElement(elementName);
    }
}
