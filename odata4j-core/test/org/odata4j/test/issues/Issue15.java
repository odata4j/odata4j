package org.odata4j.test.issues;

import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.format.xml.AtomFeedFormatParser;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.domimpl.DomXMLFactoryProvider2;

public class Issue15 {

    @Test
    public void issue15(){
        
        InputStream xml = getClass().getResourceAsStream("/META-INF/issue15.xml");
      
        XMLEventReader2 reader = DomXMLFactoryProvider2.getInstance().newXMLInputFactory2().createXMLEventReader(new InputStreamReader(xml));
        reader.nextEvent(); 
        StartElement2 propertiesElement = reader.nextEvent().asStartElement();
        for(OProperty<?> prop : AtomFeedFormatParser.parseProperties(reader, propertiesElement)){
            if (prop.getName().equals("update_date")){
                Assert.assertEquals("2010-11-21T12:21:51.000", prop.getValue().toString());
                return;
            }
        }
        Assert.fail("Expected a property update_date");
    }
    
    
    public void repro(){
        
        ODataConsumer.DUMP_RESPONSE_BODY = true;
        ODataConsumer c = ODataConsumer.create("http://localhost:6794/WcfDataService2.svc/");
        
        @SuppressWarnings("unused")
		OEntity newEntity = c.createEntity("entity1").properties(
                OProperties.string("name", "name"+System.currentTimeMillis())
                ).execute();    // throws
        
        for(OEntity e : c.getEntities("entity1").execute())
            System.out.println(e);
    }
}
