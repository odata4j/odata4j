package org.odata4j.consumer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.ReadOnlyIterator;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.json.JsonFeedFormatParser.JsonEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.CollectionInfo;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.format.xml.AtomServiceDocumentFormatParser;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.internal.BOMWorkaroundReader;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

class ODataClient<F extends Feed<E>, E extends Entry> {
	
	final FormatType type;
	final Class<F> feedClass;
	final Class<E> entryClass;

	private final OClientBehavior[] requiredBehaviors = new OClientBehavior[] { new MethodTunnelingBehavior("MERGE") }; // jersey hates MERGE, tunnel through POST
	private final OClientBehavior[] behaviors;

	private final Client client;

    public ODataClient(FormatType type, Class<F> feedClass, Class<E> entryClass, OClientBehavior... behaviors) {
    	this.feedClass = feedClass;
    	this.entryClass = entryClass;
        this.behaviors = Enumerable.create(requiredBehaviors).concat(Enumerable.create(behaviors)).toArray(OClientBehavior.class);
        this.type = type;
        this.client = ClientUtil.newClient(behaviors);
    }

    public EdmDataServices getMetadata(ODataClientRequest<E> request){
        
        ClientResponse response = doRequest(FormatType.ATOM, request, 200,404,400);
        if (response.getStatus()==404||response.getStatus()==400)
            return null;
        XMLEventReader2 reader = doXmlRequest(response);
        return EdmxFormatParser.parseMetadata(reader);
    }
    
    public Iterable<CollectionInfo> getCollections(ODataClientRequest<E> request) {
        
        ClientResponse response = doRequest(FormatType.ATOM, request, 200);
        XMLEventReader2 reader = doXmlRequest(response);
        return AtomServiceDocumentFormatParser.parseCollections(reader);
    }
    
    public E getEntity(ODataClientRequest<E> request) {
        
        ClientResponse response = doRequest(type, request, 404, 200,204);
        if (response.getStatus() == 404)
            return null;
        if (response.getStatus() == 204)
            return null;
        
        return Enumerable.create(parseFeed(response).getEntries()).firstOrNull();
    }

    private F getEntities(ODataClientRequest<E> request) {

        ClientResponse response = doRequest(type, request, 200);
        return parseFeed(response);
    }

    public E createEntity(ODataClientRequest<E> request) {

        ClientResponse response = doRequest(type, request, 201);
        return parseFeed(response).getEntries().iterator().next();
    }

    public boolean updateEntity(ODataClientRequest<E> request) {
        doRequest(type, request, 200, 204);
        return true;
    }

    public boolean deleteEntity(ODataClientRequest<E> request) {
        doRequest(type, request, 200, 204, 404);
        return true;
    }
    
    //	TODO find a better way to create the entry
    @SuppressWarnings("unchecked")
	E createEntry(List<OProperty<?>> props, List<OLink> links) {
    	switch (type) {
			case ATOM:
		        DataServicesAtomEntry dsae = new DataServicesAtomEntry();
		        dsae.contentType = dsae.getType();
		        dsae.properties = props;
		        dsae.links = links;
		        return (E)dsae;

			case JSON:
				JsonEntry je = new JsonEntry(); 
				je.properties = props;
				je.links = links;
				return (E)je;
		}
    	
    	return (E)null;
    }

    private ClientResponse doRequest(FormatType reqType, ODataClientRequest<E> request, Integer... expectedResponseStatus) {

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
        b = b.accept(reqType.getMediaTypes());

        for(String header : request.getHeaders().keySet()) {
            b.header(header, request.getHeaders().get(header));
        }

        if (ODataConsumer.dump.requestHeaders())
            log(request.getMethod() + " " + webResource.toString());

        // request body
        if (request.getEntry() != null) {

            E entry = request.getEntry();
            StringWriter sw = new StringWriter();
            FormatWriterFactory
            	.getFormatWriter(entryClass, null, type.toString(), null)
            	.write(null, sw, entry);

            String entity = sw.toString();
            if (ODataConsumer.dump.requestBody())
                log(entity);
            
            b.entity(entity, entry.getType());
        }

        // execute request
        ClientResponse response = b.method(request.getMethod(), ClientResponse.class);

        if (ODataConsumer.dump.responseHeaders())
            dumpHeaders(response);
        int status = response.getStatus();
        for(int expStatus : expectedResponseStatus) {
            if (status == expStatus) {
                return response;
            }
        }
        throw new RuntimeException(String.format("Expected status %s, found %s:", Enumerable.create(expectedResponseStatus).join(" or "), status) + "\n" + response.getEntity(String.class));

    }
    
