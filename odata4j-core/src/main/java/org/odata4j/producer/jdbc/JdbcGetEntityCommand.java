package org.odata4j.producer.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.Expression;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.Responses;
import org.odata4j.producer.command.GetEntityCommandContext;
import org.odata4j.producer.exceptions.NotFoundException;
import org.odata4j.producer.jdbc.JdbcModel.JdbcColumn;

public class JdbcGetEntityCommand implements Command<GetEntityCommandContext> {

  public static OEntity toOEntity(JdbcMetadataMapping mapping, EdmEntitySet entitySet, ResultSet results) throws SQLException {
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

  private static BoolCommonExpression prependPrimaryKeyFilter(JdbcMetadataMapping mapping, EdmEntityType entityType,
      OEntityKey entityKey, BoolCommonExpression filter) {
    BoolCommonExpression keyFilter = null;
    if (entityType.getKeys().size() == 1) {
      String key = entityType.getKeys().iterator().next();
      keyFilter = Expression.eq(Expression.simpleProperty(key), Expression.literal(entityKey.asSingleValue()));
    } else {
      throw new UnsupportedOperationException("TODO Implement complex keys");
    }
    return filter == null ? keyFilter : Expression.and(keyFilter, filter);
  }

  @Override
  public CommandResult execute(final GetEntityCommandContext context) throws Exception {
    JdbcProducerCommandContext jdbcContext = (JdbcProducerCommandContext) context;

    String entitySetName = context.getEntitySetName();

    final JdbcMetadataMapping mapping = jdbcContext.getBackend().getMetadataMapping();
    final EdmEntitySet entitySet = mapping.getMetadata().findEdmEntitySet(entitySetName);
    if (entitySet == null)
      throw new NotFoundException();

    GenerateSqlQuery queryGen = jdbcContext.get(GenerateSqlQuery.class);
    BoolCommonExpression filter = context.getQueryInfo() == null ? null : context.getQueryInfo().filter;
    filter = prependPrimaryKeyFilter(mapping, entitySet.getType(), context.getEntityKey(), filter);
    final SqlStatement sqlStatement = queryGen.generate(mapping, entitySet, filter);
    OEntity entity = jdbcContext.getJdbc().execute(new ThrowingFunc1<Connection, OEntity>() {
      @Override
      public OEntity apply(Connection conn) throws Exception {
        PreparedStatement stmt = sqlStatement.asPreparedStatement(conn);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
          return toOEntity(mapping, entitySet, results);
        }
        return null;
      }});

    if (entity == null)
      throw new NotFoundException();

    EntityResponse response = Responses.entity(entity);
    context.setResult(response);
    return CommandResult.CONTINUE;
  }

}