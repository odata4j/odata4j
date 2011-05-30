package org.odata4j.producer.resources;

import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.odata4j.core.ODataConstants;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.ODataProducer;

import com.sun.jersey.api.core.HttpContext;

@Path("")
public class ServiceDocumentResource {

  @GET
  @Produces({ ODataConstants.APPLICATION_XML_CHARSET_UTF8, ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8, ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8 })
  public Response getServiceDocument(@Context HttpContext context, @Context ODataProducer producer, @QueryParam("$format") String format, @QueryParam("$callback") String callback) {

    EdmDataServices metadata = producer.getMetadata();

    StringWriter w = new StringWriter();
    FormatWriter<EdmDataServices> fw = FormatWriterFactory.getFormatWriter(EdmDataServices.class, context.getRequest().getAcceptableMediaTypes(), format, callback);
    fw.write(context.getUriInfo(), w, metadata);

    return Response.ok(w.toString(), fw.getContentType())
        .header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER)
        .build();
  }
}
