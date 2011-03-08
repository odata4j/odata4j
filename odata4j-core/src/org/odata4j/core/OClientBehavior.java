package org.odata4j.core;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.format.Entry;

public interface OClientBehavior {

    public abstract <E extends Entry> ODataClientRequest<E> transform(ODataClientRequest<E> request);
}
