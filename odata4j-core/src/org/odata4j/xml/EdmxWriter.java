package org.odata4j.xml;

import java.io.Writer;

import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLWriter2;

public class EdmxWriter extends BaseWriter {

    public static void write(EdmDataServices services, Writer w) {

        XMLWriter2 writer = XMLFactoryProvider2.getInstance().newXMLWriterFactory2().createXMLWriter(w);
        writer.startDocument();

        writer.startElement(new QName2(edmx, "Edmx", "edmx"));
        writer.writeAttribute("Version", "1.0");
        writer.writeNamespace("edmx", edmx);

        writer.startElement(new QName2(edmx, "DataServices", "edmx"));

        // Schema
        for(EdmSchema schema : services.schemas) {

            writer.startElement(new QName2("Schema"), edm);
            writer.writeAttribute("Namespace", schema.namespace);
            writer.writeNamespace("d", d);
            writer.writeNamespace("m", m);

            // EntityType
            for(EdmEntityType eet : schema.entityTypes) {
                writer.startElement(new QName2("EntityType"));

                writer.writeAttribute("Name", eet.name);

                writer.startElement(new QName2("Key"));
                for(String key : eet.keys){
                    writer.startElement(new QName2("PropertyRef"));
                    writer.writeAttribute("Name", key);
                    writer.endElement("PropertyRef");
                }

                writer.endElement("Key");

                for(EdmProperty prop : eet.properties) {

                    writer.startElement(new QName2("Property"));

                    writer.writeAttribute("Name", prop.name);
                    writer.writeAttribute("Type", prop.type.toTypeString());
                    writer.writeAttribute("Nullable", Boolean.toString(prop.nullable));
                    if (prop.maxLength != null)
                        writer.writeAttribute("MaxLength", Integer.toString(prop.maxLength));
                    writer.endElement("Property");
                }

                for(EdmNavigationProperty np : eet.navigationProperties) {

                    writer.startElement(new QName2("NavigationProperty"));
                    writer.writeAttribute("Name", np.name);
                    writer.writeAttribute("Relationship", np.relationship.getFQName());
                    writer.writeAttribute("FromRole", np.fromRole.role);
                    writer.writeAttribute("ToRole", np.toRole.role);

                    writer.endElement("NavigationProperty");

                }

                writer.endElement("EntityType");

            }

            // Associaion
            for(EdmAssociation assoc : schema.associations) {
                writer.startElement(new QName2("Association"));

                writer.writeAttribute("Name", assoc.name);

                writer.startElement(new QName2("End"));
                writer.writeAttribute("Role", assoc.end1.role);
                writer.writeAttribute("Type", assoc.end1.type.getFQName());
                writer.writeAttribute("Multiplicity", assoc.end1.multiplicity.getSymbolString());
                writer.endElement("End");

                writer.startElement(new QName2("End"));
                writer.writeAttribute("Role", assoc.end2.role);
                writer.writeAttribute("Type", assoc.end2.type.getFQName());
                writer.writeAttribute("Multiplicity", assoc.end2.multiplicity.getSymbolString());
                writer.endElement("End");

                writer.endElement("Association");
            }

            // EntityContainer
            for(EdmEntityContainer container : schema.entityContainers) {
                writer.startElement(new QName2("EntityContainer"));

                writer.writeAttribute("Name", container.name);
                writer.writeAttribute(new QName2(m, "IsDefaultEntityContainer", "m"), Boolean.toString(container.isDefault));

                for(EdmEntitySet ees : container.entitySets) {
                    writer.startElement(new QName2("EntitySet"));
                    writer.writeAttribute("Name", ees.name);
                    writer.writeAttribute("EntityType", ees.type.getFQName());
                    writer.endElement("EntitySet");
                }
                for(EdmAssociationSet eas : container.associationSets) {
                    writer.startElement(new QName2("AssociationSet"));
                    writer.writeAttribute("Name", eas.name);
                    writer.writeAttribute("Association", eas.association.getFQName());

                    writer.startElement(new QName2("End"));
                    writer.writeAttribute("Role", eas.end1.role.role);
                    writer.writeAttribute("EntitySet", eas.end1.entitySet.name);
                    writer.endElement("End");

                    writer.startElement(new QName2("End"));
                    writer.writeAttribute("Role", eas.end2.role.role);
                    writer.writeAttribute("EntitySet", eas.end2.entitySet.name);
                    writer.endElement("End");

                    writer.endElement("AssociationSet");
                }

                writer.endElement("EntityContainer");
            }

            writer.endElement("Schema");

        }

        writer.endDocument();
    }
}
