package org.odata4j.producer.resources;

import java.io.StringWriter;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.odata4j.core.ODataConstants;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.ErrorResponse;
import org.odata4j.producer.exceptions.ODataException;
import org.odata4j.producer.exceptions.ServerErrorException;

/**
 * Provider for correctly formatted server errors.  Every {@link RuntimeException} that
 * is not already an {@link ODataException} is wrapped into a {@link ServerErrorException}
 * (resulting in an HTTP {@link Status#INTERNAL_SERVER_ERROR}).
 */
@Provider
public class ExceptionMappingProvider implements ExceptionMapper<RuntimeException> {

  @Context
  protected UriInfo uriInfo;
  @Context
  protected HttpHeaders httpHeaders;

  public Response toResponse(RuntimeException e) {
    ODataException exception;
    if (e instanceof ODataException)
      exception = (ODataException) e;
    else
      exception = new ServerErrorException(e);

    String format = uriInfo.getQueryParameters().getFirst("$format");
    String callback = uriInfo.getQueryParameters().getFirst("$callback");
    FormatWriter<ErrorResponse> fw = FormatWriterFactory.getFormatWriter(ErrorResponse.class, httpHeaders.getAcceptableMediaTypes(), format, callback);
    StringWriter sw = new StringWriter();
    fw.write(uriInfo, sw, exception);
    return Response.status(exception.getStatus())
        .type(fw.getContentType())
        .header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER)
        .entity(sw.toString())
        .build();
  }
}
