package org.odata4j.producer.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.core4j.ThrowingFunc1;
import org.odata4j.command.Command;
import org.odata4j.command.CommandResult;
import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmProperty;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.EqExpression;
import org.odata4j.expression.IntegralLiteral;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.expression.StringLiteral;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.Responses;
import org.odata4j.producer.command.GetEntitiesCommandContext;
import org.odata4j.producer.exceptions.NotFoundException;
import org.odata4j.producer.jdbc.JdbcModel.JdbcColumn;
import org.odata4j.producer.jdbc.JdbcModel.JdbcTable;

public class JdbcGetEntitiesCommand implements Command<GetEntitiesCommandContext> {

  @Override
  public CommandResult execute(GetEntitiesCommandContext context) throws Exception {
    JdbcProducerCommandContext jdbcContext = (JdbcProducerCommandContext) context;

    String entitySetName = context.getEntitySetName();

    final JdbcMetadataMapping mapping = jdbcContext.getBackend().getMetadataMapping();
    final EdmEntitySet entitySet = mapping.getMetadata().findEdmEntitySet(entitySetName);
    if (entitySet == null)
      throw new NotFoundException();

    final SqlModel sql = getSql(mapping, entitySet, context.getQueryInfo() == null ? null : context.getQueryInfo().filter);
    final List<OEntity> entities = new ArrayList<OEntity>();

    jdbcContext.getJdbc().execute(new ThrowingFunc1<Connection, Void>(){
      @Override
      public Void apply(Connection conn) throws Exception {
        PreparedStatement stmt = conn.prepareStatement(sql.sql);
        for (int i = 0; i < sql.params.size(); i++)
          stmt.setObject(i + 1, sql.params.get(i));
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
          OEntity entity = JdbcGetEntityCommand.toOEntity(mapping, entitySet, results);
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

  private static class SqlModel {
    final String sql;
    final List<Object> params;
    public SqlModel(String sql, List<Object> params) {
     this.sql = sql;
     this.params = params;
    }
  }

  private static SqlModel getSql(JdbcMetadataMapping mapping, EdmEntitySet entitySet, BoolCommonExpression filter) {
    JdbcTable table = mapping.getMappedTable(entitySet);
    StringBuilder sb = new StringBuilder("SELECT * FROM " + table.tableName);
    List<Object> params = new ArrayList<Object>();
    if (filter != null) {
      sb.append(" WHERE");
      foo(mapping, entitySet, filter, sb, params);
    }
    return new SqlModel(sb.toString(), params);

  }

  private static void foo(JdbcMetadataMapping mapping, EdmEntitySet entitySet, BoolCommonExpression filter, StringBuilder sb, List<Object> params) {
    if (filter instanceof EqExpression) {
      EqExpression eq = (EqExpression) filter;
      if (eq.getLHS() instanceof EntitySimpleProperty && eq.getRHS() instanceof LiteralExpression) {
        EntitySimpleProperty prop = (EntitySimpleProperty) eq.getLHS();
        EdmProperty edmProp = entitySet.getType().findProperty(prop.getPropertyName());
        JdbcColumn column = mapping.getMappedColumn(edmProp);
        LiteralExpression literal = (LiteralExpression) eq.getRHS();
        sb.append(" ");
        sb.append(column.columnName);
        sb.append(" = ?");
        params.add(getParam(literal));
        return;
      }
    }
    throw new UnsupportedOperationException("Filter not supported yet: " + filter);
  }

  private static Object getParam(LiteralExpression literal) {
    if (literal instanceof StringLiteral) {
      return ((StringLiteral) literal).getValue();
    }
    if (literal instanceof IntegralLiteral) {
      return ((IntegralLiteral) literal).getValue();
    }
    throw new UnsupportedOperationException("Filter literal not supported yet: " + literal);
  }

}