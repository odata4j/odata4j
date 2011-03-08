package org.odata4j.format.json;

import java.io.Writer;

import javax.ws.rs.core.MediaType;

import org.odata4j.format.json.JsonFeedFormatParser.JsonEntry;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class JsonRequestEntryFormatWriter extends JsonFormatWriter<JsonEntry> {

	public JsonRequestEntryFormatWriter(String jsonpCallback) {
		super(jsonpCallback);
	}

	@Override
	public String getContentType() {
		return MediaType.APPLICATION_JSON;
	}
	
	@Override
	public void write(ExtendedUriInfo uriInfo, Writer w, JsonEntry target) {

		JsonWriter jw = new JsonWriter(w);
		if (getJsonpCallback() != null) {
			jw.startCallback(getJsonpCallback());
		}

		jw.startObject();
		{
			writeContent(uriInfo, jw, target);
		}
		jw.endObject();
	}


	@Override
	protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw,
			JsonEntry target) {
		writeOProperties(jw, target.properties);
	}

}
