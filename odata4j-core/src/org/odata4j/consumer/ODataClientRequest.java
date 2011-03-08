package org.odata4j.consumer;

import java.util.HashMap;
import java.util.Map;

import org.odata4j.format.Entry;

public class ODataClientRequest<E extends Entry> {

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final E entry;

    private ODataClientRequest(String method, String url, Map<String, String> headers, Map<String, String> queryParams, E entry) {
        this.method = method;
        this.url = url;
        this.headers = headers == null ? new HashMap<String, String>() : headers;
        this.queryParams = queryParams == null ? new HashMap<String, String>() : queryParams;
        this.entry = entry;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public E getEntry() {
        return entry;
    }

    public static <E extends Entry> ODataClientRequest<E> get(String url) {
        return new ODataClientRequest<E>("GET", url, null, null, null);
    }

    public static <E extends Entry> ODataClientRequest<E> post(String url, E entry) {
        return new ODataClientRequest<E>("POST", url, null, null, entry);
    }

    public static <E extends Entry> ODataClientRequest<E> put(String url, E entry) {
        return new ODataClientRequest<E>("PUT", url, null, null, entry);
    }

    public static <E extends Entry> ODataClientRequest<E> merge(String url, E entry) {
        return new ODataClientRequest<E>("MERGE", url, null, null, entry);
    }

    public static <E extends Entry> ODataClientRequest<E> delete(String url) {
        return new ODataClientRequest<E>("DELETE", url, null, null, null);
    }

    public ODataClientRequest<E> header(String name, String value) {
        headers.put(name, value);
        return new ODataClientRequest<E>(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest<E> queryParam(String name, String value) {
        queryParams.put(name, value);
        return new ODataClientRequest<E>(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest<E> url(String url) {
        return new ODataClientRequest<E>(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest<E> method(String method) {
        return new ODataClientRequest<E>(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest<E> entry(E entry) {
        return new ODataClientRequest<E>(method, url, headers, queryParams, entry);
    }

}
