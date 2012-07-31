package org.odata4j.producer.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;

import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.OMediaLinkExtension;

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
    OMediaLinkExtension mediaLinkExtension = producer.findExtension(OMediaLinkExtension.class);

    if (mediaLinkExtension == null)
      throw new NotImplementedException();

    EntityResponse entityResponse = producer.getEntity(entitySet.getName(), OEntityKey.parse(entityId), queryInfo);
    InputStream entityStream = mediaLinkExtension.getInputStreamForMediaLinkEntry(entityResponse.getEntity(), null, queryInfo);
    String contentType = mediaLinkExtension.getMediaLinkContentType(entityResponse.getEntity());
    String contentDisposition = mediaLinkExtension.getMediaLinkContentDisposition(entityResponse.getEntity());
    return Response.ok(entityStream, contentType).header("Content-Disposition", contentDisposition).build();
  }

}