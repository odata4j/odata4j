package org.odata4j.core;

import org.odata4j.edm.EdmType;

public interface OProperty<T> {

	public abstract String getName();
	public abstract EdmType getType();
	public abstract T getValue();
	
	
}
