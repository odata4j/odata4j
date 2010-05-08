package org.odata4j.format.json;

import java.io.Writer;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.format.FormatWriter;
import org.odata4j.internal.InternalUtil;

public abstract class JsonFormatWriter<T> implements FormatWriter<T> {
    
    private final String jsonpCallback;
    public JsonFormatWriter(String jsonpCallback){
        this.jsonpCallback = jsonpCallback;
    }
    
    
    abstract protected void writeContent(String baseUri, JsonWriter jw,  T target);
   
    public String getContentType() {
        return jsonpCallback==null?ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8:ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8;
    }
    
    protected String getJsonpCallback(){
        return jsonpCallback;
    }
    
    public void write(String baseUri, Writer w, T target) {
        
        JsonWriter jw = new JsonWriter(w);
        if (getJsonpCallback() != null)
            jw.startCallback(getJsonpCallback());
       
        jw.startObject();
        {
            jw.writeName("d");
            writeContent(baseUri,jw,target);
        }
        jw.endObject(); 
        
        if (getJsonpCallback() != null)
            jw.endCallback();
    
    }
    
   
    
    protected void writeOEntity(String baseUri, JsonWriter jw, OEntity oe, EdmEntitySet ees){
        
        jw.startObject();
        {
            
            jw.writeName("__metadata");
            jw.startObject();
            {
                String absId = baseUri + InternalUtil.getEntityRelId(ees.type.keys,oe.getProperties(),ees.name);
                jw.writeName("uri");
                jw.writeString(absId);
                jw.writeSeparator();
                jw.writeName("type");
                jw.writeString(ees.type.getFQName());
            }
            jw.endObject();
            jw.writeSeparator();
            
            boolean isFirst = true;
            for(OProperty<?> prop : oe.getProperties()){
                if (isFirst) isFirst = false; else jw.writeSeparator();
                
                jw.writeName(prop.getName());
                jw.writeString(prop.getValue().toString());
            }
            
            
        }
        jw.endObject();
    }
}
