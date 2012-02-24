package org.odata4j.cxf.consumer;

import org.apache.http.HttpResponse;
import org.core4j.Enumerable;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntityGetRequest;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.internal.EntitySegment;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

class CxfConsumerGetEntityRequest<T> extends CxfConsumerEntityRequestBase<T> implements OEntityGetRequest<T> {

  private final Class<T> entityType;
  private final FeedCustomizationMapping fcMapping;

  private String select;
  private String expand;

  CxfConsumerGetEntityRequest(FormatType formatType, Class<T> entityType, String serviceRootUri,
      EdmDataServices metadata, String entitySetName, OEntityKey key, FeedCustomizationMapping fcMapping) {
    super(formatType, serviceRootUri, metadata, entitySetName, key);
    this.entityType = entityType;
    this.fcMapping = fcMapping;
  }

  @Override
  public CxfConsumerGetEntityRequest<T> select(String select) {
    this.select = select;
    return this;
  }

  @Override
  public CxfConsumerGetEntityRequest<T> expand(String expand) {
    this.expand = expand;
    return this;
  }

  @Override
  public T execute() {
    ODataCxfClient client = new ODataCxfClient(this.getFormatType());
    try {
      String path = Enumerable.create(getSegments()).join("/");

      ODataClientRequest request = ODataClientRequest.get(getServiceRootUri() + path);

      if (select != null) {
        request = request.queryParam("$select", select);
      }

      if (expand != null) {
        request = request.queryParam("$expand", expand);
      }

      HttpResponse response = client.getEntity(request);
      if (response == null)
        return null;

      //  the first segment contains the entitySetName we start from
      EdmEntitySet entitySet = getMetadata().getEdmEntitySet(getSegments().get(0).segment);
      for (EntitySegment segment : getSegments().subList(1, getSegments().size())) {
        EdmNavigationProperty navProperty = entitySet.getType().findNavigationProperty(segment.segment);
        entitySet = getMetadata().getEdmEntitySet(navProperty.getToRole().getType());
      }

      OEntityKey key = Enumerable.create(getSegments()).last().key;

      // TODO determine the service version from header (and metadata?)
      FormatParser<Feed> parser = FormatParserFactory
          .getParser(Feed.class, client.getFormatType(),
              new Settings(ODataConstants.DATA_SERVICE_VERSION, getMetadata(), entitySet.getName(), key, fcMapping));

      Entry entry = Enumerable.create(parser.parse(client.getFeedReader(response)).getEntries())
          .firstOrNull();

      return (T) InternalUtil.toEntity(entityType, entry.getEntity());
    } finally {
      client.shuttdown();
    }
  }

}
