package org.odata4j.format;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.json.JsonServiceDocumentFormatWriter;
import org.odata4j.format.xml.AtomServiceDocumentFormatWriter;

public class FormatWriterFactory {

    public static <T> FormatWriter<T> getFormatWriter(Class<T> targetType, String format, String callback){
        boolean json = "json".equals(format);
        
        if (targetType.equals(EdmDataServices.class)) {
            return (FormatWriter<T>)(json?new JsonServiceDocumentFormatWriter(callback):new AtomServiceDocumentFormatWriter());
        }
            
        
        throw new IllegalArgumentException("Unable to locate format writer for " + targetType.getName() + " and format " + format);
        
    }
}
