package org.odata4j.producer.resources;

import java.io.StringReader;
import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.format.xml.AtomFeedFormatParser;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.internal.InternalUtil;
import org.odata4j.stax2.XMLEventReader2;

import com.sun.jersey.api.core.HttpRequestContext;

public abstract class BaseResource {

    public static OEntity ConvertFromString(String requestEntity) {
        XMLEventReader2 reader = InternalUtil.newXMLEventReader(new StringReader(requestEntity));
        AtomEntry entry = AtomFeedFormatParser.parseFeed(reader).entries.iterator().next();
        DataServicesAtomEntry dsae = (DataServicesAtomEntry) entry;

        return InternalUtil.toOEntity(dsae, null);
    }

    protected OEntity getRequestEntity(HttpRequestContext request) {
        String requestEntity = request.getEntity(String.class);
        return ConvertFromString(requestEntity);
    }

    protected List<OProperty<?>> getRequestEntityProperties(HttpRequestContext request) {
        return getRequestEntity(request).getProperties();
    }
}
