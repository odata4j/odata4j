package org.odata4j.format.json;

import com.sun.jersey.api.core.ExtendedUriInfo;
import org.odata4j.core.OCollection;
import org.odata4j.edm.EdmBaseType;
import org.odata4j.producer.CollectionResponse;

/**
 * Writer for OCollections in JSON
 */
public class JsonCollectionFormatWriter extends JsonFormatWriter<CollectionResponse>  {

  public JsonCollectionFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, CollectionResponse target) {
    OCollection<?> c = target.getCollection();
    EdmBaseType ctype = c.getType();
    jw.startArray();
    {
      boolean isFirst = true;
      for (Object o : c) {
          if (!isFirst) { jw.writeSeparator(); }
          else { isFirst = false; }
          super.writeValue(jw, ctype, o);
      }
    }
    jw.endArray();
  }

}
