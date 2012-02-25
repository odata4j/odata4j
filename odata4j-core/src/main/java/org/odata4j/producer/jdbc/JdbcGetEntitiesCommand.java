package org.odata4j.producer.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.core4j.ThrowingFunc1;
import org.odata4j.command.Command;
import org.odata4j.command.CommandResult;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmProperty;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.Responses;
import org.odata4j.producer.command.GetEntitiesCommandContext;
import org.odata4j.producer.jdbc.JdbcModel.JdbcColumn;
import org.odata4j.producer.jdbc.JdbcModel.JdbcTable;

public class JdbcGetEntitiesCommand implements Command<GetEntitiesCommandContext> {

  private static OEntity toOEntity(JdbcMetadataMapping mapping, EdmEntitySet entitySet, JdbcTable table, ResultSet results) throws SQLException {
    List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
    for (EdmProperty edmProperty : entitySet.getType().getProperties()) {
      JdbcColumn column = mapping.getMappedColumn(edmProperty);
      Object value = results.getObject(column.columnName);
      OProperty<?> property = OProperties.simple(edmProperty.getName(), value);
      properties.add(property);
    }

    OEntityKey entityKey = OEntityKey.infer(entitySet, properties);
    return OEntities.create(entitySet, entityKey, properties, Collections.<OLink>emptyList());
  }

  @Override
  public CommandResult execute(GetEntitiesCommandContext context) throws Exception {
    JdbcProducerCommandContext jdbcContext = (JdbcProducerCommandContext) context;

    String entitySetName = context.getEntitySetName();

    final JdbcMetadataMapping mapping = jdbcContext.getBackend().getMetadataMapping();
    final EdmEntitySet entitySet = mapping.getMetadata().findEdmEntitySet(entitySetName);
    final JdbcTable table = mapping.getMappedTable(entitySet);
    final List<OEntity> entities = new ArrayList<OEntity>();

    jdbcContext.getJdbc().execute(new ThrowingFunc1<Connection, Void>(){
      @Override
      public Void apply(Connection conn) throws Exception {
        ResultSet results = conn.createStatement().executeQuery("SELECT * FROM " + table.tableName);
        while (results.next()) {
          OEntity entity = toOEntity(mapping, entitySet, table, results);
          entities.add(entity);
        }
        return null;
      }});

    Integer inlineCount = null;
    String skipToken = null;

    EntitiesResponse response = Responses.entities(entities, entitySet, inlineCount, skipToken);
    context.setResult(response);
    return CommandResult.CONTINUE;
  }

}