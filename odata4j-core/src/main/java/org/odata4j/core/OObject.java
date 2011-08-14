package org.odata4j.core;

import org.odata4j.edm.EdmType;

/**
 * OData value/instance object with the given {@link EdmType}.
 *
 * @see OEntity
 * @see OSimpleObject
 * @see OComplexObject
 * @see OCollection
 */
public interface OObject {

  EdmType getType();

}
