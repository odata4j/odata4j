package org.odata4j.producer.jpa;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.Column;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.joda.time.Instant;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OFuncs;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationEnd;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmAssociationSetEnd;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmType;

public class JPAEdmGenerator {

    private final Logger log = Logger.getLogger(getClass().getName());

    protected EdmType toEdmType(SingularAttribute<?, ?> sa) {

        Class<?> javaType = sa.getType().getJavaType();

		if (javaType.equals(String.class) || javaType.equals(Character.class)
				|| "char".equals(javaType.toString())) {
			return EdmType.STRING;
		}
        if (javaType.equals(BigDecimal.class)) {
            return EdmType.DECIMAL;
        }
        if (javaType.equals(new byte[0].getClass())) {
            return EdmType.BINARY;
        }
        if (javaType.equals(Short.class) || javaType.equals(Short.TYPE)) {
            return EdmType.INT16;
        }
        if (javaType.equals(Integer.class) || javaType.equals(Integer.TYPE)) {
            return EdmType.INT32;
        }
        if (javaType.equals(Long.class) || javaType.equals(Long.TYPE)) {
            return EdmType.INT64;
        }
        if (javaType.equals(Boolean.class) || javaType.equals(Boolean.TYPE)) {
            return EdmType.BOOLEAN;
        }
        if (javaType.equals(Double.class) || javaType.equals(Double.TYPE)) {
            return EdmType.DOUBLE;
        }
        if (javaType.equals(Float.class) || javaType.equals(Float.TYPE)) {
            return EdmType.SINGLE;
        }
        if (javaType.equals(Date.class) || javaType.equals(Calendar.class)) {
            TemporalType temporal = getTemporalType(sa);
            if (temporal == null) {
                return EdmType.DATETIME;
            } else {
                switch (temporal) {
                    case DATE:
                    case TIMESTAMP:
                        return EdmType.DATETIME;
                    case TIME:
                        return EdmType.TIME;
                }
            }
        }
        if (javaType.equals(Time.class)) {
            return EdmType.TIME;
        }
        if (javaType.equals(Instant.class)) {
            return EdmType.DATETIME;
        }
        if (javaType.equals(Timestamp.class)) {
            return EdmType.DATETIME;
        }
        if (javaType.equals(Byte.TYPE)) {
            return EdmType.BYTE;
        }


        throw new UnsupportedOperationException(javaType.toString());
    }

    protected EdmProperty toEdmProperty(String modelNamespace, SingularAttribute<?, ?> sa) {
        String name = sa.getName();
        EdmType type;
        if (sa.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
            String simpleName = sa.getJavaType().getSimpleName();
            type = EdmType.get(modelNamespace + "." + simpleName);
        } else if (sa.getBindableJavaType().isEnum()) {
            // TODO assume string mapping for now, @Enumerated info not avail in metamodel?
            type = EdmType.STRING;
        } else {
            type = toEdmType(sa);
        }
        boolean nullable = sa.isOptional();
        
		Integer maxLength = null;
		if (sa.getJavaMember() instanceof AnnotatedElement) {
			Column col = ((AnnotatedElement) sa.getJavaMember()).getAnnotation(Column.class);
			if (col != null)
				maxLength = col.length();
		}

		return new EdmProperty(name, type, nullable, maxLength, null, null, null, null, null, null, null, null);
    }

    protected List<EdmProperty> getProperties(String modelNamespace, ManagedType<?> et) {
        List<EdmProperty> properties = new ArrayList<EdmProperty>();
        for (Attribute<?, ?> att : et.getAttributes()) {

            if (att.isCollection()) {
            } else {
                SingularAttribute<?, ?> sa = (SingularAttribute<?, ?>) att;

                Type<?> type = sa.getType();
                // Do we have an embedded composite key here? If so, we have to flatten the @EmbeddedId since
                // only any set of non-nullable, immutable, <EDMSimpleType> declared properties MAY serve as the key.
                if (sa.isId() && type.getPersistenceType() == PersistenceType.EMBEDDABLE) {
                    properties.addAll(getProperties(modelNamespace, (ManagedType<?>) sa.getType()));
                } else if (type.getPersistenceType().equals(PersistenceType.BASIC) || type.getPersistenceType().equals(PersistenceType.EMBEDDABLE)) {
                    EdmProperty prop = toEdmProperty(modelNamespace, sa);
                    properties.add(prop);
                }
            }
        }
        return properties;
    }

