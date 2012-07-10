package org.odata4j.cxf.consumer;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpResponse;
import org.core4j.Enumerable;
import org.core4j.Func;
import org.core4j.ReadOnlyIterator;
import org.joda.time.LocalDateTime;
import org.odata4j.consumer.ODataClientException;
import org.odata4j.consumer.ODataServerException;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.core.Guid;
import org.odata4j.core.OCollection;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OErrors;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OFunctionParameters;
import org.odata4j.core.OFunctionRequest;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.core.UnsignedByte;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;
import org.odata4j.expression.Expression;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.internal.InternalUtil;

class CxfConsumerFunctionCallRequest<T extends OObject>
    extends CxfConsumerQueryRequestBase<T>
    implements OFunctionRequest<T> {

  private final List<OFunctionParameter> params = new LinkedList<OFunctionParameter>();
  private final EdmFunctionImport function;

  CxfConsumerFunctionCallRequest(FormatType formatType, String serviceRootUri,
      EdmDataServices metadata, String lastSegment) throws ODataServerException {
    super(formatType, serviceRootUri, metadata, lastSegment);
    // lastSegment is the function call name.
    function = metadata.findEdmFunctionImport(lastSegment);
    if (function == null)
      throw new ODataServerException(Status.NOT_FOUND, OErrors.error(null, "Function Import " + lastSegment + " not defined", null));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enumerable<T> execute() throws ODataServerException, ODataClientException {
    // turn each param into a custom query option
    for (OFunctionParameter p : params)
      custom(p.getName(), toUriString(p));

    final ODataClientRequest request = buildRequest(null);
    Enumerable<OObject> results;
    if (function.getReturnType() == null) {
      results = Enumerable.empty(null);
    } else if (function.getReturnType() instanceof EdmCollectionType) {
      final OCollection<OObject> collection = (OCollection<OObject>) doRequest(request);
      results = Enumerable.createFromIterator(
          new Func<Iterator<OObject>>() {
            @Override
            public Iterator<OObject> apply() {
              return new FunctionResultsIterator(request, collection);
            }
          });
    } else {
      results = Enumerable.create(doRequest(request));
    }
    return (Enumerable<T>) results;
  }

  private static String toUriString(OFunctionParameter p) {
    OObject obj = p.getValue();
    if (obj instanceof OSimpleObject) {
      OSimpleObject<?> simple = (OSimpleObject<?>) obj;
      LiteralExpression le = Expression.literal(simple.getType(), simple.getValue());
      return Expression.asFilterString(le);

    }
    throw new UnsupportedOperationException("type not supported: " + obj.getType().getFullyQualifiedTypeName());
  }

  // set parameters to the function call
  @Override
  public CxfConsumerFunctionCallRequest<T> parameter(String name, OObject value) {
    params.add(OFunctionParameters.create(name, value));
    return this;
  }

  @Override
  public OFunctionRequest<T> pBoolean(String name, boolean value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.BOOLEAN, value));
  }

  @Override
  public OFunctionRequest<T> pByte(String name, UnsignedByte value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.BYTE, value));
  }

  @Override
  public OFunctionRequest<T> pSByte(String name, byte value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.SBYTE, value));
  }

  @Override
  public OFunctionRequest<T> pDateTime(String name, Calendar value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.DATETIME, value));
  }

  @Override
  public OFunctionRequest<T> pDateTime(String name, Date value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.DATETIME, value));
  }

  @Override
  public OFunctionRequest<T> pDateTime(String name, LocalDateTime value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.DATETIME, value));
  }

  @Override
  public OFunctionRequest<T> pDecimal(String name, BigDecimal value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.DECIMAL, value));
  }

  @Override
  public OFunctionRequest<T> pDouble(String name, double value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.DOUBLE, value));
  }

  @Override
  public OFunctionRequest<T> pGuid(String name, Guid value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.GUID, value));
  }

  @Override
  public OFunctionRequest<T> pInt16(String name, short value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.INT16, value));
  }

  @Override
  public OFunctionRequest<T> pInt32(String name, int value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.INT32, value));
  }

  @Override
  public OFunctionRequest<T> pInt64(String name, long value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.INT64, value));
  }

  @Override
  public OFunctionRequest<T> pSingle(String name, float value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.SINGLE, value));
  }

  @Override
  public OFunctionRequest<T> pTime(String name, Calendar value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.TIME, value));
  }

  @Override
  public OFunctionRequest<T> pTime(String name, Date value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.TIME, value));
  }

  @Override
  public OFunctionRequest<T> pTime(String name, LocalDateTime value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.TIME, value));
  }

  @Override
  public OFunctionRequest<T> pString(String name, String value) {
    return parameter(name, OSimpleObjects.create(EdmSimpleType.STRING, value));
  }

  private OObject doRequest(ODataClientRequest request) throws ODataServerException, ODataClientException {
    HttpResponse response = getClient().callFunction(request);

    ODataVersion version = InternalUtil.getDataServiceVersion(response.getFirstHeader(ODataConstants.Headers.DATA_SERVICE_VERSION).getValue());

    FormatParser<? extends OObject> parser = FormatParserFactory.getParser(
        function.getReturnType().isSimple() ? OSimpleObject.class : EdmType.getInstanceType(function.getReturnType()),
        getClient().getFormatType(),
        new Settings(
            version,
            getMetadata(),
            function.getName(),
            null, // entitykey
            null, // fcMapping
            true, // isResponse
            function.getReturnType()));

    return parser.parse(getClient().getFeedReader(response));
  }

  private class FunctionResultsIterator extends ReadOnlyIterator<OObject> {

    private ODataClientRequest request;
    private OCollection<OObject> current = null;
    private Iterator<OObject> iter = null;
    private int count = 0;

    public FunctionResultsIterator(ODataClientRequest request, OCollection<OObject> current) {
      this.request = request;
      this.current = current;
      this.iter = current.iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected IterationResult<OObject> advance() throws Exception {

      if (current == null) {
        current = (OCollection<OObject>) doRequest(this.request);
        iter = current.iterator();
        count = 0;
      }

      if (iter != null && iter.hasNext()) {
        count++;
        return IterationResult.next(iter.next());
      } else {
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
