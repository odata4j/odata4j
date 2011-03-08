package org.odata4j.format.xml;

import java.io.Writer;

import org.odata4j.core.ODataConstants;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomEntry;
import org.odata4j.format.xml.AtomFeedFormatParser.DataServicesAtomEntry;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class AtomRequestEntryFormatWriter implements
		FormatWriter<AtomEntry> {

	@Override
	public void write(ExtendedUriInfo uriInfo, Writer w,
			AtomEntry target) {
		new AtomEntryFormatWriter().writeRequestEntry(w, (DataServicesAtomEntry)target);
	}

	@Override
	public String getContentType() {
		return ODataConstants.APPLICATION_XML_CHARSET_UTF8;
	}

}
