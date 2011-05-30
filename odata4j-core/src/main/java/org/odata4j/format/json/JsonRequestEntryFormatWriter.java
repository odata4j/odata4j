package org.odata4j.format.json;

import java.io.Writer;

import javax.ws.rs.core.MediaType;

import org.odata4j.format.Entry;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class JsonRequestEntryFormatWriter extends JsonFormatWriter<Entry> {

  public JsonRequestEntryFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  public String getContentType() {
    return MediaType.APPLICATION_JSON;
  }

  @Override
  public void write(ExtendedUriInfo uriInfo, Writer w, Entry target) {

    JsonWriter jw = new JsonWriter(w);
    if (getJsonpCallback() != null) {
      jw.startCallback(getJsonpCallback());
    }

    writeContent(uriInfo, jw, target);
  }

  @Override
  protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, Entry target) {
    writeOEntity(uriInfo, jw, target.getEntity(),
        target.getEntity().getEntitySet(), false);
  }

}
