package org.odata4j.producer.resources;

import java.io.StringReader;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.xml.AtomFeedFormatParser;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;

import com.sun.jersey.api.core.HttpRequestContext;

public abstract class BaseResource {

   
    protected OEntity getRequestEntity(HttpRequestContext request, EdmDataServices metadata, String entitySetName) {
        String requestEntity = request.getEntity(String.class);
        return convertFromString(requestEntity, metadata, entitySetName);
    }
    
    private static OEntity convertFromString(String requestEntity, EdmDataServices metadata, String entitySetName) {
        XMLEventReader2 reader = InternalUtil.newXMLEventReader(new StringReader(requestEntity));
        AtomEntry entry = AtomFeedFormatParser.parseFeed(reader).entries.iterator().next();
        DataServicesAtomEntry dsae = (DataServicesAtomEntry) entry;

        return InternalUtil.toOEntity(metadata, metadata.getEdmEntitySet(entitySetName), dsae, null);
    }

}
