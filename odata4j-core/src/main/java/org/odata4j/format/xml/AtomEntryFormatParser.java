package org.odata4j.format.xml;

import java.io.Reader;

import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.internal.InternalUtil;

public class AtomEntryFormatParser implements FormatParser<Entry> {

	protected EdmDataServices metadata;
	protected String entitySetName;
	protected OEntityKey entityKey;
	
	public AtomEntryFormatParser(EdmDataServices metadata, String entitySetName, OEntityKey entityKey) {
		this.metadata = metadata;
		this.entitySetName = entitySetName;
		this.entityKey = entityKey;
	}
	
	@Override
	public Entry parse(Reader reader) {
		return new AtomFeedFormatParser(metadata, entitySetName, entityKey)
			.parseFeed(InternalUtil.newXMLEventReader(reader))
			.entries.iterator().next();
	}

}
