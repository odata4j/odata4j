package org.odata4j.core;

import org.core4j.Enumerable;

public interface OQueryRequest<T> extends Iterable<T> {

    public abstract Enumerable<T> execute();

    public abstract OQueryRequest<T> top(int top);

    public abstract OQueryRequest<T> skip(int skip);

    public abstract OQueryRequest<T> orderBy(String orderBy);

    public abstract OQueryRequest<T> filter(String filter);

    public abstract OQueryRequest<T> select(String select);

    public abstract OQueryRequest<T> nav(Object keyValue, String navProperty);
    
    public abstract OQueryRequest<T> nav(OEntityKey key, String navProperty);

    public abstract OQueryRequest<T> custom(String name, String value);
    
    public abstract OQueryRequest<T> expand(String expand);
}
