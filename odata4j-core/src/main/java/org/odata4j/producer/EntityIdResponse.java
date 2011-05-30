package org.odata4j.producer;

import java.util.Collection;

import org.odata4j.core.OEntityId;
import org.odata4j.edm.EdmMultiplicity;

public interface EntityIdResponse {

  EdmMultiplicity getMultiplicity();

  Collection<OEntityId> getEntities();
}
