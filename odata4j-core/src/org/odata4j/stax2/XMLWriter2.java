package org.odata4j.stax2;



public interface XMLWriter2 {

	public abstract void startElement(String name);

	public abstract void startElement(QName2 qname);

	public abstract void startElement(QName2 qname, String xmlns);

	public abstract void writeAttribute(String localName, String value);

	public abstract void writeAttribute(QName2 qname, String value);

	public abstract void writeText(String content);

	public abstract void writeNamespace(String prefix, String namespaceUri);

	public abstract void startDocument();

	public abstract void endElement(String localName);

	public abstract void endDocument();

}