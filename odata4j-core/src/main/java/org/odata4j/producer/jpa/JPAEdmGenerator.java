package org.odata4j.producer.jpa;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
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
import javax.persistence.ManyToMany;
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
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.edm.EdmType;

public class JPAEdmGenerator {

  private final Logger log = Logger.getLogger(getClass().getName());

  protected EdmSimpleType<?> toEdmType(SingularAttribute<?, ?> sa) {

    Class<?> javaType = sa.getType().getJavaType();

    if (javaType.equals(Date.class) || javaType.equals(Calendar.class)) {
      TemporalType temporal = getTemporalType(sa);
      if (temporal == null) {
        return EdmSimpleType.DATETIME;
      } else {
        switch (temporal) {
        case DATE:
        case TIMESTAMP:
          return EdmSimpleType.DATETIME;
        case TIME:
          return EdmSimpleType.TIME;
        }
      }
    }
    if (javaType.equals(Time.class)) {
      return EdmSimpleType.TIME;
    }
    if (javaType.equals(Instant.class)) {
      return EdmSimpleType.DATETIME;
    }
    if (javaType.equals(Timestamp.class)) {
      return EdmSimpleType.DATETIME;
    }

    EdmSimpleType<?> rt = EdmSimpleType.forJavaType(javaType);
    if (rt != null)
      return rt;

    throw new UnsupportedOperationException(javaType.toString());
  }

  protected EdmProperty.Builder toEdmProperty(String modelNamespace, SingularAttribute<?, ?> sa) {
    String name = sa.getName();
    EdmType type;
    if (sa.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
      String simpleName = sa.getJavaType().getSimpleName();
      type = EdmType.get(modelNamespace + "." + simpleName);
    } else if (sa.getBindableJavaType().isEnum()) {
      // TODO assume string mapping for now, @Enumerated info not avail in metamodel?
      type = EdmSimpleType.STRING;
    } else {
      type = toEdmType(sa);
    }
    boolean nullable = sa.isOptional();

    Integer maxLength = null;
    if (sa.getJavaMember() instanceof AnnotatedElement) {
      Column col = ((AnnotatedElement) sa.getJavaMember()).getAnnotation(Column.class);
      if (col != null && Enumerable.<EdmType>create(EdmSimpleType.BINARY, EdmSimpleType.STRING).contains(type))
        maxLength = col.length();
    }

    return EdmProperty.newBuilder(name).setType(type).setNullable(nullable).setMaxLength(maxLength);
  }

