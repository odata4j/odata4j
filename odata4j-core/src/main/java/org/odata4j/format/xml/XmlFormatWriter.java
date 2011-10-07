package org.odata4j.format.xml;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.core.OSimpleObject;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLWriter2;

public class XmlFormatWriter {

  protected static final String edmx = "http://schemas.microsoft.com/ado/2007/06/edmx";
  protected static final String d = "http://schemas.microsoft.com/ado/2007/08/dataservices";
  protected static final String m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
  protected static final String edm = "http://schemas.microsoft.com/ado/2006/04/edm";
  protected static final String atom = "http://www.w3.org/2005/Atom";
  protected static final String app = "http://www.w3.org/2007/app";
  protected static final String scheme = "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme";
  public static final String related = "http://schemas.microsoft.com/ado/2007/08/dataservices/related/";
  public static final String atom_feed_content_type = "application/atom+xml;type=feed";
  public static final String atom_entry_content_type = "application/atom+xml;type=entry";

  protected void writeProperties(XMLWriter2 writer, List<OProperty<?>> properties) {
    for (OProperty<?> prop : properties) {
      writeProperty(writer, prop, false);
    }
  }

  protected void writeProperty(XMLWriter2 writer, OProperty<?> prop, boolean isDocumentElement) {
    writeProperty(writer, prop.getName(), prop.getType(), prop.getValue(), isDocumentElement, true);
  }

