package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OProperty;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.ExpressionParser;
import org.odata4j.expression.OrderByExpression;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.xml.AtomFeedWriter;

import com.sun.jersey.api.core.HttpContext;

@Path("{entitySetName}{optionalParens: (\\(\\))?}")
public class EntitiesRequestResource extends BaseResource {

	private static final Logger log = Logger.getLogger(EntitiesRequestResource.class.getName());
	

	@POST
	@Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET)
	public Response createEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName){
		
		log.info(String.format("createEntity(%s)",entitySetName));
		
		List<OProperty<?>> properties = this.getRequestEntityProperties(context.getRequest());

		EntityResponse response = producer.createEntity(entitySetName,properties);
		
		String baseUri = context.getUriInfo().getBaseUri().toString();
		StringWriter sw = new StringWriter();
		String entryId = AtomFeedWriter.generateResponseEntry(baseUri,response,sw);
		String responseEntity = sw.toString();
		
		return Response.ok(responseEntity,ODataConstants.APPLICATION_ATOM_XML_CHARSET)
					.status(Status.CREATED)
					.location(URI.create(entryId))
					.header(ODataConstants.Headers.DATA_SERVICE_VERSION,ODataConstants.DATA_SERVICE_VERSION).build();
		
	}
	
	
	@GET
	@Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET)
	public Response getEntities(
			@Context HttpContext context,
			@Context ODataProducer producer,
		    @PathParam("entitySetName") String entitySetName, 
		    @QueryParam("$inlinecount") String inlineCount, 
			@QueryParam("$top") String top, 
			@QueryParam("$skip") String skip,
			@QueryParam("$filter") String filter,
			@QueryParam("$orderby") String orderBy){
		
		log.info(String.format("getEntities(%s,%s,%s,%s,%s,%s)",entitySetName,inlineCount,top,skip,filter,orderBy));
		
		final QueryInfo finalQuery = new QueryInfo(parseInlineCount(inlineCount),parseTop(top),parseSkip(skip),parseFilter(filter),parseOrderBy(orderBy));
		
		
		EntitiesResponse response = producer.getEntities(entitySetName,finalQuery);
		
		String baseUri = context.getUriInfo().getBaseUri().toString();
		StringWriter sw = new StringWriter();
		AtomFeedWriter.generateFeed(baseUri,response,sw);
		String entity = sw.toString();
		//log.info("entity: " + entity);
		return Response.ok(entity,ODataConstants.APPLICATION_ATOM_XML_CHARSET).header(ODataConstants.Headers.DATA_SERVICE_VERSION,ODataConstants.DATA_SERVICE_VERSION).build();
		
	}
	
	private InlineCount parseInlineCount(String inlineCount) {
		if (inlineCount==null)
			return null;
		Map<String,InlineCount> rt = new HashMap<String,InlineCount>();
		rt.put("allpages", InlineCount.ALLPAGES);
		rt.put("none",InlineCount.NONE);
		return rt.get(inlineCount);
	}

	private Integer parseTop(String top){
		return top==null?null:Integer.parseInt(top);
	}
	private Integer parseSkip(String skip){
		return skip==null?null:Integer.parseInt(skip);
	}
	private BoolCommonExpression parseFilter(String filter){
		if (filter==null)
			return null;
		CommonExpression ce = ExpressionParser.parse(filter);
		if (!(ce instanceof BoolCommonExpression))
			throw new RuntimeException("Bad filter");
		return (BoolCommonExpression)ce;
	}
	private List<OrderByExpression> parseOrderBy(String orderBy){
		if (orderBy==null)
			return null;
		return ExpressionParser.parseOrderBy(orderBy);
	}

}
