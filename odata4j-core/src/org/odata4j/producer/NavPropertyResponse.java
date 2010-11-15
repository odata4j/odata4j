package org.odata4j.producer;

import org.odata4j.edm.EdmMultiplicity;

public interface NavPropertyResponse extends EntitiesResponse {

    public EdmMultiplicity getMultiplicity();
}
