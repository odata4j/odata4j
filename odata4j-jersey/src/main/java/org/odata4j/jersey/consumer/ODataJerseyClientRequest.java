package org.odata4j.jersey.consumer;

import java.util.Map;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.format.Entry;
import org.odata4j.format.SingleLink;

/**
 * Generic OData http request builder.  Only interesting for developers of custom {@link OClientBehavior} implementations.
 */
public class ODataJerseyClientRequest extends ODataClientRequest {

  private ODataJerseyClientRequest(String method, String url, Map<String, String> headers, Map<String, String> queryParams, Object payload) {
    super(method, url, headers, queryParams, payload);
  }

  /**
  * Creates a new GET request.
  * 
  * @param url  the request url
  * @return a new request builder
  */
  public static ODataJerseyClientRequest get(String url) {
    return new ODataJerseyClientRequest("GET", url, null, null, null);
  }

  /**
   * Creates a new POST request.
   * 
   * @param url  the request url
   * @param entry  the normalized OData payload
   * @return a new request builder
   */
  public static ODataJerseyClientRequest post(String url, Entry entry) {
    return new ODataJerseyClientRequest("POST", url, null, null, entry);
  }

  /**
   * Creates a new POST request.
   * 
   * @param url  the request url
   * @param link  the link
   * @return a new request builder
   */
  public static ODataJerseyClientRequest post(String url, SingleLink link) {
    return new ODataJerseyClientRequest("POST", url, null, null, link);
  }

  /**
   * Creates a new PUT request.
   * 
   * @param url  the request url
   * @param entry  the normalized OData payload
   * @return a new request builder
   */
  public static ODataJerseyClientRequest put(String url, Entry entry) {
    return new ODataJerseyClientRequest("PUT", url, null, null, entry);
  }

  /**
   * Creates a new PUT request.
   * 
   * @param url  the request url
   * @param link  the link
   * @return a new request builder
   */
  public static ODataJerseyClientRequest put(String url, SingleLink link) {
    return new ODataJerseyClientRequest("PUT", url, null, null, link);
  }

  /**
   * Creates a new MERGE request.
   * 
   * @param url  the request url
   * @param entry  the normalized OData payload
   * @return a new request builder
   */
  public static ODataJerseyClientRequest merge(String url, Entry entry) {
    return new ODataJerseyClientRequest("MERGE", url, null, null, entry);
  }

  /**
   * Creates a new MERGE request.
   * 
   * @param url  the request url
   * @param link  the link
   * @return a new request builder
   */
  public static ODataJerseyClientRequest merge(String url, SingleLink link) {
    return new ODataJerseyClientRequest("MERGE", url, null, null, link);
  }

  /**
   * Creates a new DELETE request.
   * 
   * @param url  the request url
   * @return a new request builder
   */
  public static ODataJerseyClientRequest delete(String url) {
    return new ODataJerseyClientRequest("DELETE", url, null, null, null);
  }

  /**
   * Sets an http request header.
   * 
   * @param name  the header name
   * @param value  the header value
   * @return the request builder
   */
  public ODataJerseyClientRequest header(String name, String value) {
    this.getHeaders().put(name, value);
    return new ODataJerseyClientRequest(this.getMethod(), this.getUrl(), this.getHeaders(), this.getQueryParams(), this.getPayload());
  }

  /**
   * Sets a request query parameter.
   * 
   * @param name  the query parameter name
   * @param value  the query parameter value
   * @return the request builder
   */
  public ODataJerseyClientRequest queryParam(String name, String value) {
    this.getQueryParams().put(name, value);
    return new ODataJerseyClientRequest(this.getMethod(), this.getUrl(), this.getHeaders(), this.getQueryParams(), this.getPayload());
  }

  /**
   * Sets the request url.
   * 
   * @param url  the request url
   * @return the request builder
   */
  public ODataJerseyClientRequest url(String url) {
    return new ODataJerseyClientRequest(this.getMethod(), url, this.getHeaders(), this.getQueryParams(), this.getPayload());
  }

  /**
   * Sets the http request method.
   * 
   * @param method  the method
   * @return the request builder
   */
  public ODataJerseyClientRequest method(String method) {
    return new ODataJerseyClientRequest(method, this.getUrl(), this.getHeaders(), this.getQueryParams(), this.getPayload());
  }

  /**
   * Sets the normalized OData payload.
   * 
   * @param entry  the entry payload
   * @return the request builder
   */
  public ODataJerseyClientRequest entryPayload(Entry entry) {
    return new ODataJerseyClientRequest(this.getMethod(), this.getUrl(), this.getHeaders(), this.getQueryParams(), entry);
  }

  /**
   * Sets the normalized OData payload.
   * 
   * @param link  the link payload
   * @return the request builder
   */
  public ODataJerseyClientRequest linkPayload(SingleLink link) {
    return new ODataJerseyClientRequest(this.getMethod(), this.getUrl(), this.getHeaders(), this.getQueryParams(), link);
  }

}