  @SuppressWarnings("unchecked")
  protected void writeProperty(XMLWriter2 writer, String name, EdmType type, Object value, boolean isDocumentElement, boolean writeType) {
    if (isDocumentElement)
      writer.startElement(new QName2(name), d);
    else
      writer.startElement(new QName2(d, name, "d"));

    String sValue = null;

    if (!type.isSimple()) {
      if (writeType) {
        String typename = type.getFullyQualifiedTypeName();
        if (value instanceof OCollection) {
          EdmCollectionType collectionType = (EdmCollectionType) type;
          typename = "Bag(" + collectionType.getCollectionType().getFullyQualifiedTypeName() + ")";
        }
        writer.writeAttribute(new QName2(m, "type", "m"), typename);
      }
      // complex or collection
      if (value instanceof OCollection) {
        writeCollection(writer, name, (OCollection<? extends OObject>)value);
      } else if (value instanceof OComplexObject) {
        writeProperties(writer, ((OComplexObject)value).getProperties());
      } else {
        // deprecated form of a complex object.
        List<OProperty<?>> complexProperties = (List<OProperty<?>>) value;
        if (complexProperties != null) {
          writeProperties(writer, complexProperties);
        }
      }
    } else {
      // simple
      // write the type attribute if requested and not a string
      if (writeType && type != EdmSimpleType.STRING) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.getFullyQualifiedTypeName());
      }
      // now write the value
      if (type == EdmSimpleType.INT32) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.INT16) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.INT64) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.BOOLEAN) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.BYTE) {
        if (value != null) {
          sValue = Hex.encodeHexString(
              new byte[] { (Byte) value });
        }
      } else if (type == EdmSimpleType.DECIMAL) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.SINGLE) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.DOUBLE) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.STRING) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.DATETIME) {
        if (value != null)
          sValue = InternalUtil.formatDateTime(
                  (LocalDateTime) value);
      } else if (type == EdmSimpleType.BINARY) {
        byte[] bValue = (byte[]) value;
        if (value != null) {
          sValue = Base64.encodeBase64String(bValue);
        }
      } else if (type == EdmSimpleType.GUID) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmSimpleType.TIME) {
        if (value != null) {
          sValue = InternalUtil.toString((LocalTime) value);
        }
      } else if (type == EdmSimpleType.DATETIMEOFFSET) {
        // Edm.DateTimeOffset '-'? yyyy '-' mm '-' dd 'T' hh ':' mm
        // ':' ss ('.' s+)? (zzzzzz)?
        if (value != null) {
          sValue = InternalUtil.toString((DateTime) value);
        }
      } else {
        throw new UnsupportedOperationException("Implement " + type);
      }
    }

    if (value == null) {
      writer.writeAttribute(new QName2(m, "null", "m"), "true");
    } else if (sValue != null) {
      writer.writeText(sValue);
    }
    writer.endElement(name);

  }

  protected String writeEntry(XMLWriter2 writer, OEntity oe,
      List<OProperty<?>> entityProperties, List<OLink> entityLinks,
      String entitySetName, String baseUri, String updated,
      EdmEntitySet ees, boolean isResponse) {

    String relid = null;
    String absid = null;
    if (isResponse) {
      relid = InternalUtil.getEntityRelId(oe);
      absid = baseUri + relid;
      writeElement(writer, "id", absid);
    }

    writeElement(writer, "title", null, "type", "text");
    writeElement(writer, "updated", updated);

    writer.startElement("author");
    writeElement(writer, "name", null);
    writer.endElement("author");

    if (isResponse) {
      writeElement(writer, "link", null, "rel", "edit", "title",
          entitySetName, "href", relid);
    }

    if (entityLinks != null) {
    if (isResponse) {
        // the producer has populated the link collection, we just what he gave us.
        for (OLink link : entityLinks) {
          String rel = related + link.getTitle();
          String type = (link.isCollection())
                  ? atom_feed_content_type
                  : atom_entry_content_type;
          String href = relid + "/" + link.getTitle();
          if (link.isInline()) {
          writer.startElement("link");
          writer.writeAttribute("rel", rel);
          writer.writeAttribute("type", type);
            writer.writeAttribute("title", link.getTitle());
          writer.writeAttribute("href", href);
          // write the inlined entities inside the link element
            writeLinkInline(writer, link,
              href, baseUri, updated, isResponse);
          writer.endElement("link");
          } else {
            // deferred link.
            writeElement(writer, "link", null,
                    "rel", rel,
                    "type", type,
                    "title", link.getTitle(),
                    "href", href);
        }
      }
    } else {
      // for requests we include only the provided links
        // Note: It seems that OLinks for responses are only built using the
        // title and OLinks for requests have the additional info in them
        // alread.  I'm leaving that inconsistency in place for now but this
        // else and its preceding if could probably be unified.
        for (OLink olink : entityLinks) {
          String type = olink.isCollection()
              ? atom_feed_content_type
              : atom_entry_content_type;

          writer.startElement("link");
          writer.writeAttribute("rel", olink.getRelation());
          writer.writeAttribute("type", type);
          writer.writeAttribute("title", olink.getTitle());
          writer.writeAttribute("href", olink.getHref());
          if (olink.isInline()) {
            // write the inlined entities inside the link element
            writeLinkInline(writer, olink, olink.getHref(),
                baseUri, updated, isResponse);
          }
          writer.endElement("link");
        }
      }
    } // else entityLinks null

    writeElement(writer, "category", null,
        "term", ees.getType().getFullyQualifiedTypeName(),
        "scheme", scheme);

    writer.startElement("content");
    writer.writeAttribute("type", MediaType.APPLICATION_XML);

    writer.startElement(new QName2(m, "properties", "m"));

    writeProperties(writer, entityProperties);

    writer.endElement("properties");
    writer.endElement("content");
    return absid;

  }

  protected void writeLinkInline(XMLWriter2 writer, OLink linkToInline,
      String href, String baseUri, String updated, boolean isResponse) {

    writer.startElement(new QName2(m, "inline", "m"));
    if (linkToInline instanceof ORelatedEntitiesLinkInline) {
      ORelatedEntitiesLinkInline relLink = ((ORelatedEntitiesLinkInline) linkToInline);
      List<OEntity> entities = relLink.getRelatedEntities();

      if (entities != null && !entities.isEmpty()) {
        writer.startElement(new QName2("feed"));
        writeElement(
            writer,
            "title",
            linkToInline.getTitle(),
            "type",
            "text");

        writeElement(writer, "id", baseUri + href);
        writeElement(writer, "updated", updated);
        writeElement(
            writer,
            "link",
            null,
            "rel",
            "self",
            "title",
            linkToInline.getTitle(),
            "href",
            href);

        for (OEntity entity : ((ORelatedEntitiesLinkInline) linkToInline).getRelatedEntities()) {
          writer.startElement("entry");
          writeEntry(writer, entity,
              entity.getProperties(), entity.getLinks(),
              entity.getEntitySet().getName(),
              baseUri, updated,
              entity.getEntitySet(), isResponse);

          writer.endElement("entry");
        }
        writer.endElement("feed");
      }
    } else if (linkToInline instanceof ORelatedEntityLinkInline) {
      OEntity entity = ((ORelatedEntityLinkInline) linkToInline).getRelatedEntity();
      if (entity != null) {
        writer.startElement("entry");
        writeEntry(writer, entity,
              entity.getProperties(), entity.getLinks(),
              entity.getEntitySet().getName(),
              baseUri, updated,
              entity.getEntitySet(), isResponse);

        writer.endElement("entry");
      }
    } else
      throw new RuntimeException(
          "Unknown OLink type " + linkToInline.getClass());
    writer.endElement("inline");
  }

  protected void writeElement(
      XMLWriter2 writer,
      String elementName,
      String elementText,
      String... attributes) {
    writer.startElement(elementName);
    for (int i = 0; i < attributes.length; i += 2) {
      writer.writeAttribute(attributes[i], attributes[i + 1]);
    }
    if (elementText != null) {
      writer.writeText(elementText);
    }
    writer.endElement(elementName);
  }

  @SuppressWarnings("rawtypes")
  private void writeCollection(XMLWriter2 writer, String name, OCollection<? extends OObject> c) {
    Iterator<? extends OObject> iter = c.iterator();
    while (iter.hasNext()) {
      OObject o = iter.next();
      if (o instanceof OComplexObject) {
        writeProperty(writer, "element", o.getType(), o, false, false);
      } else if (o instanceof OSimpleObject) {
        writeProperty(writer, "element", o.getType(), ((OSimpleObject)o).getValue(), false, false); // not a doc element and don't write the typename
      } else {
        // TODO...
      }
    }
  }
}
