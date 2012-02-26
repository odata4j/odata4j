package org.odata4j.stax2.staximpl;

import java.io.Reader;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.odata4j.core.Throwables;
import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.EndElement2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.XMLEventWriter2;
import org.odata4j.stax2.XMLFactoryProvider2;
import org.odata4j.stax2.XMLInputFactory2;
import org.odata4j.stax2.XMLOutputFactory2;
import org.odata4j.stax2.XMLWriter2;
import org.odata4j.stax2.XMLWriterFactory2;

public class StaxXMLFactoryProvider2 extends XMLFactoryProvider2 {

  public static QName toQName(QName2 qname) {
    if (qname.getPrefix() == null)
      return new QName(qname.getNamespaceUri(), qname.getLocalPart());
    return new QName(qname.getNamespaceUri(), qname.getLocalPart(), qname.getPrefix());
  }

  @Override
  public XMLWriterFactory2 newXMLWriterFactory2() {
    return new StaxXMLWriterFactory2();
  }

  private static class StaxXMLWriterFactory2 implements XMLWriterFactory2 {

    @Override
    public XMLWriter2 createXMLWriter(Writer writer) {
      return new StaxXMLWriter2(writer);
    }

  }

  @Override
  public XMLInputFactory2 newXMLInputFactory2() {
    return new StaxXMLInputFactory2(XMLInputFactory.newInstance());
  }

  private static class StaxXMLInputFactory2 implements XMLInputFactory2 {

    private final XMLInputFactory factory;

    public StaxXMLInputFactory2(XMLInputFactory factory) {
      this.factory = factory;
    }

    @Override
    public XMLEventReader2 createXMLEventReader(Reader reader) {
      try {
        XMLEventReader real = factory.createXMLEventReader(reader);
        return new StaxXMLEventReader2(real);
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      }
    }

  }

  private static class StaxXMLEventReader2 implements XMLEventReader2 {
    private final XMLEventReader real;

    public StaxXMLEventReader2(XMLEventReader real) {
      this.real = real;
    }

    @Override
    public String getElementText() {
      try {
        return real.getElementText();
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      }
    }

    @Override
    public boolean hasNext() {
      return real.hasNext();
    }

    @Override
    public XMLEvent2 nextEvent() {
      try {
        return new StaxXMLEvent2(real.nextEvent());
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      }
    }

  }

  @Override
  public XMLOutputFactory2 newXMLOutputFactory2() {
    return new StaxXMLOutputFactory2(XMLOutputFactory.newInstance());
  }

  private static class StaxXMLOutputFactory2 implements XMLOutputFactory2 {

    private final XMLOutputFactory factory;

    public StaxXMLOutputFactory2(XMLOutputFactory factory) {
      this.factory = factory;
    }

    @Override
    public XMLEventWriter2 createXMLEventWriter(Writer writer) {

      try {
        XMLEventWriter real = factory.createXMLEventWriter(writer);
        return new StaxXMLEventWriter2(real);
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      }

    }

  }

  private static class StaxXMLEventWriter2 implements XMLEventWriter2 {
    private final XMLEventWriter real;

    public StaxXMLEventWriter2(XMLEventWriter real) {
      this.real = real;
    }

    @Override
    public void add(XMLEvent2 event) {
      XMLEvent realXMLEvent = ((StaxXMLEvent2) event).getXMLEvent();
      try {
        real.add(realXMLEvent);
      } catch (XMLStreamException e) {
        throw Throwables.propagate(e);
      }
    }

  }

  private static class StaxXMLEvent2 implements XMLEvent2 {
    private final XMLEvent real;

    public StaxXMLEvent2(XMLEvent real) {
      this.real = real;
    }

    public XMLEvent getXMLEvent() {
      return real;
    }

    @Override
    public EndElement2 asEndElement() {
      return new StaxEndElement2(real.asEndElement());
    }

    @Override
    public StartElement2 asStartElement() {
      return new StaxStartElement2(real.asStartElement());
    }

    @Override
    public boolean isEndElement() {
      return real.isEndElement();
    }

    @Override
    public boolean isStartElement() {
      return real.isStartElement();
    }
  }

  private static class StaxEndElement2 implements EndElement2 {
    private final EndElement real;

    public StaxEndElement2(EndElement real) {
      this.real = real;
    }

    @Override
    public QName2 getName() {
      return new QName2(real.getName().getNamespaceURI(), real.getName().getLocalPart());
    }
  }

  private static class StaxStartElement2 implements StartElement2 {
    private final StartElement real;

    public StaxStartElement2(StartElement real) {
      this.real = real;
    }

    @Override
    public QName2 getName() {
      return new QName2(real.getName().getNamespaceURI(), real.getName().getLocalPart());
    }

    @Override
    public Attribute2 getAttributeByName(String name) {
      return getAttributeByName(new QName2(name));
    }

    @Override
    public Attribute2 getAttributeByName(QName2 name) {
      Attribute att = real.getAttributeByName(toQName(name));
      if (att == null)
        return null;
      return new StaxAttribute2(att);
    }
  }

  private static class StaxAttribute2 implements Attribute2 {
    private final Attribute real;

    public StaxAttribute2(Attribute real) {
      this.real = real;
    }

    @Override
    public String getValue() {
      return real.getValue();
    }
  }

}
