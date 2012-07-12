package org.odata4j.producer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

/**
 * This class provides implementations of the {@link ErrorResponseExtension} interface for common
 * use cases.
 */
public class ErrorResponseExtensions {

  /**
   * Constant used as default system property and query parameter.
   *
   * @see ErrorResponseExtensions#RETURN_INNER_ERRORS_BASED_ON_DEFAULT_SYSTEM_PROPERTY
   * @see ErrorResponseExtensions#RETURN_INNER_ERRORS_BASED_ON_DEFAULT_QUERY_PARAMETER
   */
  public static final String ODATA4J_DEBUG = "odata4j.debug";

  /**
   * With this ErrorResponseExtension, inner errors are always returned.
   */
  public static final ErrorResponseExtension ALWAYS_RETURN_INNER_ERRORS = new ErrorResponseExtension() {

    public boolean returnInnerError(HttpHeaders httpHeaders, UriInfo uriInfo) {
      return true;
    }
  };

  /**
   * With this ErrorResponseExtension, inner errors are returned when the default system property
   * is set to {@code true}.
   *
   * @see ErrorResponseExtensions#ODATA4J_DEBUG
   */
  public static final ErrorResponseExtension RETURN_INNER_ERRORS_BASED_ON_DEFAULT_SYSTEM_PROPERTY = RETURN_INNER_ERRORS_BASED_ON_CUSTOM_SYSTEM_PROPERTY(ODATA4J_DEBUG);

  /**
   * With this ErrorResponseExtension, inner errors are returned when a custom system property is
   * set to {@code true}.
   *
   * @param customProperty  the custom property key
   */
  public static final ErrorResponseExtension RETURN_INNER_ERRORS_BASED_ON_CUSTOM_SYSTEM_PROPERTY(final String customProperty) {
    return new ErrorResponseExtension() {

      public boolean returnInnerError(HttpHeaders httpHeaders, UriInfo uriInfo) {
        return Boolean.parseBoolean(System.getProperty(customProperty));
      }
    };
  };

  /**
   * With this ErrorResponseExtension, inner errors are returned when the default query parameter
   * is set to {@code true}.
   *
   * @see ErrorResponseExtensions#ODATA4J_DEBUG
   */
  public static final ErrorResponseExtension RETURN_INNER_ERRORS_BASED_ON_DEFAULT_QUERY_PARAMETER = RETURN_INNER_ERRORS_BASED_ON_CUSTOM_QUERY_PARAMETER(ODATA4J_DEBUG);

  /**
   * With this ErrorResponseExtension, inner errors are returned when a custom query parameter is
   * set to {@code true}.
   *
   * @param customParameter  the custom parameter key
   */
  public static final ErrorResponseExtension RETURN_INNER_ERRORS_BASED_ON_CUSTOM_QUERY_PARAMETER(final String customParameter) {
    return new ErrorResponseExtension() {

      public boolean returnInnerError(HttpHeaders httpHeaders, UriInfo uriInfo) {
        return Boolean.parseBoolean(uriInfo.getQueryParameters().getFirst(customParameter));
      }
    };
  };
}
