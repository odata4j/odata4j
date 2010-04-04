package org.odata4j.xml;

import java.util.ArrayList;
import java.util.List;

import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.xml.AtomFeedParser.CollectionInfo;

public class ServiceDocumentParser extends BaseParser {

    public static Iterable<CollectionInfo> parseCollections(XMLEventReader2 reader) {

        String baseUrl = null;
        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();
            if (isStartElement(event, APP_SERVICE)) {
                baseUrl = event.asStartElement().getAttributeByName(XML_BASE).getValue();
            }
            if (isStartElement(event, APP_WORKSPACE)) {
                return parseWorkspace(baseUrl, reader, event.asStartElement());
            }
            if (event.isStartElement()) {
                // log(event.toString());
            }

        }
        throw new RuntimeException();
    }
    
    private static Iterable<CollectionInfo> parseWorkspace(String baseUrl, XMLEventReader2 reader, StartElement2 startElement) {
        List<CollectionInfo> rt = new ArrayList<CollectionInfo>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                return rt;
            }

            if (isStartElement(event, APP_COLLECTION)) {
                rt.add(parseCollection(baseUrl, reader, event.asStartElement()));
            }

        }
        return rt;
    }
    
    private static CollectionInfo parseCollection(String baseUrl, XMLEventReader2 reader, StartElement2 startElement) {
        CollectionInfo rt = new CollectionInfo();

        String href = getAttributeValueIfExists(startElement, "href");
        rt.url = urlCombine(baseUrl, href);

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (event.isEndElement() && event.asEndElement().getName().equals(startElement.getName())) {
                return rt;
            }

            if (isStartElement(event, ATOM_TITLE)) {

                rt.title = reader.getElementText();
            }

            if (isStartElement(event, APP_ACCEPT)) {

                rt.accept = reader.getElementText();
            }

        }

        return rt;
    }

}
