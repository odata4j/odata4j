package org.odata4j.core;

import org.odata4j.consumer.ODataClientRequest;

import com.sun.jersey.api.client.config.ClientConfig;

/**
 * Extension-point for modifying client http requests.
 * <p>The {@link OClientBehaviors} static factory class can be used to create built-in <code>OClientBehavior</code> instances.</p>
 */
public interface OClientBehavior {

    /**
     * Transforms the current http request.
     * 
     * @param request  the current http request
     * @return the modified http request
     */
    public abstract ODataClientRequest transform(ODataClientRequest request);
    
    /**
     * Allows for modification of the jersey client api configuration.
     * 
     * @param clientConfig  the jersey client api configuration
     */
    public abstract void modify(ClientConfig clientConfig);
}
