package org.odata4j.consumer;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.core.Throwables;
import org.odata4j.format.FormatType;

/**
 * Helper class allowing the creation of {@link ODataConsumer} objects without using an
 * implementation-specific sub-class.
 *
 * <p>If found, the Jersey implementation of {@link ODataConsumer} is used by default. Otherwise
 * the CXF implementation is loaded. If specified the {@code odata4j.consumerimpl} system property
 * overrules this default behavior. Its value can be either one of the predefined constants {@code
 * jersey} and {@code cxf} or any other concrete full class name, e.g. {@code
 * foo.bar.OtherConsumer}. By convention, {@code foo.bar.OtherConsumer} must implement a factory
 * method {@code public static ODataConsumer.Builder newBuilder(String serviceRootUri)}.</p>
 */
public class ODataConsumers {

  public static final String CONSUMERIMPL_PROPERTY = "odata4j.consumerimpl";
  public static final String JERSEY_CONSUMERIMPL = "jersey";
  public static final String CXF_CONSUMERIMPL = "cxf";
  public static final String JERSEY_CONSUMER_CLASSNAME = "org.odata4j.jersey.consumer.ODataJerseyConsumer";
  public static final String CXF_CONSUMER_CLASSNAME = "org.odata4j.cxf.consumer.ODataCxfConsumer";

  private static ClassLoader classLoader;

  /**
   * Builder for {@link ODataConsumer} objects.
   */
  public static class Builder implements ODataConsumer.Builder {

    private ODataConsumer.Builder consumerBuilder;

    private Builder(String serviceRootUri) {
      if (classLoader == null)
        classLoader = getClass().getClassLoader();

      try {
        String[] classNames = getClassNames();
        Class<?> consumerClass = getConsumerClass(classNames);
        Method newBuilderMethod = consumerClass.getDeclaredMethod("newBuilder", String.class);
        consumerBuilder = (ODataConsumer.Builder) newBuilderMethod.invoke(consumerClass, serviceRootUri);
      } catch (Exception e) {
        Throwables.propagate(e);
      }
    }

    private String[] getClassNames() {
      String impl = System.getProperty(CONSUMERIMPL_PROPERTY);
      if (JERSEY_CONSUMERIMPL.equalsIgnoreCase(impl))
        return new String[] { JERSEY_CONSUMER_CLASSNAME };
      else if (CXF_CONSUMERIMPL.equalsIgnoreCase(impl))
        return new String[] { CXF_CONSUMER_CLASSNAME };
      else if (impl != null && impl.length() > 0)
        return new String[] { impl };
      else
        // default
        return new String[] { JERSEY_CONSUMER_CLASSNAME, CXF_CONSUMER_CLASSNAME };
    }

    private Class<?> getConsumerClass(String[] classNames) throws ClassNotFoundException {
      for (String className : classNames) {
        try {
          Class<?> consumerClass = Class.forName(className, true, classLoader);
          if (consumerClass != null)
            return consumerClass;
        } catch (ClassNotFoundException e) {
          // will be re-thrown at the end of this method (if no class could be loaded)
        }
      }
      throw new ClassNotFoundException("Unable to load ODataConsumer implementation. The following class(es) could not be found: " + Arrays.toString(classNames));
    }

    @Override
    public ODataConsumer.Builder setFormatType(FormatType formatType) {
      return consumerBuilder.setFormatType(formatType);
    }

    @Override
    public ODataConsumer.Builder setClientBehaviors(OClientBehavior... clientBehaviors) {
      return consumerBuilder.setClientBehaviors(clientBehaviors);
    }

    @Override
    public ODataConsumer build() {
      return consumerBuilder.build();
    }
  }

  /**
   * Constructs a new builder for an {@link ODataConsumer} object.
   *
   * @param serviceRootUri  the OData service root uri
   */
  public static ODataConsumer.Builder newBuilder(String serviceRootUri) {
    return new Builder(serviceRootUri);
  }

  /**
   * Creates a new {@link ODataConsumer} for the given OData service root uri.
   *
   * <p>Wrapper for {@code ODataConsumers.newBuilder(serviceRootUri).build()}.
   *
   * @param serviceRootUri  the OData service root uri
   * @return a new OData consumer
   */
  public static ODataConsumer create(String serviceRootUri) {
    return ODataConsumers.newBuilder(serviceRootUri).build();
  }
}
