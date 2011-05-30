package org.odata4j.producer.resources;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;

import org.odata4j.core.ODataConstants;
import org.odata4j.producer.resources.ODataBatchProvider.HTTP_METHOD;

import com.sun.jersey.api.core.ExtendedUriInfo;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public class BatchBodyPart {

  private MultivaluedMap<String, String> headers = new StringKeyStringValueIgnoreCaseMultivaluedMap();
  private HTTP_METHOD httpMethod;
  private HttpContext context;
  private String entity;
  private String uri;
  private String uriLast;

  BatchBodyPart(HttpContext context) {
    this.context = context;
  }

  public String getEntity() {
    return this.entity;
  }

  public void setEntity(String entity) {
    this.entity = entity;
  }

  public MultivaluedMap<String, String> getHeaders() {
    return this.headers;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;

    int i = this.uri.lastIndexOf('/');
    if (this.uri.length() > i) {
      this.uriLast = this.uri.substring(this.uri.lastIndexOf('/') + 1);
    } else {
      this.uriLast = this.uri;
    }
  }

  public HTTP_METHOD getHttpMethod() {
    return httpMethod;
  }

  public void setHttpMethod(HTTP_METHOD httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getEntitySetName() {
    int i = this.uriLast.indexOf('(');
    return i != -1
        ? this.uriLast.substring(0, i)
        : this.uriLast;
  }

  public String getEntityKey() {
    int i = this.uriLast.indexOf('(');
    return i > -1 && i < this.uriLast.length()
        ? this.uriLast.substring(i)
        : null;
  }

  public HttpContext createHttpContext() {
    final ExtendedUriInfo exUriInfo = this.context.getUriInfo();
    return new HttpContext() {

      @Override
      public ExtendedUriInfo getUriInfo() {
        return exUriInfo;
      }

      @Override
      public HttpRequestContext getRequest() {
        return createHttpRequestContext();
      }

      @Override
      public HttpResponseContext getResponse() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Map<String, Object> getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isTracingEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void trace(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    };
  }

  private HttpRequestContext createHttpRequestContext() {
    final MultivaluedMap<String, String> headersLocal = this.getHeaders();
    final String methodName = this.getHttpMethod().name();
    final String entityLocal = this.getEntity();

    HttpRequestContext hrc = new HttpRequestContext() {

      @Override
      public URI getBaseUri() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      @Deprecated
      public List<MediaType> getAcceptableMediaTypes(List<QualitySourceMediaType> priorityMediaTypes) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      @Deprecated
      public MediaType getAcceptableMediaType(List<MediaType> mediaTypes) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public String getHeaderValue(String name) {
        if (name.equals(ODataConstants.Headers.X_HTTP_METHOD)) {
          return methodName;
        }

        if (headersLocal.containsKey(name)) {
          return headersLocal.getFirst(name);
        }

        return null;
      }

      @Override
      @SuppressWarnings("unchecked")
      public <T> T getEntity(Class<T> type) throws WebApplicationException {
        if (type == null || type != String.class) {
          throw new IllegalStateException("Support only String Entity instance.");
        }

        return (T) entityLocal;
      }

      @Override
      public UriBuilder getBaseUriBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public URI getRequestUri() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public UriBuilder getRequestUriBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public URI getAbsolutePath() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public UriBuilder getAbsolutePathBuilder() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public String getPath() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public String getPath(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public List<PathSegment> getPathSegments() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public List<PathSegment> getPathSegments(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public MultivaluedMap<String, String> getQueryParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public MultivaluedMap<String, String> getCookieNameValueMap() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public <T> T getEntity(Class<T> type, Type genericType, Annotation[] as) throws WebApplicationException {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Form getFormParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public List<String> getRequestHeader(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public MultivaluedMap<String, String> getRequestHeaders() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public List<MediaType> getAcceptableMediaTypes() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public List<Locale> getAcceptableLanguages() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public MediaType getMediaType() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Locale getLanguage() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Map<String, Cookie> getCookies() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public String getMethod() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Variant selectVariant(List<Variant> list) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public ResponseBuilder evaluatePreconditions(EntityTag et) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public ResponseBuilder evaluatePreconditions(Date date) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public ResponseBuilder evaluatePreconditions(Date date, EntityTag et) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public ResponseBuilder evaluatePreconditions() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Principal getUserPrincipal() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isUserInRole(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isSecure() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public String getAuthenticationScheme() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean isTracingEnabled() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void trace(String message) {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    };

    return hrc;
  }
}
