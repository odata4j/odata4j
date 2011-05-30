package org.odata4j.producer.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.exceptions.NotImplementedException;

import com.sun.jersey.api.core.HttpContext;

public class LinksRequestResource extends BaseResource {

  @POST
  @Path("{navProp:.+}")
  public Response updateEntity(
      @Context HttpContext context,
      @Context ODataProducer producer,
      @PathParam("entitySetName") String entitySetName,
      @PathParam("id") String id,
      @PathParam("navProp") String navProp) {

    throw new NotImplementedException("NavProp: updateEntity not supported yet.");

  }

}
