package org.odata4j.producer;

import org.odata4j.core.OEntity;

public interface EntityResponse extends BaseResponse {
    public OEntity getEntity();
}
