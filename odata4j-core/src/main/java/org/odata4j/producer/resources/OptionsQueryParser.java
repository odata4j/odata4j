package org.odata4j.producer.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.ExpressionParser;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.producer.InlineCount;

import com.sun.jersey.api.core.HttpContext;

/**
 *
 * @author sergei.grizenok
 */
public class OptionsQueryParser {

    public static InlineCount parseInlineCount(String inlineCount) {
        if (inlineCount == null) {
            return null;
        }
        Map<String, InlineCount> rt = new HashMap<String, InlineCount>();
        rt.put("allpages", InlineCount.ALLPAGES);
        rt.put("none", InlineCount.NONE);
        return rt.get(inlineCount);
    }

    public static Integer parseTop(String top) {
        return top == null ? null : Integer.parseInt(top);
    }

    public static Integer parseSkip(String skip) {
        return skip == null ? null : Integer.parseInt(skip);
    }

    public static BoolCommonExpression parseFilter(String filter) {
        if (filter == null) {
            return null;
        }
        CommonExpression ce = ExpressionParser.parse(filter);
        if (!(ce instanceof BoolCommonExpression)) {
            throw new RuntimeException("Bad filter");
        }
        return (BoolCommonExpression) ce;
    }

    public static List<OrderByExpression> parseOrderBy(String orderBy) {
        if (orderBy == null) {
            return null;
        }
        return ExpressionParser.parseOrderBy(orderBy);
    }

    public static String parseSkipToken(String skipToken) {
        return skipToken;
    }

    public static Map<String, String> parseCustomOptions(HttpContext context) {
        Map<String, String> rt = new HashMap<String, String>();


        for (String qp : context.getRequest().getQueryParameters().keySet()) {
            if (!qp.startsWith("$")) {
                rt.put(qp, context.getRequest().getQueryParameters().getFirst(qp));


            }
        }
        return rt;


    }

    public static Object parseIdObject(String id) {
        String cleanid = null;


        if (id != null && id.length() > 0) {
            if (id.startsWith("(") && id.endsWith(")")) {
                cleanid = id.substring(1, id.length() - 1);
                // log.info("cleanid!: " + cleanid);


            }
        }
        if (cleanid == null) {
            throw new RuntimeException("unable to parse id");


        }

        Object idObject;


        if (cleanid.startsWith("'") && cleanid.endsWith("'")) {
            idObject = cleanid.substring(1, cleanid.length() - 1);


        } else if (cleanid.endsWith("L")) {
            idObject = Long.parseLong(cleanid.substring(0, cleanid.length() - 1));


        } else {
            idObject = Integer.parseInt(cleanid);


        }

        return idObject;
    }

    public static List<EntitySimpleProperty> parseExpand(String expand) {
        if (expand == null) {
            return null;
        }
        return ExpressionParser.parseExpand(expand);
    }

	public static List<EntitySimpleProperty> parseSelect(String select) {
        if (select == null) {
            return null;
        }
        return ExpressionParser.parseExpand(select);
	}
}