    private F parseFeed(ClientResponse response)  {

        if (ODataConsumer.dump.responseBody()) {
            String textEntity = response.getEntity(String.class);
            log(textEntity);
            return FormatParserFactory.getParser(feedClass, type).parse(new BOMWorkaroundReader(new StringReader(textEntity)));
        }
        
        InputStream textEntity = response.getEntityInputStream();
        try {
            return FormatParserFactory.getParser(feedClass, type).parse(new BOMWorkaroundReader(new InputStreamReader(textEntity,"UTF-8")));
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    
    private XMLEventReader2 doXmlRequest(ClientResponse response)  {

        if (ODataConsumer.dump.responseBody()) {
            String textEntity = response.getEntity(String.class);
            log(textEntity);
            return InternalUtil.newXMLEventReader(new BOMWorkaroundReader(new StringReader(textEntity)));
        }
        
        InputStream textEntity = response.getEntityInputStream();
        try {
            return InternalUtil.newXMLEventReader(new BOMWorkaroundReader(new InputStreamReader(textEntity,"UTF-8")));
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
    
    Enumerable<E> getEntries(final ODataClientRequest<E> request) {

        return Enumerable.createFromIterator(new Func<Iterator<E>>() {
            public Iterator<E> apply() {
                return new EntryIterator(ODataClient.this, request);
            }
        });

    }
    
    private class EntryIterator extends ReadOnlyIterator<E> {

    	private ODataClient<F, E> client;
        private ODataClientRequest<E> request;
        private Feed<E> feed;
        private Iterator<E> feedEntries;
        private int feedEntryCount;

        public EntryIterator(ODataClient<F, E> client, ODataClientRequest<E> request) {
        	this.client = client;
            this.request = request;
        }

        @Override
        protected IterationResult<E> advance() throws Exception {

            if (feed == null) {
                feed = client.getEntities(request);
                feedEntries = feed.getEntries().iterator();
                feedEntryCount = 0;
            }

            if (feedEntries.hasNext()) {
                feedEntryCount++;
                return IterationResult.next(feedEntries.next());
            }

            // old-style paging: $page and $itemsPerPage
            if (request.getQueryParams().containsKey("$page") && request.getQueryParams().containsKey("$itemsPerPage")) {

                if (feedEntryCount == 0)
                    return IterationResult.done();

                int page = Integer.parseInt(request.getQueryParams().get("$page"));
                // int itemsPerPage = Integer.parseInt(request.getQueryParams().get("$itemsPerPage"));

                request = request.queryParam("$page", Integer.toString(page + 1));

            }
            // new-style paging: $skiptoken
            else {

                if (feed.getNext() == null)
                    return IterationResult.done();

                int skipTokenIndex = feed.getNext().indexOf("$skiptoken=");
                if( skipTokenIndex > -1) {
                    String skiptoken = feed.getNext().substring(skipTokenIndex + "$skiptoken=".length());
                    //	decode the skiptoken first since it gets encoded as a query param
                    skiptoken = URLDecoder.decode(skiptoken, "UTF-8");
                    request = request.queryParam("$skiptoken", skiptoken);
                } else if (feed.getNext().toLowerCase().startsWith("http")){
                    request = ODataClientRequest.get(feed.getNext());
                } else {
                    throw new UnsupportedOperationException();
                }
               
            }

            feed = null;

            return advance(); // TODO stackoverflow possible here
        }

    }


}
