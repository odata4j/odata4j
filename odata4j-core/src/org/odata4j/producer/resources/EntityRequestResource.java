package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;

import com.sun.jersey.api.core.HttpContext;

@Path("{entitySetName}{id: (\\(.+\\))}")
public class EntityRequestResource extends BaseResource {

    private static final Logger log = Logger.getLogger(EntityRequestResource.class.getName());

    @PUT
    public Response updateEntity(@Context HttpContext context, @Context ODataProducer producer, final @PathParam("entitySetName") String entitySetName, @PathParam("id") String id) {

        log.info(String.format("updateEntity(%s,%s)", entitySetName, id));

        OEntity entity = this.getRequestEntity(context.getRequest());

        Object idObject = OptionsQueryParser.parseIdObject(id);

        producer.updateEntity(entitySetName, idObject, entity);

        return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION).build();
    }

    @POST
    public Response mergeEntity(@Context HttpContext context, @Context ODataProducer producer, final @PathParam("entitySetName") String entitySetName, @PathParam("id") String id) {

        log.info(String.format("mergeEntity(%s,%s)", entitySetName, id));
        Object idObject = OptionsQueryParser.parseIdObject(id);

        if ("MERGE".equals(context.getRequest().getHeaderValue(ODataConstants.Headers.X_HTTP_METHOD))) {
            OEntity entity = this.getRequestEntity(context.getRequest());
            producer.mergeEntity(entitySetName, idObject, entity);

            return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION).build();
        }

        if ("DELETE".equals(context.getRequest().getHeaderValue(ODataConstants.Headers.X_HTTP_METHOD))) {
            producer.deleteEntity(entitySetName, idObject);
            
            return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION).build();
        }

        throw new RuntimeException("Expected a tunnelled MERGE or DELETE");
    }

    @DELETE
    public Response deleteEntity(@Context HttpContext context, @Context ODataProducer producer, final @PathParam("entitySetName") String entitySetName, @PathParam("id") String id) {

        log.info(String.format("getEntity(%s,%s)", entitySetName, id));

        Object idObject = OptionsQueryParser.parseIdObject(id);

        producer.deleteEntity(entitySetName, idObject);

        return Response.ok().header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION).build();
    }

    @GET
    @Produces({ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8, ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8, ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8})
    public Response getEntity(@Context HttpContext context, @Context ODataProducer producer, final @PathParam("entitySetName") String entitySetName, @PathParam("id") String id, @QueryParam("$format") String format, @QueryParam("$callback") String callback) {

        log.info(String.format("getEntity(%s,%s)", entitySetName, id));

        Object idObject = OptionsQueryParser.parseIdObject(id);

        EntityResponse response = producer.getEntity(entitySetName, idObject);

        if (response == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        StringWriter sw = new StringWriter();
        FormatWriter<EntityResponse> fw = FormatWriterFactory.getFormatWriter(EntityResponse.class, context.getRequest().getAcceptableMediaTypes(), format, callback);
        fw.write(context.getUriInfo(), sw, response);
        String entity = sw.toString();

        return Response.ok(entity, fw.getContentType()).header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION).build();

    }

    @Path("{navProp:.+}")
    public PropertyRequestResource getNavProperty() {
        return new PropertyRequestResource();
    }
}
