package org.odata4j.consumer;

public abstract class AbstractODataConsumer implements ODataConsumer {

  private String serviceRootUri;
  
  protected AbstractODataConsumer(String serviceRootUri) {
    this.serviceRootUri = serviceRootUri;
  }

  @Override
  public String getServiceRootUri() {
    return this.serviceRootUri;
  }

  
  
}
