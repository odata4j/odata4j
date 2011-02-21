package org.odata4j.producer;

import org.odata4j.core.OProperty;

public interface PropertyResponse extends BaseResponse {

	public OProperty<?> getProperty();
}
