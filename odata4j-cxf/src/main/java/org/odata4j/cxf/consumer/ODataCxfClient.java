package org.odata4j.cxf.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.core4j.Enumerable;
import org.odata4j.consumer.AbstractODataClient;
import org.odata4j.consumer.ODataClientException;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataServerException;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataHttpMethod;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OError;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.core.Throwables;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.format.SingleLink;
import org.odata4j.format.xml.AtomCollectionInfo;
import org.odata4j.format.xml.AtomServiceDocumentFormatParser;
import org.odata4j.format.xml.AtomSingleLinkFormatParser;
import org.odata4j.format.xml.AtomWorkspaceInfo;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.internal.BOMWorkaroundReader;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;

/**
 * OData client based on Apache's HTTP client implementation.
 */
public class ODataCxfClient extends AbstractODataClient {

  private HttpClient httpClient;
  private OClientBehavior[] behaviors = new OClientBehavior[] { OClientBehaviors.methodTunneling("MERGE") };

  public ODataCxfClient(FormatType formatType) {
    super(formatType);
    this.httpClient = new DefaultHttpClient();

    if (System.getProperties().containsKey("http.proxyHost") && System.getProperties().containsKey("http.proxyPort")) {
      // support proxy settings
      String hostName = System.getProperties().getProperty("http.proxyHost");
      String hostPort = System.getProperties().getProperty("http.proxyPort");

      HttpHost proxy = new HttpHost(hostName, Integer.parseInt(hostPort));
      this.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
    }
  }

  public ODataCxfClient(FormatType formatType, OClientBehavior[] additionalBehaviors) {
    this(formatType);
    this.behaviors = Enumerable.create(this.behaviors).concat(Enumerable.create(additionalBehaviors)).toArray(OClientBehavior.class);
  }

  public EdmDataServices getMetadata(ODataClientRequest request) throws ODataServerException, ODataClientException {
    HttpResponse response = doRequest(FormatType.ATOM, request, Status.OK);
    EdmDataServices metadata = new EdmxFormatParser().parseMetadata(doXmlRequest(response));
    try {
      response.getEntity().getContent().close();
    } catch (IOException e) {
      throw new ODataClientException("Error while closing socket", e);
    }
    return metadata;
  }

  public Iterable<AtomCollectionInfo> getCollections(ODataClientRequest request) throws ODataServerException, ODataClientException {
    HttpResponse response = doRequest(FormatType.ATOM, request, Status.OK);
    return Enumerable.create(AtomServiceDocumentFormatParser.parseWorkspaces(doXmlRequest(response)))
        .selectMany(AtomWorkspaceInfo.GET_COLLECTIONS);
  }

  public Iterable<SingleLink> getLinks(ODataClientRequest request) throws ODataServerException, ODataClientException {
    HttpResponse response = doRequest(FormatType.ATOM, request, Status.OK);
    return AtomSingleLinkFormatParser.parseLinks(doXmlRequest(response));
  }

  public HttpResponse getEntity(ODataClientRequest request) throws ODataServerException, ODataClientException {
    return doRequest(getFormatType(), request, Status.OK, Status.NO_CONTENT);
  }

  Entry createRequestEntry(EdmEntitySet entitySet, OEntityKey entityKey, List<OProperty<?>> props, List<OLink> links) {
    final OEntity oentity = entityKey == null
        ? OEntities.createRequest(entitySet, props, links)
        : OEntities.create(entitySet, entityKey, props, links);

    return new Entry() {

      @Override
      public String getUri() {
        return null;
      }

      @Override
      public OEntity getEntity() {
        return oentity;
      }

      @Override
      public String getETag() {
        return null;
      }
    };
  }

  public HttpResponse getEntities(ODataClientRequest request) throws ODataServerException, ODataClientException {
    return doRequest(getFormatType(), request, Status.OK);
  }

  public HttpResponse callFunction(ODataClientRequest request) throws ODataServerException, ODataClientException {
    return doRequest(getFormatType(), request, Status.OK, Status.NO_CONTENT);
  }

  public HttpResponse createEntity(ODataClientRequest request) throws ODataServerException, ODataClientException {
    return doRequest(getFormatType(), request, Status.CREATED);
  }

  public void updateEntity(ODataClientRequest request) throws ODataServerException, ODataClientException {
    doRequest(getFormatType(), request, Status.OK, Status.NO_CONTENT);
  }

  public void deleteEntity(ODataClientRequest request) throws ODataServerException, ODataClientException {
    doRequest(getFormatType(), request, Status.OK, Status.NO_CONTENT);
  }

  public void deleteLink(ODataClientRequest request) throws ODataServerException, ODataClientException {
    doRequest(getFormatType(), request, Status.NO_CONTENT);
  }

  public void createLink(ODataClientRequest request) throws ODataServerException, ODataClientException {
    doRequest(getFormatType(), request, Status.NO_CONTENT);
  }

  public void updateLink(ODataClientRequest request) throws ODataServerException, ODataClientException {
    doRequest(getFormatType(), request, Status.NO_CONTENT);
  }

