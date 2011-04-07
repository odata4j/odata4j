package org.odata4j.core;

import java.util.List;

public interface ORelatedEntitiesLinkInline extends ORelatedEntitiesLink {
	public abstract List<OEntity> getRelatedEntities();
}
