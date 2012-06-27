package org.odata4j.producer;

import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.HttpHeaders;

import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OExtension;
import org.odata4j.edm.EdmEntitySet;

/**
 * An optional extension that a producer can expose to work with Media Link Entries.
 */
public interface OMediaLinkExtension extends OExtension<ODataProducer> {

  /**
   * get an InputStream that streams the bytes of the media resource associated
   * with the given media link entry entity.
   * 
   * @param mle   the media link entry entity
   * @param etag  for future extension
   * @param query additional request information
   * @return InputStream for the media resource
   */
  InputStream getInputStreamForMediaLinkEntry(OEntity mle, String etag, EntityQueryInfo query);

  /**
   * get an OutputStream for the purpose of creating a media resource.
   * 
   * @param mle   the media link entry entity
   * @param etag  for future extension
   * @param query additional request information
   * @return 
   */
  OutputStream getOutputStreamForMediaLinkEntryCreate(OEntity mle, String etag, QueryInfo query);

  /**
   * get an OutputStream for the purpose of updating an existing media resource.
   * 
   * @param mle   the media link entry entity
   * @param etag  for future extension
   * @param query additional request information
   * @return 
   */
  OutputStream getOutputStreamForMediaLinkEntryUpdate(OEntity mle, String etag, QueryInfo query);

  /**
   * delete the media resource defined by the given media link entry entity
   * @param mle     an existing media link entry
   * @param query   additional request information
   */
  void deleteStream(OEntity mle, QueryInfo query);

  /**
   * get the mime content type for the given media link entry entity
   * @param mle
   * @return the mime content type
   */
  String getMediaLinkContentType(OEntity mle);

  /**
   * create an OEntity for a new media link entry request just received.
   * 
   * @param entitySet
   * @param httpHeaders - Atom protocol says the Slug header can contain additional create info.
   * @return 
   */
  OEntity createMediaLinkEntry(EdmEntitySet entitySet, HttpHeaders httpHeaders);

  /**
  * get an OEntity for an existing media link entry with the given key.
  * 
  * @param entitySet
  * @param key         - entity key
  * @param httpHeaders - Atom protocol says the Slug header can contain additional create info.
  * @return 
  */
  OEntity getMediaLinkEntryForUpdateOrDelete(EdmEntitySet entitySet, OEntityKey key, HttpHeaders httpHeaders);

  /**
   * after a create or update of a media resource, more information may be
   * available about the media link entity that was created before the media
   * resource bits were processed.  updateMediaLinkEntry will be called after the
   * media resource bits have been written to outStream and outStream has been
   * closed.
   * 
   * Note: this is only necessary because OEntity is immutable
   * 
   * @param mle
   * @param outStream
   * @return an updated Media Link Entity
   */
  OEntity updateMediaLinkEntry(OEntity mle, OutputStream outStream);

}
