package org.odata4j.format.json;

import org.odata4j.format.SingleLink;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class JsonSingleLinkFormatWriter extends JsonFormatWriter<SingleLink> {

  public JsonSingleLinkFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, SingleLink link) {
    jw.startObject();
    {
      jw.writeName("uri");
      jw.writeString(link.getUri());
    }
    jw.endObject();
  }
}

/*
{
"d" : {
"uri": "http://services.odata.org/northwind/Northwind.svc/Categories(1)"
}
}
*/
