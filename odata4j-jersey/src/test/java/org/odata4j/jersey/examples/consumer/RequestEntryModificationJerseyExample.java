package org.odata4j.jersey.examples.consumer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import org.core4j.Func1;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.ODataConstants;
import org.odata4j.examples.consumers.AbstractRequestEntryModificationExample;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.consumer.behaviors.JerseyClientBehavior;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;
import com.sun.jersey.core.util.ReaderWriter;

public class RequestEntryModificationJerseyExample extends AbstractRequestEntryModificationExample {

  public static void main(String[] args) {
    RequestEntryModificationJerseyExample example = new RequestEntryModificationJerseyExample();
    example.run(args);
  }

  @Override
  public ODataConsumer create(String endpointUri, FormatType formatType, String methodToTunnel) {
    final ModifiableAtomEntryMessageBodyWriter writer = new ModifiableAtomEntryMessageBodyWriter();
    ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).setClientBehaviors(new JerseyClientBehavior() {
      @Override
      public void modify(ClientConfig cc) {
        cc.getSingletons().add(writer);
      }

      @Override
      public ODataClientRequest transform(ODataClientRequest request) {
        return request;
      }

      @Override
      public void modifyClientFilters(Filterable client) {
        // nop
      }

      @Override
      public void modifyWebResourceFilters(Filterable webResource) {
        // nop
      }
    }).build();

    writer.setEntryXmlModification(insertCategory("NorthwindModel.Categories"));

    return consumer;
  }

  private static Func1<String, String> insertCategory(final String term) {
    return new Func1<String, String>() {
      @Override
      public String apply(String entryXml) {
        int i = entryXml.indexOf("</author><content");
        if (i < 0)
          return entryXml;
        i += "</author>".length();
        String categoryXml = String.format("<category term=\"%s\" scheme=\"http://schemas.microsoft.com/ado/2007/08/dataservices/scheme\"/>", term);
        return entryXml.substring(0, i) + categoryXml + entryXml.substring(i);
      }
    };
  }

  @Produces(ODataConstants.APPLICATION_ATOM_XML_CHARSET_UTF8)
  private static class ModifiableAtomEntryMessageBodyWriter implements MessageBodyWriter<String> {

    private Func1<String, String> entryXmlModification;

    public void setEntryXmlModification(Func1<String, String> entryXmlModification) {
      this.entryXmlModification = entryXmlModification;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return type == String.class;
    }

    @Override
    public long getSize(String entryXml, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
      return -1L;
    }

    @Override
    public void writeTo(String entryXml, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
        throws IOException, WebApplicationException {
      if (entryXmlModification != null)
        entryXml = entryXmlModification.apply(entryXml);
      ReaderWriter.writeToAsString(entryXml, entityStream, mediaType);
    }

  }

}
