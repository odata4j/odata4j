package org.odata4j.format.json;

import org.odata4j.core.OEntity;
import org.odata4j.producer.EntitiesResponse;

public class JsonFeedFormatWriter extends JsonFormatWriter<EntitiesResponse> {

    public JsonFeedFormatWriter(String jsonpCallback) {
        super(jsonpCallback);
    }

    @Override
    public void writeContent(String baseUri, JsonWriter jw, EntitiesResponse target) {
        
        jw.startObject();
        {
            jw.writeName("results");
            
            jw.startArray();
            {
                boolean isFirst = true;
                for(OEntity oe : target.getEntities()){
                    
                    if (isFirst) isFirst = false; else jw.writeSeparator();
                    
                    writeOEntity(baseUri, jw,oe,target.getEntitySet());
                }
                
            }
            jw.endArray(); 
            
            // TODO __count or __next
            
        
        }
        jw.endObject();
    }


    
    
    
/*

    // entities v2
    {
    "d" : {
    "results": [
    {
    "__metadata": {
    "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(1)", "type": "ODataServices.ODataConsumer"
    }, "Id": 1, "Name": "Browsers", "Description": "Most modern browsers allow you to browse Atom based feeds. Simply point your browser at one of the OData Producers.", "ApplicationUrl": ""
    }, {
    "__metadata": {
    "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(6)", "type": "ODataServices.ODataConsumer"
    }, "Id": 6, "Name": "Sesame - OData Browser", "Description": "A preview version of Fabrice Marguerie\'s OData Browser.", "ApplicationUrl": "http://metasapiens.com/sesame/data-browser"
    }
    ], "__count": "3", "__next": "http://odata.netflix.com/Catalog/Titles/?$filter=substringof('matrix',Name)&$skiptoken='IHKWS'"
    }
    }
    
    // entities v1
    {
    "d" : [
    {
    "__metadata": {
    "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(1)", "type": "ODataServices.ODataConsumer"
    }, "Id": 1, "Name": "Browsers", "Description": "Most modern browsers allow you to browse Atom based feeds. Simply point your browser at one of the OData Producers.", "ApplicationUrl": ""
    }, {
    "__metadata": {
    "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(6)", "type": "ODataServices.ODataConsumer"
    }, "Id": 6, "Name": "Sesame - OData Browser", "Description": "A preview version of Fabrice Marguerie\'s OData Browser.", "ApplicationUrl": "http://metasapiens.com/sesame/data-browser"
    }
    ]
    }
*/
}
