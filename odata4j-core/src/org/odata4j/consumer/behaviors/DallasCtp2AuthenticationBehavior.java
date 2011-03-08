package org.odata4j.consumer.behaviors;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.OClientBehavior;
import org.odata4j.format.Entry;

import com.sun.jersey.api.client.config.ClientConfig;

public class DallasCtp2AuthenticationBehavior implements OClientBehavior {

    private final String accountKey;
    private final String uniqueUserId;

    public DallasCtp2AuthenticationBehavior(String accountKey, String uniqueUserId) {
        this.accountKey = accountKey;
        this.uniqueUserId = uniqueUserId;
    }
    
    @Override
    public void modify(ClientConfig clientConfig) { }

    @Override
    public <E extends Entry> ODataClientRequest<E> transform(ODataClientRequest<E> request) {
        return request.header("$uniqueUserID", uniqueUserId).header("$accountKey", accountKey).header("DataServiceVersion", "2.0").queryParam("$format", "atom10");

    }

}