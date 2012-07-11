package org.odata4j.jersey.consumer;

import org.odata4j.consumer.CountRequestBase;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.format.FormatType;

public class JerseyCountRequest extends CountRequestBase {

  private ODataJerseyClient client;

  public JerseyCountRequest(ODataJerseyClient client, String serviceRootUri, FormatType formatType) {
    super(serviceRootUri, formatType);

    this.client = client;
  }

  @Override
  protected String getResponseAsString(FormatType formatType, ODataClientRequest request) {
    String response = this.client.requestBody(formatType, request);
    return response;
  }

}
