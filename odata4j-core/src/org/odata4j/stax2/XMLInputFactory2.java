package org.odata4j.stax2;

import java.io.Reader;

public interface XMLInputFactory2 {

	public XMLEventReader2 createXMLEventReader(Reader reader);

}
