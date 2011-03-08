package org.odata4j.format.xml;

import java.io.Reader;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.internal.InternalUtil;

public class AtomEntryFormatParser implements FormatParser<AtomEntry> {

	@Override
	public AtomEntry parse(Reader reader) {
		return AtomFeedFormatParser.parseFeed(InternalUtil
				.newXMLEventReader(reader)).entries.iterator().next();
	}

	@Override
	public <E> E toOEntity(Entry entry, Class<E> entityType,
			EdmDataServices metadata, EdmEntitySet entitySet,
			FeedCustomizationMapping fcMapping) {
		return InternalUtil.toEntity(entityType, metadata, entitySet,
				(DataServicesAtomEntry) entry, fcMapping);
	}

}
