package org.odata4j.producer.resources;

import java.io.StringWriter;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.odata4j.core.ODataConstants;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.ErrorResponse;
import org.odata4j.producer.ErrorResponseExtension;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.Responses;
import org.odata4j.producer.exceptions.ODataException;
import org.odata4j.producer.exceptions.ServerErrorException;

/**
 * Provider for correctly formatted server errors.  Every {@link RuntimeException} that
 * is not already an {@link ODataException} is wrapped into a {@link ServerErrorException}
 * (resulting in an HTTP {@link Status#INTERNAL_SERVER_ERROR}).
 *
 * @see ErrorResponseExtension
 */
@Provider
public class ExceptionMappingProvider implements ExceptionMapper<RuntimeException> {

  @Context
  protected ContextResolver<ODataProducer> producerResolver;
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

    ErrorResponseExtension errorResponseExtension = producerResolver.getContext(ODataProducer.class).findExtension(ErrorResponseExtension.class);
    boolean includeInnerError = errorResponseExtension != null && errorResponseExtension.returnInnerError(httpHeaders, uriInfo);

    FormatWriter<ErrorResponse> fw = FormatWriterFactory.getFormatWriter(ErrorResponse.class, httpHeaders.getAcceptableMediaTypes(),
        getFormatParameter(), getCallbackParameter());
    StringWriter sw = new StringWriter();
    fw.write(uriInfo, sw, Responses.error(exception.toOError(includeInnerError)));

    return Response.status(exception.getStatus())
        .type(fw.getContentType())
        .header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER)
        .entity(sw.toString())
        .build();
  }

  private String getFormatParameter() {
    return uriInfo.getQueryParameters().getFirst("$format");
  }

  private String getCallbackParameter() {
    return uriInfo.getQueryParameters().getFirst("$callback");
  }
}
