package org.odata4j.xml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.XMLEventWriter2;
import org.odata4j.stax2.XMLFactoryProvider2;

import core4j.Enumerable;

public class AtomFeedParser extends BaseParser {

    public static class CollectionInfo {
        public String url;
        public String title;
        public String accept;

        @Override
        public String toString() {
            return InternalUtil.reflectionToString(this);
        }
    }

    public static class AtomFeed {
        public String next;
        public Iterable<AtomEntry> entries;
    }

    public abstract static class AtomEntry {
        public String id;
        public String title;
        public String summary;
        public String updated;
        public String categoryTerm;
        public String categoryScheme;
        public String contentType;

    }

    public static class BasicAtomEntry extends AtomEntry {
        public String content;

        @Override
        public String toString() {
            return InternalUtil.reflectionToString(this);
        }
    }

    public static class DataServicesAtomEntry extends AtomEntry {
        public String etag;
        public List<OProperty<?>> properties;

        @Override
        public String toString() {
            return InternalUtil.reflectionToString(this);
        }
    }

    

    public static AtomFeed parseFeed(XMLEventReader2 reader) {

        AtomFeed feed = new AtomFeed();
        List<AtomEntry> rt = new ArrayList<AtomEntry>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (isStartElement(event, ATOM_ENTRY)) {

                rt.add(parseEntry(reader, event.asStartElement()));
            } else if (isStartElement(event, ATOM_LINK)) {
                if ("next".equals(event.asStartElement().getAttributeByName(new QName2("rel")).getValue())) {
                    feed.next = event.asStartElement().getAttributeByName(new QName2("href")).getValue();
                }
            }

        }
        feed.entries = rt;

        return feed;

    }

   

    private static Iterable<OProperty<?>> parseProperties(XMLEventReader2 reader, StartElement2 propertiesElement) {
        List<OProperty<?>> rt = new ArrayList<OProperty<?>>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(propertiesElement.getName())) {
                return rt;
            }

            if (event.isStartElement() && event.asStartElement().getName().getNamespaceURI().equals(NS_DATASERVICES)) {

                String name = event.asStartElement().getName().getLocalPart();
                Attribute2 typeAttribute = event.asStartElement().getAttributeByName(M_TYPE);
                Attribute2 nullAttribute = event.asStartElement().getAttributeByName(M_NULL);
                boolean isNull = nullAttribute != null && nullAttribute.getValue().equals("true");

                OProperty<?> op = null;

                String type = null;
                boolean isComplexType = false;
                if (typeAttribute != null) {
                    type = typeAttribute.getValue();
                    EdmType et = EdmType.get(type);
                    isComplexType = !et.isPrimitive();
                }

                if (isComplexType) {
                    op = OProperties.complex(name, type, isNull ? null : Enumerable.create(parseProperties(reader, event.asStartElement())).toList());
                } else {
                    op = OProperties.parse(name, type, isNull ? null : reader.getElementText());
                }
                rt.add(op);

            }

        }

        throw new RuntimeException();
    }

   

    private static DataServicesAtomEntry parseDSAtomEntry(String etag, XMLEventReader2 reader, XMLEvent2 event) {
        DataServicesAtomEntry dsae = new DataServicesAtomEntry();
        dsae.etag = etag;
        dsae.properties = Enumerable.create(parseProperties(reader, event.asStartElement())).toList();
        return dsae;
    }

    private static String innerText(XMLEventReader2 reader, StartElement2 element) {
        StringWriter sw = new StringWriter();
        XMLEventWriter2 writer = XMLFactoryProvider2.getInstance().newXMLOutputFactory2().createXMLEventWriter(sw);
        while (reader.hasNext()) {

            XMLEvent2 event = reader.nextEvent();
            if (event.isEndElement() && event.asEndElement().getName().equals(element.getName())) {

                return sw.toString();
            } else {
                writer.add(event);
            }

        }
        throw new RuntimeException();
    }

    private static AtomEntry parseEntry(XMLEventReader2 reader, StartElement2 entryElement) {

        String id = null;
        String categoryTerm = null;
        String categoryScheme = null;
        String title = null;
        String summary = null;
        String updated = null;
        String contentType = null;

        String etag = getAttributeValueIfExists(entryElement, M_ETAG);

        AtomEntry rt = null;

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(entryElement.getName())) {
                rt.id = id;
                rt.title = title;
                rt.summary = summary;
                rt.updated = updated;
                rt.categoryScheme = categoryScheme;
                rt.categoryTerm = categoryTerm;
                rt.contentType = contentType;
                return rt;
            }

            if (isStartElement(event, ATOM_ID)) {
                id = reader.getElementText();
            } else if (isStartElement(event, ATOM_TITLE)) {
                title = reader.getElementText();
            } else if (isStartElement(event, ATOM_SUMMARY)) {
                summary = reader.getElementText();
            } else if (isStartElement(event, ATOM_UPDATED)) {
                updated = reader.getElementText();
            } else if (isStartElement(event, ATOM_CATEGORY)) {
                categoryTerm = getAttributeValueIfExists(event.asStartElement(), "term");
                categoryScheme = getAttributeValueIfExists(event.asStartElement(), "scheme");

            } else if (isStartElement(event, M_PROPERTIES)) {
                rt = parseDSAtomEntry(etag, reader, event);
            } else if (isStartElement(event, ATOM_CONTENT)) {
                contentType = getAttributeValueIfExists(event.asStartElement(), "type");

                if (contentType.equals(MediaType.APPLICATION_XML)) {

                    StartElement2 contentElement = event.asStartElement();
                    StartElement2 valueElement = null;
                    while (reader.hasNext()) {

                        XMLEvent2 event2 = reader.nextEvent();

                        if (valueElement == null && event2.isStartElement()) {
                            valueElement = event2.asStartElement();

                            if (isStartElement(event2, M_PROPERTIES)) {
                                rt = parseDSAtomEntry(etag, reader, event2);
                            } else {
                                BasicAtomEntry bae = new BasicAtomEntry();
                                bae.content = innerText(reader, event2.asStartElement());
                                rt = bae;
                            }

                        }
                        if (event2.isEndElement() && event2.asEndElement().getName().equals(contentElement.getName())) {

                            break;
                        }

                    }

                } else {
                    BasicAtomEntry bae = new BasicAtomEntry();
                    bae.content = innerText(reader, event.asStartElement());
                    rt = bae;
                }

            }

        }

        throw new RuntimeException();
    }

   

  
   
}
