package org.odata4j.producer.inmemory;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.QueryInfo;

/**
 *
 * @author sergei.grizenok
 */
public class ListUtils {

    private static final String ID_PROPNAME = "EntityId";

    public static List<OEntity> applyQuery(
            final List<OEntity> entities,
            final QueryInfo query,
            final int maxResults) {

        if (entities.isEmpty()) {
            return entities;
        }

        PropertyModel properties = convertEntityToPropertyModel(entities.get(0));
        Enumerable<OEntity> objects = Enumerable.create(entities);

        // apply filter
        if (query.filter != null) {
            objects = objects.where(filterToPredicate(query.filter, properties));
        }

        // apply ordering
        if (query.orderBy != null) {
            objects = orderBy(objects, query.orderBy, properties);
        }

        // skip records by $skipToken
        if (query.skipToken != null) {
            final Boolean[] skipping = new Boolean[]{true};
            objects = objects.skipWhile(new Predicate1<OEntity>() {

                public boolean apply(OEntity input) {
                    if (skipping[0]) {
                        String inputKey = keyString(input);
                        if (query.skipToken.equals(inputKey)) {
                            skipping[0] = false;
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        // skip records by $skip amount
        if (query.skip != null) {
            objects = objects.skip(query.skip);
        }

        // apply limit
        int limit = computeLimit(query.top, maxResults);
        objects = objects.take(limit + 1);

        // materialize OEntities
        return objects.toList();
    }

    public static String computeSkipToken(
            final List<OEntity> entities,
            final QueryInfo query,
            final int maxResults) {
        int limit = computeLimit(query.top, maxResults);

        String skipToken = null;
        if (entities.size() > limit) {
            skipToken = entities.isEmpty()
                    ? null
                    : ListUtils.keyString(Enumerable.create(entities).last());
        }

        return skipToken;
    }

    private static Enumerable<OEntity> orderBy(Enumerable<OEntity> iter, List<OrderByExpression> orderBys, final PropertyModel properties) {
        for (final OrderByExpression orderBy : Enumerable.create(orderBys).reverse()) {
            iter = iter.orderBy(new Comparator<OEntity>() {

                @SuppressWarnings("unchecked")
                public int compare(OEntity o1, OEntity o2) {
                    Comparable lhs = (Comparable) InMemoryEvaluation.evaluate(orderBy.getExpression(), o1, properties);
                    Comparable rhs = (Comparable) InMemoryEvaluation.evaluate(orderBy.getExpression(), o2, properties);
                    return (orderBy.isAscending() ? 1 : -1) * lhs.compareTo(rhs);
                }
            });
        }
        return iter;
    }

    private static Predicate1<OEntity> filterToPredicate(final BoolCommonExpression filter, final PropertyModel properties) {
        return new Predicate1<OEntity>() {

            public boolean apply(OEntity input) {
                return InMemoryEvaluation.evaluate(filter, input, properties);
            }
        };
    }

    public static Integer computeInlineCount(List<OEntity> entities, QueryInfo query) {
        return query.inlineCount == InlineCount.ALLPAGES
                ? entities.size()
                : null;
    }

    private static String keyString(OEntity entity) {
        return InternalUtil.keyString(entity.getProperty(ID_PROPNAME).getValue(), false);
    }

    private static int computeLimit(Integer top, int maxResults) {
        int limit = maxResults;
        if (top != null && top < limit) {
            limit = top;
        }

        return limit;
    }

    private static PropertyModel convertEntityToPropertyModel(final OEntity entity) {
        return new PropertyModel() {

            @Override
            public Object getPropertyValue(Object target, String propertyName) {
                return ((OEntity) target).getProperty(propertyName).getValue();
            }

            @Override
            public Iterable<String> getPropertyNames() {
                List<String> names = new LinkedList<String>();
                for (OProperty<?> oprop : entity.getProperties()) {
                    names.add(oprop.getName());
                }

                return names;
            }

            @Override
            public Class<?> getPropertyType(String propertyName) {
                return entity.getProperty(propertyName).getValue().getClass();
            }
        };
    }
}
