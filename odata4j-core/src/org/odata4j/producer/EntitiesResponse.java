package org.odata4j.producer;

import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;


/** Interface returned by a call to ODataProducer#getEntities.
 *
 */
public interface EntitiesResponse extends BaseResponse {

	/** Get the metadata for this specific response.
	 * 
	 * @return a structure identifying the name and type of the set
	 */
    public EdmEntitySet getEntitySet();

    /** Return the list of entities for this call.
     * 
     * @return a list of entity objects to be returned
     */
    public List<OEntity> getEntities();

    /** Optionally, get the number of entities that will be returned
     * 
     * @return the number of entities in the set, or null to allow this to be calculated
     */
    public Integer getInlineCount();
    
    /** I don't know what a skip token is, but it seems to be something JSON-specific
     * TODO: figure this out
     * @return the skip token for this entity set
     */
    public String getSkipToken();
}
