package org.odata4j.core;

import org.odata4j.exceptions.ODataProducerException;

public interface OCountRequest {

  OCountRequest setEntitySetName(String entitySetName);

  OCountRequest top(int top);

  /**
   * @throws ODataProducerException  error from the producer
   */
  int execute() throws ODataProducerException;
}
