package org.odata4j.producer;

import org.odata4j.core.OCollection;
import org.odata4j.core.OObject;

/**
 * Encapsulates a client response that is a OCollection of OObjects
 */
public interface CollectionResponse<T  extends OObject> extends BaseResponse {

  OCollection<T> getCollection();

}
