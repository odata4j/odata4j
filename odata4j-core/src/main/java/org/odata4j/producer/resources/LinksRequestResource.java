package org.odata4j.producer.resources;

import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.core4j.Enumerable;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntityId;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.SingleLink;
import org.odata4j.format.SingleLinks;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.ODataProducer;

import com.sun.jersey.api.core.HttpContext;

public class LinksRequestResource extends BaseResource {

  private final OEntityId sourceEntity;
  private final String navProp;
  
  public LinksRequestResource(OEntityId sourceEntity, String navProp) {
    this.sourceEntity = sourceEntity;
    this.navProp = navProp;
  }

  @GET
  public Response getLinks(@Context HttpContext context, @Context ODataProducer producer,
      @QueryParam("$format") String format,
      @QueryParam("$callback") String callback) {
    
    EntityIdResponse response = producer.getLinks(sourceEntity, navProp);
    
    StringWriter sw = new StringWriter();
    String serviceRootUri = context.getUriInfo().getBaseUri().toString();
    String contentType;
    if (response.getMultiplicity() == EdmMultiplicity.MANY) {
      SingleLinks links = SingleLinks.create(serviceRootUri, response.getEntities());
      FormatWriter<SingleLinks> fw = FormatWriterFactory.getFormatWriter(SingleLinks.class, context.getRequest().getAcceptableMediaTypes(), format, callback);
      fw.write(context.getUriInfo(), sw, links);
      contentType = fw.getContentType();
    } else {
      OEntityId entityId = Enumerable.create(response.getEntities()).firstOrNull();
      if (entityId == null) 
        throw new UnsupportedOperationException();  // TODO(0.5) implement
      
      SingleLink link = SingleLinks.create(serviceRootUri, entityId);
      FormatWriter<SingleLink> fw = FormatWriterFactory.getFormatWriter(SingleLink.class, context.getRequest().getAcceptableMediaTypes(), format, callback);
      fw.write(context.getUriInfo(), sw, link);
      contentType = fw.getContentType();
    }
    
    String entity = sw.toString();

    return Response.ok(entity, contentType).header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
  }

}
