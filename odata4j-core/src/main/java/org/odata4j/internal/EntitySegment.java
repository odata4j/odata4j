package org.odata4j.internal;

import org.odata4j.core.OEntityKey;

public class EntitySegment {
    public final String segment;
    public final OEntityKey key;

    public EntitySegment(String segment, OEntityKey key) {
        this.segment = segment;
        this.key = key;
    }
    
    // TODO OEK
    public static EntitySegment temp(String segment, Object...key){
    	return new EntitySegment(segment,OEntityKey.create(key));
    }

    @Override
    public String toString() {
        return this.segment + (key==null?"":key.toKeyString());
    }
}
