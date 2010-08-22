package org.odata4j.core;

public class OLinks {

    public static ORelatedEntitiesLink relatedEntities(final String relation, final String title, final String href){
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
            }};
    }
    
    public static ORelatedEntityLink relatedEntity(final String relation, final String title, final String href){
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
            }};
    }
}
