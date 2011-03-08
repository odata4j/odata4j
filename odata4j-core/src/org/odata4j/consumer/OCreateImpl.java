package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.odata4j.core.OCreate;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.xml.XmlFormatWriter;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

public class OCreateImpl<T, F extends Feed<E>, E extends Entry> implements OCreate<T> {

    private final ODataClient<F, E> client;
    private final EdmDataServices metadata;
    private final String serviceRootUri;
    private final String entitySetName;
    private OEntity parent;
    private String navProperty;

    private final List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    private final List<OLink> links = new ArrayList<OLink>();

    private final FeedCustomizationMapping fcMapping;
    
    public OCreateImpl(ODataClient<F, E> client, String serviceRootUri, EdmDataServices metadata, String entitySetName, FeedCustomizationMapping fcMapping) {
        this.client = client;
        this.serviceRootUri = serviceRootUri;
        this.metadata = metadata;
        this.entitySetName = entitySetName;
        this.fcMapping = fcMapping;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T execute() {

        E entry = client.createEntry(props, links);
        	
        StringBuilder url = new StringBuilder(serviceRootUri);
        if (parent != null) {
        	url.append(InternalUtil.getEntityRelId(parent))
        	   .append("/")
        	   .append(navProperty);
        } else {
        	url.append(entitySetName);
        }
       
        ODataClientRequest<E> request = ODataClientRequest.post(url.toString(), entry);
        
		final FormatParser<Entry> parser = FormatParserFactory.getParser(Entry.class, client.type);
		return (T) parser.toOEntity(client.createEntity(request),
				OEntity.class, metadata,
				metadata.getEdmEntitySet(entitySetName), fcMapping);
    }
    
	@SuppressWarnings("unchecked")
	@Override
	public T get() {
        EdmEntitySet entitySet = metadata.getEdmEntitySet(entitySetName);
		return (T)OEntities.create(entitySet, props, links, null);
	}

    @Override
    public OCreate<T> properties(OProperty<?>... props) {
        for(OProperty<?> prop : props)
            this.props.add(prop);
        return this;
    }

    @Override
    public OCreate<T> addToRelation(OEntity parent, String navProperty) {
    	if (parent == null || navProperty == null) {
    		throw new IllegalArgumentException("please provide the parent and the navProperty");
    	}
    	
    	this.parent = parent;
    	this.navProperty = navProperty;
    	return this;
    }

	@Override
	public OCreate<T> link(String navProperty, OEntity target) {
        EdmEntitySet entitySet = metadata.getEdmEntitySet(entitySetName);
		EdmNavigationProperty navProp = entitySet.type.getNavigationProperty(navProperty);
        if (navProp == null) {
        	throw new IllegalArgumentException("unknown navigation property "
        			+ navProperty);
        }
		if (navProp.toRole.multiplicity == EdmMultiplicity.MANY) {
			throw new IllegalArgumentException("many associations are not supported");
		}
		
		StringBuilder href = new StringBuilder(serviceRootUri);
		if (!serviceRootUri.endsWith("/")) {
			href.append("/");
		}
		href.append(InternalUtil.getEntityRelId(target));
		
		//	TODO get rid of XmlFormatWriter
		String rel = XmlFormatWriter.related +  navProperty;
		
		this.links.add(OLinks.relatedEntity(rel, navProperty, href.toString()));
		return this;
	}

	@Override
	public OCreate<T> inline(String navProperty, OEntity... entities) {
        EdmEntitySet entitySet = metadata.getEdmEntitySet(entitySetName);
		EdmNavigationProperty navProp = entitySet.type.getNavigationProperty(navProperty);
		if (navProp == null) {
			throw new IllegalArgumentException("unknown navigation property "
					+ navProperty);
		}

		//	TODO get rid of XmlFormatWriter
		String rel = XmlFormatWriter.related + navProperty;
		String href = entitySetName + "/" + navProperty;
		if (navProp.toRole.multiplicity == EdmMultiplicity.MANY) {
			links.add(OLinks.relatedEntitiesInline(rel, navProperty, href,
					Arrays.asList(entities)));
		} else {
			if (entities.length > 1) {
				throw new IllegalArgumentException(
						"only one entity is allowed for this navigation property "
								+ navProperty);
			}
			links.add(OLinks.relatedEntityInline(rel, navProperty, href,
					entities.length > 0 ? entities[0] : null));
		}

		return this;
	}

}
