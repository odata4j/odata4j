package org.odata4j.format.json;

import java.io.Reader;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.FormatParser;
import org.odata4j.internal.FeedCustomizationMapping;

public class JsonFeedFormatParser implements FormatParser<JsonFeedFormatParser.JsonFeed> {
	
	public static class JsonFeed implements Feed<JsonEntry> {

		@Override
		public String getNext() {
			return null;
		}

		@Override
		public Iterable<JsonEntry> getEntries() {
			return null;
		}

	}
	
	public static class JsonEntry implements Entry {
	    public String etag;
	    public List<OProperty<?>> properties;
	    public List<OLink> links;

		public String getType() {
	    	return MediaType.APPLICATION_JSON;
	    }
	}

	@Override
	public JsonFeed parse(Reader reader) {
		return null;
	}

	@Override
	public <E> E toOEntity(Entry entry, Class<E> entityType,
			EdmDataServices metadata, EdmEntitySet entitySet,
			FeedCustomizationMapping fcMapping) {
		return null;
	}

}
