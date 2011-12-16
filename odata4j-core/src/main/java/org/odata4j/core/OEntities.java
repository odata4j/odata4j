package org.odata4j.core;

import java.util.Collections;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmType;

/**
 * A static factory to create immutable {@link OEntity} instances.
 */
public class OEntities {

  private OEntities() {}

  /**
   * Creates a new entity.
   *
   * @param entitySet  the entity-set
   * @param entityKey  the entity-key
   * @param properties  the entity properties, if any
   * @param links  the entity links, if any
   * @return the new entity
   */
  public static OEntity create(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> properties, List<OLink> links) {
    return new OEntityImpl(entitySet, entityKey, true, properties, links);
  }
  
  /**
   * Creates a new entity.
   *
   * @param entitySet  the entity-set
   * @param entityType the entity type
   * @param entityKey  the entity-key
   * @param properties  the entity properties, if any
   * @param links  the entity links, if any
   * @return the new entity
   */
  public static OEntity create(EdmEntitySet entitySet, EdmEntityType entityType, OEntityKey entityKey, List<OProperty<?>> properties, List<OLink> links) {
    return new OEntityImpl(entitySet, entityType, entityKey, true, properties, links);
  }

  /**
   * Creates a new request-entity.
   * <p>A request-entity is a new entity that has not yet been created in an OData service, and therefore allowed to not have an entity-key.</p>
   *
   * @param entitySet  the entity-set
   * @param properties  the entity properties, if any
   * @param links  the entity links, if any
   * @return the new entity
   */
  public static OEntity createRequest(EdmEntitySet entitySet, List<OProperty<?>> properties, List<OLink> links) {
    return new OEntityImpl(entitySet, null, false, properties, links);
  }

  /**
   * Creates a new entity with additional Atom information.
   *
   * @param entitySet  the entity-set
   * @param entityKey  the entity-key
   * @param properties  the entity properties, if any
   * @param links  the entity links, if any
   * @param title  the Atom title
   * @param categoryTerm  the Atom category term
   * @return the new entity
   */
  public static OEntity create(EdmEntitySet entitySet, EdmEntityType entityType, OEntityKey entityKey, List<OProperty<?>> properties, List<OLink> links, String title, String categoryTerm) {
    return new OEntityAtomImpl(entitySet, entityType, entityKey, true, properties, links, title, categoryTerm);
  }

  /**
   * Creates a new request-entity with additional Atom information.
   * <p>A request-entity is a new entity that has not yet been created in an OData service, and therefore allowed to not have an entity-key.</p>
   *
   * @param entitySet  the entity-set
   * @param properties  the entity properties, if any
   * @param links  the entity links, if any
   * @param title  the Atom title
   * @param categoryTerm  the Atom category term
   * @return the new entity
   */
  public static OEntity createRequest(EdmEntitySet entitySet, List<OProperty<?>> properties, List<OLink> links, String title, String categoryTerm) {
    return new OEntityAtomImpl(entitySet, null, null, false, properties, links, title, categoryTerm);
  }

  private static class OEntityAtomImpl extends OEntityImpl implements AtomInfo {

    private final String title;
    private final String categoryTerm;

    public OEntityAtomImpl(EdmEntitySet entitySet, EdmEntityType entityType, OEntityKey entityKey, boolean entityKeyRequired, List<OProperty<?>> properties, List<OLink> links, String title, String categoryTerm) {
      super(entitySet, entityType, entityKey, entityKeyRequired, properties, links);
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
    private final EdmEntityType entityType;
    private final OEntityKey entityKey;
    private final List<OProperty<?>> properties;
    private final List<OLink> links;

    public OEntityImpl(EdmEntitySet entitySet, OEntityKey entityKey, boolean entityKeyRequired, List<OProperty<?>> properties, List<OLink> links) {
      this(entitySet, null, entityKey, entityKeyRequired, properties, links);
    }
    
    public OEntityImpl(EdmEntitySet entitySet, EdmEntityType entityType, OEntityKey entityKey, boolean entityKeyRequired, List<OProperty<?>> properties, List<OLink> links) {
      if (entitySet == null)
        throw new IllegalArgumentException("entitySet cannot be null");
      if (entityKeyRequired && entityKey == null)
        throw new IllegalArgumentException("entityKey cannot be null");

      this.entitySet = entitySet;
      this.entityType = entityType;
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
    public EdmEntityType getEntityType() {
      return null == entityType 
              ? (null == entitySet ? null : entitySet.getType())
              : entityType;
    }
    
    @Override
    public String getEntitySetName() {
      return entitySet.getName();
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
      for (OLink link : getLinks())
        if (link.getTitle().equals(title))
          return (T) link;
      throw new IllegalArgumentException("No link with title: " + title);
    }

    @Override
    public EdmType getType() {
      return this.entitySet.getType();
    }

  }

}
