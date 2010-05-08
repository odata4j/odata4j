package org.odata4j.format.json;

import org.odata4j.core.OEntity;
import org.odata4j.producer.EntitiesResponse;

public class JsonFeedFormatWriter extends JsonFormatWriter<EntitiesResponse> {

    public JsonFeedFormatWriter(String jsonpCallback) {
        super(jsonpCallback);
    }

    @Override
    public void writeContent(String baseUri, JsonWriter jw, EntitiesResponse target) {
        
        jw.startArray();
        {
            boolean isFirst = true;
            for(OEntity oe : target.getEntities()){
                
                if (isFirst) isFirst = false; else jw.writeSeparator();
                
                writeOEntity(baseUri, jw,oe,target.getEntitySet());
            }
            
        }
        jw.endArray(); 
      
    }


    
    
    
/*


    
    
    // entities
    {
        "d" : [
        {
        "__metadata": {
        "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(1)", "type": "ODataServices.ODataConsumer"
        }, "Id": 1, "Name": "Browsers", "Description": "Most modern browsers allow you to browse Atom based feeds. Simply point your browser at one of the OData Producers.", "ApplicationUrl": ""
        }, {
        "__metadata": {
        "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(2)", "type": "ODataServices.ODataConsumer"
        }, "Id": 2, "Name": "OData Explorer", "Description": "A Silverlight application that can browse OData Services. It is available as part of the OData SDK Code Samples, and is available online at Silverlight.net/ODataExplorer.", "ApplicationUrl": "http://silverlight.net/ODataExplorer"
        }, {
        "__metadata": {
        "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(3)", "type": "ODataServices.ODataConsumer"
        }, "Id": 3, "Name": "Excel 2010", "Description": "PowerPivot for Excel 2010 is a plugin to Excel 2010 that has OData support built-in.", "ApplicationUrl": "http://www.powerpivot.com/"
        }, {
        "__metadata": {
        "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(4)", "type": "ODataServices.ODataConsumer"
        }, "Id": 4, "Name": "LINQPad", "Description": "LINQPad is a tool for building OData queries interactively.", "ApplicationUrl": "http://www.linqpad.net/Beta.aspx"
        }, {
        "__metadata": {
        "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(5)", "type": "ODataServices.ODataConsumer"
        }, "Id": 5, "Name": "Client Libraries", "Description": "Client libraries are programming libraries that make it easy to consume OData services. We already have libraries that target: Javascript, PHP, Java, Windows Phone 7 Series, iPhone (Objective C) and .NET. For a complete list visit the OData SDK.", "ApplicationUrl": "http://www.odata.org/developers/odata-sdk"
        }, {
        "__metadata": {
        "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(6)", "type": "ODataServices.ODataConsumer"
        }, "Id": 6, "Name": "Sesame - OData Browser", "Description": "A preview version of Fabrice Marguerie\'s OData Browser.", "ApplicationUrl": "http://metasapiens.com/sesame/data-browser"
        }
        ]
        }
*/
}
