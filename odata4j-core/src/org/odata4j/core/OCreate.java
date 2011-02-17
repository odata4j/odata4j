package org.odata4j.core;

public interface OCreate<T> {

    public abstract OCreate<T> properties(OProperty<?>... props);
    public abstract OCreate<T> addToRelation(OEntity parent, String navProperty);
	public abstract OCreate<T> link(String navProperty, OEntity target);
	public abstract OCreate<T> inline(String navProperty, OEntity... entities);

	/**
	 * Sends the create request to the OData service and returns the newly 
	 * created entity.
	 * 
	 * @return newly created entity
	 */
    public abstract T execute();
    
    /**
     * Gives the locally build entity and does not send the create request
     * to the service. The result of this method can be used to create
     * entities to inline them in a create request.
     * 
     * @return new locally build entity
     * @see #inline(String, OEntity...)
     */
    public abstract T get();

}
