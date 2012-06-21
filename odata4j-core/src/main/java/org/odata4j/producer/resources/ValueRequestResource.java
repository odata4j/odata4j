package org.odata4j.producer.resources;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import org.odata4j.core.ODataConstants;

import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.OMediaLinkExtension;
import org.odata4j.producer.exceptions.NotFoundException;
import org.odata4j.producer.exceptions.NotImplementedException;

public class ValueRequestResource {

  @GET
  public Response get(
      @Context UriInfo uriInfo,
      @Context ContextResolver<ODataProducer> producerResolver,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @QueryParam("$expand") String expand,
      @QueryParam("$select") String select) {
    ODataProducer producer = producerResolver.getContext(ODataProducer.class);
    EdmEntitySet entitySet = producer.getMetadata().findEdmEntitySet(entitySetName);

    if (entitySet != null && entitySet.getType().getHasStream()) {
      return getStreamResponse(producer, entitySet, id, new EntityQueryInfo(
          null,
          OptionsQueryParser.parseCustomOptions(uriInfo),
          OptionsQueryParser.parseExpand(expand),
          OptionsQueryParser.parseSelect(select)));
    }
    throw new NotFoundException();
  }

  protected Response getStreamResponse(ODataProducer producer, EdmEntitySet entitySet, String entityId, EntityQueryInfo queryInfo) {
    OMediaLinkExtension mediaLinkExtension = null;
    try {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(ODataConstants.Params.EdmEntitySet, entitySet);
      params.put(ODataConstants.Params.ODataProducer, producer);
      mediaLinkExtension = producer.findExtension(OMediaLinkExtension.class, params);
    } catch (UnsupportedOperationException e) { }

    if (mediaLinkExtension == null)
      throw new NotImplementedException();

    EntityResponse entityResponse = producer.getEntity(entitySet.getName(), OEntityKey.parse(entityId), queryInfo);
    InputStream entityStream = mediaLinkExtension.getInputStreamForMediaLinkEntry(entityResponse.getEntity(), null, queryInfo);
    String contentType = mediaLinkExtension.getMediaLinkContentType(entityResponse.getEntity());
    return Response.ok(entityStream, contentType).build();
  }

}