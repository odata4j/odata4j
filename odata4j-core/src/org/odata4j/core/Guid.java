package org.odata4j.core;

import java.util.UUID;

public class Guid {

    private final String value;
    private Guid(String value){
        this.value = value;
    }
    
    public static Guid fromString(String value){
        return new Guid(value);
    }

    public static Guid randomGuid() {
        return new Guid(UUID.randomUUID().toString());
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
          
         Guid other = (Guid)obj;
         return obj != null && other.value.equals(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}

