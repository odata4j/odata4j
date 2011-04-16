package org.odata4j.core;

/**
 * A consumer-side modification request builder.  Call {@link #execute()} to issue the request.
 *
 * @param <T>  the entity representation as a java type
 */
public interface OModifyRequest<T> {

	 /**
     * Set properties on the new entity.
     * 
     * @param props  the properties
     * @return the modification request builder 
     */
    public abstract OModifyRequest<T> properties(OProperty<?>... props);
    
    /**
     * Set properties on the new entity.
     * 
     * @param props  the properties
     * @return the modification request builder 
     */
    public abstract OModifyRequest<T> properties(Iterable<OProperty<?>> props);
    
    /**
	 * Define an explicit link to another related entity.
	 * 
	 * @param navProperty  the entity's relationship navigation property
	 * @param target  the link target entity
	 * @return the modification request builder
	 */
	public abstract OModifyRequest<T> link(String navProperty, OEntity target);
	
	/**
	 * Define an explicit link to another related entity.
	 * 
	 * @param navProperty  the entity's relationship navigation property
	 * @param targetKey  the key of the link target entity
	 * @return the modification request builder
	 */
	public abstract OModifyRequest<T> link(String navProperty, OEntityKey targetKey);
    
    
    /**
	 * Sends the modification request to the OData service and returns success or failure.
	 * 
	 * @return success or failure
	 */
    public abstract boolean execute();

    
    
    /**
     * Select a new modification entity by navigating to a referenced entity in a child collection.
     * 
     * @param navProperty  the child collection
     * @param key  the referenced entity's key
     * @return the modification request builder
     */
    public abstract OModifyRequest<T> nav(String navProperty, OEntityKey key);

}
