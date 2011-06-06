package org.odata4j.format.xml;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;
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
  public static final String related = "http://schemas.microsoft.com/ado/2007/08/dataservices/related/";
  public static final String atom_feed_content_type = "application/atom+xml;type=feed";
  public static final String atom_entry_content_type = "application/atom+xml;type=entry";

  protected void writeProperties(XMLWriter2 writer, List<OProperty<?>> properties) {
    for (OProperty<?> prop : properties) {
      writeProperty(writer, prop, false);
    }
  }

  @SuppressWarnings("unchecked")
  protected void writeProperty(XMLWriter2 writer, OProperty<?> prop, boolean isDocumentElement) {

    String name = prop.getName();
    EdmType type = prop.getType();
    Object value = prop.getValue();

    if (isDocumentElement)
      writer.startElement(new QName2(name), d);
    else
      writer.startElement(new QName2(d, name, "d"));

    String sValue = null;

    if (!type.isSimple()) {
      writer.writeAttribute(
          new QName2(m, "type", "m"),
          type.toTypeString());
      // complex
      List<OProperty<?>> complexProperties = (List<OProperty<?>>) value;
      if (complexProperties != null) {
        writeProperties(writer, complexProperties);
      }
    } else {
      // simple
      if (type == EdmType.INT32) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.INT16) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.INT64) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.BOOLEAN) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.BYTE) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = Hex.encodeHexString(
              new byte[] { (Byte) value });
        }
      } else if (type == EdmType.DECIMAL) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.SINGLE) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.DOUBLE) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.STRING) {
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.DATETIME) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null)
          sValue = InternalUtil.formatDateTime(
                  (LocalDateTime) value);
      } else if (type == EdmType.BINARY) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        byte[] bValue = (byte[]) value;
        if (value != null) {
          sValue = Base64.encodeBase64String(bValue);
        }
      } else if (type == EdmType.GUID) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = value.toString();
        }
      } else if (type == EdmType.TIME) {
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = InternalUtil.toString((LocalTime) value);
        }
      } else if (type == EdmType.DATETIMEOFFSET) {
        // Edm.DateTimeOffset '-'? yyyy '-' mm '-' dd 'T' hh ':' mm
        // ':' ss ('.' s+)? (zzzzzz)?
        writer.writeAttribute(
            new QName2(m, "type", "m"),
            type.toTypeString());
        if (value != null) {
          sValue = InternalUtil.toString((DateTime) value);
        }
      } else {
        throw new UnsupportedOperationException("Implement " + type);
      }
    }

    if (value == null) {
      writer.writeAttribute(new QName2(m, "null", "m"), "true");
    } else {
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

    if (isResponse) {
      // for responses we need to include all links whether inlined or not
      for (EdmNavigationProperty np : ees.type.getAllNavigationProperties()) {
        if (!np.selected) {
          continue;
        }

        String otherEntity = np.name;
        String rel = related + otherEntity;
        String type = atom_feed_content_type;
        if (np.toRole.multiplicity != EdmMultiplicity.MANY) {
          type = atom_entry_content_type;
        }
        final String title = otherEntity;
        String href = relid + "/" + otherEntity;

        // check whether we have to write inlined entities
        OLink linkToInline = entityLinks != null
            ? Enumerable.create(entityLinks).firstOrNull(
                new Predicate1<OLink>() {
                  @Override
                  public boolean apply(OLink input) {
                    return title.equals(input.getTitle());
                  }
                })
            : null;

        if (linkToInline == null) {
          writeElement(writer, "link", null,
              "rel", rel,
              "type", type,
              "title", title,
              "href", href);
        } else {
          writer.startElement("link");
          writer.writeAttribute("rel", rel);
          writer.writeAttribute("type", type);
          writer.writeAttribute("title", title);
          writer.writeAttribute("href", href);
          // write the inlined entities inside the link element
          writeLinkInline(writer, linkToInline,
              href, baseUri, updated, isResponse);
          writer.endElement("link");
        }
      }

      writeElement(writer, "category", null,
          "term", ees.type.getFQNamespaceName(),
          "scheme", scheme);
    } else {
      // for requests we include only the provided links
      if (entityLinks != null) {
        for (OLink olink : entityLinks) {
          String type = olink instanceof ORelatedEntitiesLink
              ? atom_feed_content_type
              : atom_entry_content_type;

          writer.startElement("link");
          writer.writeAttribute("rel", olink.getRelation());
          writer.writeAttribute("type", type);
          writer.writeAttribute("title", olink.getTitle());
          writer.writeAttribute("href", olink.getHref());
          if (olink instanceof ORelatedEntitiesLinkInline
              || olink instanceof ORelatedEntityLinkInline) {
            // write the inlined entities inside the link element
            writeLinkInline(writer, olink, olink.getHref(),
                baseUri, updated, isResponse);
          }
          writer.endElement("link");
        }
      }
    }

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
              entity.getEntitySet().name,
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
              entity.getEntitySet().name,
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
}
