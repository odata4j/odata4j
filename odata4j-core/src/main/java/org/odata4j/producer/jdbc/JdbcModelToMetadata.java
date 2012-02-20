package org.odata4j.producer.jdbc;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.core4j.Func1;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.jdbc.JdbcModel.JdbcColumn;
import org.odata4j.producer.jdbc.JdbcModel.JdbcPrimaryKey;
import org.odata4j.producer.jdbc.JdbcModel.JdbcSchema;
import org.odata4j.producer.jdbc.JdbcModel.JdbcTable;

public class JdbcModelToMetadata implements Func1<JdbcModel, JdbcMetadataMapping> {

  @Override
  public JdbcMetadataMapping apply(JdbcModel jdbcModel) {
    String modelNamespace = "JdbcModel";

    List<EdmEntityType.Builder> entityTypes = new ArrayList<EdmEntityType.Builder>();
    List<EdmEntityContainer.Builder> entityContainers = new ArrayList<EdmEntityContainer.Builder>();
    List<EdmEntitySet.Builder> entitySets = new ArrayList<EdmEntitySet.Builder>();

    Map<EdmEntitySet.Builder, JdbcTable> entitySetMapping = new HashMap<EdmEntitySet.Builder, JdbcTable>();
    Map<EdmProperty.Builder, JdbcColumn> propertyMapping = new HashMap<EdmProperty.Builder, JdbcColumn>();

    for (JdbcSchema jdbcSchema : jdbcModel.schemas) {
      for (JdbcTable jdbcTable : jdbcSchema.tables) {
        if (jdbcTable.primaryKeys.isEmpty()) {
          System.err.println("Skipping JdbcTable " + jdbcTable.tableName + ", no keys");
          continue;
        }

        EdmEntityType.Builder entityType = EdmEntityType.newBuilder()
            .setName(jdbcTable.tableName)
            .setNamespace(modelNamespace);
        entityTypes.add(entityType);

        for (JdbcPrimaryKey primaryKey : jdbcTable.primaryKeys) {
          entityType.addKeys(primaryKey.columnName);
        }

        for (JdbcColumn jdbcColumns : jdbcTable.columns) {
          EdmProperty.Builder property = EdmProperty.newBuilder(jdbcColumns.columnName)
              .setType(getEdmType(jdbcColumns.columnType))
              .setNullable(jdbcColumns.isNullable);
          entityType.addProperties(property);
          propertyMapping.put(property, jdbcColumns);
        }

        EdmEntitySet.Builder entitySet = EdmEntitySet.newBuilder()
            .setName(jdbcTable.tableName)
            .setEntityType(entityType);
        entitySets.add(entitySet);
        entitySetMapping.put(entitySet, jdbcTable);
      }

      EdmEntityContainer.Builder entityContainer = EdmEntityContainer.newBuilder()
          .setName(jdbcSchema.schemaName)
          .setIsDefault(jdbcSchema.isDefault)
          .addEntitySets(entitySets);
      entityContainers.add(entityContainer);
    }

    List<EdmSchema.Builder> edmSchemas = new ArrayList<EdmSchema.Builder>();
    EdmSchema.Builder modelSchema = EdmSchema.newBuilder()
        .setNamespace(modelNamespace)
        .addEntityTypes(entityTypes);
    edmSchemas.add(modelSchema);
    for (EdmEntityContainer.Builder entityContainer : entityContainers) {
      EdmSchema.Builder containerSchema = EdmSchema.newBuilder()
          .setNamespace("JdbcEntities." + entityContainer.getName())
          .addEntityContainers(entityContainer);
      edmSchemas.add(containerSchema);
    }
    EdmDataServices metadata = EdmDataServices.newBuilder()
        .addSchemas(edmSchemas)
        .build();

    Map<EdmEntitySet, JdbcTable> finalEntitySetMapping = new HashMap<EdmEntitySet, JdbcTable>();
    for (Map.Entry<EdmEntitySet.Builder, JdbcTable> entry : entitySetMapping.entrySet()) {
      finalEntitySetMapping.put(entry.getKey().build(), entry.getValue());
    }
    Map<EdmProperty, JdbcColumn> finalPropertyMapping = new HashMap<EdmProperty, JdbcColumn>();
    for (Map.Entry<EdmProperty.Builder, JdbcColumn> entry : propertyMapping.entrySet()) {
      finalPropertyMapping.put(entry.getKey().build(), entry.getValue());
    }
    return new JdbcMetadataMapping(metadata, jdbcModel, finalEntitySetMapping, finalPropertyMapping);
  }
  
  private static EdmType getEdmType(int jdbcType) {
    Map<Integer, EdmType> map = new HashMap<Integer, EdmType>();
    map.put(Types.INTEGER, EdmSimpleType.INT32);
    map.put(Types.VARCHAR, EdmSimpleType.STRING);
    map.put(Types.BOOLEAN, EdmSimpleType.BOOLEAN);
    if (!map.containsKey(jdbcType))
      throw new UnsupportedOperationException("TODO implement edmtype conversion for jdbc type: " + jdbcType);
    return map.get(jdbcType);
  }

}
