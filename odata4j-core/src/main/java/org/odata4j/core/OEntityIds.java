package org.odata4j.core;


// TODO(0.5) javadoc
public class OEntityIds {

  private OEntityIds() {}

  public static OEntityId create(String entitySetName, Object... entityKeyValues) {
    return create(entitySetName, OEntityKey.create(entityKeyValues));
  }
  public static OEntityId create(String entitySetName, OEntityKey entityKey) {
    if (entitySetName == null)
      throw new NullPointerException("Must provide entity-set name");
    if (entityKey == null)
      throw new NullPointerException("Must provide entity-key");

    return new OEntityIdImpl(entitySetName, entityKey);
  }

  public static OEntityId parse(String entityId) {
    if (entityId == null)
      throw new NullPointerException("Must provide entity-id");

    int indexOfParen = entityId.indexOf('(');
    if (indexOfParen == -1)
      throw new IllegalArgumentException("Invalid entity-id: " + entityId);

    String entitySetName = entityId.substring(0, indexOfParen);
    OEntityKey entityKey = OEntityKey.parse(entityId.substring(indexOfParen));
    return create(entitySetName, entityKey);
  }

  public static OEntityId parse(String serviceRootUri, String uri) {
    if (serviceRootUri == null)
      throw new NullPointerException("Must provide service-root-uri");
    if (uri == null)
      throw new NullPointerException("Must provide uri");

    String entityId = uri;
    if (entityId.toLowerCase().startsWith(serviceRootUri.toLowerCase()))
      entityId = entityId.substring(serviceRootUri.length());
    if (entityId.startsWith("/"))
      entityId = entityId.substring(1);

    return parse(entityId);
  }
  
  public static String toKeyString(OEntityId entity) {
    if (entity == null)
      return null;
    return entity.getEntitySetName() + entity.getEntityKey().toKeyString();
  }
  
  public static boolean equals(OEntityId lhs, OEntityId rhs) {
    if (lhs == null)
      return rhs == null;
    return toKeyString(lhs).equals(toKeyString(rhs));
  }

  private static class OEntityIdImpl implements OEntityId {

    private final String entitySetName;
    private final OEntityKey entityKey;

    public OEntityIdImpl(String entitySetName, OEntityKey entityKey) {
      this.entitySetName = entitySetName;
      this.entityKey = entityKey;
    }

    @Override
    public String getEntitySetName() {
      return entitySetName;
    }

    @Override
    public OEntityKey getEntityKey() {
      return entityKey;
    }

    @Override
    public String toString() {
      return String.format("OEntityId[%s%s]", entitySetName, entityKey.toKeyString());
    }
  }

}
