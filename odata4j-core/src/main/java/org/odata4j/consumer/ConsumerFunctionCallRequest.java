package org.odata4j.consumer;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.ReadOnlyIterator;
import org.joda.time.LocalDateTime;
import org.odata4j.core.Guid;
import org.odata4j.core.OCollection;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OFunctionParameters;
import org.odata4j.core.OFunctionRequest;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.expression.Expression;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.Settings;
import org.odata4j.internal.InternalUtil;

import com.sun.jersey.api.client.ClientResponse;

/**
 * A builder for consumer function calls.
 * Usage example:
 * <pre>
 * {@code
 * Enumerable<OObject> e = myConsumer.callFunction("AFunction")
 *     .pBoolean("Parameter1", false)
 *     .pInt32("Parameter2", 55)
 *     .execute();
 * }
 * </pre>
 * Note:
 * OData functions can return single instances or collections of instances.
 * To keep the interface simple, callFunction always returns an Enumerable.
 */
class ConsumerFunctionCallRequest<T extends OObject>
    extends ConsumerQueryRequestBase<T>
    implements OFunctionRequest<T> {

  private final List<OFunctionParameter> params = new LinkedList<OFunctionParameter>();
  private final EdmFunctionImport function;
  
  protected ConsumerFunctionCallRequest(ODataClient client, String serviceRootUri,
      EdmDataServices metadata, String lastSegment) {
    super(client, serviceRootUri, metadata, lastSegment);
    // lastSegment is the function call name.
    function = metadata.findEdmFunctionImport(lastSegment);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enumerable<T> execute() {
    // turn each param into a custom query option
    for (OFunctionParameter p : params) {
      custom(p.getName(), toURIString(p));
    }
    final ODataClientRequest request = buildRequest(null);
    Enumerable<OObject> results = Enumerable.createFromIterator(
        new Func<Iterator<OObject>>() {
          @Override
          public Iterator<OObject> apply() {
            return new FunctionResultsIterator(getClient(), request);
          }
        });
    return (Enumerable<T>) results;
  }
  
  private static String toURIString(OFunctionParameter p) {
    OObject obj = p.getValue();
    if (obj instanceof OSimpleObject) {
      LiteralExpression le = Expression.literal(((OSimpleObject<?>)obj).getValue());
      return Expression.asFilterString(le);
  
    }
    throw new UnsupportedOperationException("type not supported: " + obj.getType().getFullyQualifiedTypeName());
  }
  

  // set parameters to the function call
  @Override
  public ConsumerFunctionCallRequest<T> parameter(String name, OObject value) {
    params.add(OFunctionParameters.create(name, value));
    return this;
  }

  @Override
  public OFunctionRequest<T> pBoolean(String name, boolean value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.BOOLEAN));
  }

  @Override
  public OFunctionRequest<T> pByte(String name, byte value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.BYTE));
  }

  @Override
  public OFunctionRequest<T> pDateTime(String name, Calendar value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.DATETIME));
  }

  @Override
  public OFunctionRequest<T> pDateTime(String name, Date value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.DATETIME));
  }

  @Override
  public OFunctionRequest<T> pDateTime(String name, LocalDateTime value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.DATETIME));
  }

  @Override
  public OFunctionRequest<T> pDecimal(String name, BigDecimal value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.DECIMAL));
  }

  @Override
  public OFunctionRequest<T> pDouble(String name, double value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.DOUBLE));
  }

  @Override
  public OFunctionRequest<T> pGuid(String name, Guid value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.GUID));
  }

  @Override
  public OFunctionRequest<T> pInt16(String name, short value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.INT16));
  }

  @Override
  public OFunctionRequest<T> pInt32(String name, int value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.INT32));
  }

  @Override
  public OFunctionRequest<T> pInt64(String name, long value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.INT64));
  }

  @Override
  public OFunctionRequest<T> pSingle(String name, float value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.SINGLE));
  }

  @Override
  public OFunctionRequest<T> pTime(String name, Calendar value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.TIME));
  }

  @Override
  public OFunctionRequest<T> pTime(String name, Date value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.TIME));
  }

  @Override
  public OFunctionRequest<T> pTime(String name, LocalDateTime value) {
    return parameter(name, OSimpleObjects.create(value, EdmSimpleType.TIME));
  }

  private class FunctionResultsIterator extends ReadOnlyIterator<OObject> {

    private ODataClient client;
    private ODataClientRequest request;
    private FormatParser<? extends OObject> parser;
    private OObject current = null;
    private Iterator<OObject> iter = null;
    private boolean done = false;
    private int count = 0;

    public FunctionResultsIterator(ODataClient client, ODataClientRequest request) {
      this.client = client;
      this.request = request;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IterationResult<OObject> advance() throws Exception {

      if (done) {
        return IterationResult.done();
      }

      if (current == null) {
        ClientResponse response = client.callFunction(request);

        ODataVersion version = InternalUtil.getDataServiceVersion(response.getHeaders().getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));

        parser = FormatParserFactory.getParser(
            OFunctionParameters.getResultClass(function.returnType),
            client.getFormatType(),
            new Settings(
                version,
                getMetadata(),
                getLastSegment(),
                null, // entitykey
                null, // fcMapping
                true, // isResponse
                function.returnType));

        current = parser.parse(client.getFeedReader(response));
        if (function.returnType instanceof EdmCollectionType) {
          iter = ((OCollection<OObject>) current).iterator();
        } else {
          done = true;
          return IterationResult.next(current);
        }
      }

      if (iter.hasNext()) {
        count++;
        return IterationResult.next(iter.next());
      } else {
        done = true;
        return IterationResult.done();
      }

      /* TODO support paging, this code was from ConsumerQueryEntitiesRequest...we'll need something like this.
      // old-style paging: $page and $itemsPerPage
      if (request.getQueryParams().containsKey("$page") && request.getQueryParams().containsKey("$itemsPerPage")) {
          if (feedEntryCount == 0) {
              return IterationResult.done();
          }

          int page = Integer.parseInt(request.getQueryParams().get("$page"));
          // int itemsPerPage = Integer.parseInt(request.getQueryParams().get("$itemsPerPage"));

          request = request.queryParam("$page", Integer.toString(page + 1));
      } // new-style paging: $skiptoken
      else {
          if (feed.getNext() == null) {
              return IterationResult.done();
          }

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

      } */

    }
  }

}
