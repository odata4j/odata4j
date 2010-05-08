package org.odata4j.producer.resources;

import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.odata4j.core.ODataConstants;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.producer.ODataProducer;
import org.odata4j.xml.EdmxFormatWriter;

@Path("{first: \\$}metadata")
public class MetadataResource {

    @GET
    @Produces(ODataConstants.APPLICATION_XML_CHARSET_UTF8)
    public Response getMetadata(@Context ODataProducer producer) {

        EdmDataServices s = producer.getMetadata();

        StringWriter w = new StringWriter();
        EdmxFormatWriter.write(s, w);

        return Response.ok(w.toString(), ODataConstants.APPLICATION_XML_CHARSET_UTF8).header(ODataConstants.Headers.DATA_SERVICE_VERSION, ODataConstants.DATA_SERVICE_VERSION).build();
    }
}
