package org.odata4j.format.xml;

import java.io.Reader;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.internal.InternalUtil;

public class AtomEntryFormatParser implements FormatParser<Entry> {

	protected EdmDataServices metadata;
	protected String entitySetName;
	
	public AtomEntryFormatParser(EdmDataServices metadata, String entitySetName) {
		this.metadata = metadata;
		this.entitySetName = entitySetName;
	}
	
	@Override
	public Entry parse(Reader reader) {
		return new AtomFeedFormatParser(metadata, entitySetName).parseFeed(InternalUtil
				.newXMLEventReader(reader)).entries.iterator().next();
	}

}
