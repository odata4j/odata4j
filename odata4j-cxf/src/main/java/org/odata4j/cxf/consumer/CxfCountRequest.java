package org.odata4j.cxf.consumer;

import org.odata4j.consumer.CountRequestBase;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.format.FormatType;

public class CxfCountRequest extends CountRequestBase {

  public CxfCountRequest(String serviceRootUri, FormatType formatType) {
    super(serviceRootUri, formatType);
  }

  @Override
  protected String getResponseAsString(FormatType formatType, ODataClientRequest request) {
    ODataCxfClient client = new ODataCxfClient(formatType);
    return client.getSingleValueRequest(request);
  }

}
