package org.odata4j.producer.resources;

import com.sun.jersey.api.core.DefaultResourceConfig;

public class ODataResourceConfig extends DefaultResourceConfig {

    public ODataResourceConfig() {
        super(EntitiesRequestResource.class, EntityRequestResource.class, MetadataResource.class, ServiceDocumentResource.class, ODataProducerProvider.class, ODataBatchProvider.class);

    }
}
