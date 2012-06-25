package org.odata4j.producer.resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.OMediaLinkExtension;
import org.odata4j.producer.exceptions.NotFoundException;
import org.odata4j.producer.exceptions.NotImplementedException;

@Path("{entitySetName: [^/()]+?}{id: \\(.+?\\)}")
public class EntityRequestResource extends BaseResource {

  private static final Logger log = Logger.getLogger(EntityRequestResource.class.getName());

  @PUT
  public Response updateEntity(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo, @Context ContextResolver<ODataProducer> producerResolver,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      InputStream payload) throws Exception {

    log.info(String.format("updateEntity(%s,%s)", entitySetName, id));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    // is this a new media resource?
    // check for HasStream
    EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);
    if (null == entitySet) {
      throw new NotFoundException();
    }
    
    if (Boolean.TRUE.equals(entitySet.getType().getHasStream())) { // getHasStream can return null
      // yes it is!
      return updateMediaLinkEntry(httpHeaders, uriInfo, producer, entitySet, payload, OEntityKey.parse(id));
    }
    
    OEntity entity = this.getRequestEntity(httpHeaders, uriInfo, payload, producer.getMetadata(), entitySetName, OEntityKey.parse(id));
    producer.updateEntity(entitySetName, entity);

    // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
    return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }
  
  /**
   * update an entity given a String payload.
   * Note: currently this exists because EntitiesRequestResource processBatch needs
   *       a version with a String payload.  It may be possible (desirable?) to 
   *       re-write batch handling completely such that it streamed individual batch
   *       items instead of loading the entire batch payload into memory and then
   *       processing the batch items.
   * @param httpHeaders
   * @param uriInfo
   * @param producerResolver
   * @param entitySetName
   * @param id
   * @param payload
   * @return
   * @throws Exception 
   */
  protected Response updateEntity(HttpHeaders httpHeaders, UriInfo uriInfo, ContextResolver<ODataProducer> producerResolver,
      String entitySetName,
      String id,
      String payload) throws Exception {

    log.info(String.format("updateEntity(%s,%s)", entitySetName, id));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    // is this a new media resource?
    // check for HasStream
    EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);
    if (null == entitySet) {
      throw new NotFoundException();
    }
    
    if (Boolean.TRUE.equals(entitySet.getType().getHasStream())) { // getHasStream can return null
      // yes it is!
      ByteArrayInputStream inStream = new ByteArrayInputStream(payload.getBytes());
      try {
        return updateMediaLinkEntry(httpHeaders, uriInfo, producer, entitySet, inStream, OEntityKey.parse(id));
      } finally {
        inStream.close();
      }
    }
    
    OEntity entity = this.getRequestEntity(httpHeaders, uriInfo, payload, producer.getMetadata(), entitySetName, OEntityKey.parse(id));
    producer.updateEntity(entitySetName, entity);

    // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
    return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }

  /**
   * update the media resource found in the payload for the media link entry (mle)
   * identified by the given key.
   * 
   * @param httpHeaders
   * @param uriInfo
   * @param producer
   * @param entitySet
   * @param payload
   * @param key
   * @return HTTP 204 No Content response if successful.
   * @throws IOException 
   */
  protected Response updateMediaLinkEntry(HttpHeaders httpHeaders,
          UriInfo uriInfo, ODataProducer producer, EdmEntitySet entitySet, InputStream payload, OEntityKey key) throws IOException {
     
    OEntity mle = super.createOrUpdateMediaLinkEntry(httpHeaders, uriInfo, entitySet, producer, payload, key);
    
     // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
    return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }
  
  @POST
  public Response mergeEntity(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo, @Context ContextResolver<ODataProducer> producerResolver,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      String payload) {

    log.info(String.format("mergeEntity(%s,%s)", entitySetName, id));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    OEntityKey entityKey = OEntityKey.parse(id);

    String method = httpHeaders.getRequestHeaders().getFirst(ODataConstants.Headers.X_HTTP_METHOD);
    if ("MERGE".equals(method)) {
      OEntity entity = this.getRequestEntity(httpHeaders, uriInfo, payload, producer.getMetadata(), entitySetName, entityKey);
      producer.mergeEntity(entitySetName, entity);

      // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
      return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
    }

    if ("DELETE".equals(method)) {
      producer.deleteEntity(entitySetName, entityKey);

      // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
      return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
    }

    if ("PUT".equals(method)) {
      OEntity entity = this.getRequestEntity(httpHeaders, uriInfo, payload, producer.getMetadata(), entitySetName, OEntityKey.parse(id));
      producer.updateEntity(entitySetName, entity);

      // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
      return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
    }

    throw new RuntimeException("Expected a tunnelled PUT, MERGE or DELETE");
  }

  @DELETE
  public Response deleteEntity(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo,
  @Context ContextResolver<ODataProducer> producerResolver,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id) {

    log.info(String.format("deleteEntity(%s,%s)", entitySetName, id));

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);

    OEntityKey entityKey = OEntityKey.parse(id);
    
    // is this a new media resource?
    // check for HasStream
    EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);
    if (null == entitySet) {
      throw new NotFoundException();
    }
    
    if (Boolean.TRUE.equals(entitySet.getType().getHasStream())) { // getHasStream can return null
      // yes it is!
      // first, the producer must support OMediaLinkExtension
      OMediaLinkExtension mediaLinkExtension = getMediaLinkExtension(httpHeaders, uriInfo, entitySet, producer);

      // get a media link entry from the extension
      OEntity mle = mediaLinkExtension.getMediaLinkEntryForUpdateOrDelete(entitySet, entityKey, httpHeaders);
      mediaLinkExtension.deleteStream(mle, null /* QueryInfo, may need to get rid of */);
       // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
      return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
    } 
    
    producer.deleteEntity(entitySetName, entityKey);
    
    // TODO: hmmh..isn't this supposed to be HTTP 204 No Content?
    return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }

  @GET
  @Produces({ ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8, ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8, ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
  public Response getEntity(@Context HttpHeaders httpHeaders, @Context UriInfo uriInfo, @Context ContextResolver<ODataProducer> producerResolver,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @QueryParam("$format") String format,
      @QueryParam("$callback") String callback,
      @QueryParam("$expand") String expand,
      @QueryParam("$select") String select) {

    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
    return getEntityImpl(httpHeaders, uriInfo, producer, entitySetName, id, format, callback, expand, select);
  }

  protected Response getEntityImpl(HttpHeaders httpHeaders, UriInfo uriInfo, ODataProducer producer,
      String entitySetName,
      String id,
      String format,
      String callback,
      String expand,
      String select) {

    EntityQueryInfo query = new EntityQueryInfo(
        null,
        OptionsQueryParser.parseCustomOptions(uriInfo),
        OptionsQueryParser.parseExpand(expand),
        OptionsQueryParser.parseSelect(select));

    log.info(String.format(
        "getEntity(%s,%s,%s,%s)",
        entitySetName,
        id,
        expand,
        select));

    EntityResponse response = producer.getEntity(entitySetName, OEntityKey.parse(id), query);

    StringWriter sw = new StringWriter();
    FormatWriter<EntityResponse> fw = FormatWriterFactory.getFormatWriter(EntityResponse.class, httpHeaders.getAcceptableMediaTypes(), format, callback);
    fw.write(uriInfo, sw, response);
    String entity = sw.toString();

    return Response.ok(entity, fw.getContentType()).header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }

  @Path("{first: \\$}links/{targetNavProp:.+?}{targetId: (\\(.+?\\))?}")
  public LinksRequestResource getLinks(
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @PathParam("targetNavProp") String targetNavProp,
      @PathParam("targetId") String targetId) {

    OEntityKey targetEntityKey = targetId == null || targetId.isEmpty() ? null : OEntityKey.parse(targetId);

    return new LinksRequestResource(OEntityIds.create(entitySetName, OEntityKey.parse(id)), targetNavProp, targetEntityKey);
  }

  @Path("{first: \\$}value")
  public ValueRequestResource getValue() {
    return new ValueRequestResource();
  }

  @Path("{navProp: .+}")
  public PropertyRequestResource getNavProperty() {
    return new PropertyRequestResource();
  }

  @Path("{navProp: .+?}{optionalParens: ((\\(\\)))}")
  public PropertyRequestResource getSimpleNavProperty() {
    return new PropertyRequestResource();
  }

}