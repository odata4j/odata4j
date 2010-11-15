package org.odata4j.producer;

import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmMultiplicity;

public class Responses {

    public static EntitiesResponse entities(final List<OEntity> entities, final EdmEntitySet entitySet, final Integer inlineCount, final String skipToken) {
        return new EntitiesResponse() {

            @Override
            public List<OEntity> getEntities() {
                return entities;
            }

            @Override
            public EdmEntitySet getEntitySet() {
                return entitySet;
            }

            @Override
            public Integer getInlineCount() {
                return inlineCount;
            }

            @Override
            public String getSkipToken() {
                return skipToken;
            }
        };
    }

    public static NavPropertyResponse navProperty(
            final List<OEntity> entities,
            final EdmEntitySet entitySet,
            final EdmMultiplicity multiplicity,
            final Integer inlineCount,
            final String skipToken) {
        return new NavPropertyResponse() {

            @Override
            public List<OEntity> getEntities() {
                return entities;
            }

            @Override
            public EdmEntitySet getEntitySet() {
                return entitySet;
            }

            @Override
            public EdmMultiplicity getMultiplicity() {
                return multiplicity;
            }

            @Override
            public Integer getInlineCount() {
                return inlineCount;
            }

            @Override
            public String getSkipToken() {
                return skipToken;
            }
        };
    }
}
