package org.odata4j.consumer;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Func1;
import org.core4j.ReadOnlyIterator;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OQuery;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.Settings;
import org.odata4j.internal.EntitySegment;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

import com.sun.jersey.api.client.ClientResponse;

public class OQueryImpl<T> implements OQuery<T> {

    private final ODataClient client;
    private final Class<T> entityType;
    private final String serviceRootUri;
    private final EdmDataServices metadata;
    private final List<EntitySegment> segments = new ArrayList<EntitySegment>();
    private final Map<String, String> customs = new HashMap<String, String>();

    private Integer top;
    private Integer skip;
    private String orderBy;
    private String filter;
    private String select;
    private String lastSegment;
    private String expand;
    
    private final FeedCustomizationMapping fcMapping;

    public OQueryImpl(ODataClient client, Class<T> entityType, String serviceRootUri, EdmDataServices metadata, String entitySetName, FeedCustomizationMapping fcMapping) {
        this.client = client;
        this.entityType = entityType;
        this.serviceRootUri = serviceRootUri;
        this.metadata = metadata;
        this.lastSegment = entitySetName;
        
        this.fcMapping = fcMapping;
    }

    @Override
    public OQuery<T> top(int top) {
        this.top = top;
        return this;
    }

    @Override
    public OQuery<T> skip(int skip) {
        this.skip = skip;
        return this;
    }

    @Override
    public OQuery<T> orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    @Override
    public OQuery<T> filter(String filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public OQuery<T> select(String select) {
        this.select = select;
        return this;
    }

    @Override
    public OQuery<T> expand(String expand) {
        this.expand = expand;
        return this;
    }

    @Override
    public OQuery<T> nav(Object key, String navProperty) {
        return nav(new Object[] { key }, navProperty);
    }

    @Override
    public OQuery<T> nav(Object[] key, String navProperty) {
        segments.add(EntitySegment.temp(lastSegment, key));
        lastSegment = navProperty;
        return this;
    }

    @Override
    public OQuery<T> custom(String name, String value) {
        customs.put(name, value);
        return this;
    }

    @Override
    public Enumerable<T> execute() {

        String path = Enumerable.create(segments).join("/");
        path += (path.length() == 0 ? "" : "/") + lastSegment;

        ODataClientRequest request = ODataClientRequest.get(serviceRootUri + path);

        if (top != null) {
            request = request.queryParam("$top", Integer.toString(top));
        }
        if (skip != null) {
            request = request.queryParam("$skip", Integer.toString(skip));
        }
        if (orderBy != null) {
            request = request.queryParam("$orderby", orderBy);
        }
        if (filter != null) {
            request = request.queryParam("$filter", filter);
        }
        if (select != null) {
            request = request.queryParam("$select", select);
        }
        for(String name : customs.keySet()) {
            request = request.queryParam(name, customs.get(name));
        }
        if (expand != null) {
            request = request.queryParam("$expand", expand);
        }
        
        Enumerable<Entry> entries = getEntries(request);    
        
        return entries.select(new Func1<Entry, T>() {
            public T apply(Entry input) {
            	return InternalUtil.toEntity(entityType, input.getEntity());
            }
        }).cast(entityType);
    }
    
    Enumerable<Entry> getEntries(final ODataClientRequest request) {

        return Enumerable.createFromIterator(new Func<Iterator<Entry>>() {
            public Iterator<Entry> apply() {
                return new EntryIterator(client, request);
            }
        });

    }
    
    private class EntryIterator extends ReadOnlyIterator<Entry> {

    	private ODataClient client;
        private ODataClientRequest request;
        private FormatParser<Feed> parser;
        private Feed feed;
        private Iterator<Entry> feedEntries;
        private int feedEntryCount;

        public EntryIterator(ODataClient client, ODataClientRequest request) {
        	this.client = client;
            this.request = request;
        }

        @Override
        protected IterationResult<Entry> advance() throws Exception {

            if (feed == null) {
            	ClientResponse response = client.getEntities(request);
            	
            	InternalUtil.getDataServiceVersion(response.getHeaders()
            			.getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));
            	
                parser = FormatParserFactory.getParser(Feed.class, client.type, 
                		new Settings(ODataVersion.V2, metadata, lastSegment, null, fcMapping)); 

            	feed = parser.parse(client.getFeedReader(response));
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