  Reader getFeedReader(HttpResponse response) {
    try {
      InputStream textEntity = response.getEntity().getContent();
      return new BOMWorkaroundReader(new InputStreamReader(textEntity, "UTF-8"));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @SuppressWarnings("unchecked")
  private HttpResponse doRequest(FormatType reqType, ODataClientRequest request, StatusType... expectedResponseStatus) throws ODataServerException, ODataClientException {
    UriBuilder uriBuilder = UriBuilder.fromPath(request.getUrl());
    for (String key : request.getQueryParams().keySet())
      uriBuilder = uriBuilder.queryParam(key, request.getQueryParams().get(key));
    URI uri = uriBuilder.build();

    if (this.behaviors != null) {
      for (OClientBehavior behavior : behaviors)
        request = behavior.transform(request);
    }

    HttpUriRequest httpRequest = this.getRequestByMethod(request.getMethod(), uri);

    // maybe something better is needed here
    String acceptHeader = "";
    for (int i = 0; i < reqType.getAcceptableMediaTypes().length; i++) {
      acceptHeader += reqType.getAcceptableMediaTypes()[i];
      if (i < reqType.getAcceptableMediaTypes().length - 1)
        acceptHeader += ", ";
    }
    if (acceptHeader.length() > 0)
      httpRequest.addHeader(HttpHeaders.ACCEPT, acceptHeader);

    for (String header : request.getHeaders().keySet())
      httpRequest.addHeader(header, request.getHeaders().get(header));

    if (!request.getHeaders().containsKey(ODataConstants.Headers.USER_AGENT))
      httpRequest.addHeader(ODataConstants.Headers.USER_AGENT, "odata4j.org");

    if (request.getPayload() != null && httpRequest instanceof HttpEntityEnclosingRequest) {
      HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpRequest;

      Class<?> payloadClass;
      if (request.getPayload() instanceof Entry)
        payloadClass = Entry.class;
      else if (request.getPayload() instanceof SingleLink)
        payloadClass = SingleLink.class;
      else
        throw new ODataClientException("Unsupported payload: " + request.getPayload());

      StringWriter sw = new StringWriter();
      FormatWriter<Object> fw = (FormatWriter<Object>)
          FormatWriterFactory.getFormatWriter(payloadClass, null, this.getFormatType().toString(), null);
      fw.write(null, sw, request.getPayload());
      String entityString = sw.toString();

      // allow the client to override the default format writer content-type
      String contentType = request.getHeaders().containsKey(ODataConstants.Headers.CONTENT_TYPE)
          ? request.getHeaders().get(ODataConstants.Headers.CONTENT_TYPE)
          : fw.getContentType();

      try {
        StringEntity entity = new StringEntity(entityString);

        entity.setContentType(contentType);

        entityRequest.setEntity(entity);
      } catch (UnsupportedEncodingException e) {
        throw new ODataClientException(e);
      }
    }

    // execute request
    HttpResponse httpResponse;
    try {
      httpResponse = this.httpClient.execute(httpRequest);
    } catch (IOException e) {
      throw new ODataClientException("HTTP error occurred", e);
    }

    StatusType status = Status.fromStatusCode(httpResponse.getStatusLine().getStatusCode());
    for (StatusType expStatus : expectedResponseStatus)
      if (expStatus.equals(status))
        return httpResponse;

    // the server responded with an unexpected status
    RuntimeException exception;
    String textEntity = entityToString(httpResponse.getEntity()); // input stream can only be consumed once
    try {
      // report error as ODataServerException in case we get a well-formed OData error...
      MediaType contentType = MediaType.valueOf(httpResponse.getEntity().getContentType().getValue());
      OError error = FormatParserFactory.getParser(OError.class, contentType, null).parse(new StringReader(textEntity));
      exception = new ODataServerException(status, error);
    } catch (RuntimeException e) {
      // ... otherwise throw a RuntimeError
      exception = new RuntimeException(String.format("Expected status %s, found %s. Server response:",
          Enumerable.create(expectedResponseStatus).join(" or "), status) + "\n" + textEntity);
    }
    throw exception;
  }

  private HttpUriRequest getRequestByMethod(String method, URI uri) {
    switch (ODataHttpMethod.fromString(method)) {
    case GET:
      return new HttpGet(uri);
    case PUT:
      return new HttpPut(uri);
    case POST:
      return new HttpPost(uri);
    case DELETE:
      return new HttpDelete(uri);
    case OPTIONS:
      return new HttpOptions(uri);
    case HEAD:
      return new HttpHead(uri);
    default:
      throw new RuntimeException("Method unknown: " + method);
    }
  }

  private XMLEventReader2 doXmlRequest(HttpResponse response) {
    try {
      InputStream textEntity = response.getEntity().getContent();
      return InternalUtil.newXMLEventReader(new BOMWorkaroundReader(new InputStreamReader(textEntity, "UTF-8")));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private String entityToString(HttpEntity entity) {
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
      StringBuilder stringBuilder = new StringBuilder();
      String line = null;

      while ((line = bufferedReader.readLine()) != null)
        stringBuilder.append(line);

      bufferedReader.close();
      return stringBuilder.toString();
    } catch (IOException e) {
      throw new ODataClientException(e);
    }
  }

}
