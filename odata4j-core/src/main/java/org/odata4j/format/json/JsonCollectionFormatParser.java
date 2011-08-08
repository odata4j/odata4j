package org.odata4j.format.json;

import java.io.Reader;
import org.odata4j.core.OCollection;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OFunctionParameters;
import org.odata4j.core.OObject;
import org.odata4j.edm.EdmBaseType;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.FormatType;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonComplexObjectFormatParser;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;
import org.odata4j.producer.exceptions.NotImplementedException;

/**
 * Parsers an OCollection in JSON format.
 * 
 * Collection types handled so far:
 * - OComplexObject
 * 
 * TODO:
 * - all other types
 */
public class JsonCollectionFormatParser extends JsonFormatParser implements FormatParser<OCollection<? extends OObject>> {

    public JsonCollectionFormatParser(Settings s) {
        super(s);
        returnType = (EdmCollectionType) (null == s ? null : s.parseType);
    }
    private EdmCollectionType returnType = null;

    @Override
    public OCollection<? extends OObject> parse(Reader reader) {
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

            // parse the entry
            OCollection<? extends OObject> o = parseCollection(jsr);

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

    protected OCollection<? extends OObject> parseCollection(JsonStreamReader jsr) {
        // an array of objects:
        ensureNext(jsr);
        ensureStartArray(jsr.nextEvent());

        OCollection c = createCollection();

        FormatParser<? extends OObject> parser = createItemParser(this.returnType.getCollectionType());
        
        while (jsr.hasNext()) {
            // this is what I really want to do next:
            // OObject o = parser.parse(jsr);
            // however, the FormatParser api would have to be genericized, we would need an interface for 
            // the event-oriented parsers (JsonStreamReader, XMLStreamReader).
            // I just don't have the time at this momement...
            
            if (parser instanceof JsonComplexObjectFormatParser) {
                OComplexObject obj = ((JsonComplexObjectFormatParser)parser).parseSingleObject(jsr);
                // null if not there
                if (null != obj) {
                    c.add(obj);
                } else {
                    break;
                }
            } else {
                throw new NotImplementedException("collections of type: " + this.returnType.getCollectionType().toTypeString() + " not implemented");
            }
        }
        
        // we should see the end of the array
        ensureEndArray(jsr.previousEvent());

        return c;
    }
    
    protected OCollection<? extends OObject> createCollection() {
        // hmmh...design issue?...
        if (this.returnType.getCollectionType() instanceof EdmComplexType) {
            return new OCollection<OComplexObject>(this.returnType.getCollectionType());
        }
        
        throw new NotImplementedException("unsupported collection type " + this.returnType.getCollectionType().toTypeString());
    }
    
    protected FormatParser<? extends OObject> createItemParser(EdmBaseType edmType) {
        // each item is parsed as a standalone item, not a response item
        Settings s = new Settings(
            this.version,
            this.metadata,
            this.entitySetName, 
            this.entityKey, 
            null,   // FeedCustomizationMapping fcMapping,
            false,  // boolean isResponse);
            edmType);   // expected type
        
        return FormatParserFactory.getParser(OFunctionParameters.getResultClass(edmType), FormatType.JSON, s);
    }
}
