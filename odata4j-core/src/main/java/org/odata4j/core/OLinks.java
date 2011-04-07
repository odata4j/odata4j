package org.odata4j.core;

import java.util.List;

/** Create links between entities.  This static class provides the opportunity to create five different types
 * of links.
 *
 * TODO: WTF is going on here?  What are these different types?
 * It looks like inline means that the values go back right now, while the alternative is that a link goes back
 * Is the other dimension multiplicity?
 * 
 * And then what's the deal with Link itself?  This just seems to cause the server to cough.
 */
public class OLinks {
	
    public static OLink link(final String relation, final String title, final String href){
    	return new OLinkImpl(OLink.class,relation,title,href);
    }
    
    public static ORelatedEntitiesLink relatedEntities(final String relation, final String title, final String href){
    	return new ORelatedEntitiesLinkImpl(relation,title,href);
    }
    
    public static ORelatedEntitiesLinkInline relatedEntitiesInline(final String relation, final String title, final String href, final List<OEntity> relatedEntities){
    	return new ORelatedEntitiesLinkInlineImpl(relation,title,href,relatedEntities);
    }
    
    public static ORelatedEntityLink relatedEntity(final String relation, final String title, final String href){
    	return new ORelatedEntityLinkImpl(relation,title,href);
    }
    
    public static ORelatedEntityLinkInline relatedEntityInline(final String relation, final String title, final String href, final OEntity relatedEntity){
       return new ORelatedEntityLinkInlineImpl(relation,title,href,relatedEntity);
    }
    
    
    
    
    
    
    private static class OLinkImpl implements OLink{
    	private final Class<?> interfaceType;
    	
    	private final String title;
    	private final String relation;
    	private final String href;
    	
    	public OLinkImpl(Class<?> interfaceType, String relation, String title,String href){
    		this.interfaceType = interfaceType;
    		this.title = title;
    		this.relation = relation;
    		this.href = href;
    	}
    	
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getRelation() {
			return relation;
		}

		@Override
		public String getHref() {
			return href;
		}
		
		@Override
		public String toString() {
			return String.format("%s[rel=%s,title=%s,href=%s]",interfaceType.getSimpleName(),relation,title,href);
		}
    	
    }
    
    private static class ORelatedEntitiesLinkImpl extends OLinkImpl implements ORelatedEntitiesLink{
		public ORelatedEntitiesLinkImpl(String relation, String title,String href) {
			super(ORelatedEntitiesLink.class, relation, title, href);
		}
    }
    private static class ORelatedEntitiesLinkInlineImpl extends OLinkImpl implements ORelatedEntitiesLinkInline{
    	private final List<OEntity> relatedEntities;
    	public ORelatedEntitiesLinkInlineImpl(String relation,String title, String href, List<OEntity> relatedEntities) {
			super(ORelatedEntitiesLinkInline.class, relation, title, href);
			this.relatedEntities = relatedEntities;
		}
		@Override
		public List<OEntity> getRelatedEntities() {
			return relatedEntities;
		}
    }
    private static class ORelatedEntityLinkImpl extends OLinkImpl implements ORelatedEntityLink{
		public ORelatedEntityLinkImpl(String relation, String title,String href) {
			super(ORelatedEntityLink.class, relation, title, href);
		}
    }
    private static class ORelatedEntityLinkInlineImpl extends OLinkImpl implements ORelatedEntityLinkInline{
    	private final OEntity relatedEntity;
    	public ORelatedEntityLinkInlineImpl(String relation,String title, String href, OEntity relatedEntity) {
			super(ORelatedEntityLinkInline.class, relation, title, href);
			this.relatedEntity = relatedEntity;
		}
		@Override
		public OEntity getRelatedEntity() {
			return relatedEntity;
		}
		
    }
    
    
}
