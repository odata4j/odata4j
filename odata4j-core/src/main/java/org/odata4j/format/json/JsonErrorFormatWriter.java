package org.odata4j.format.json;

import java.io.Writer;

import javax.ws.rs.core.UriInfo;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OError;
import org.odata4j.format.FormatWriter;
import org.odata4j.producer.ErrorResponse;

public class JsonErrorFormatWriter implements FormatWriter<ErrorResponse> {

  private final String jsonpCallback;

  public JsonErrorFormatWriter(String jsonpCallback) {
    this.jsonpCallback = jsonpCallback;
  }

  public void write(UriInfo uriInfo, Writer w, ErrorResponse target) {
    String debugParameter = uriInfo.getQueryParameters().getFirst("odata4j.debug");
    boolean innerError = debugParameter != null && Boolean.parseBoolean(debugParameter);
    JsonWriter jw = new JsonWriter(w);
    jw.startObject();
    writeError(jw, target.getError(), innerError);
    jw.endObject();
  }

  public String getContentType() {
    return ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8;
  }

  private void writeError(JsonWriter jw, OError error, boolean innerError) {
    if (jsonpCallback != null)
      jw.startCallback(jsonpCallback);

    jw.writeName("error");
    jw.startObject();
    {
      jw.writeName("code");
      jw.writeString(error.getCode());
      jw.writeSeparator();
      jw.writeName("message");
      jw.startObject();
      {
        jw.writeName("lang");
        jw.writeString("en-US");
        jw.writeSeparator();
        jw.writeName("value");
        jw.writeString(error.getMessage());
      }
      jw.endObject();
      if (innerError) {
        jw.writeSeparator();
        jw.writeName("innererror");
        jw.writeString(error.getInnerError());
      }
    }
    jw.endObject();

    if (jsonpCallback != null)
      jw.endCallback();
  }
}
