package org.odata4j.format.json;

import com.sun.jersey.api.core.ExtendedUriInfo;
import org.odata4j.producer.ComplexObjectResponse;

/**
 * Writer for OComplexObjects in JSON
 */
public class JsonComplexObjectFormatWriter extends JsonFormatWriter<ComplexObjectResponse> {

  public JsonComplexObjectFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, ComplexObjectResponse target) {
    super.writeComplexObject(jw, target.getObject().getType().toTypeString(), target.getObject().getProperties());
  }

}
