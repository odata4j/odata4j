package org.odata4j.producer.jpa.eclipselink;

import java.util.Map;

import javax.persistence.metamodel.SingularAttribute;

import org.odata4j.edm.EdmProperty;
import org.odata4j.producer.jpa.JPAEdmGenerator;

public class EclipseLinkJPAEdmGenerator extends JPAEdmGenerator {
    protected EdmProperty toEdmProperty(String modelNamespace, SingularAttribute<?, ?> sa) {
    	EdmProperty p = super.toEdmProperty(modelNamespace, sa);
    	
    	Integer maxLength = null;
        Map<String, Object> eclipseLinkProps = EclipseLink.getPropertyInfo(sa, p.type);
        if (eclipseLinkProps.containsKey("MaxLength"))
            maxLength = (Integer) eclipseLinkProps.get("MaxLength");

        return new EdmProperty(p.name, p.type, p.nullable, maxLength, null, null, null, null, null, null, null, null);
    }
}
