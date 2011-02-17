package org.odata4j.consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

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
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.format.xml.XmlFormatWriter;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

public class OCreateImpl<T> implements OCreate<T> {

    private final ODataClient client;
    private final EdmDataServices metadata;
    private final String serviceRootUri;
    private final String entitySetName;
    private OEntity parent;
    private String navProperty;

    private final List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    private final List<OLink> links = new ArrayList<OLink>();

    private final FeedCustomizationMapping fcMapping;
    
    public OCreateImpl(ODataClient client, String serviceRootUri, EdmDataServices metadata, String entitySetName, FeedCustomizationMapping fcMapping) {
        this.client = client;
        this.serviceRootUri = serviceRootUri;
        this.metadata = metadata;
        this.entitySetName = entitySetName;
        this.fcMapping = fcMapping;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T execute() {

        DataServicesAtomEntry entry = new DataServicesAtomEntry();
        entry.contentType = MediaType.APPLICATION_XML;
        entry.properties = props;
        entry.links = links;
        
        StringBuilder url = new StringBuilder(serviceRootUri);
        if (parent != null) {
        	url.append(InternalUtil.getEntityRelId(parent))
        	   .append("/")
        	   .append(navProperty);
        } else {
        	url.append(entitySetName);
        }
        
        ODataClientRequest request = ODataClientRequest.post(url.toString(), entry);

        DataServicesAtomEntry dsae = client.createEntity(request);
        OEntity rt = InternalUtil.toOEntity(metadata, 
        		metadata.getEdmEntitySet(entitySetName), dsae,fcMapping);
        return (T) rt;
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
		StringBuilder href = new StringBuilder(serviceRootUri);
		if (!serviceRootUri.endsWith("/")) {
			href.append("/");
		}
		href.append(InternalUtil.getEntityRelId(target));
		
		String rel = XmlFormatWriter.related +  navProperty;
		
		this.links.add(OLinks.link(rel, navProperty, href.toString()));
		return this;
	}

	@Override
	public OCreate<T> inline(String navProperty, OEntity... entities) {
        EdmEntitySet entitySet = metadata.getEdmEntitySet(entitySetName);
		EdmNavigationProperty navProp = entitySet.type
				.getNavigationProperty(navProperty);
		if (navProp == null) {
			throw new IllegalArgumentException("unknown navigation property "
					+ navProperty);
		}

		String rel = XmlFormatWriter.related + navProperty;
		String href = entitySetName + "/" + navProperty;
		if (navProp.toRole.multiplicity == EdmMultiplicity.MANY) {
			links.add(OLinks.relatedEntities(rel, navProperty, href,
					Arrays.asList(entities)));
		} else {
			if (entities.length > 1) {
				throw new IllegalArgumentException(
						"only one entity is allowed for this navigation property "
								+ navProperty);
			}
			links.add(OLinks.relatedEntity(rel, navProperty, href,
					entities.length > 0 ? entities[0] : null));
		}

		return this;
	}

}
