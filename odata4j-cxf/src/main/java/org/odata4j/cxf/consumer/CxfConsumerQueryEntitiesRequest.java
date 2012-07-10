package org.odata4j.cxf.consumer;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.Func1;
import org.core4j.ReadOnlyIterator;
import org.odata4j.consumer.ODataClientException;
import org.odata4j.consumer.ODataServerException;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

class CxfConsumerQueryEntitiesRequest<T> extends CxfConsumerQueryRequestBase<T> {

  private final Class<T> entityType;
  private final FeedCustomizationMapping fcMapping;

  CxfConsumerQueryEntitiesRequest(FormatType formatType, Class<T> entityType, String serviceRootUri, EdmDataServices metadata, String entitySetName, FeedCustomizationMapping fcMapping) {
    super(formatType, serviceRootUri, metadata, entitySetName);
    this.entityType = entityType;
    this.fcMapping = fcMapping;
  }

  @Override
  public Enumerable<T> execute() throws ODataServerException, ODataClientException {
    ODataClientRequest request = buildRequest(null);
    Enumerable<Entry> entries = getEntries(request);

    return entries.select(new Func1<Entry, T>() {
      public T apply(Entry input) {
        return InternalUtil.toEntity(entityType, input.getEntity());
      }
    }).cast(entityType);
  }

  private Enumerable<Entry> getEntries(final ODataClientRequest request) throws ODataServerException, ODataClientException {
    final Feed feed = doRequest(request);
    return Enumerable.createFromIterator(new Func<Iterator<Entry>>() {
      public Iterator<Entry> apply() {
        return new EntryIterator(request, feed);
      }
    });
  }

  private Feed doRequest(ODataClientRequest request) throws ODataServerException, ODataClientException {
    HttpResponse response = getClient().getEntities(request);

    ODataVersion version = InternalUtil.getDataServiceVersion(response.getFirstHeader(ODataConstants.Headers.DATA_SERVICE_VERSION).getValue());

    FormatParser<Feed> parser = FormatParserFactory.getParser(Feed.class, getClient().getFormatType(),
        new Settings(version, getMetadata(), getEntitySet().getName(), null, fcMapping));

    Feed feed = parser.parse(getClient().getFeedReader(response));
    try {
      response.getEntity().getContent().close();
    } catch (IOException e) {
      throw new ODataClientException("Error while closing socket", e);
    }
    return feed;
  }

  private class EntryIterator extends ReadOnlyIterator<Entry> {

    private ODataClientRequest request;
    private Feed feed;
    private Iterator<Entry> feedEntries;
    private int feedEntryCount;

    public EntryIterator(ODataClientRequest request, Feed feed) {
      this.request = request;
      this.feed = feed;
      feedEntries = feed.getEntries().iterator();
      feedEntryCount = 0;
    }

    @Override
    protected IterationResult<Entry> advance() throws Exception {

      if (feed == null) {
        feed = doRequest(request);
        feedEntries = feed.getEntries().iterator();
        feedEntryCount = 0;
      }

      if (feedEntries.hasNext()) {
        feedEntryCount++;
        return IterationResult.next(feedEntries.next());
      }

      // old-style paging: $page and $itemsPerPage
      if (request.getQueryParams().containsKey("$page") && request.getQueryParams().containsKey("$itemsPerPage")) {
        if (feedEntryCount == 0)
          return IterationResult.done();

        int page = Integer.parseInt(request.getQueryParams().get("$page"));
        // int itemsPerPage = Integer.parseInt(request.getQueryParams().get("$itemsPerPage"));

        request = request.queryParam("$page", Integer.toString(page + 1));
      }

      // new-style paging: $skiptoken
      else {
        if (feed.getNext() == null)
          return IterationResult.done();

        int skipTokenIndex = feed.getNext().indexOf("$skiptoken=");
        if (skipTokenIndex > -1) {
          String skiptoken = feed.getNext().substring(skipTokenIndex + "$skiptoken=".length());
          // decode the skiptoken first since it gets encoded as a query param
          skiptoken = URLDecoder.decode(skiptoken, "UTF-8");
          request = request.queryParam("$skiptoken", skiptoken);
        } else if (feed.getNext().toLowerCase().startsWith("http")) {
          request = ODataClientRequest.get(feed.getNext());
        } else {
          throw new UnsupportedOperationException();
        }

      }

      feed = null;

      return advance(); // TODO stackoverflow possible here
    }

  }

}
