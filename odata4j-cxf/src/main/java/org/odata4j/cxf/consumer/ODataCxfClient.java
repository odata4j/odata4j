package org.odata4j.cxf.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

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
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.consumer.behaviors.OClientBehaviors;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.core.Throwables;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
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

  public void shuttdown() {
    if (this.httpClient != null) {
      // this.client.getConnectionManager().shutdown();
    }
  }

  //  private DefaultHttpClient getThreadSafeClient()  {
  //
  //    DefaultHttpClient client = new DefaultHttpClient();
  //    ClientConnectionManager mgr = client.getConnectionManager();
  //    HttpParams params = client.getParams();
  //    client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,
  //
  //            mgr.getSchemeRegistry()), params);
  //    return client;
  //}

  public EdmDataServices getMetadata(ODataClientRequest request) {
    HttpResponse response = this.doRequest(FormatType.ATOM, request, 200, 404, 400);
    if (response.getStatusLine().getStatusCode() == 404 || response.getStatusLine().getStatusCode() == 400) {
      return null;
    }
    XMLEventReader2 reader = this.doXmlRequest(response);
    EdmDataServices eds = new EdmxFormatParser().parseMetadata(reader);
    return eds;
  }

  public Iterable<AtomCollectionInfo> getCollections(ODataClientRequest request) {
    HttpResponse response = this.doRequest(FormatType.ATOM, request, 200);
    XMLEventReader2 reader = doXmlRequest(response);
    return Enumerable.create(AtomServiceDocumentFormatParser.parseWorkspaces(reader))
        .selectMany(AtomWorkspaceInfo.GET_COLLECTIONS);
  }

  public Iterable<SingleLink> getLinks(ODataClientRequest request) {
    HttpResponse response = this.doRequest(FormatType.ATOM, request, 200);
    XMLEventReader2 reader = doXmlRequest(response);
    return AtomSingleLinkFormatParser.parseLinks(reader);
  }

  public HttpResponse getEntity(ODataClientRequest request) {
    HttpResponse response = this.doRequest(this.getFormatType(), request, 404, 200, 204);
    if (response.getStatusLine().getStatusCode() == 404)
      return null;
    if (response.getStatusLine().getStatusCode() == 204)
      return null;

    return response;
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

  public HttpResponse getEntities(ODataClientRequest request) {
    HttpResponse response = this.doRequest(this.getFormatType(), request, 200);
    return response;
  }

  public HttpResponse callFunction(ODataClientRequest request) {
    HttpResponse response = this.doRequest(this.getFormatType(), request, 200, 204);
    return response;
  }

  public HttpResponse createEntity(ODataClientRequest request) {
    return this.doRequest(this.getFormatType(), request, 201);
  }

  public boolean updateEntity(ODataClientRequest request) {
    this.doRequest(this.getFormatType(), request, 200, 204);
    return true;
  }

  public boolean deleteEntity(ODataClientRequest request) {
    this.doRequest(this.getFormatType(), request, 200, 204, 404);
    return true;
  }

  public void deleteLink(ODataClientRequest request) {
    this.doRequest(this.getFormatType(), request, 204);
  }

  public void createLink(ODataClientRequest request) {
    this.doRequest(this.getFormatType(), request, 204);
  }

  public void updateLink(ODataClientRequest request) {
    this.doRequest(this.getFormatType(), request, 204);
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
  private HttpResponse doRequest(FormatType reqType, ODataClientRequest request, Integer... expectedResponseStatus) {
    try {
      UriBuilder uriBuilder = UriBuilder.fromPath(request.getUrl());
      for (String key : request.getQueryParams().keySet()) {
        uriBuilder = uriBuilder.queryParam(key, request.getQueryParams().get(key));
      }
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
        if (i < reqType.getAcceptableMediaTypes().length - 1) {
          acceptHeader += ", ";
        }
      }
      if (acceptHeader.length() > 0) {
        httpRequest.addHeader("accept", acceptHeader);
      }

      for (String header : request.getHeaders().keySet()) {
        httpRequest.addHeader(header, request.getHeaders().get(header));
      }

      if (!request.getHeaders().containsKey(ODataConstants.Headers.USER_AGENT))
      {
        httpRequest.addHeader(ODataConstants.Headers.USER_AGENT, "odata4j.org");
      }

      if (request.getPayload() != null && httpRequest instanceof HttpEntityEnclosingRequest) {
        HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpRequest;

        Class<?> payloadClass;
        if (request.getPayload() instanceof Entry)
          payloadClass = Entry.class;
        else if (request.getPayload() instanceof SingleLink)
          payloadClass = SingleLink.class;
        else
          throw new UnsupportedOperationException("Unsupported payload: " + request.getPayload());

        StringWriter sw = new StringWriter();
        FormatWriter<Object> fw = (FormatWriter<Object>) (Object)
            FormatWriterFactory.getFormatWriter(payloadClass, null, this.getFormatType().toString(), null);
        fw.write(null, sw, request.getPayload());
        String entityString = sw.toString();

        // allow the client to override the default format writer content-type
        String contentType = request.getHeaders().containsKey(ODataConstants.Headers.CONTENT_TYPE)
            ? request.getHeaders().get(ODataConstants.Headers.CONTENT_TYPE)
            : fw.getContentType();

        StringEntity entity = new StringEntity(entityString);
        entity.setContentType(contentType);

        entityRequest.setEntity(entity);
      }

      HttpResponse httpResponse = this.httpClient.execute(httpRequest);

      int status = httpResponse.getStatusLine().getStatusCode();
      boolean error = true;
      for (int expStatus : expectedResponseStatus) {
        if (status == expStatus) {
          error = false;
        }
      }

      if (error) {
        throw new RuntimeException(String.format("Expected status %s, found %s:",
            Enumerable.create(expectedResponseStatus).join(" or "), status) + "\n" + httpResponse.getEntity().getContent());
      }

      return httpResponse;
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  private HttpUriRequest getRequestByMethod(String method, URI uri) {
    HttpUriRequest request;
    if ("get".equalsIgnoreCase(method)) {
      request = new HttpGet(uri);
    }
    else if ("put".equalsIgnoreCase(method)) {
      request = new HttpPut(uri);
    }
    else if ("post".equalsIgnoreCase(method)) {
      request = new HttpPost(uri);
    }
    else if ("delete".equalsIgnoreCase(method)) {
      request = new HttpDelete(uri);
    }
    else if ("options".equalsIgnoreCase(method)) {
      request = new HttpOptions(uri);
    }
    else if ("head".equalsIgnoreCase(method)) {
      request = new HttpHead(uri);
    }
    else {
      throw new RuntimeException("Method unknown: " + method);
    }
    return request;
  }

  private XMLEventReader2 doXmlRequest(HttpResponse response) {
    try {
      InputStream textEntity = response.getEntity().getContent();
      return InternalUtil.newXMLEventReader(new BOMWorkaroundReader(new InputStreamReader(textEntity, "UTF-8")));
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @SuppressWarnings("unused")
  private String inputStreamToString(InputStream in) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
    StringBuilder stringBuilder = new StringBuilder();
    String line = null;

    while ((line = bufferedReader.readLine()) != null) {
      stringBuilder.append(line);
    }

    bufferedReader.close();
    return stringBuilder.toString();
  }

}
