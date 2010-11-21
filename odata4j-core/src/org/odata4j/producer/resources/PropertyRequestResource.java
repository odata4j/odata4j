package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.NavPropertyResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;

import com.sun.jersey.api.core.HttpContext;

public class PropertyRequestResource extends BaseResource {

    private static final Logger log = Logger.getLogger(PropertyRequestResource.class.getName());

    @PUT
    public Response updateEntity(
            @Context HttpContext context,
            @Context ODataProducer producer,
            final @PathParam("entitySetName") String entitySetName,
            final @PathParam("id") String id,
            final @PathParam("navProp") String navProp) {

//        throw new UnsupportedOperationException("Not supported yet.");
        log.info("NavProp: updateEntityNot supported yet.");
        return Response.ok().build();
    }

    @POST
    public Response mergeEntity(
            @Context HttpContext context,
            @Context ODataProducer producer,
            final @PathParam("entitySetName") String entitySetName,
            final @PathParam("id") String id,
            final @PathParam("navProp") String navProp) {

        if (!"MERGE".equals(context.getRequest().getHeaderValue(
                ODataConstants.Headers.X_HTTP_METHOD))) {
//            throw new RuntimeException("Expected a tunnelled MERGE");
            log.info("NavProp: mergeEntity Expected a tunnelled MERGE");
            return Response.ok().build();
        }

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @DELETE
    public Response deleteEntity(
            @Context HttpContext context,
            @Context ODataProducer producer,
            final @PathParam("entitySetName") String entitySetName,
            final @PathParam("id") String id,
            final @PathParam("navProp") String navProp) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @GET
    @Produces({
        ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8,
        ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8,
        ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8})
    public Response getNavProperty(
            @Context HttpContext context,
            @Context ODataProducer producer,
            final @PathParam("entitySetName") String entitySetName,
            final @PathParam("id") String id,
            final @PathParam("navProp") String navProp,
            final @QueryParam("$inlinecount") String inlineCount,
            final @QueryParam("$top") String top,
            final @QueryParam("$skip") String skip,
            final @QueryParam("$filter") String filter,
            final @QueryParam("$orderby") String orderBy,
            final @QueryParam("$format") String format,
            final @QueryParam("$callback") String callback,
            final @QueryParam("$skiptoken") String skipToken) {

        QueryInfo query = new QueryInfo(
                OptionsQueryParser.parseInlineCount(inlineCount),
                OptionsQueryParser.parseTop(top),
                OptionsQueryParser.parseSkip(skip),
                OptionsQueryParser.parseFilter(filter),
                OptionsQueryParser.parseOrderBy(orderBy),
                OptionsQueryParser.parseSkipToken(skipToken),
                OptionsQueryParser.parseCustomOptions(context));

        Object idObject = OptionsQueryParser.parseIdObject(id);
        final NavPropertyResponse response = producer.getNavProperty(
                entitySetName,
                idObject,
                navProp,
                query);

        if (response == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        StringWriter sw = new StringWriter();
        FormatWriter fwBase = null;

        if (response.getMultiplicity() == EdmMultiplicity.ONE
                && response.getEntities().size() == 1) {

            FormatWriter<EntityResponse> fw = FormatWriterFactory.getFormatWriter(
                    EntityResponse.class,
                    context.getRequest().getAcceptableMediaTypes(),
                    format,
                    callback);

            fw.write(context.getUriInfo(), sw, new EntityResponse() {

                @Override
                public OEntity getEntity() {
                    return response.getEntities().get(0);
                }

                @Override
                public EdmEntitySet getEntitySet() {
                    return response.getEntitySet();
                }
            });

            fwBase = fw;

        } else {
            FormatWriter<EntitiesResponse> fw = FormatWriterFactory.getFormatWriter(
                    EntitiesResponse.class,
                    context.getRequest().getAcceptableMediaTypes(),
                    format,
                    callback);

            fw.write(context.getUriInfo(), sw, response);
            fwBase = fw;
        }

        String entity = sw.toString();
        return Response.ok(
                entity,
                fwBase.getContentType()).header(
                ODataConstants.Headers.DATA_SERVICE_VERSION,
                ODataConstants.DATA_SERVICE_VERSION).build();
    }
}
