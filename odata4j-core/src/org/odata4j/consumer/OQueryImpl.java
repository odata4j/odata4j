package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OQuery;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.internal.EntitySegment;
import org.odata4j.internal.FeedCustomizationMapping;

public class OQueryImpl<T, F extends Feed<E>, E extends Entry> implements OQuery<T> {

    private final ODataClient<F, E> client;
    private final Class<T> entityType;
    private final String serviceRootUri;
    private final EdmDataServices ees;
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

    public OQueryImpl(ODataClient<F, E> client, Class<T> entityType, String serviceRootUri, EdmDataServices ees, String entitySetName, FeedCustomizationMapping fcMapping) {
        this.client = client;
        this.entityType = entityType;
        this.serviceRootUri = serviceRootUri;
        this.ees = ees;
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
        segments.add(new EntitySegment(lastSegment, key));
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

        ODataClientRequest<E> request = ODataClientRequest.get(serviceRootUri + path);

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
        
    	final FormatParser<E> parser = FormatParserFactory.getParser(client.entryClass, client.type);        
        Enumerable<E> entries = client.getEntries(request);        
        return entries.select(new Func1<E, T>() {
            public T apply(E input) {
            	return parser.toOEntity(input, entityType, ees, ees.getEdmEntitySet(lastSegment), fcMapping);
            }
        }).cast(entityType);
    }



}
