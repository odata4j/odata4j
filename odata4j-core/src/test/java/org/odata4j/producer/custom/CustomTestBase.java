
package org.odata4j.producer.custom;

import javax.ws.rs.core.MediaType;

import org.core4j.Func1;
import org.junit.AfterClass;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.examples.producer.ProducerUtil;
import org.odata4j.format.FormatType;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.server.ODataServer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 *
 */
public class CustomTestBase {
    protected static final String endpointUri = "http://localhost:8810/CustomTest.svc/";
    protected static ODataServer server;
    protected static CustomProducer producer;

    public static void setUpClass(int maxResults) throws Exception {
        setUpClass(maxResults, null);
    }

    public static void setUpClass(int maxResults, Func1<ODataProducer, ODataProducer> producerModification) throws Exception {
        
        producer = new CustomProducer();

        ODataProducer p = producer;
        if (producerModification != null) {
            p = producerModification.apply(producer);
        }

        DefaultODataProducerProvider.setInstance(p);
        server = ProducerUtil.startODataServer(endpointUri);
    }

    protected ODataConsumer createConsumer(FormatType format) {
        return ODataConsumer.newBuilder(endpointUri).setFormatType(format).build();
    }
    
    public void dumpResourceJSON(String path) {
      dumpResource(path, FormatType.JSON);
    }
    
    public void dumpResource(String path, FormatType ft) {
        WebResource r = Client.create().resource(endpointUri + path);
        switch(ft) {
          case JSON: r.accept(MediaType.APPLICATION_JSON_TYPE); break;
          case ATOM: r.accept(MediaType.APPLICATION_ATOM_XML_TYPE); break;
          default: break;
        }
        System.out.println(r.get(String.class));
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
        producer = null;
    }
}
