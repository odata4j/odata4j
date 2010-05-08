package org.odata4j.producer.resources;

import java.io.StringReader;
import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.xml.AtomFeedFormatParser;
import org.odata4j.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.xml.AtomFeedFormatParser.DataServicesAtomEntry;

import com.sun.jersey.api.core.HttpRequestContext;

public abstract class BaseResource {

    protected List<OProperty<?>> getRequestEntityProperties(HttpRequestContext request) {
        String requestEntity = request.getEntity(String.class);

        XMLEventReader2 reader = InternalUtil.newXMLEventReader(new StringReader(requestEntity));
        AtomEntry entry = AtomFeedFormatParser.parseFeed(reader).entries.iterator().next();
        DataServicesAtomEntry dsae = (DataServicesAtomEntry) entry;
        OEntity entity = InternalUtil.toOEntity(dsae,null);

        final List<OProperty<?>> properties = entity.getProperties();
        return properties;
    }
}