  protected List<EdmProperty.Builder> getProperties(String modelNamespace, ManagedType<?> et) {
    List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
    for (Attribute<?, ?> att : et.getAttributes()) {

      if (att.isCollection()) {} else {
        SingularAttribute<?, ?> sa = (SingularAttribute<?, ?>) att;

        Type<?> type = sa.getType();
        // Do we have an embedded composite key here? If so, we have to flatten the @EmbeddedId since
        // only any set of non-nullable, immutable, <EDMSimpleType> declared properties MAY serve as the key.
        if (sa.isId() && type.getPersistenceType() == PersistenceType.EMBEDDABLE) {
          properties.addAll(getProperties(modelNamespace, (ManagedType<?>) sa.getType()));
        } else if (type.getPersistenceType().equals(PersistenceType.BASIC) || type.getPersistenceType().equals(PersistenceType.EMBEDDABLE)) {
          EdmProperty.Builder prop = toEdmProperty(modelNamespace, sa);
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
      List<EdmProperty.Builder> properties = getProperties(modelNamespace, et);

      EdmComplexType ect = new EdmComplexType(modelNamespace, name, properties);
      edmComplexTypes.add(ect);
    }

    // entities
    for (EntityType<?> et : mm.getEntities()) {

      String name = getEntitySetName(et);

      List<String> keys = new ArrayList<String>();
      SingularAttribute<?, ?> idAttribute = null;
      if (et.hasSingleIdAttribute()) {
        idAttribute = getIdAttribute(et);
        // handle composite embedded keys (@EmbeddedId)
        if (idAttribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
          keys = Enumerable.create(getProperties(modelNamespace, (ManagedType<?>) idAttribute.getType()))
                          .select(OFuncs.name(EdmProperty.Builder.class)).toList();
        } else {
          keys = Enumerable.create(idAttribute.getName()).toList();
        }
      } else {
        throw new IllegalArgumentException("IdClass not yet supported");
      }

      List<EdmProperty.Builder> properties = getProperties(modelNamespace, et);
      List<EdmNavigationProperty> navigationProperties = new ArrayList<EdmNavigationProperty>();

      EdmEntityType eet = new EdmEntityType(modelNamespace, null, name, null, keys, properties, navigationProperties);
      edmEntityTypes.add(eet);

      EdmEntitySet ees = new EdmEntitySet(name, eet);
      entitySets.add(ees);
    }

    Map<String, EdmEntityType> eetsByName = Enumerable.create(edmEntityTypes).toMap(OFuncs.name(EdmEntityType.class));
    Map<String, EdmEntitySet> eesByName = Enumerable.create(entitySets).toMap(OFuncs.name(EdmEntitySet.class));

    // associations + navigationproperties on the non-collection side
    for (EntityType<?> et : mm.getEntities()) {

      for (Attribute<?, ?> att : et.getAttributes()) {

        if (!att.isCollection()) {

          SingularAttribute<?, ?> singularAtt = (SingularAttribute<?, ?>) att;

          Type<?> singularAttType = singularAtt.getType();
          if (singularAttType.getPersistenceType().equals(PersistenceType.ENTITY)) {

            // we found a single attribute to an entity
            // create an edm many-to-one relationship  (* 0..1)
            EntityType<?> singularAttEntityType = (EntityType<?>) singularAttType;

            EdmEntityType fromEntityType = eetsByName.get(getEntitySetName(et));
            EdmEntityType toEntityType = eetsByName.get(getEntitySetName(singularAttEntityType));

            // add EdmAssociation, EdmAssociationSet
            EdmAssociation association = defineManyTo(EdmMultiplicity.ZERO_TO_ONE, associations, fromEntityType, toEntityType, modelNamespace, eesByName, associationSets);

            // add EdmNavigationProperty
            EdmNavigationProperty navigationProperty = new EdmNavigationProperty(
                singularAtt.getName(),
                association,
                association.getEnd1(),
                association.getEnd2());
            fromEntityType.addNavigationProperty(navigationProperty);
          }
        }
      }
    }

    // navigation properties for the collection side of associations
    for (EntityType<?> et : mm.getEntities()) {

      for (Attribute<?, ?> att : et.getAttributes()) {

        if (att.isCollection()) {

          // one-to-many
          PluralAttribute<?, ?, ?> pluralAtt = (PluralAttribute<?, ?, ?>) att;
          JPAMember m = JPAMember.create(pluralAtt, null);
          ManyToMany manyToMany = m.getAnnotation(ManyToMany.class);

          EntityType<?> pluralAttEntityType = (EntityType<?>) pluralAtt.getElementType();

          final EdmEntityType fromEntityType = eetsByName.get(getEntitySetName(et));
          final EdmEntityType toEntityType = eetsByName.get(getEntitySetName(pluralAttEntityType));

          try {
            EdmAssociation association = Enumerable.create(associations).firstOrNull(new Predicate1<EdmAssociation>() {
              public boolean apply(EdmAssociation input) {
                return input.getEnd1().getType().equals(toEntityType) && input.getEnd2().getType().equals(fromEntityType);
              }
            });

            EdmAssociationEnd fromRole, toRole;

            if (association == null) {
              // no EdmAssociation, EdmAssociationSet defined, backfill!

              // add EdmAssociation, EdmAssociationSet
              if (manyToMany != null) {
                association = defineManyTo(EdmMultiplicity.MANY, associations, fromEntityType, toEntityType, modelNamespace, eesByName, associationSets);
                fromRole = association.getEnd1();
                toRole = association.getEnd2();
              } else {
                association = defineManyTo(EdmMultiplicity.ZERO_TO_ONE, associations, toEntityType, fromEntityType, modelNamespace, eesByName, associationSets);
                fromRole = association.getEnd2();
                toRole = association.getEnd1();
              }

            } else {
              fromRole = association.getEnd2();
              toRole = association.getEnd1();
            }

            // add EdmNavigationProperty
            EdmNavigationProperty navigationProperty = new EdmNavigationProperty(
                            pluralAtt.getName(),
                            association,
                            fromRole,
                            toRole);
            fromEntityType.addNavigationProperty(navigationProperty);

          } catch (Exception e) {
            log.log(Level.WARNING, "Exception building Edm associations: " + e.getMessage(), e);
          }
        }

      }

    }

    EdmEntityContainer container = new EdmEntityContainer(namespace + "Entities", true, null, entitySets, associationSets, null);

    EdmSchema modelSchema = new EdmSchema(modelNamespace, null, edmEntityTypes, edmComplexTypes, associations, null);
    EdmSchema containerSchema = new EdmSchema(namespace + "Container", null, null, null, null, Enumerable.create(container).toList());

    EdmDataServices services = new EdmDataServices(ODataConstants.DATA_SERVICE_VERSION, Enumerable.create(containerSchema, modelSchema).toList());
    return services;
  }

  private static EdmAssociation defineManyTo(EdmMultiplicity toMult, List<EdmAssociation> associations, EdmEntityType fromEntityType, EdmEntityType toEntityType,
      String modelNamespace, Map<String, EdmEntitySet> eesByName, List<EdmAssociationSet> associationSets) {
    EdmMultiplicity fromMult = EdmMultiplicity.MANY;

    String assocName = getAssociationName(associations, fromEntityType, toEntityType);

    // add EdmAssociation
    EdmAssociationEnd fromAssociationEnd = new EdmAssociationEnd(fromEntityType.getName(), fromEntityType, fromMult);
    String toAssociationEndName = toEntityType.getName();
    if (toAssociationEndName.equals(fromEntityType.getName())) {
      toAssociationEndName = toAssociationEndName + "1";
    }
    EdmAssociationEnd toAssociationEnd = new EdmAssociationEnd(toAssociationEndName, toEntityType, toMult);
    EdmAssociation association = new EdmAssociation(modelNamespace, null, assocName, fromAssociationEnd, toAssociationEnd);
    associations.add(association);

    // add EdmAssociationSet
    EdmEntitySet fromEntitySet = eesByName.get(fromEntityType.getName());
    EdmEntitySet toEntitySet = eesByName.get(toEntityType.getName());
    EdmAssociationSet associationSet = new EdmAssociationSet(
        assocName,
        association,
        new EdmAssociationSetEnd(fromAssociationEnd, fromEntitySet),
        new EdmAssociationSetEnd(toAssociationEnd, toEntitySet));
    associationSets.add(associationSet);

    return association;
  }

  public static <X> SingularAttribute<? super X, ?> getIdAttribute(EntityType<X> et) {
    return Enumerable.create(et.getSingularAttributes()).firstOrNull(new Predicate1<SingularAttribute<? super X, ?>>() {
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

  protected static String getAssociationName(List<EdmAssociation> associations, EdmEntityType fromEntityType, EdmEntityType toEntityType) {
    for (int i = 0;; i++) {
      final String assocName = i == 0
          ? String.format("FK_%s_%s", fromEntityType.getName(), toEntityType.getName())
          : String.format("FK_%s_%s_%d", fromEntityType.getName(), toEntityType.getName(), i);

      boolean exists = Enumerable.create(associations).where(new Predicate1<EdmAssociation>() {
        public boolean apply(EdmAssociation input) {
          return assocName.equals(input.getName());
        }
      }).count() > 0;

      if (!exists)
        return assocName;
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

    return temporal == null ? null : temporal.value();
  }
}
