package org.odata4j.edm;

import java.util.Map;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.ImmutableList;
import org.odata4j.core.OFuncs;

/**
 * A type in the EDM type system.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/bb399591.aspx">[msdn] Types (Metadata)</a>
 */
public abstract class EdmType extends EdmItem {

  private static class LazyInit {

    private static final Map<String, EdmSimpleType<?>> POOL = Enumerable.create(EdmSimpleType.ALL).toMap(
        new Func1<EdmSimpleType<?>, String>() {
          @Override
          public String apply(EdmSimpleType<?> t) {
            return t.getFullyQualifiedTypeName();
          }
        });
  }

  private final String fullyQualifiedTypeName;

  protected EdmType(String fullyQualifiedTypeName) {
    this(fullyQualifiedTypeName, null, null);
  }

  protected EdmType(String fullyQualifiedTypeName, EdmDocumentation documentation, ImmutableList<EdmAnnotation<?>> annotations) {
    super(documentation, annotations);
    this.fullyQualifiedTypeName = fullyQualifiedTypeName;
  }

  /**
   * Gets an edm-type for a given type name.
   *
   * @param fullyQualifiedTypeName  the fully-qualified type name
   * @return the edm-type
   */
  public static EdmSimpleType<?> getSimple(String fullyQualifiedTypeName) {
    if (fullyQualifiedTypeName == null)
      return null;
    return LazyInit.POOL.get(fullyQualifiedTypeName);
  }

  /**
   * Gets the fully-qualified type name for this edm-type.
   */
  public String getFullyQualifiedTypeName() {
    return this.fullyQualifiedTypeName;
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", getClass().getSimpleName(), getFullyQualifiedTypeName());
  }

  @Override
  public int hashCode() {
    return this.fullyQualifiedTypeName.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof EdmType && ((EdmType) other).fullyQualifiedTypeName.equals(this.fullyQualifiedTypeName);
  }

  public abstract boolean isSimple();

  public abstract static class Builder<T, TBuilder> extends EdmItem.Builder<T, TBuilder> {

    private EdmType builtType = null;
    
    public Builder() {}
    
    public Builder(EdmType type) {
      this.builtType = type;
    }
    
    public abstract EdmType build();
    
    protected final EdmType _build() {
      if (null == builtType) {
        builtType = buildImpl();
      }  
      return builtType;
    }
    
    protected abstract EdmType buildImpl();

  }
  
  public static DeferredBuilder<?, ?> newDeferredBuilder(String fqTypeName, EdmDataServices.Builder dataServices) {
    return new DeferredBuilder(fqTypeName, dataServices);
  }
  
  public static class DeferredBuilder<T, TBuilder> extends Builder<T, TBuilder> {

    private final String fqTypeName;
    private final EdmDataServices.Builder dataServices;
    
    private DeferredBuilder(String fqTypeName, EdmDataServices.Builder dataServices) {
      this.fqTypeName = fqTypeName;
      this.dataServices = dataServices;
    }
    
    @Override
    public EdmType build() {
      return _build();
    }

    @Override
    protected EdmType buildImpl() {
      Builder<?, ?> b = dataServices.resolveType(fqTypeName);
      if (null == b) {
        throw new RuntimeException("Edm-type not found: " + fqTypeName);
      }
      return b.build();
    }

    @Override
    Object newBuilder(Object item, BuilderContext context) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    
  }

}
