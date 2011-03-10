package org.odata4j.core;

import java.util.Collections;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.edm.EdmEntitySet;

/** A factory class for creating Entity objects.
 * 
 * Entities are described by the schema-defined entity set to which they belong.  That definition is used
 * here to create an internal object of the "appropriate type".  In fact, all entity objects belong to the same
 * Java class.
 *
 */
public class OEntities {

	/** Create a new entity in a given set with the identified properties and links
	 * 
	 * @param entitySet the set into which the new entity will be "placed"; you still need to specifically add it to a result set
	 * @param properties the values of all the properties in this entity
	 * @param links the values of the links/associations to other entities from this entity
	 * @param id the id value of this entity
	 * @return a new entity which reflects this entity in the grand scheme of things
	 */
    public static OEntity create(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> properties, List<OLink> links) {
        return new OEntityImpl(entitySet, entityKey, properties, links);
    }

    public static OEntity create(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> properties, List<OLink> links, String title, String categoryTerm) {
        return new OEntityAtomImpl(entitySet, entityKey, properties, links, title, categoryTerm);
    }

    private static class OEntityAtomImpl extends OEntityImpl implements AtomInfo {

        private final String title;
        private final String categoryTerm;

        public OEntityAtomImpl(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> properties, List<OLink> links, String title, String categoryTerm) {
            super(entitySet, entityKey, properties, links);
            this.title = title;
            this.categoryTerm = categoryTerm;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getCategoryTerm() {
            return categoryTerm;
        }
    }

    private static class OEntityImpl implements OEntity {

    	private final EdmEntitySet entitySet;
    	private final OEntityKey entityKey;
        private final List<OProperty<?>> properties;
        private final List<OLink> links;

        public OEntityImpl(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> properties, List<OLink> links) {
        	if (entitySet==null) throw new IllegalArgumentException("entitySet cannot be null");
        	if (entityKey==null) throw new IllegalArgumentException("entityKey cannot be null");
        	this.entitySet = entitySet;
        	this.entityKey = entityKey;
            this.properties = Collections.unmodifiableList(properties);
            this.links = Collections.unmodifiableList(links);
        }

        @Override
        public String toString() {
            return "OEntity[" + Enumerable.create(getProperties()).join(",") + "]";
        }
        
		@Override
		public EdmEntitySet getEntitySet() {
			return entitySet;
		}
		
		@Override
		public OEntityKey getEntityKey() {
			return entityKey;
		}

        @Override
        public List<OProperty<?>> getProperties() {
            return properties;
        }

        @Override
        public OProperty<?> getProperty(String propName) {
            return Enumerable.create(properties).first(OPredicates.propertyNameEquals(propName));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> OProperty<T> getProperty(String propName, Class<T> propClass) {
            return (OProperty<T>) getProperty(propName);
        }
        
        
        @Override
        public List<OLink> getLinks() {
            return links;
        }
        
        @SuppressWarnings("unchecked")
		@Override
        public <T extends OLink> T getLink(String title, Class<T> linkClass) {
           for(OLink link : getLinks())
               if (link.getTitle().equals(title))
                   return (T)link;
           throw new IllegalArgumentException("No link with title: " + title);
        }

	

    }
}
