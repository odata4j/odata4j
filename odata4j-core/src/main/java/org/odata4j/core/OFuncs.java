package org.odata4j.core;

import org.core4j.Func1;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmType;

/**
 * A static factory to create useful generic function instances.
 */
public class OFuncs {

  private OFuncs() {}

  public static Func1<EdmProperty, String> edmPropertyName() {
    return new Func1<EdmProperty, String>() {
      public String apply(EdmProperty input) {
        return input.name;
      }
    };
  }

  public static Func1<EdmEntityType, String> edmEntityTypeName() {
    return new Func1<EdmEntityType, String>() {
      public String apply(EdmEntityType input) {
        return input.getName();
      }
    };
  }

  public static Func1<EdmEntitySet, String> edmEntitySetName() {
    return new Func1<EdmEntitySet, String>() {
      public String apply(EdmEntitySet input) {
        return input.getName();
      }
    };
  }

  public static Func1<EdmType, String> edmTypeFullyQualifiedTypeName() {
    return new Func1<EdmType, String>() {
      public String apply(EdmType input) {
        return input.getFullyQualifiedTypeName();
      }
    };
  }

  public static <TProperty> Func1<OEntity, TProperty> entityPropertyValue(final String propName, final Class<TProperty> propClass) {
    return new Func1<OEntity, TProperty>() {
      public TProperty apply(OEntity input) {
        return input.getProperty(propName, propClass).getValue();
      }
    };
  }

  public static <T> Func1<NamedValue<T>, OProperty<T>> namedValueToProperty() {
    return new Func1<NamedValue<T>, OProperty<T>>() {
      public OProperty<T> apply(NamedValue<T> input) {
        return OProperties.simple(input.getName(), input.getValue());
      }
    };
  }

  @SuppressWarnings("rawtypes")
  public static Func1<NamedValue, OProperty<?>> namedValueToPropertyRaw() {
    return new Func1<NamedValue, OProperty<?>>() {
      public OProperty<?> apply(NamedValue input) {
        return OProperties.simple(input.getName(), input.getValue());
      }
    };
  }

  public static Func1<EntitySetInfo, String> entitySetInfoTitle() {
    return new Func1<EntitySetInfo, String>() {
      public String apply(EntitySetInfo input) {
        return input.getTitle();
      }
    };
  }
}
