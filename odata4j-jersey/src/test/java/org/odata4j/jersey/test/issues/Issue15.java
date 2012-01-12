package org.odata4j.jersey.test.issues;

import org.junit.Ignore;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.test.issues.AbstractIssue15;

@Ignore
public class Issue15 extends AbstractIssue15 {

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    return ODataJerseyConsumer.newBuilder(endpointUri).setFormatType(formatType).build();
  }

}
