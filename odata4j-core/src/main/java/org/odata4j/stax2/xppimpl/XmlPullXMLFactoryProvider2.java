package org.odata4j.stax2.xppimpl;

import java.io.Reader;
import java.io.Writer;

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
import org.odata4j.stax2.domimpl.ManualXMLWriter2;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlPullXMLFactoryProvider2 extends XMLFactoryProvider2 {

  @Override
  public XMLInputFactory2 newXMLInputFactory2() {
    return new XmlPullXMLInputFactory2();
  }

  @Override
  public XMLOutputFactory2 newXMLOutputFactory2() {
    return new XmlPullXMLOutputFactory2();
  }

  @Override
  public XMLWriterFactory2 newXMLWriterFactory2() {
    return new XmlPullXMLWriterFactory2();
  }

  private static class XmlPullXMLWriterFactory2 implements XMLWriterFactory2 {

    @Override
    public XMLWriter2 createXMLWriter(Writer writer) {
      return new ManualXMLWriter2(writer);
    }

  }

  private static class XmlPullXMLOutputFactory2 implements XMLOutputFactory2 {

    @Override
    public XMLEventWriter2 createXMLEventWriter(Writer writer) {
      return new XmlPullXMLEventWriter2(writer);
    }

  }

  private static class XmlPullXMLEventWriter2 implements XMLEventWriter2 {

    @SuppressWarnings("unused")
    private final Writer writer;

    public XmlPullXMLEventWriter2(Writer writer) {
      this.writer = writer;
    }

    @Override
    public void add(XMLEvent2 event) {
      throw new UnsupportedOperationException();

    }

  }

  private static class XmlPullXMLInputFactory2 implements XMLInputFactory2 {

    @Override
    public XMLEventReader2 createXMLEventReader(Reader reader) {
      try {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);

        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(reader);

        return new XmlPullXMLEventReader2(xpp);
      } catch (XmlPullParserException e) {
        throw new RuntimeException(e);
      }

    }

  }

  private static class XmlPullXMLEventReader2 implements XMLEventReader2 {

    private final XmlPullParser xpp;

    public XmlPullXMLEventReader2(XmlPullParser xpp) {
      this.xpp = xpp;
    }

    @Override
    public String getElementText() {
      try {
        return xpp.nextText();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean hasNext() {
      try {
        int eventType = xpp.next();
        return eventType != XmlPullParser.END_DOCUMENT;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    }

    @Override
    public XMLEvent2 nextEvent() {
      return new XmlPullXMLEvent2(xpp);
    }

  }

  private static class XmlPullXMLEvent2 implements XMLEvent2 {
    private final XmlPullParser xpp;

    public XmlPullXMLEvent2(XmlPullParser xpp) {
      this.xpp = xpp;
    }

    @Override
    public EndElement2 asEndElement() {
      if (!isEndElement())
        return null;
      return new XmlPullEndElement2(new QName2(xpp.getNamespace(), xpp.getName()));
    }

    @Override
    public StartElement2 asStartElement() {
      if (!isStartElement())
        return null;
      return new XmlPullStartElement2(xpp);
    }

    @Override
    public boolean isEndElement() {
      try {
        return xpp.getEventType() == XmlPullParser.END_TAG;
      } catch (XmlPullParserException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isStartElement() {
      try {
        return xpp.getEventType() == XmlPullParser.START_TAG;
      } catch (XmlPullParserException e) {
        throw new RuntimeException(e);
      }
    }

  }

  private static class XmlPullStartElement2 implements StartElement2 {
    private final XmlPullParser xpp;
    private final QName2 name;

    public XmlPullStartElement2(XmlPullParser xpp) {
      this.xpp = xpp;
      name = new QName2(xpp.getNamespace(), xpp.getName());
    }

    @Override
    public Attribute2 getAttributeByName(final QName2 arg0) {
      final String value = xpp.getAttributeValue(arg0.getNamespaceUri(), arg0.getLocalPart());
      if (value == null)
        return null;

      return new Attribute2() {
        public String getValue() {
          return value;
        }
      };

    }

    @Override
    public Attribute2 getAttributeByName(final String arg0) {

      final String value = xpp.getAttributeValue(null, arg0);
      if (value == null)
        return null;

      return new Attribute2() {

        @Override
        public String getValue() {
          return value;
        }
      };
    }

    @Override
    public QName2 getName() {
      return name;
    }

  }

  private static class XmlPullEndElement2 implements EndElement2 {
    private final QName2 name;

    public XmlPullEndElement2(QName2 name) {
      this.name = name;
    }

    @Override
    public QName2 getName() {
      return name;
    }

  }

}
