package org.odata4j.format;

import org.odata4j.core.OEntity;

/**
 * Building block for OData payload. 
 * 
 * @see Feed
 */
public interface Entry {
	public String getUri();
	
	public String getETag();
	
	public OEntity getEntity();

}
