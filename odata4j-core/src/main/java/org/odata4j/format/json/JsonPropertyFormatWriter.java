package org.odata4j.format.json;

import org.odata4j.producer.PropertyResponse;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class JsonPropertyFormatWriter extends JsonFormatWriter<PropertyResponse> {

    public JsonPropertyFormatWriter(String jsonpCallback) {
        super(jsonpCallback);
    }

    @Override
    protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, PropertyResponse target) {
    	jw.startObject();
		{
			writeProperty(jw, target.getProperty());
		}
		jw.endObject();
    }
    
/*
    // property
{
"d" : {
"CategoryName": "Beverages"
}
}
 */
}