    public EdmDataServices buildEdm(EntityManagerFactory emf, String namespace) {

        String modelNamespace = namespace + "Model";

        List<EdmEntityType> edmEntityTypes = new ArrayList<EdmEntityType>();
        List<EdmComplexType> edmComplexTypes = new ArrayList<EdmComplexType>();
        List<EdmAssociation> associations = new ArrayList<EdmAssociation>();

        List<EdmEntitySet> entitySets = new ArrayList<EdmEntitySet>();
        List<EdmAssociationSet> associationSets = new ArrayList<EdmAssociationSet>();

        Metamodel mm = emf.getMetamodel();


        // complex types
        for (EmbeddableType<?> et : mm.getEmbeddables()) {

            String name = et.getJavaType().getSimpleName();
            List<EdmProperty> properties = getProperties(modelNamespace, et);

            EdmComplexType ect = new EdmComplexType(modelNamespace, name, properties);
            edmComplexTypes.add(ect);
        }

        // entities
        for (EntityType<?> et : mm.getEntities()) {

            String name = getEntitySetName(et);

            List<String> keys = new ArrayList<String>();
            SingularAttribute<?, ?> idAttribute = null;
            if (et.hasSingleIdAttribute()) {
                idAttribute = getId(et);
                //	handle composite embedded keys (@EmbeddedId)
                if (idAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
                    keys = Enumerable.create(getProperties(modelNamespace, (ManagedType<?>) idAttribute.getType()))
                    			.select(OFuncs.edmPropertyName()).toList();
                } else {
                    keys = Enumerable.create(idAttribute.getName()).toList();
                }
            } else {
                throw new IllegalArgumentException("IdClass not yet supported");
            }

            List<EdmProperty> properties = getProperties(modelNamespace, et);
            List<EdmNavigationProperty> navigationProperties = new ArrayList<EdmNavigationProperty>();

            EdmEntityType eet = new EdmEntityType(modelNamespace, null, name, null, keys, properties, navigationProperties);
            edmEntityTypes.add(eet);

            EdmEntitySet ees = new EdmEntitySet(name, eet);
            entitySets.add(ees);
        }

        Map<String, EdmEntityType> eetsByName = Enumerable.create(edmEntityTypes).toMap(OFuncs.edmEntityTypeName());
        Map<String, EdmEntitySet> eesByName = Enumerable.create(entitySets).toMap(OFuncs.edmEntitySetName());

        // associations + navigationproperties on one side

        for (EntityType<?> et2 : mm.getEntities()) {

            for (Attribute<?, ?> att : et2.getAttributes()) {

                if (att.isCollection()) {
                    // /CollectionAttribute<?,?> ca = (CollectionAttribute<?,?>)att;
                } else {
                    SingularAttribute<?, ?> sa = (SingularAttribute<?, ?>) att;

                    Type<?> type = sa.getType();
                    if (type.getPersistenceType().equals(PersistenceType.ENTITY)) {

                        EntityType<?> aet = (EntityType<?>) type;

                        EdmEntityType eet1 = eetsByName.get(getEntitySetName(et2));
                        EdmEntityType eet2 = eetsByName.get(getEntitySetName(aet));
                        EdmMultiplicity m1 = EdmMultiplicity.MANY;
                        EdmMultiplicity m2 = EdmMultiplicity.ZERO_TO_ONE;

                        String assocName = getAssociationName(associations, eet1, eet2);

                        EdmAssociationEnd assocEnd1 = new EdmAssociationEnd(eet1.name, eet1, m1);
                        String assocEnd2Name = eet2.name;
                        if (assocEnd2Name.equals(eet1.name)) {
                            assocEnd2Name = assocEnd2Name + "1";
                        }
                        EdmAssociationEnd assocEnd2 = new EdmAssociationEnd(assocEnd2Name, eet2, m2);
                        EdmAssociation assoc = new EdmAssociation(modelNamespace, null, assocName, assocEnd1, assocEnd2);

                        associations.add(assoc);

                        EdmEntitySet ees1 = eesByName.get(eet1.name);
                        EdmEntitySet ees2 = eesByName.get(eet2.name);
                        EdmAssociationSet eas = new EdmAssociationSet(assocName, assoc, new EdmAssociationSetEnd(assocEnd1, ees1), new EdmAssociationSetEnd(assocEnd2, ees2));

                        associationSets.add(eas);

                        EdmNavigationProperty np = new EdmNavigationProperty(sa.getName(), assoc, assoc.end1, assoc.end2);

                        eet1.navigationProperties.add(np);

                    }

                }

            }

        }

        // navigation properties for the collection side of associations

        for (EntityType<?> et3 : mm.getEntities()) {

            for (Attribute<?, ?> att : et3.getAttributes()) {

                if (att.isCollection()) {

                    PluralAttribute<?, ?, ?> ca = (PluralAttribute<?, ?, ?>) att;

                    EntityType<?> cat = (EntityType<?>) ca.getElementType();

                    final EdmEntityType eet1 = eetsByName.get(getEntitySetName(cat));
                    final EdmEntityType eet2 = eetsByName.get(getEntitySetName(et3));



                    EdmNavigationProperty np = null;
                    try {
                        EdmAssociation assoc = Enumerable.create(associations).firstOrNull(new Predicate1<EdmAssociation>() {

                            @Override
                            public boolean apply(EdmAssociation input) {
                                return input.end1.type.equals(eet1) && input.end2.type.equals(eet2);
                            }
                        });
                        if (assoc==null)
                        	throw new RuntimeException(String.format("No assoc found where eet1 = %s and eet2 = %s",eet1,eet2));
                        np = new EdmNavigationProperty(ca.getName(), assoc, assoc.end2, assoc.end1);

                        eet2.navigationProperties.add(np);

                    } catch (Exception e) {
                        log.log(Level.WARNING, "Exception building Edm associations: " + e.getMessage(),e);
                    }
                }

            }

        }

        EdmEntityContainer container = new EdmEntityContainer(namespace + "Entities", true, null, entitySets, associationSets, null);

        EdmSchema modelSchema = new EdmSchema(modelNamespace, null, edmEntityTypes, edmComplexTypes, associations, null);
        EdmSchema containerSchema = new EdmSchema(namespace + "Container", null, null, null, null, Enumerable.create(container).toList());

        EdmDataServices services = new EdmDataServices(ODataConstants.DATA_SERVICE_VERSION, Enumerable.create(modelSchema, containerSchema).toList());
        return services;
    }

