package org.odata4j.core;

import java.util.List;

import org.odata4j.edm.EdmEntitySet;


/**
 * An immutable OData entity instance, consisting of:
 * <ul>
 *   <li>An identity: an entity-set and a unique entity-key within that set</li>
 *   <li>Properties: typed, named values</li>
 *   <li>Links: references to other entities</li>
 * <ul>
 */
public interface OEntity { 
	
    /**
     * Gets the entity-set for this instance.
     * 
     * @return the entity-set
     */
    public abstract EdmEntitySet getEntitySet();
    
    /**
     * Gets the entity-key for this instance.
     * 
     * @return the entity-key
     */
    public abstract OEntityKey getEntityKey(); 
    
    /**
     * Get all properties of this instance.
     * 
     * @return the properties
     */
    public abstract List<OProperty<?>> getProperties();
    
    /**
     * Get a property by name.
     * 
     * @param propName  the property name
     * @return the property
     */
    public abstract OProperty<?> getProperty(String propName);
    
    /**
     * Get a property by name as a strongly-typed OProperty.
     * 
     * @param <T>  the java-type of the property
     * @param propName  the property name
     * @param propClass  the java-type of the property
     * @return the property
     */
    public abstract <T> OProperty<T> getProperty(String propName, Class<T> propClass);
    
    /**
     * Get all links of this instance.
     * 
     * @return the links
     */
    public abstract List<OLink> getLinks();
    
    /**
     * Get a link with a given name and link-type.
     * 
     * @param <T>  the link-type as a java-type
     * @param title  the link title
     * @param linkClass  the link-type as a java-type
     * @return the link strongly-typed as the java-type
     */
    public abstract <T extends OLink> T getLink(String title, Class<T> linkClass);
}
