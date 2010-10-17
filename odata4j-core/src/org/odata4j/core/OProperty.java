package org.odata4j.core;

import org.odata4j.edm.EdmType;

public interface OProperty<T> extends NamedValue<T>{

    public abstract EdmType getType();
    
}
