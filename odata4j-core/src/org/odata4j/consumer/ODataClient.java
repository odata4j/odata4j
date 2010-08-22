package org.odata4j.consumer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.ws.rs.core.MediaType;

import org.core4j.Enumerable;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;
import org.odata4j.core.OClientBehavior;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.AtomEntryFormatWriter;
import org.odata4j.format.xml.AtomFeedFormatParser;
import org.odata4j.format.xml.AtomServiceDocumentFormatParser;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomFeed;
import org.odata4j.format.xml.AtomFeedFormatParser.CollectionInfo;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ODataClient {



    private final OClientBehavior[] requiredBehaviors = new OClientBehavior[] { new MethodTunnelingBehavior("MERGE") }; // jersey hates MERGE, tunnel through POST
    private final OClientBehavior[] behaviors;

    private final Client client = ClientUtil.newClient();

    public ODataClient(OClientBehavior... behaviors) {

        this.behaviors = Enumerable.create(requiredBehaviors).concat(Enumerable.create(behaviors)).toArray(OClientBehavior.class);
    }

    
    public EdmDataServices getMetadata(ODataClientRequest request){
        
        ClientResponse response = doRequest(request, 200,404,400);
        if (response.getStatus()==404||response.getStatus()==400)
            return null;
        XMLEventReader2 reader = doXmlRequest(response);
        return EdmxFormatParser.parseMetadata(reader);
    }
    
    public Iterable<CollectionInfo> getCollections(ODataClientRequest request) {
        
        ClientResponse response = doRequest(request, 200);
        XMLEventReader2 reader = doXmlRequest(response);
        return AtomServiceDocumentFormatParser.parseCollections(reader);
    }

    public AtomEntry getEntity(ODataClientRequest request) {
        
        ClientResponse response = doRequest(request, 404, 200,204);
        if (response.getStatus() == 404)
            return null;
        if (response.getStatus() == 204)
            return null;
        XMLEventReader2 reader = doXmlRequest(response);
        return AtomFeedFormatParser.parseFeed(reader).entries.iterator().next();
    }

    public AtomFeed getEntities(ODataClientRequest request) {

        ClientResponse response = doRequest(request, 200);
        XMLEventReader2 reader = doXmlRequest(response);
        return AtomFeedFormatParser.parseFeed(reader);
    }

    public DataServicesAtomEntry createEntity(ODataClientRequest request) {

        ClientResponse response = doRequest(request, 201);
        XMLEventReader2 reader = doXmlRequest(response);
        return (DataServicesAtomEntry) AtomFeedFormatParser.parseFeed(reader).entries.iterator().next();
    }

    public boolean updateEntity(ODataClientRequest request) {
        doRequest(request, 200, 204);
        return true;
    }

    public boolean deleteEntity(ODataClientRequest request) {
        doRequest(request, 200, 204, 404);
        return true;
    }

    private ClientResponse doRequest(ODataClientRequest request, Integer... expectedResponseStatus) {

        if (behaviors != null) {
            for(OClientBehavior behavior : behaviors)
                request = behavior.transform(request);
        }

        WebResource webResource = client.resource(request.getUrl());

        // set query params
        for(String qpn : request.getQueryParams().keySet()) {
            webResource = webResource.queryParam(qpn, request.getQueryParams().get(qpn));
        }

        WebResource.Builder b = webResource.getRequestBuilder();

        // set headers
        b = b.accept(MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML);

        for(String header : request.getHeaders().keySet()) {
            b.header(header, request.getHeaders().get(header));
        }

        if (ODataConsumer.DUMP_REQUEST_HEADERS)
            log(request.getMethod() + " " + webResource.toString());

        // request body
        if (request.getEntry() != null) {

            DataServicesAtomEntry dsae = request.getEntry();

            StringWriter sw = new StringWriter();
            new AtomEntryFormatWriter().writeRequestEntry(sw,dsae);
            String entity = sw.toString();
            if (ODataConsumer.DUMP_REQUEST_BODY)
                log(entity);
            b.entity(entity, MediaType.APPLICATION_ATOM_XML);

        }

        // execute request
        ClientResponse response = b.method(request.getMethod(), ClientResponse.class);

        if (ODataConsumer.DUMP_RESPONSE_HEADERS)
            dumpHeaders(response);
        int status = response.getStatus();
        for(int expStatus : expectedResponseStatus) {
            if (status == expStatus) {
                return response;
            }
        }
        throw new RuntimeException(String.format("Expected status %s, found %s:", Enumerable.create(expectedResponseStatus).join(" or "), status) + "\n" + response.getEntity(String.class));

    }

    private XMLEventReader2 doXmlRequest(ClientResponse response)  {

        
        if (ODataConsumer.DUMP_RESPONSE_BODY) {
            String textEntity = response.getEntity(String.class);
            log(textEntity);
            return InternalUtil.newXMLEventReader(new StringReader(textEntity));
        }
        
        InputStream textEntity = response.getEntityInputStream();
        try {
            return InternalUtil.newXMLEventReader(new InputStreamReader(textEntity,"UTF-8"));
        } catch (Exception e){
            throw new RuntimeException(e);
        }

       
    }

    private void dumpHeaders(ClientResponse response) {
        log("Status: " + response.getStatus());
        for(String key : response.getHeaders().keySet()) {
            log(key + ": " + response.getHeaders().getFirst(key));
        }
    }

    private static void log(String message) {
        System.out.println(message);
    }

}
