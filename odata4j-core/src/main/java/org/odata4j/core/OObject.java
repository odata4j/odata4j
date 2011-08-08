package org.odata4j.core;

import org.odata4j.edm.EdmBaseType;

/**
 * base OData object class
 *
 * @see OEntity
 * @see OSimpleObject
 * @see OComplexObject
 * @see OCollection
 */
public interface OObject {

    public EdmBaseType getType();

}
