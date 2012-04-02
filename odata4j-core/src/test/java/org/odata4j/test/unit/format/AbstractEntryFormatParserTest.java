package org.odata4j.test.unit.format;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.joda.time.LocalDateTime;
import org.odata4j.core.ODataVersion;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;

public abstract class AbstractEntryFormatParserTest {

  protected static final String DATE_TIME = "DateTime";
  protected static final String ENTITY_SET_NAME = "EntitySetName";

  protected static FormatParser<Entry> formatParser;

  protected static void createFormatParser(FormatType format) {
    formatParser = FormatParserFactory.getParser(Entry.class, format, getSettings());
  }

  protected void verifyDateTimePropertyValue(Entry entry) {
    assertThat((LocalDateTime) entry.getEntity().getProperty(DATE_TIME).getValue(), is(new LocalDateTime(2003, 7, 1, 0, 0)));
  }

  private static Settings getSettings() {
    return new Settings(ODataVersion.V1, getMetadata(), ENTITY_SET_NAME, null, null);
  }

  private static EdmDataServices getMetadata() {
    EdmProperty.Builder property = EdmProperty.newBuilder(DATE_TIME).setType(EdmSimpleType.DATETIME);
    EdmEntityType.Builder entityType = new EdmEntityType.Builder().setName("EntityType").addKeys("EntityKey").addProperties(property);
    EdmEntitySet.Builder entitySet = new EdmEntitySet.Builder().setName(ENTITY_SET_NAME).setEntityType(entityType);
    EdmEntityContainer.Builder container = new EdmEntityContainer.Builder().addEntitySets(entitySet);
    EdmSchema.Builder schema = new EdmSchema.Builder().addEntityContainers(container).addEntityTypes(entityType);
    return new EdmDataServices.Builder().addSchemas(schema).build();
  }
}
