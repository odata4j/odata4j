package org.odata4j.producer;

import java.io.InputStream;

import org.odata4j.core.OEntity;
import org.odata4j.core.OExtension;

/**
 * An optional extension that a producer can expose to work with Media Link Entries.
 */
public interface OMediaLinkExtension extends OExtension<ODataProducer> {

  InputStream getInputStreamForMediaLinkEntry(OEntity mle, String etag, EntityQueryInfo query);

  String getMediaLinkContentType(OEntity mle);
}
