package org.odata4j.edm;

/**
 * Base class for all Edm types.
 */
public class EdmBaseType {

  public EdmBaseType(String fqTypeName) {
      this.fqTypeName = fqTypeName;
  }

  /**
   * Gets the fully-qualified type name for this edm-type.
   * Note: I would prefer a different name for this...leaving for now because
   *   this was the name before the type refactoring.
   * @return the fully-qualified type name
   */
  public String toTypeString() {
    return this.fqTypeName;
  }

  @Override
  public String toString() {
    return toTypeString();
  }

  @Override
  public int hashCode() {
    return this.fqTypeName.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof EdmBaseType && ((EdmBaseType) other).fqTypeName.equals(this.fqTypeName);
  }

  private String fqTypeName = null;
}
