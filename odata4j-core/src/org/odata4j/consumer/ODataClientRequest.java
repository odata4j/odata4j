package org.odata4j.consumer;

import java.util.HashMap;
import java.util.Map;

import org.odata4j.format.Entry;

public class ODataClientRequest {

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Entry entry;

    private ODataClientRequest(String method, String url, Map<String, String> headers, Map<String, String> queryParams, Entry entry) {
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
    
    public Entry getEntry() {
        return entry;
    }

    public static ODataClientRequest get(String url) {
        return new ODataClientRequest("GET", url, null, null, null);
    }

    public static ODataClientRequest post(String url, Entry entry) {
        return new ODataClientRequest("POST", url, null, null, entry);
    }

    public static ODataClientRequest put(String url, Entry entry) {
        return new ODataClientRequest("PUT", url, null, null, entry);
    }

    public static ODataClientRequest merge(String url, Entry entry) {
        return new ODataClientRequest("MERGE", url, null, null, entry);
    }

    public static ODataClientRequest delete(String url) {
        return new ODataClientRequest("DELETE", url, null, null, null);
    }

    public ODataClientRequest header(String name, String value) {
        headers.put(name, value);
        return new ODataClientRequest(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest queryParam(String name, String value) {
        queryParams.put(name, value);
        return new ODataClientRequest(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest url(String url) {
        return new ODataClientRequest(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest method(String method) {
        return new ODataClientRequest(method, url, headers, queryParams, entry);
    }

    public ODataClientRequest entry(Entry entry) {
        return new ODataClientRequest(method, url, headers, queryParams, entry);
    }

}
