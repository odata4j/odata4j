package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.Guid;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.xml.AtomEntryFormatWriter;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;

import com.sun.jersey.api.core.HttpContext;

@Path("{entitySetName}{optionalParens: ((\\(\\))?)}")
public class EntitiesRequestResource extends BaseResource {

	private static final Logger log = Logger.getLogger(EntitiesRequestResource.class.getName());

	@POST
	@Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8)
	public Response createEntity(
			@Context HttpContext context,
			@Context ODataProducer producer,
			final @PathParam("entitySetName") String entitySetName) {

		log.info(String.format("createEntity(%s)", entitySetName));

		OEntity entity = this.getRequestEntity(context.getRequest(),producer.getMetadata(),entitySetName);

		EntityResponse response = producer.createEntity(entitySetName, entity);

		StringWriter sw = new StringWriter();
		String entryId = new AtomEntryFormatWriter().writeAndReturnId(
				context.getUriInfo(), 
				sw, 
				response);
		
		String responseEntity = sw.toString();

		return Response
				.ok(responseEntity,ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8)
				.status(Status.CREATED)
				.location(URI.create(entryId))
				.header(ODataConstants.Headers.DATA_SERVICE_VERSION,
						ODataConstants.DATA_SERVICE_VERSION).build();

	}

	@GET
	@Produces({ ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8,
			ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8,
			ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
	public Response getEntities(@Context HttpContext context,
			@Context ODataProducer producer,
			@PathParam("entitySetName") String entitySetName,
			@QueryParam("$inlinecount") String inlineCount,
			@QueryParam("$top") String top,
			@QueryParam("$skip") String skip,
			@QueryParam("$filter") String filter,
			@QueryParam("$orderby") String orderBy,
			@QueryParam("$format") String format,
			@QueryParam("$callback") String callback,
			@QueryParam("$skiptoken") String skipToken,
			@QueryParam("$expand") String expand,
			@QueryParam("$select") String select) {

		log.info(String.format(
				"getEntities(%s,%s,%s,%s,%s,%s,%s,%s)",
				entitySetName,
				inlineCount,
				top,
				skip,
				filter,
				orderBy,
				skipToken,
				expand));

		QueryInfo query = new QueryInfo(
				OptionsQueryParser.parseInlineCount(inlineCount),
				OptionsQueryParser.parseTop(top),
				OptionsQueryParser.parseSkip(skip),
				OptionsQueryParser.parseFilter(filter),
				OptionsQueryParser.parseOrderBy(orderBy),
				OptionsQueryParser.parseSkipToken(skipToken),
				OptionsQueryParser.parseCustomOptions(context),
				OptionsQueryParser.parseExpand(expand),
				OptionsQueryParser.parseSelect(select));

		EntitiesResponse response = producer.getEntities(entitySetName, query);

		StringWriter sw = new StringWriter();
		FormatWriter<EntitiesResponse> fw =
				FormatWriterFactory.getFormatWriter(
						EntitiesResponse.class,
						context.getRequest()
								.getAcceptableMediaTypes(),
						format,
						callback);

		fw.write(context.getUriInfo(), sw, response);
		String entity = sw.toString();

		return Response
				.ok(entity, fw.getContentType())
				.header(ODataConstants.Headers.DATA_SERVICE_VERSION,
						ODataConstants.DATA_SERVICE_VERSION).build();

	}

	@POST
	@Consumes(ODataBatchProvider.MULTIPART_MIXED)
	@Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8)
	public Response processBatch(
			@Context ODataProducer producer,
			final List<BatchBodyPart> bodyParts) {

		log.info(String.format("processBatch(%s)", ""));

		EntityRequestResource er = new EntityRequestResource();

		String changesetBoundary = "changesetresponse_"
				+ Guid.randomGuid().toString();
		String batchBoundary = "batchresponse_" + Guid.randomGuid().toString();
		StringBuilder batchResponse = new StringBuilder("\n--");
		batchResponse.append(batchBoundary);

		batchResponse
				.append("\n").append(ODataConstants.Headers.CONTENT_TYPE).append(": multipart/mixed; boundary=")
				.append(changesetBoundary);

		batchResponse.append('\n');

		for (BatchBodyPart bodyPart : bodyParts) {
			HttpContext context = bodyPart.createHttpContext();
			String entitySetName = bodyPart.getEntitySetName();
			String entityId = bodyPart.getEntityKey();
			Response response = null;

			switch (bodyPart.getHttpMethod()) {
				case POST:
					response = this.createEntity(context, producer,
							entitySetName);
					break;
				case PUT:
					response = er.updateEntity(context, producer,
							entitySetName, entityId);
					break;
				case MERGE:
					response = er.mergeEntity(context, producer, entitySetName,
							entityId);
					break;
				case DELETE:
					response = er.deleteEntity(context, producer,
							entitySetName, entityId);
					break;
				case GET:
					throw new UnsupportedOperationException(
							"Not supported yet.");
			}

			batchResponse.append("\n--").append(changesetBoundary);
			batchResponse.append("\n").append(ODataConstants.Headers.CONTENT_TYPE).append(": application/http");
			batchResponse.append("\nContent-Transfer-Encoding: binary\n");

			batchResponse.append(ODataBatchProvider.createResponseBodyPart(
					bodyPart,
					response));
		}

		batchResponse.append("--").append(changesetBoundary).append("--\n");
		batchResponse.append("--").append(batchBoundary).append("--\n");

		return Response
				.status(Status.ACCEPTED)
				.type(ODataBatchProvider.MULTIPART_MIXED + ";boundary="
								+ batchBoundary).header(
						ODataConstants.Headers.DATA_SERVICE_VERSION,
						ODataConstants.DATA_SERVICE_VERSION)
				.entity(batchResponse.toString()).build();
	}
}
