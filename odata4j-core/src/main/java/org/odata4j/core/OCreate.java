package org.odata4j.core;

/**
 * A consumer-side create request builder.  Call {@link #execute()} to issue the request.
 *
 * @param <T>  the entity representation as a java type
 */
public interface OCreate<T> {

	
    /**
     * Set properties on the new entity.
     * 
     * @param props  the properties
     * @return the create request builder 
     */
    public abstract OCreate<T> properties(OProperty<?>... props);
    
    public abstract OCreate<T> properties(Iterable<OProperty<?>> props);
    
    /**
     * Use a related parent entity's relationship collection to define an implicit link. 
     * <p>e.g. create a new Product entity using /Categories(10)/Products instead of /Products</p>
     * 
     * @param parent  the parent entity
     * @param navProperty  the parent entity's relationship collection navigation property
     * @return the create request builder
     * 
     * @see #link(String, OEntity)
     * @see <a href="http://www.odata.org/developers/protocols/operations#CreatingnewEntries">http://www.odata.org/developers/protocols/operations#CreatingnewEntries</a>
     */
    public abstract OCreate<T> addToRelation(OEntity parent, String navProperty);
    
    
	/**
	 * Define an explicit link to another related entity.
	 * 
	 * @param navProperty  the new entity's relationship navigation property
	 * @param target  the link target entity
	 * @return the create request builder
	 * 
	 * @see #addToRelation(OEntity, String)
     * @see <a href="http://www.odata.org/developers/protocols/operations#CreatingnewEntries">http://www.odata.org/developers/protocols/operations#CreatingnewEntries</a>
	 */
	public abstract OCreate<T> link(String navProperty, OEntity target);
	
	
	/**
	 * Create related entities inline as part of a single request.
	 *  
	 * @param navProperty  the new entity's relationship navigation property
	 * @param entities  related entities, returned by {@link #get()}
	 * @return  the create request builder
	 * 
	 * @see #get()
	 */
	public abstract OCreate<T> inline(String navProperty, OEntity... entities);

	/**
	 * Sends the create request to the OData service and returns the newly 
	 * created entity.
	 * 
	 * @return newly created entity
	 */
    public abstract T execute();
    
    /**
     * Gives the locally built entity and does not send the create request
     * to the service. The result of this method can be used to create
     * entities to inline them in a create request.
     * 
     * @return new locally built entity
     * @see #inline(String, OEntity...)
     */
    public abstract T get();

}
