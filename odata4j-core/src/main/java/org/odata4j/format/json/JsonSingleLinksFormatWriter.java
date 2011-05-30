package org.odata4j.format.json;

import org.odata4j.format.SingleLink;
import org.odata4j.format.SingleLinks;

import com.sun.jersey.api.core.ExtendedUriInfo;

public class JsonSingleLinksFormatWriter extends JsonFormatWriter<SingleLinks> {

  public JsonSingleLinksFormatWriter(String jsonpCallback) {
    super(jsonpCallback);
  }

  @Override
  protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, SingleLinks links) {
    jw.startObject();
    {
      jw.writeName("results");
      jw.startArray();
      for (SingleLink link : links)
        JsonSingleLinkFormatWriter.writeUri(jw, link);
      jw.endArray();
    }
    jw.endObject();
  }
}

/*
{
"d" : {
"results": [
{
"uri": "http://services.odata.org/northwind/Northwind.svc/Order_Details(OrderID=10285,ProductID=1)"
}, {
"uri": "http://services.odata.org/northwind/Northwind.svc/Order_Details(OrderID=10294,ProductID=1)"
}
]
}
}
*/
