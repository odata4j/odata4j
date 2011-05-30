package org.odata4j.format.json;

import org.odata4j.producer.EntityResponse;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class JsonEntryFormatWriter extends JsonFormatWriter<EntityResponse> {

  public JsonEntryFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, EntityResponse target) {
    writeOEntity(uriInfo, jw, target.getEntity(), target.getEntity().getEntitySet(), true);
  }
}
