package org.odata4j.format;

import javax.ws.rs.core.MediaType;

import org.odata4j.format.json.JsonFeedFormatParser.JsonEntry;
import org.odata4j.format.json.JsonFeedFormatParser.JsonFeed;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomFeed;

public enum FormatType {
    ATOM(AtomFeed.class, AtomEntry.class, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML),
    JSON(JsonFeed.class, JsonEntry.class, MediaType.APPLICATION_JSON);

    private FormatType(Class<?> feedClass, Class<?> entryClass, String... mediaTypes) {
    	this.feedClass = feedClass;
    	this.entryClass = entryClass;
    	this.mediaTypes = mediaTypes;
    }
    
    private final String[] mediaTypes;
    private final Class<?> feedClass;
    private final Class<?> entryClass;
    
    public String[] getMediaTypes() {
    	return mediaTypes;
    }
    
	public Class<?> getFeedClass() {
    	return feedClass;
    }
    
	public Class<?> getEntryClass() {
    	return entryClass;
    }
	
    public static FormatType parse(String format){
        if ("json".equalsIgnoreCase(format)) return JSON;
        if ("atom".equalsIgnoreCase(format)) return ATOM;
        throw new UnsupportedOperationException("Unsupported format " + format);
    }
}
