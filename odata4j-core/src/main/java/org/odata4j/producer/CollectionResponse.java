package org.odata4j.producer;

import org.odata4j.core.OCollection;
import org.odata4j.core.OObject;

/**
 * An <code>CollectionResponse</code> is a response to a client request expecting a collection of OData objects.
 * <p>The {@link Responses} static factory class can be used to create <code>CollectionResponse</code> instances.</p>
 */
public interface CollectionResponse<T extends OObject> extends BaseResponse {

  OCollection<T> getCollection();

}
