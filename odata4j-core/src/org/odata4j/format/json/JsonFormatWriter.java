package org.odata4j.format.json;

import java.io.Writer;
import java.math.BigDecimal;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatWriter;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

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
                if (prop.getValue()==null)
                    jw.writeNull();
               
                else if (prop.getType().equals(EdmType.BINARY))
                    jw.writeString( Base64.encodeBase64String((byte[])prop.getValue()));
                else if (prop.getType().equals(EdmType.BOOLEAN)) 
                    jw.writeBoolean((Boolean)prop.getValue());
                else if (prop.getType().equals(EdmType.BYTE))
                    jw.writeString(Hex.encodeHexString(new byte[]{(Byte)prop.getValue()}));
                else if (prop.getType().equals(EdmType.DATETIME)) {
                    LocalDateTime ldt = (LocalDateTime)prop.getValue();
                    long millis = ldt.toDateTime(DateTimeZone.UTC).getMillis();
                    String date = "\"\\/Date(" + millis + ")\\/\"";
                    jw.writeRaw(date);
                }
                else if (prop.getType().equals(EdmType.DECIMAL))
                    jw.writeString("decimal'" + (BigDecimal)prop.getValue() + "'");
                else if (prop.getType().equals(EdmType.DOUBLE)) 
                    jw.writeString(prop.getValue().toString());
                else if (prop.getType().equals(EdmType.GUID))
                    jw.writeString("guid'" + (UUID)prop.getValue() + "'");
                else if (prop.getType().equals(EdmType.INT16)) 
                    jw.writeNumber((Short)prop.getValue());
                else if (prop.getType().equals(EdmType.INT32)) 
                    jw.writeNumber((Integer)prop.getValue());
                else if (prop.getType().equals(EdmType.INT64)) 
                    jw.writeString(prop.getValue().toString());
                else if (prop.getType().equals(EdmType.SINGLE)) 
                    jw.writeString(prop.getValue().toString() + "f");
                else if (prop.getType().equals(EdmType.TIME)) {
                    LocalTime ldt = (LocalTime)prop.getValue();
                    jw.writeString("time'" + ldt + "'");
                }
                else if (prop.getType().equals(EdmType.DATETIMEOFFSET))
                    jw.writeString("datetimeoffset'" + InternalUtil.toString((DateTime)prop.getValue()) + "'");
                else {
                    String value = prop.getValue().toString();
                    jw.writeString(value);
                }
            }
            
            
        }
        jw.endObject();
    }
}
