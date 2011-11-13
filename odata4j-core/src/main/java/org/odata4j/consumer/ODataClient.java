package org.odata4j.consumer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.core4j.Enumerable;
import org.core4j.xml.XDocument;
import org.core4j.xml.XmlFormat;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OClientBehaviors;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.PartialRequestBuilder;
import com.sun.jersey.api.client.WebResource;

class ODataClient {

  private final FormatType type;

  private final OClientBehavior[] requiredBehaviors = new OClientBehavior[] { OClientBehaviors.methodTunneling("MERGE") }; // jersey hates MERGE, tunnel through POST
  private final OClientBehavior[] behaviors;

  private final Client client;

  public ODataClient(FormatType type, ClientFactory clientFactory, OClientBehavior... behaviors) {
    this.behaviors = Enumerable.create(requiredBehaviors).concat(Enumerable.create(behaviors)).toArray(OClientBehavior.class);
    this.type = type;
    this.client = ClientUtil.newClient(clientFactory, behaviors);
  }

  public FormatType getFormatType() {
    return type;
  }

  public EdmDataServices getMetadata(ODataClientRequest request) {
    ClientResponse response = doRequest(FormatType.ATOM, request, 200, 404, 400);
    if (response.getStatus() == 404 || response.getStatus() == 400)
      return null;
    XMLEventReader2 reader = doXmlRequest(response);
    return new EdmxFormatParser().parseMetadata(reader);
  }

  public Iterable<AtomCollectionInfo> getCollections(ODataClientRequest request) {
    ClientResponse response = doRequest(FormatType.ATOM, request, 200);
    XMLEventReader2 reader = doXmlRequest(response);
    return Enumerable.create(AtomServiceDocumentFormatParser.parseWorkspaces(reader))
        .selectMany(AtomWorkspaceInfo.GET_COLLECTIONS);
  }

  public Iterable<SingleLink> getLinks(ODataClientRequest request) {
    ClientResponse response = doRequest(FormatType.ATOM, request, 200);
    XMLEventReader2 reader = doXmlRequest(response);
    return AtomSingleLinkFormatParser.parseLinks(reader);
  }

  public ClientResponse getEntity(ODataClientRequest request) {
    ClientResponse response = doRequest(type, request, 404, 200, 204);
    if (response.getStatus() == 404)
      return null;
    if (response.getStatus() == 204)
      return null;

    return response;
  }

  public ClientResponse getEntities(ODataClientRequest request) {
    ClientResponse response = doRequest(type, request, 200);
    return response;
  }

  public ClientResponse callFunction(ODataClientRequest request) {
    ClientResponse response = doRequest(type, request, 200, 204);
    return response;
  }

  public ClientResponse createEntity(ODataClientRequest request) {
    return doRequest(type, request, 201);
  }

  public boolean updateEntity(ODataClientRequest request) {
    doRequest(type, request, 200, 204);
    return true;
  }

  public boolean deleteEntity(ODataClientRequest request) {
    doRequest(type, request, 200, 204, 404);
    return true;
  }

  public void deleteLink(ODataClientRequest request) {
    doRequest(type, request, 204);
  }

  public void createLink(ODataClientRequest request) {
    doRequest(type, request, 204);
  }

