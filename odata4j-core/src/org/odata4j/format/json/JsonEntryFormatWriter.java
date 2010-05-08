package org.odata4j.format.json;

import org.odata4j.producer.EntityResponse;

public class JsonEntryFormatWriter extends JsonFormatWriter<EntityResponse> {

    public JsonEntryFormatWriter(String jsonpCallback) {
        super(jsonpCallback);
    }


    @Override
    protected void writeContent(String baseUri, JsonWriter jw, EntityResponse target) {
        writeOEntity(baseUri, jw,target.getEntity(),target.getEntitySet());
    }
    
/*
    // entity
    {
    "d" : {
    "__metadata": {
    "uri": "http://services.odata.org/Website/odata.svc/ODataConsumers(1)", "type": "ODataServices.ODataConsumer"
    }, "Id": 1, "Name": "Browsers", "Description": "Most modern browsers allow you to browse Atom based feeds. Simply point your browser at one of the OData Producers.", "ApplicationUrl": ""
    }
    }
 */
}
