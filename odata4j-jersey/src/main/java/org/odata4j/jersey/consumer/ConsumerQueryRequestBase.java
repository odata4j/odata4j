package org.odata4j.jersey.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OQueryRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.internal.EntitySegment;

public abstract class ConsumerQueryRequestBase<T> implements OQueryRequest<T> {

  private final ODataJerseyClient client;
  private final String serviceRootUri;
  private final EdmDataServices metadata;

  private Integer top;
  private Integer skip;
  private String orderBy;
  private String filter;
  private String select;
  private String lastSegment;
  private String expand;

  private final List<EntitySegment> segments = new ArrayList<EntitySegment>();
  private final Map<String, String> customs = new HashMap<String, String>();

  public ConsumerQueryRequestBase(ODataJerseyClient client, String serviceRootUri, EdmDataServices metadata, String lastSegment) {
    this.client = client;
    this.serviceRootUri = serviceRootUri;
    this.metadata = metadata;
    this.lastSegment = lastSegment;
  }

  protected ODataJerseyClient getClient() {
    return client;
  }

  protected String getLastSegment() {
    return lastSegment;
  }

  protected String getServiceRootUri() {
    return serviceRootUri;
  }

  protected EdmDataServices getMetadata() {
    return metadata;
  }

  protected ODataJerseyClientRequest buildRequest(Func1<String, String> pathModification) {
    String path = Enumerable.create(segments).join("/");
    path += (path.length() == 0 ? "" : "/") + lastSegment;
    if (pathModification != null)
      path = pathModification.apply(path);

    ODataJerseyClientRequest request = ODataJerseyClientRequest.get(serviceRootUri + path);

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
    for (String name : customs.keySet()) {
      request = request.queryParam(name, customs.get(name));
    }
    if (expand != null) {
      request = request.queryParam("$expand", expand);
    }

    return request;
  }

  @Override
  public OQueryRequest<T> top(int top) {
    this.top = top;
    return this;
  }

  @Override
  public OQueryRequest<T> skip(int skip) {
    this.skip = skip;
    return this;
  }

  @Override
  public OQueryRequest<T> orderBy(String orderBy) {
    this.orderBy = orderBy;
    return this;
  }

  @Override
  public OQueryRequest<T> filter(String filter) {
    this.filter = filter;
    return this;
  }

  @Override
  public OQueryRequest<T> select(String select) {
    this.select = select;
    return this;
  }

  @Override
  public OQueryRequest<T> expand(String expand) {
    this.expand = expand;
    return this;
  }

  @Override
  public OQueryRequest<T> nav(Object keyValue, String navProperty) {
    return nav(OEntityKey.create(keyValue), navProperty);
  }

  @Override
  public OQueryRequest<T> nav(OEntityKey key, String navProperty) {
    segments.add(new EntitySegment(lastSegment, key));
    lastSegment = navProperty;
    return this;
  }

  @Override
  public OQueryRequest<T> custom(String name, String value) {
    customs.put(name, value);
    return this;
  }

  @Override
  public Iterator<T> iterator() {
    return execute().iterator();
  }

}
