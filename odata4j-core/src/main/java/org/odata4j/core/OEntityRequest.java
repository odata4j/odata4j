package org.odata4j.core;

public interface OEntityRequest<T> {

    T execute();

    OEntityRequest<T> nav(String navProperty, OEntityKey key);
    OEntityRequest<T> nav(String navProperty);
}
