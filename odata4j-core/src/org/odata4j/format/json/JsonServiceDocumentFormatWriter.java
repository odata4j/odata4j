package org.odata4j.format.json;

import java.io.Writer;

import org.odata4j.core.ODataConstants;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.FormatWriter;

public class JsonServiceDocumentFormatWriter implements FormatWriter<EdmDataServices> {

    private final String jsonpCallback;
    public JsonServiceDocumentFormatWriter(String jsonpCallback){
        this.jsonpCallback = jsonpCallback;
    }
    
    @Override
    public String getContentType() {
        return jsonpCallback==null?ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8:ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8;
    }

    
    @Override
    public void write(String baseUri, Writer w, EdmDataServices target) {
        
        JsonWriter jw = new JsonWriter(w);
        if (jsonpCallback != null)
            jw.startCallback(jsonpCallback);
       
        jw.startObject();
        {
            jw.writeName("d");
            jw.startObject();
            {
                jw.writeName("EntitySets");
                jw.startArray();
                {
                    boolean isFirst = true;
                    for(EdmEntitySet ees : target.getEntitySets()) {
                        if (isFirst)
                            isFirst = false;
                        else
                            jw.writeArraySeparator();
                        
                        jw.writeString(ees.name);
                    }
                    
                }
                jw.endArray();
            }
            jw.endObject(); 
        }
        jw.endObject(); 
        
        if (jsonpCallback != null)
            jw.endCallback();
        
    }

}

/*
// jsonp
callback({
"d" : {
"EntitySets": [
"TitleAudioFormats", "TitleAwards", "Titles", "TitleScreenFormats", "Genres", "Languages", "People"
]
}
});

// json
{
"d" : {
"EntitySets": [
"TitleAudioFormats", "TitleAwards", "Titles", "TitleScreenFormats", "Genres", "Languages", "People"
]
}
}
*/