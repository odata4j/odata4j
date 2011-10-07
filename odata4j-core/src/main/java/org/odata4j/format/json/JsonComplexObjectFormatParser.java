package org.odata4j.format.json;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonParseException;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;

/**
 * Parser for OComplexObjects in JSON
 */
public class JsonComplexObjectFormatParser extends JsonFormatParser implements FormatParser<OComplexObject> {

  public JsonComplexObjectFormatParser(Settings s) {
    super(s);
    returnType = (EdmComplexType) (null == s ? null : s.parseType);
  }
  
  public JsonComplexObjectFormatParser(EdmComplexType type) {
    super(null);
    returnType = type;
  }

  private EdmComplexType returnType = null;

  @Override
  public OComplexObject parse(Reader reader) {
    JsonStreamReader jsr = JsonStreamReaderFactory.createJsonStreamReader(reader);
    try {

      if (isResponse) {
        ensureNext(jsr);
        ensureStartObject(jsr.nextEvent()); // the response object

        // "d" property
        ensureNext(jsr);
        ensureStartProperty(jsr.nextEvent(), DATA_PROPERTY);

        // "aresult" for DataServiceVersion > 1.0
        if (version.compareTo(ODataVersion.V1) > 0) {
          ensureNext(jsr);
          ensureStartObject(jsr.nextEvent());
          ensureNext(jsr);
          ensureStartProperty(jsr.nextEvent(), RESULTS_PROPERTY);
        }
      }

      // parse the entry, should start with startObject
      OComplexObject o = parseSingleObject(jsr);

      if (isResponse) {

        // the "d" property was our object...it is also a property.
        ensureNext(jsr);
        ensureEndProperty(jsr.nextEvent());

        if (version.compareTo(ODataVersion.V1) > 0) {
          ensureNext(jsr);
          ensureEndObject(jsr.nextEvent());
          ensureNext(jsr);
          ensureEndProperty(jsr.nextEvent()); // "results"
        }
        ensureNext(jsr);
        ensureEndObject(jsr.nextEvent()); // the response object
      }

      return o;

    } finally {
      jsr.close();
    }
  }

  public OComplexObject parseSingleObject(JsonStreamReader jsr) {
    ensureNext(jsr);

    // this can be used in a context where we require an object and one
    // where there *may* be an object...like a collection

    JsonEvent event = jsr.nextEvent();
    if (event.isStartObject()) {

      List<OProperty<?>> props = new ArrayList<OProperty<?>>();
      return eatProps(props, jsr);
    } else {
      // not a start object.
      return null;
    }
  }
  
  public OComplexObject parseSingleObject(JsonStreamReader jsr, JsonEvent startPropertyEvent) {
    
    // the current JsonFormatParser implemenation, when parsing a complex object property value
    // has already eaten the startobject and the startproperty.
    
    List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    addProperty(props, startPropertyEvent.asStartProperty().getName(), jsr);
    return eatProps(props, jsr);
  }
  
  private OComplexObject eatProps(List<OProperty<?>> props, JsonStreamReader jsr) {
    
    ensureNext(jsr);
    while (jsr.hasNext()) {
        JsonEvent event = jsr.nextEvent();

        if (event.isStartProperty()) {
          addProperty(props, event.asStartProperty().getName(), jsr);
        } else if (event.isEndObject()) {
          break;
        } else {
          throw new JsonParseException("unexpected parse event: " + event.toString());
        }
      }
      return OComplexObjects.create(returnType, props);
  }

  protected void addProperty(List<OProperty<?>> props, String name, JsonStreamReader jsr) {

    JsonEvent event = jsr.nextEvent();

    if (event.isEndProperty()) {
      // scalar property
      EdmProperty ep = returnType.findProperty(name);

      if (ep == null) {
        throw new IllegalArgumentException("unknown property " + name + " for " + returnType.getFullyQualifiedTypeName());
      }
      // TODO support complex type properties
      if (!ep.getType().isSimple())
        throw new UnsupportedOperationException("Only simple properties supported");
      props.add(JsonTypeConverter.parse(name, (EdmSimpleType) ep.getType(), event.asEndProperty().getValue()));
    }
    else {
      throw new JsonParseException("expecting endproperty, got: " + event.toString());
    }
  }

}
