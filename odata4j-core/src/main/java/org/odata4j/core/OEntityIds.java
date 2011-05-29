package org.odata4j.core;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;

// TODO(0.5) javadoc
public class OEntityIds {

  private OEntityIds() {}
  
  public static OEntityId create(EdmEntitySet entitySet, OEntityKey entityKey) {
    if (entitySet == null)
      throw new NullPointerException("Must provide entity-set");
    if (entityKey == null)
      throw new NullPointerException("Must provide entity-key");
    
    return new OEntityIdImpl(entitySet, entityKey);
  }
  
  public static OEntityId create(EdmDataServices metadata, String entitySetName, OEntityKey entityKey) {
    if (metadata == null)
      throw new NullPointerException("Must provide metadata to lookup entity-set");
    if (entitySetName == null)
      throw new NullPointerException("Must provide entity-set name");
    
    EdmEntitySet entitySet = metadata.findEdmEntitySet(entitySetName);
    if (entitySet == null)
      throw new IllegalArgumentException("Entity-set not found: " + entitySetName);
    
    return create(entitySet, entityKey);
  }
  
  public static OEntityId parse(EdmDataServices metadata, String entityId) {
    if (entityId == null)
      throw new NullPointerException("Must provide entity-id");
    
    int indexOfParen = entityId.indexOf('(');
    if (indexOfParen == -1)
      throw new IllegalArgumentException("Invalid entity-id: " + entityId);
    
    String entitySetName = entityId.substring(0, indexOfParen);
    OEntityKey entityKey = OEntityKey.parse(entityId.substring(indexOfParen));
    return create(metadata, entitySetName, entityKey);
  }
  
  public static OEntityId parse(EdmDataServices metadata, String serviceRootUri, String uri) {
    if (serviceRootUri == null)
      throw new NullPointerException("Must provide service-root-uri");
    if (uri == null)
      throw new NullPointerException("Must provide uri");
    
    if (uri.toLowerCase().startsWith(serviceRootUri.toLowerCase()))
      uri = uri.substring(serviceRootUri.length());
    if (uri.startsWith("/"))
      uri = uri.substring(1);
    
    return parse(metadata, uri);
  }
  
  private static class OEntityIdImpl implements OEntityId {

    private final EdmEntitySet entitySet;
    private final OEntityKey entityKey;
    
    public OEntityIdImpl(EdmEntitySet entitySet, OEntityKey entityKey) {
      this.entitySet = entitySet;
      this.entityKey = entityKey;
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
    public String toString() {
      return String.format("OEntityId[%s%s]", entitySet.name, entityKey.toKeyString());
    }
  }
}
