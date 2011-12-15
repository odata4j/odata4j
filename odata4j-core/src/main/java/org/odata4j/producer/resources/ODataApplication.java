package org.odata4j.producer.resources;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class ODataApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    classes.add(EntitiesRequestResource.class);
    classes.add(EntityRequestResource.class);
    classes.add(MetadataResource.class);
    classes.add(ServiceDocumentResource.class);
    classes.add(ODataProducerProvider.class);
    classes.add(ODataBatchProvider.class);
    return classes;
  }
}
