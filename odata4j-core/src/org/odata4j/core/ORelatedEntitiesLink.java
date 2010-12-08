package org.odata4j.core;

import java.util.List;

public interface ORelatedEntitiesLink extends OLink {
	public abstract List<OEntity> getRelatedEntities();
}
