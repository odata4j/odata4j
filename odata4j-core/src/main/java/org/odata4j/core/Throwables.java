package org.odata4j.core;

public class Throwables {

  public static RuntimeException propagate(Throwable throwable) {
    propagateIfInstanceOf(throwable, Error.class);
    propagateIfInstanceOf(throwable, RuntimeException.class);
    throw new RuntimeException(throwable);
  }

  public static <X extends Throwable> void propagateIfInstanceOf(Throwable throwable, Class<X> declaredType) throws X {
    if (throwable != null && declaredType.isInstance(throwable)) {
      throw declaredType.cast(throwable);
    }
  }

}
