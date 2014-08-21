package org.odata4j.producer.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.core4j.Enumerable;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.expression.OrderByExpression.Direction;

public class GenerateJPQLCommand implements Command {

  private boolean isCount;

  public GenerateJPQLCommand() {
    this(false);
  }

  public GenerateJPQLCommand(boolean isCount) {
    this.isCount = isCount;
  }

  @Override
  public boolean execute(JPAContext context) {
    context.setJPQLQuery(generateJPQL(context));

    return false;
  }

  private String generateJPQL(JPAContext context) {
    String alias = "t0";
    String from = context.getEntity().getJPAEntityType().getName()
        + " " + alias;
    String where = null;
    
    if (context.getEntity().getTypeSafeEntityKey() != null) {
    	
      where = whereKeyEquals(context.getEntity().getJPAEntityType(),
          context.getEntity().getKeyAttributeName(),
          context.getEntity().getTypeSafeEntityKey(), alias);
    	
    }
    
    if (context.getNavProperty() != null) {

      String prop = null;
      int propCount = 0;

      for (String pn : context.getNavProperty().split("/")) {
      	
        String[] propSplit = pn.split("\\(");
        prop = propSplit[0];
        propCount++;

        if (context.getEdmPropertyBase() instanceof EdmProperty) {
          throw new UnsupportedOperationException(
              String.format(
                  "The request URI is not valid. Since the segment '%s' "
                      + "refers to a collection, this must be the last segment "
                      + "in the request URI. All intermediate segments must refer "
                      + "to a single resource.",
                  alias));
        }
        
        /* This is a murderer! It picks any property in 
         * the universe having the given name!
         */
//      context.setEdmPropertyBase(context.getMetadata()
//      .findEdmProperty(prop));
        
        /* Replace this with the following. */
        
        EdmEntitySet edmEntitySet = context.getEntity().getEdmEntitySet();
        
        // Reset the search for property.
        context.setEdmPropertyBase(null);
        
        for (EdmNavigationProperty property : edmEntitySet.getType().getNavigationProperties())
        {
        	if (property.getName().equals(prop))
        	{
        		context.setEdmPropertyBase(property);
        		break;
        	}
        }
        
        if (context.getEdmPropertyBase() == null)
        {
	        // If no navigation property was found having the given name, 
	        // search for primitive properties.
	        for (EdmProperty property : edmEntitySet.getType().getProperties())
	        {
	        	if (property.getName().equals(prop))
	        	{
	        		context.setEdmPropertyBase(property);
	        		break;
	        	}
	        }
        }
        
        if (context.getEdmPropertyBase() instanceof EdmNavigationProperty) {
          EdmNavigationProperty propInfo = (EdmNavigationProperty) context
              .getEdmPropertyBase();

          context.getEntity().setEntitySetName(
              propInfo.getToRole().getType().getName());

          prop = alias + "." + prop;
          alias = "t" + Integer.toString(propCount);
          from = String
              .format("%s JOIN %s %s", from, prop, alias);

          if (propSplit.length > 1) {
            OEntityKey entityKey = OEntityKey.parse("("
                + propSplit[1]);
            context.getEntity().setOEntityKey(entityKey);

            where = whereKeyEquals(context.getEntity()
                .getJPAEntityType(),
                context.getEntity().getKeyAttributeName(),
                context.getEntity().getTypeSafeEntityKey(),
                alias);
          }
        } else if (context.getEdmPropertyBase() instanceof EdmProperty) {
          EdmProperty propInfo = (EdmProperty) context
              .getEdmPropertyBase();

          alias = alias + "." + propInfo.getName();
          // TODO?
        }

        if (context.getEdmPropertyBase() == null) {
          throw new EntityNotFoundException(
              String.format(
                  "Resource not found for the segment '%s'.",
                  pn));
        }
      }
    }

    /* Start of 'expand' support, with added eager loading of expanded relationships. */
    
    String select = isCount ? "COUNT(" + alias + ")" : "DISTINCT(" + alias + ")";
    
    String jpql = String.format("SELECT %s FROM %s", select, from);

    if (!isCount && context.getQueryInfo().expand != null)
    {
    	int fetchAliasNumber = 0;
    	
    	for (EntitySimpleProperty expandProperty : context.getQueryInfo().expand)
    	{
    		String previousAlias = alias;
    		
    		String nextAlias = String.format("_f%d", fetchAliasNumber++);
    		
    		for (String pathElement : expandProperty.getPropertyName().split("/"))
    		{
    			jpql += String.format(" LEFT JOIN FETCH %s.%s %s", previousAlias, pathElement, nextAlias);
    			
    			previousAlias = nextAlias;
    			
    			nextAlias = String.format("_f%d", fetchAliasNumber++);
    		}
    	}
    }

    /* End of 'expand' support, with added eager loading of expanded relationships. */

    JPQLGenerator jpqlGen = new JPQLGenerator(context.getEntity()
        .getKeyAttributeName(), alias);

    if (context.getQueryInfo() != null
        && context.getQueryInfo().filter != null) {
      String filterPredicate = jpqlGen
          .toJpql(context.getQueryInfo().filter);
      where = addWhereExpression(where, filterPredicate, "AND");
    }

    if (context.getQueryInfo() != null
        && context.getQueryInfo().skipToken != null) {
      BoolCommonExpression skipTokenPredicateExpr = JPASkipToken
          .parse(jpqlGen.getPrimaryKeyName(),
              context.getQueryInfo().orderBy,
              context.getQueryInfo().skipToken);
      String skipTokenPredicate = jpqlGen
          .toJpql(skipTokenPredicateExpr);
      where = addWhereExpression(where, skipTokenPredicate, "AND");
    }

    if (where != null)
      jpql = String.format("%s WHERE %s", jpql, where);

    if (!isCount && context.getQueryInfo() != null
        && context.getQueryInfo().orderBy != null
        && !context.getQueryInfo().orderBy.isEmpty()) {
      List<String> orderBys = new ArrayList<String>();
      for (OrderByExpression orderBy : context.getQueryInfo().orderBy) {
        String field = jpqlGen.toJpql(orderBy.getExpression());
        orderBys.add(field
            + (orderBy.getDirection() == Direction.ASCENDING
                ? ""
                : " DESC"));
      }
      jpql = jpql + " ORDER BY "
          + Enumerable.create(orderBys).join(",");
    }

    return jpql;
  }

