package org.odata4j.core;

import org.core4j.Enumerable;

public interface OQuery<T> {

    public abstract Enumerable<T> execute();

    public abstract OQuery<T> top(int top);

    public abstract OQuery<T> skip(int skip);

    public abstract OQuery<T> orderBy(String orderBy);

    public abstract OQuery<T> filter(String filter);

    public abstract OQuery<T> select(String select);

    public abstract OQuery<T> nav(Object keyValue, String navProperty);
    
    public abstract OQuery<T> nav(OEntityKey key, String navProperty);

    public abstract OQuery<T> custom(String name, String value);
    
    public abstract OQuery<T> expand(String expand);
}
