package org.odata4j.xml;

import java.io.Writer;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmSchema;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;

import org.core4j.Enumerable;
import org.core4j.Funcs;

public class ServiceDocumentWriter extends BaseWriter {

    public static void write(String base, EdmDataServices services, Writer w) {
        XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
        writer.startDocument();

        String xmlns = app;

        writer.startElement(new QName2("service"), xmlns);
        writer.writeAttribute(new QName2("xml:base"), base);
        writer.writeNamespace("atom", atom);
        writer.writeNamespace("app", app);

        writer.startElement(new QName2("workspace"));
        writeAtomTitle(writer, atom, "Default");

        for(EdmSchema es : services.schemas) {
            for(EdmEntityContainer eec : es.entityContainers) {
                for(EdmEntitySet ees : Enumerable.create(eec.entitySets).orderBy(Funcs.field(EdmEntitySet.class, String.class, "name"))) {
                    writer.startElement("collection");
                    writer.writeAttribute("href", ees.name);
                    writeAtomTitle(writer, atom, ees.name);

                    writer.endElement("collection");
                }
            }
        }

        writer.endElement("workspace");
        writer.endDocument();
    }

    private static void writeAtomTitle(XMLWriter2 writer, String atom, String title) {
        writer.startElement(new QName2(atom, "title", "atom"));
        writer.writeText(title);
        writer.endElement("title");
    }
}