    public static <X> SingularAttribute<? super X, ?> getId(EntityType<X> et) {
        return Enumerable.create(et.getSingularAttributes()).firstOrNull(new Predicate1<SingularAttribute<? super X, ?>>() {

            @Override
            public boolean apply(SingularAttribute<? super X, ?> input) {
                return input.isId();
            }
        });
    }

    public static <X> String getEntitySetName(EntityType<X> et) {
        String name = et.getName();
        int idx = name.lastIndexOf('.');
        return idx > 0
                ? name.substring(idx + 1)
                : name;
    }

    protected String getAssociationName(List<EdmAssociation> associations, EdmEntityType eet1, EdmEntityType eet2) {
        for (int i = 0;; i++) {
            final String assocName = i == 0
                    ? String.format("FK_%s_%s", eet1.name, eet2.name)
                    : String.format("FK_%s_%s_%d", eet1.name, eet2.name, i);

            boolean exists = Enumerable.create(associations).where(new Predicate1<EdmAssociation>() {

                @Override
                public boolean apply(EdmAssociation input) {
                    return assocName.equals(input.name);
                }
            }).count() > 0;

            if (!exists) {
                return assocName;
            }
        }
    }

    protected TemporalType getTemporalType(SingularAttribute<?, ?> sa) {
        Member member = sa.getJavaMember();

        Temporal temporal = null;
        if (member instanceof Field) {
            temporal = ((Field) member).getAnnotation(Temporal.class);
        } else if (member instanceof Method) {
            temporal = ((Method) member).getAnnotation(Temporal.class);
        }

        return temporal.value();
    }
}
