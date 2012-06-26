package org.odata4j.producer.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OError;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.ErrorResponse;

@Provider
public class ODataException extends RuntimeException implements ErrorResponse, ExceptionMapper<ODataException> {

  private static final long serialVersionUID = 1L;

  private StatusType status = Status.INTERNAL_SERVER_ERROR;

  @Context
  protected UriInfo uriInfo;
  @Context
  protected HttpHeaders httpHeaders;

  public ODataException() {
  }

  public ODataException(StatusType status) {
    this.status = status;
  }

  public ODataException(StatusType status, String message) {
    super(message);
    this.status = status;
  }

  public ODataException(StatusType status, Throwable cause) {
    super(cause);
    this.status = status;
  }

  public ODataException(StatusType status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public Response toResponse(ODataException exception) {
    String format = uriInfo.getQueryParameters().getFirst("$format");
    String callback = uriInfo.getQueryParameters().getFirst("$callback");
    FormatWriter<ErrorResponse> fw = FormatWriterFactory.getFormatWriter(ErrorResponse.class, httpHeaders.getAcceptableMediaTypes(), format, callback);
    StringWriter sw = new StringWriter();
    fw.write(uriInfo, sw, exception);
    return Response.status(exception.status)
        .type(fw.getContentType())
        .header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION_HEADER)
        .entity(sw.toString())
        .build();
  }

  public OError getError() {
    return new OError() {
      public String getMessage() {
        return ODataException.this.getMessage() != null ? ODataException.this.getMessage() : status.getReasonPhrase();
      }

      public String getCode() {
        return ODataException.this.getClass().getSimpleName();
      }

      public String getInnerError() {
        StringWriter sw = new StringWriter();
        ODataException.this.printStackTrace(new PrintWriter(sw));
        return sw.toString();
      }
    };
  }

}
