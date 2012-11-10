package org.odata4j.test.unit.issues;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.Assert;

import org.core4j.Enumerable;
import org.junit.Test;
import org.odata4j.core.OPredicates;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmType;
import org.odata4j.format.Entry;
import org.odata4j.format.xml.AtomFeedFormatParser;

// http://code.google.com/p/odata4j/issues/detail?id=143
public class Issue143Test {

  @Test
  public void issue143() {
    InputStream xml = getClass().getResourceAsStream("/META-INF/sap_no_property_type.xml");
    EdmDataServices metadata = getMetadata();
    AtomFeedFormatParser.AtomFeed feed = new AtomFeedFormatParser(metadata, "FlightCollection", null, null).parse(new InputStreamReader(xml));
    Assert.assertNotNull(feed);
    Entry entry = feed.getEntries().iterator().next();
    OProperty<?> complexTypeProp = entry.getEntity().getProperty("flightDetails");
    Assert.assertEquals("RMTSAMPLEFLIGHT.flightDetails", complexTypeProp.getType().getFullyQualifiedTypeName());
    Assert.assertEquals("Edm.String", entry.getEntity().getProperty("carrid").getType().getFullyQualifiedTypeName());
    List<OProperty<?>> props = (List<OProperty<?>>) complexTypeProp.getValue();
    Assert.assertTrue(Enumerable.create(props).any(OPredicates.propertyNameEquals("distance")));
  }

  private static EdmDataServices getMetadata() {
    EdmDataServices.Builder metadata = new EdmDataServices.Builder();
    EdmSchema.Builder schema = new EdmSchema.Builder();
    EdmEntityContainer.Builder container = new EdmEntityContainer.Builder();

    EdmComplexType.Builder complexType = EdmComplexType.newBuilder()
        .setName("flightDetails")
        .setNamespace("RMTSAMPLEFLIGHT")
        .addProperties(EdmProperty.newBuilder("countryFrom").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("countryTo").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("cityFrom").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("cityTo").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("airportFrom").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("airportTo").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("flightTime").setType(EdmType.getSimple("Edm.Int32")),
            EdmProperty.newBuilder("departureTime").setType(EdmType.getSimple("Edm.Time")),
            EdmProperty.newBuilder("arrivalTime").setType(EdmType.getSimple("Edm.Time")),
            EdmProperty.newBuilder("distance").setType(EdmType.getSimple("Edm.Decimal")));

    EdmEntityType.Builder entityType = new EdmEntityType.Builder()
        .addKeys("Key1")
        .setNamespace("RMTSAMPLEFLIGHT")
        .setName("Flight")
        .addProperties(EdmProperty.newBuilder("carrid").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("connid").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("fldate").setType(EdmType.getSimple("Edm.DateTime")),
            EdmProperty.newBuilder("PLANETYPE").setType(EdmType.getSimple("Edm.String")),
            EdmProperty.newBuilder("flightDetails").setType(complexType));

    EdmEntitySet.Builder entitySet = new EdmEntitySet.Builder().setName("FlightCollection").setEntityType(entityType);
    container.addEntitySets(entitySet);
    schema.addEntityContainers(container);
    schema.addEntityTypes(entityType);
    schema.addComplexTypes(complexType);
    metadata.addSchemas(schema);
    return metadata.build();
  }
}
