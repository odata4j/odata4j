package org.odata4j.consumer;

import org.odata4j.format.FormatType;

abstract public class CountRequestBase implements CountRequest {

  private String baseUri;
  private Integer top;
  private FormatType formatType;
  private String entitySetName;

  public CountRequestBase(String serviceRootUri, FormatType formatType) {
    this.baseUri = serviceRootUri;
    this.formatType = formatType;
  }

  @Override
  public int execute() {
    String uri = this.baseUri;

    if (this.entitySetName != null) {
      uri = uri + this.entitySetName + "/";
    }

    uri = uri + "$count";

    if (this.top != null) {
      uri = uri + "?$top=" + this.top;
    }

    ODataClientRequest request = ODataClientRequest.get(uri);
    String valueString = this.getResponseAsString(this.formatType, request);
    int value = Integer.parseInt(valueString);

    return value;
  }

  abstract protected String getResponseAsString(FormatType formatType, ODataClientRequest request);

  @Override
  public void setEntitySetName(String entitySetName) {
    this.entitySetName = entitySetName;
  }

  @Override
  public CountRequest top(int top) {
    this.top = Integer.valueOf(top);
    return this;
  }

}
