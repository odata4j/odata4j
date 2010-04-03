package org.odata4j.producer;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;

public interface EntityResponse {

    public EdmEntitySet getEntitySet();

    public OEntity getEntity();
}
