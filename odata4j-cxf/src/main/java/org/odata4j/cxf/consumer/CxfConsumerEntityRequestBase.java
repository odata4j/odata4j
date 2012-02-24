package org.odata4j.cxf.consumer;

import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityIds;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityRequest;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.FormatType;
import org.odata4j.format.SingleLink;
import org.odata4j.format.SingleLinks;
import org.odata4j.internal.EntitySegment;

abstract class CxfConsumerEntityRequestBase<T> implements OEntityRequest<T> {

  private final EdmDataServices metadata;
  private final String serviceRootUri;
  private final List<EntitySegment> segments = new ArrayList<EntitySegment>();
  private FormatType formatType;

  CxfConsumerEntityRequestBase(FormatType formatType, String serviceRootUri,
      EdmDataServices metadata, String entitySetName, OEntityKey key) {

    this.formatType = formatType;
    this.serviceRootUri = serviceRootUri;
    this.metadata = metadata;

    segments.add(new EntitySegment(entitySetName, key));
  }

  protected FormatType getFormatType() {
    return this.formatType;
  }

  protected EdmDataServices getMetadata() {
    return metadata;
  }

  protected List<EntitySegment> getSegments() {
    return segments;
  }

  protected String getServiceRootUri() {
    return serviceRootUri;
  }

  @Override
  public OEntityRequest<T> nav(String navProperty, OEntityKey key) {
    segments.add(new EntitySegment(navProperty, key));
    return this;
  }

  @Override
  public OEntityRequest<T> nav(String navProperty) {
    segments.add(new EntitySegment(navProperty, null));
    return this;
  }

  protected SingleLink toSingleLink(OEntityId entity) {
    String uri = getServiceRootUri();
    if (!uri.endsWith("/"))
      uri += "/";
    uri += OEntityIds.toKeyString(entity);
    return SingleLinks.create(uri);
  }

}