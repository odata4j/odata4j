package org.odata4j.producer;

import org.odata4j.core.OExtension;

/**
 * An optional extension that a producer can expose to control error responses.
 */
public interface ErrorResponseExtension extends OExtension<ODataProducer> {

  boolean returnInnerError();
}
