package org.odata4j.core;

public interface OCreate<T> {

    public abstract OCreate<T> properties(OProperty<?>... props);
    public abstract OCreate<T> properties(Iterable<OProperty<?>> props);

    public abstract T execute();

}
