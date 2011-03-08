package org.odata4j.format.json;

import java.io.Reader;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.json.JsonFeedFormatParser.JsonEntry;
import org.odata4j.internal.FeedCustomizationMapping;

public class JsonEntryFormatParser implements FormatParser<JsonEntry> {

	@Override
	public JsonEntry parse(Reader reader) {
		return null;
	}

	@Override
	public <E> E toOEntity(Entry entry, Class<E> entityType,
			EdmDataServices metadata, EdmEntitySet entitySet,
			FeedCustomizationMapping fcMapping) {
		return null;
	}

}
