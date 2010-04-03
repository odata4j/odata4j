package org.odata4j.core;

public interface OModify<T> {

    public abstract OModify<T> properties(OProperty<?>... props);

    public abstract boolean execute();

    public abstract OModify<T> nav(String navProperty, Object... key);

}
