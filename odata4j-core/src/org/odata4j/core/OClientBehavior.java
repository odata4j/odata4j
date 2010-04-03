package org.odata4j.core;

import org.odata4j.consumer.ODataClientRequest;

public interface OClientBehavior {

    public abstract ODataClientRequest transform(ODataClientRequest request);
}