  public void updateLink(ODataClientRequest request) {
    doRequest(type, request, 204);
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

  @SuppressWarnings("unchecked")
  private ClientResponse doRequest(FormatType reqType, ODataClientRequest request, Integer... expectedResponseStatus) {

    if (behaviors != null) {
      for (OClientBehavior behavior : behaviors)
        request = behavior.transform(request);
    }

    WebResource webResource = ClientUtil.resource(client, request.getUrl(), behaviors);

    // set query params
    for (String qpn : request.getQueryParams().keySet()) {
      webResource = webResource.queryParam(qpn, request.getQueryParams().get(qpn));
    }

    WebResource.Builder b = webResource.getRequestBuilder();

    // set headers
    b = b.accept(reqType.getAcceptableMediaTypes());

    for (String header : request.getHeaders().keySet()) {
      b.header(header, request.getHeaders().get(header));
    }
    if (!request.getHeaders().containsKey(ODataConstants.Headers.USER_AGENT))
      b.header(ODataConstants.Headers.USER_AGENT, "odata4j.org");

    if (ODataConsumer.dump.requestHeaders())
      dumpHeaders(request, webResource, b);

    // request body
    if (request.getPayload() != null) {

      Class<?> payloadClass;
      if (request.getPayload() instanceof Entry)
        payloadClass = Entry.class;
      else if (request.getPayload() instanceof SingleLink)
        payloadClass = SingleLink.class;
      else
        throw new UnsupportedOperationException("Unsupported payload: " + request.getPayload());

      StringWriter sw = new StringWriter();
      FormatWriter<Object> fw = (FormatWriter<Object>)(Object)
          FormatWriterFactory.getFormatWriter(payloadClass, null, type.toString(), null);
      fw.write(null, sw, request.getPayload());

      String entity = sw.toString();
      if (ODataConsumer.dump.requestBody())
        log(entity);

      // allow the client to override the default format writer content-type
      String contentType = request.getHeaders().containsKey(ODataConstants.Headers.CONTENT_TYPE)
          ? request.getHeaders().get(ODataConstants.Headers.CONTENT_TYPE)
          : fw.getContentType();

      b.entity(entity, contentType);
    }

    // execute request
    ClientResponse response = b.method(request.getMethod(), ClientResponse.class);

    if (ODataConsumer.dump.responseHeaders())
      dumpHeaders(response);
    int status = response.getStatus();
    for (int expStatus : expectedResponseStatus) {
      if (status == expStatus) {
        return response;
      }
    }
    throw new RuntimeException(String.format("Expected status %s, found %s:",
        Enumerable.create(expectedResponseStatus).join(" or "), status) + "\n" + response.getEntity(String.class));
  }

  Reader getFeedReader(ClientResponse response) {
    if (ODataConsumer.dump.responseBody()) {
      String textEntity = response.getEntity(String.class);
      dumpResponseBody(textEntity, response.getType());
      return new BOMWorkaroundReader(new StringReader(textEntity));
    }

    InputStream textEntity = response.getEntityInputStream();
    try {
      return new BOMWorkaroundReader(new InputStreamReader(textEntity, "UTF-8"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private XMLEventReader2 doXmlRequest(ClientResponse response) {

    if (ODataConsumer.dump.responseBody()) {
      String textEntity = response.getEntity(String.class);
      dumpResponseBody(textEntity, response.getType());
      return InternalUtil.newXMLEventReader(new BOMWorkaroundReader(new StringReader(textEntity)));
    }

    InputStream textEntity = response.getEntityInputStream();
    try {
      return InternalUtil.newXMLEventReader(new BOMWorkaroundReader(new InputStreamReader(textEntity, "UTF-8")));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void dumpResponseBody(String textEntity, MediaType type) {
    String logXml = textEntity;
    if (type.toString().contains("xml") || logXml != null && logXml.startsWith("<feed")) {
      try {
        logXml = XDocument.parse(logXml).toString(XmlFormat.INDENTED);
      } catch (Exception ignore) {}
    }
    log(logXml);
  }

  private void dumpHeaders(ClientResponse response) {
    log("Status: " + response.getStatus());
    dump(response.getHeaders());
  }

  private static boolean dontTryRequestHeaders;

  @SuppressWarnings("unchecked")
  private MultivaluedMap<String, Object> getRequestHeaders(WebResource.Builder b) {
    if (dontTryRequestHeaders)
      return null;

    //  protected MultivaluedMap<String, Object> metadata;
    try {
      Field f = PartialRequestBuilder.class.getDeclaredField("metadata");
      f.setAccessible(true);
      return (MultivaluedMap<String, Object>) f.get(b);
    } catch (Exception e) {
      dontTryRequestHeaders = true;
      return null;
    }

  }

  private void dumpHeaders(ODataClientRequest request, WebResource webResource, WebResource.Builder b) {
    log(request.getMethod() + " " + webResource);
    dump(getRequestHeaders(b));
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void dump(MultivaluedMap headers) {
    if (headers == null)
      return;

    for (Object header : headers.keySet())
      log(header + ": " + headers.getFirst(header));
  }

  private static void log(String message) {
    System.out.println(message);
  }

}