  private String addWhereExpression(String expression,
      String nextExpression, String condition) {

    return expression == null
        ? nextExpression
        : String.format(
            "%s %s %s",
            expression,
            condition,
            nextExpression);
  }

  private String whereKeyEquals(EntityType<?> jpsEntityType,
      String keyAttributeName, Object typeSafeEntityKey, String alias) {
    SingularAttribute<?, ?> idAtt = jpsEntityType
        .getSingularAttribute(keyAttributeName);
    if (idAtt.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
      List<String> predicates = new ArrayList<String>();
      EmbeddableType<?> et = (EmbeddableType<?>) idAtt.getType();
      for (Attribute<?, ?> subAtt : et.getAttributes()) {
        Object subAttValue = JPAMember
            .create(subAtt, typeSafeEntityKey).get();
        String jpqlLiteral = JPQLGenerator
            .toJpqlLiteral(subAttValue);
        String predicate = String.format(
            "(%s.%s.%s = %s)",
            alias,
            keyAttributeName,
            subAtt.getName(),
            jpqlLiteral);
        predicates.add(predicate);
      }

      return "(" + Enumerable.create(predicates).join(" AND ") + ")";
    }

    String jpqlLiteral = JPQLGenerator.toJpqlLiteral(typeSafeEntityKey);
    return String.format(
        "(%s.%s = %s)",
        alias,
        keyAttributeName,
        jpqlLiteral);
  }
}