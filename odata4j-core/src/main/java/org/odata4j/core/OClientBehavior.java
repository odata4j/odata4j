package org.odata4j.core;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.format.Entry;

import com.sun.jersey.api.client.config.ClientConfig;

public interface OClientBehavior {

    public abstract <E extends Entry> ODataClientRequest transform(ODataClientRequest request);
    public abstract void modify(ClientConfig clientConfig);
}
