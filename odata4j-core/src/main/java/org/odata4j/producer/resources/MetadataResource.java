package org.odata4j.producer.resources;

import com.sun.jersey.api.Responses;
import com.sun.jersey.api.core.HttpContext;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.odata4j.core.ODataConstants;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatType;
import org.odata4j.format.xml.EdmxFormatWriter;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.edm.MetadataProducer;

@Path("{first: \\$}metadata")
public class MetadataResource {

  @GET
  @Produces({ODataConstants.APPLICATION_XML_CHARSET_UTF8, ODataConstants.APPLICATION_ATOMSVC_XML_CHARSET_UTF8})
  public Response getMetadata(@Context HttpContext context, 
    @Context ODataProducer producer,
    @QueryParam("$format") String format) {

    

    // a request for media type atomsvc+xml means give me the service document of the metadata producer
    if ("atomsvc".equals(format) || isAtomSvcRequest(context)) {
      MetadataProducer md = producer.getMetadataProducer();
      if (null == md) {
        return noMetadata();
      }
      ServiceDocumentResource r = new ServiceDocumentResource();
      return r.getServiceDocument(context, md, FormatType.ATOM.name(), null); 
    } else {
      StringWriter w = new StringWriter();
      ODataProducer source = "metamodel".equals(format) ? producer.getMetadataProducer() : producer;
      if (null == source) {
        return noMetadata();
      }
      EdmDataServices s =  source.getMetadata();
      EdmxFormatWriter.write(s, w);

      return Response.ok(w.toString(), ODataConstants.APPLICATION_XML_CHARSET_UTF8)
              .header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER).build();
    }
  }
  
  private boolean isAtomSvcRequest(HttpContext c) {
    for (MediaType mt : c.getRequest().getAcceptableMediaTypes()) {
      if (mt.equals(ODataConstants.APPLICATION_ATOMSVC_XML_TYPE)) {
        return true;
      }
    }
    return false;
  }
  
  @GET
  @Path("{entitySetName}")
  @Produces({ ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8,
      ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8,
      ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
  public Response getMetadataEntities(
          @Context HttpContext context,
          @Context ODataProducer producer,
          final @PathParam("entitySetName") String entitySetName,
          final @PathParam("optionalId") String optionalId,
          @QueryParam("$inlinecount") String inlineCount,
          @QueryParam("$top") String top,
          @QueryParam("$skip") String skip,
          @QueryParam("$filter") String filter,
          @QueryParam("$orderby") String orderBy,
          @QueryParam("$format") String format,
          @QueryParam("$callback") String callback,
          @QueryParam("$skiptoken") String skipToken,
          @QueryParam("$expand") String expand,
          @QueryParam("$select") String select) throws Exception {

    MetadataProducer md = producer.getMetadataProducer();
    if (null == md) {
      return noMetadata();
    }

    EntitiesRequestResource r = new EntitiesRequestResource();
    return r.getEntities(context, md, entitySetName, inlineCount, top, skip, filter, orderBy, format, callback, skipToken, expand, select);
    // return Response.ok("getMetadataEntities: " + entitySetName + " optionalId: " + optionalId, "text/plain").build();
  }
  
  @GET
  @Path("{entitySetName}{id: (\\(.+?\\))}")
  @Produces({ ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8, 
    ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8, 
    ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
  public Response getMetadataEntity(
          @Context HttpContext context,
          @Context ODataProducer producer,
          final @PathParam("entitySetName") String entitySetName,
          final @PathParam("id") String id,
          @QueryParam("$format") String format,
          @QueryParam("$callback") String callback,
          @QueryParam("$expand") String expand,
          @QueryParam("$select") String select) {

    MetadataProducer md = producer.getMetadataProducer();
    if (null == md) {
      return noMetadata();
    }

    EntityRequestResource r = new EntityRequestResource();
    return r.getEntity(context, md, entitySetName, id, format, callback, expand, select);
    //return Response.ok("getMetadataEntity: " + entitySetName + " id: " + id, "text/plain").build();
  }
  
  private Response _404(String msg) {
    return Response.status(Responses.NOT_FOUND).
                entity(msg).type("text/plain").build();
  }
  
  public static final int HTTP_NOT_IMPLEMENTED = 501;
  
  private Response error(int status, String msg) {
    return Response.status(status).
                entity(msg).type("text/plain").build();
  }
  
  private Response noMetadata() {
    return error(HTTP_NOT_IMPLEMENTED, "Queryable metadata not implemented by this producer");
  }
}
