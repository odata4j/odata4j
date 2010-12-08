package org.odata4j.core;

import java.util.List;

public class OLinks {

    public static ORelatedEntitiesLink relatedEntities(final String relation, final String title, final String href, final List<OEntity> relatedEntities){
        return new ORelatedEntitiesLink(){

            @Override
            public String getHref() {
                return href;
            }

            @Override
            public String getRelation() {
                return relation;
            }

            @Override
            public String getTitle() {
               return title;
            }

			@Override
			public List<OEntity> getRelatedEntities() {
				return relatedEntities;
			}};
            
            
    }
    
    public static ORelatedEntityLink relatedEntity(final String relation, final String title, final String href, final OEntity relatedEntity){
        return new ORelatedEntityLink(){

            @Override
            public String getHref() {
                return href;
            }

            @Override
            public String getRelation() {
                return relation;
            }

            @Override
            public String getTitle() {
               return title;
            }

			@Override
			public OEntity getRelatedEntity() {
				return relatedEntity;
			}};
    }
}
