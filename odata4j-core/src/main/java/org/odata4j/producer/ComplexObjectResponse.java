package org.odata4j.producer;

import org.odata4j.core.OComplexObject;

/**
 * a response that includes a single instance of an EdmComplexType
 */
public interface ComplexObjectResponse extends BaseResponse  {

    OComplexObject getObject();

}
