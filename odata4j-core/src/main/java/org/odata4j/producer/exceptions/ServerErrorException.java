package org.odata4j.producer.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ServerErrorException extends ODataException {

  private static final long serialVersionUID = 1L;

  public ServerErrorException() {
    super(Response.status(500).build());
  }

  public ServerErrorException(String message) {
    super(Response.status(500).entity(message).type(MediaType.TEXT_PLAIN_TYPE).build());
  }

}
