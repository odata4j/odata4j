package org.odata4j.format.json;

import org.odata4j.producer.EntityResponse;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class JsonEntryFormatWriter extends JsonFormatWriter<EntityResponse> {

    public JsonEntryFormatWriter(String jsonpCallback) {
        super(jsonpCallback);
    }


    @Override
    protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, EntityResponse target) {
        writeOEntity(uriInfo, jw,target.getEntity(),target.getEntitySet());
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
