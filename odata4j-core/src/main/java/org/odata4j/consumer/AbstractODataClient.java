package org.odata4j.consumer;

import org.odata4j.format.FormatType;

public abstract class AbstractODataClient implements ODataClient {

  protected AbstractODataClient(FormatType formatType) {
    this.formatType = formatType;
  }

  private FormatType formatType;

  @Override
  public FormatType getFormatType() {
    return this.formatType;
  }

}
