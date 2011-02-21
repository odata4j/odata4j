package org.odata4j.format;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.json.JsonEntryFormatWriter;
import org.odata4j.format.json.JsonFeedFormatWriter;
import org.odata4j.format.json.JsonPropertyFormatWriter;
import org.odata4j.format.json.JsonServiceDocumentFormatWriter;
import org.odata4j.format.xml.AtomEntryFormatWriter;
import org.odata4j.format.xml.AtomFeedFormatWriter;
import org.odata4j.format.xml.AtomServiceDocumentFormatWriter;
import org.odata4j.format.xml.XmlPropertyFormatWriter;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.PropertyResponse;

public class FormatWriterFactory {

    private static enum FormatType{
        ATOM,
        JSON;
        
        public static FormatType parse(String format){
            if ("json".equalsIgnoreCase(format)) return JSON;
            if ("atom".equalsIgnoreCase(format)) return ATOM;
            throw new UnsupportedOperationException("Unsupported format " + format);
        }
    }
    
    private static interface FormatWriters {
        
        public FormatWriter<EdmDataServices> getServiceDocumentFormatWriter();
        public FormatWriter<EntitiesResponse> getFeedFormatWriter();
        public FormatWriter<EntityResponse> getEntryFormatWriter();
        public FormatWriter<PropertyResponse> getPropertyFormatWriter();
    }
    

    
    @SuppressWarnings("unchecked")
    public static <T> FormatWriter<T> getFormatWriter(Class<T> targetType, List<MediaType> acceptTypes, String format, String callback){
        
        FormatType type = null;
        
        // if format is explicitly specified, use that
        if (format != null){
            type = FormatType.parse(format);
        }
        
        // if header accepts json, use that
        if (type==null && acceptTypes != null) {
            for(MediaType acceptType : acceptTypes){
                if (acceptType.equals(MediaType.APPLICATION_JSON_TYPE)){
                    type = FormatType.JSON;
                    break;
                }
            }
        }
        
        // else default to atom
        if (type==null)
            type = FormatType.ATOM;
     
        
        FormatWriters formatWriters = type.equals(FormatType.JSON)?new JsonWriters(callback):new AtomWriters();
        
        if (targetType.equals(EdmDataServices.class)) {
            return (FormatWriter<T>)formatWriters.getServiceDocumentFormatWriter();
        }
        if (targetType.equals(EntitiesResponse.class)) {
            return (FormatWriter<T>)formatWriters.getFeedFormatWriter();
        } 
        if (targetType.equals(EntityResponse.class)) {
            return (FormatWriter<T>)formatWriters.getEntryFormatWriter();
        } 
        if (targetType.equals(PropertyResponse.class)) {
            return (FormatWriter<T>)formatWriters.getPropertyFormatWriter();
        } 
        throw new IllegalArgumentException("Unable to locate format writer for " + targetType.getName() + " and format " + type);
        
    }
    
    
    public static class JsonWriters implements FormatWriters {

        private final String callback;
        public JsonWriters(String callback){
            this.callback = callback;
        }
        @Override
        public FormatWriter<EdmDataServices> getServiceDocumentFormatWriter() {
            return new JsonServiceDocumentFormatWriter(callback);
        }

        @Override
        public FormatWriter<EntitiesResponse> getFeedFormatWriter() {
            return new JsonFeedFormatWriter(callback);
        }

        @Override
        public FormatWriter<EntityResponse> getEntryFormatWriter() {
            return new JsonEntryFormatWriter(callback);
        }
        
        @Override
        public FormatWriter<PropertyResponse> getPropertyFormatWriter() {
        	return new JsonPropertyFormatWriter(callback);
        }
        
    }
    public static class AtomWriters implements FormatWriters {

        @Override
        public FormatWriter<EdmDataServices> getServiceDocumentFormatWriter() {
            return new AtomServiceDocumentFormatWriter();
        }

        @Override
        public FormatWriter<EntitiesResponse> getFeedFormatWriter() {
            return new AtomFeedFormatWriter();
        }

        @Override
        public FormatWriter<EntityResponse> getEntryFormatWriter() {
            return new AtomEntryFormatWriter();
        }
        
        @Override
        public FormatWriter<PropertyResponse> getPropertyFormatWriter() {
        	 return new XmlPropertyFormatWriter();
        }
        
    }
}
