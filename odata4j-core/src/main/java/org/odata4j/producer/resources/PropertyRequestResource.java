package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.PropertyResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.exceptions.NotImplementedException;

import com.sun.jersey.api.core.HttpContext;

public class PropertyRequestResource extends BaseResource {

  private static final Logger log =
      Logger.getLogger(PropertyRequestResource.class.getName());

  @PUT
  public Response updateEntity(
      @Context HttpContext context,
      @Context ODataProducer producer,
      @PathParam("entitySetName") String entitySetName,
      final @PathParam("id") String id,
      final @PathParam("navProp") String navProp) {

    log.info("NavProp: updateEntity Not supported yet.");
    throw new NotImplementedException("NavProp: updateEntity not supported yet.");

  }

  @POST
  public Response mergeEntity(
      @Context HttpContext context,
      @Context ODataProducer producer,
      @Context HttpHeaders headers,
      final @PathParam("entitySetName") String entitySetName,
      final @PathParam("id") String id,
      final @PathParam("navProp") String navProp) throws Exception {

    if (!"MERGE".equals(context.getRequest().getHeaderValue(
        ODataConstants.Headers.X_HTTP_METHOD))) {

      // determine the expected entity set
      EdmDataServices metadata = producer.getMetadata();
      EdmEntitySet ees = metadata
          .getEdmEntitySet(metadata.getEdmEntitySet(entitySetName).type
              .findNavigationProperty(navProp).toRole.type);

      // parse the request entity 
      OEntity entity = getRequestEntity(context.getRequest(), metadata, ees.name, OEntityKey.parse(id));

      // execute the create
      EntityResponse response = producer.createEntity(entitySetName, OEntityKey.parse(id), navProp, entity);

      if (response == null) {
        return Response.status(Status.NOT_FOUND).build();
      }

      // get the FormatWriter for the accepted media types requested by client
      StringWriter sw = new StringWriter();
      FormatWriter<EntityResponse> fw = FormatWriterFactory
          .getFormatWriter(EntityResponse.class, headers.getAcceptableMediaTypes(), null, null);
      fw.write(context.getUriInfo(), sw, response);

      // calculate the uri for the location header
      String relid = InternalUtil.getEntityRelId(response.getEntity());
      String entryId = context.getUriInfo().getBaseUri().toString() + relid;

      // create the response
      String responseEntity = sw.toString();
      return Response
          .ok(responseEntity, fw.getContentType())
          .status(Status.CREATED)
          .location(URI.create(entryId))
          .header(ODataConstants.Headers.DATA_SERVICE_VERSION,
              ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
    }

    throw new NotImplementedException("Not supported yet.");

  }

  @DELETE
  public Response deleteEntity(
      @Context HttpContext context,
      @Context ODataProducer producer,
      final @PathParam("entitySetName") String entitySetName,
      final @PathParam("id") String id,
      final @PathParam("navProp") String navProp) {

    throw new NotImplementedException("Not supported yet.");

  }

  @GET
  @Produces({
      ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8,
      ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8,
      ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
  public Response getNavProperty(
      @Context HttpContext context,
      @Context ODataProducer producer,
      final @PathParam("entitySetName") String entitySetName,
      final @PathParam("id") String id,
      final @PathParam("navProp") String navProp,
      final @QueryParam("$inlinecount") String inlineCount,
      final @QueryParam("$top") String top,
      final @QueryParam("$skip") String skip,
      final @QueryParam("$filter") String filter,
      final @QueryParam("$orderby") String orderBy,
      final @QueryParam("$format") String format,
      final @QueryParam("$callback") String callback,
      final @QueryParam("$skiptoken") String skipToken,
      final @QueryParam("$expand") String expand,
      final @QueryParam("$select") String select) throws Exception {

    QueryInfo query = new QueryInfo(
        OptionsQueryParser.parseInlineCount(inlineCount),
        OptionsQueryParser.parseTop(top),
        OptionsQueryParser.parseSkip(skip),
        OptionsQueryParser.parseFilter(filter),
        OptionsQueryParser.parseOrderBy(orderBy),
        OptionsQueryParser.parseSkipToken(skipToken),
        OptionsQueryParser.parseCustomOptions(context),
        OptionsQueryParser.parseSelect(expand),
        OptionsQueryParser.parseSelect(select));

    final BaseResponse response = producer.getNavProperty(
        entitySetName,
        OEntityKey.parse(id),
        navProp,
        query);

    if (response == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    ODataVersion version = ODataConstants.DATA_SERVICE_VERSION;

    StringWriter sw = new StringWriter();
    FormatWriter<?> fwBase;
    if (response instanceof PropertyResponse) {
      FormatWriter<PropertyResponse> fw =
          FormatWriterFactory.getFormatWriter(
              PropertyResponse.class,
              context.getRequest().getAcceptableMediaTypes(),
              format,
              callback);

      fw.write(context.getUriInfo(), sw, (PropertyResponse) response);
      fwBase = fw;
    } else if (response instanceof EntityResponse) {
      FormatWriter<EntityResponse> fw =
          FormatWriterFactory.getFormatWriter(
              EntityResponse.class,
              context.getRequest().getAcceptableMediaTypes(),
              format,
              callback);

      fw.write(context.getUriInfo(), sw, (EntityResponse) response);
      fwBase = fw;
    } else if (response instanceof EntitiesResponse) {
      FormatWriter<EntitiesResponse> fw =
          FormatWriterFactory.getFormatWriter(
              EntitiesResponse.class,
              context.getRequest().getAcceptableMediaTypes(),
              format,
              callback);

      fw.write(context.getUriInfo(), sw, (EntitiesResponse) response);
      fwBase = fw;

      // TODO remove this hack, check whether we are Version 2.0 compatible anyway
      // the JsonWriter writes feed currently always as Version 2.0
      version = MediaType.valueOf(fw.getContentType()).isCompatible(MediaType.APPLICATION_JSON_TYPE)
          ? ODataVersion.V2 : ODataVersion.V2;

    } else {
      throw new NotImplementedException("Unknown BaseResponse type: " + response.getClass().getName());
    }

    String entity = sw.toString();
    return Response.ok(
        entity,
        fwBase.getContentType()).header(
        ODataConstants.Headers.DATA_SERVICE_VERSION,
        version.asString).build();

  }
}
