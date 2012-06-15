
package org.odata4j.producer;

import java.io.InputStream;
import org.odata4j.core.OEntity;

/**
* an optional service that a producer can expose to work with Media Link Entries
*/
public interface OMediaLinkProvider {
  
  InputStream getInputStreamForMediaLinkEntry(OEntity mle, String etag, QueryInfo query);
  
  String getMediaLinkContentType(OEntity mle);
}
