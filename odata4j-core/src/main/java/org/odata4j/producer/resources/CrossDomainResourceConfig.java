package org.odata4j.producer.resources;

import com.sun.jersey.api.core.DefaultResourceConfig;

public class CrossDomainResourceConfig extends DefaultResourceConfig {

    public CrossDomainResourceConfig() {
        super(CrossDomainXmlResource.class, ClientAccessPolicyXmlResource.class);
    }
}
