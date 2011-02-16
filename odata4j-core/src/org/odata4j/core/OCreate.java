package org.odata4j.core;

public interface OCreate<T> {

    public abstract OCreate<T> properties(OProperty<?>... props);
    public abstract OCreate<T> addToRelation(OEntity parent, String navProperty);
	public abstract OCreate<T> link(String navProperty, OEntity target);

    public abstract T execute();

}
