package org.odata4j.producer;

import java.util.List;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;

public interface EntitiesResponse extends BaseResponse {

    public EdmEntitySet getEntitySet();

    public List<OEntity> getEntities();

    public Integer getInlineCount();
    
    public String getSkipToken();
}
