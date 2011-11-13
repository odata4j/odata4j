package org.odata4j.producer.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.core4j.Predicate1;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationEnd;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmAssociationSetEnd;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmDecorator;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmGenerator;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;

public class InMemoryEdmGenerator implements EdmGenerator {

  private final Logger log = Logger.getLogger(getClass().getName());

  private static final String CONTAINER_NAME = "Container";

  private final String namespace;
  private final InMemoryTypeMapping typeMapping;
  private final String idPropertyName;
  private final Map<String, InMemoryEntityInfo<?, ?>> eis;

  public InMemoryEdmGenerator(String namespace, InMemoryTypeMapping typeMapping, String idPropertyName, Map<String, InMemoryEntityInfo<?, ?>> eis) {
    this.namespace = namespace;
    this.typeMapping = typeMapping;
    this.idPropertyName = idPropertyName;
    this.eis = eis;
  }

  @Override
  public EdmDataServices.Builder generateEdm(EdmDecorator decorator) {

    List<EdmSchema.Builder> schemas = new ArrayList<EdmSchema.Builder>();
    List<EdmEntityContainer.Builder> containers = new ArrayList<EdmEntityContainer.Builder>();
    List<EdmEntitySet.Builder> entitySets = new ArrayList<EdmEntitySet.Builder>();
    List<EdmEntityType.Builder> entityTypes = new ArrayList<EdmEntityType.Builder>();
    List<EdmAssociation.Builder> associations = new ArrayList<EdmAssociation.Builder>();
    List<EdmAssociationSet.Builder> associationSets = new ArrayList<EdmAssociationSet.Builder>();

    // creates id other basic SUPPORTED_TYPE properties(structural) entities
    createStructuralEntities(decorator, entitySets, entityTypes);

    // TODO handle back references too
    // create hashmaps from sets
    // --------------------------------------
    // create entityname:entityTypes
    Map<String, EdmEntityType.Builder> entityTypesByName = Enumerable.create(
            entityTypes).toMap(new Func1<EdmEntityType.Builder, String>() {
      public String apply(EdmEntityType.Builder input) {
        return input.getName();
      }
    });

    // create entityname:entitySet
    Map<String, EdmEntitySet.Builder> entitySetByName = Enumerable.create(
            entitySets).toMap(new Func1<EdmEntitySet.Builder, String>() {
      public String apply(EdmEntitySet.Builder input) {
        return input.getName();
      }
    });

    Map<Class<?>, String> entityNameByClass = new HashMap<Class<?>, String>();

    for (Entry<String, InMemoryEntityInfo<?, ?>> e : eis.entrySet())
      entityNameByClass.put(e.getValue().entityClass, e.getKey());

    createNavigationProperties(associations, associationSets,
            entityTypesByName, entitySetByName, entityNameByClass);

    EdmEntityContainer.Builder container = EdmEntityContainer.newBuilder().setName(CONTAINER_NAME).setIsDefault(true)
        .addEntitySets(entitySets).addAssociationSets(associationSets);

    containers.add(container);

    EdmSchema.Builder schema = EdmSchema.newBuilder().setNamespace(namespace).addEntityTypes(entityTypes)
            .addAssociations(associations).addEntityContainers(containers);
    if (decorator != null) {
      schema.setDocumentation(decorator.getDocumentationForSchema(namespace));
      schema.setAnnotations(decorator.getAnnotationsForSchema(namespace));
    }

    schemas.add(schema);
    EdmDataServices.Builder rt = EdmDataServices.newBuilder().addSchemas(schemas);
    if (decorator != null)
      rt.addNamespaces(decorator.getNamespaces());
    return rt;
  }

  private void createStructuralEntities(EdmDecorator decorator, List<EdmEntitySet.Builder> entitySets,
      List<EdmEntityType.Builder> entityTypes) {

    for (String entitySetName : eis.keySet()) {
      InMemoryEntityInfo<?, ?> entityInfo = eis.get(entitySetName);

      List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();

      properties.addAll(toEdmProperties(decorator, entityInfo.properties, entitySetName));

      EdmEntityType.Builder eet = EdmEntityType.newBuilder()
          .setNamespace(namespace)
          .setName(entitySetName)
          .addKeys(idPropertyName)
          .addProperties(properties);
      if (decorator != null) {
        eet.setDocumentation(decorator.getDocumentationForEntityType(namespace, entitySetName));
        eet.setAnnotations(decorator.getAnnotationsForEntityType(namespace, entitySetName));
      }

      EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName(entitySetName).setEntityType(eet);

      entitySets.add(ees);
      entityTypes.add(eet);
    }
  }

  private void createNavigationProperties(List<EdmAssociation.Builder> associations,
      List<EdmAssociationSet.Builder> associationSets,
      Map<String, EdmEntityType.Builder> entityTypesByName,
      Map<String, EdmEntitySet.Builder> entitySetByName,
      Map<Class<?>, String> entityNameByClass) {

    for (String entitySetName : eis.keySet()) {
      InMemoryEntityInfo<?, ?> ei = eis.get(entitySetName);
      Class<?> clazz1 = ei.entityClass;

      generateToOneNavProperties(associations, associationSets,
              entityTypesByName, entitySetByName, entityNameByClass,
              entitySetName, ei);

      generateToManyNavProperties(associations, associationSets,
              entityTypesByName, entitySetByName, entityNameByClass,
              entitySetName, ei, clazz1);
    }
  }

  private void generateToOneNavProperties(
      List<EdmAssociation.Builder> associations,
      List<EdmAssociationSet.Builder> associationSets,
      Map<String, EdmEntityType.Builder> entityTypesByName,
      Map<String, EdmEntitySet.Builder> entitySetByName,
      Map<Class<?>, String> entityNameByClass, String entitySetName,
      InMemoryEntityInfo<?, ?> ei) {

    for (String assocProp : ei.properties.getPropertyNames()) {

      EdmEntityType.Builder eet1 = entityTypesByName.get(entitySetName);
      Class<?> clazz2 = ei.properties.getPropertyType(assocProp);
      String eetName2 = entityNameByClass.get(clazz2);

      if (eet1.findProperty(assocProp) != null || eetName2 == null)
        continue;

      EdmEntityType.Builder eet2 = entityTypesByName.get(eetName2);

      EdmMultiplicity m1 = EdmMultiplicity.MANY;
      EdmMultiplicity m2 = EdmMultiplicity.ONE;

      String assocName = String.format("FK_%s_%s", eet1.getName(), eet2.getName());
      EdmAssociationEnd.Builder assocEnd1 = EdmAssociationEnd.newBuilder().setRole(eet1.getName())
              .setType(eet1).setMultiplicity(m1);
      String assocEnd2Name = eet2.getName();
      if (assocEnd2Name.equals(eet1.getName()))
          assocEnd2Name = assocEnd2Name + "1";

      EdmAssociationEnd.Builder assocEnd2 = EdmAssociationEnd.newBuilder().setRole(assocEnd2Name).setType(eet2).setMultiplicity(m2);
      EdmAssociation.Builder assoc = EdmAssociation.newBuilder().setNamespace(namespace).setName(assocName).setEnds(assocEnd1, assocEnd2);

      associations.add(assoc);

      EdmEntitySet.Builder ees1 = entitySetByName.get(eet1.getName());
      EdmEntitySet.Builder ees2 = entitySetByName.get(eet2.getName());
      EdmAssociationSet.Builder eas = EdmAssociationSet.newBuilder().setName(assocName).setAssociation(assoc).setEnds(
          EdmAssociationSetEnd.newBuilder().setRole(assocEnd1).setEntitySet(ees1),
          EdmAssociationSetEnd.newBuilder().setRole(assocEnd2).setEntitySet(ees2));

      associationSets.add(eas);

      EdmNavigationProperty.Builder np = EdmNavigationProperty.newBuilder(assocProp)
          .setRelationship(assoc).setFromTo(assoc.getEnd1(), assoc.getEnd2());

      eet1.addNavigationProperties(np);
    }
  }

  private void generateToManyNavProperties(List<EdmAssociation.Builder> associations,
      List<EdmAssociationSet.Builder> associationSets,
      Map<String, EdmEntityType.Builder> entityTypesByName,
      Map<String, EdmEntitySet.Builder> entitySetByName,
      Map<Class<?>, String> entityNameByClass, String entitySetName,
      InMemoryEntityInfo<?, ?> ei, Class<?> clazz1) {

    for (String assocProp : ei.properties.getCollectionNames()) {

      final EdmEntityType.Builder eet1 = entityTypesByName.get(entitySetName);

      Class<?> clazz2 = ei.properties.getCollectionElementType(assocProp);
      String eetName2 = entityNameByClass.get(clazz2);
      if (eetName2 == null)
        continue;

      final EdmEntityType.Builder eet2 = entityTypesByName.get(eetName2);

      try {
        EdmAssociation.Builder assoc = Enumerable.create(associations).firstOrNull(new Predicate1<EdmAssociation.Builder>() {

          public boolean apply(EdmAssociation.Builder input) {
            return input.getEnd1().getType().equals(eet2) && input.getEnd2().getType().equals(eet1);
          }
        });

        EdmAssociationEnd.Builder fromRole, toRole;

        if (assoc == null) {
          //no association already exists
          EdmMultiplicity m1 = EdmMultiplicity.ZERO_TO_ONE;
          EdmMultiplicity m2 = EdmMultiplicity.MANY;

          //find ei info of class2
          InMemoryEntityInfo<?, ?> class2eiInfo = eis.get(eetName2);
          for (String tmp : class2eiInfo.properties.getCollectionNames()) {
            //class2 has a ref to class1
            //Class<?> tmpc = class2eiInfo.properties.getCollectionElementType(tmp);
            if (clazz1 == class2eiInfo.properties.getCollectionElementType(tmp)) {
              m1 = EdmMultiplicity.MANY;
              m2 = EdmMultiplicity.MANY;
              break;
            }
          }

          String assocName = String.format("FK_%s_%s", eet1.getName(), eet2.getName());
          EdmAssociationEnd.Builder assocEnd1 = EdmAssociationEnd.newBuilder().setRole(eet1.getName()).setType(eet1).setMultiplicity(m1);
          String assocEnd2Name = eet2.getName();
          if (assocEnd2Name.equals(eet1.getName()))
              assocEnd2Name = assocEnd2Name + "1";
          EdmAssociationEnd.Builder assocEnd2 = EdmAssociationEnd.newBuilder().setRole(assocEnd2Name).setType(eet2).setMultiplicity(m2);
          assoc = EdmAssociation.newBuilder().setNamespace(namespace).setName(assocName).setEnds(assocEnd1, assocEnd2);

          associations.add(assoc);

          EdmEntitySet.Builder ees1 = entitySetByName.get(eet1.getName());
          EdmEntitySet.Builder ees2 = entitySetByName.get(eet2.getName());
          EdmAssociationSet.Builder eas = EdmAssociationSet.newBuilder().setName(assocName).setAssociation(assoc).setEnds(
              EdmAssociationSetEnd.newBuilder().setRole(assocEnd1).setEntitySet(ees1),
              EdmAssociationSetEnd.newBuilder().setRole(assocEnd2).setEntitySet(ees2));
          associationSets.add(eas);

          fromRole = assoc.getEnd1();
          toRole = assoc.getEnd2();
        } else {
          fromRole = assoc.getEnd2();
          toRole = assoc.getEnd1();
        }

        EdmNavigationProperty.Builder np = EdmNavigationProperty.newBuilder(assocProp).setRelationship(assoc).setFromTo( fromRole, toRole);

        eet1.addNavigationProperties(np);
      } catch (Exception e) {
        log.log(Level.WARNING, "Exception building Edm associations: " + e.getMessage(), e);
      }
    }
  }

  private Collection<EdmProperty.Builder> toEdmProperties(EdmDecorator decorator, PropertyModel model, String structuralTypename) {
    List<EdmProperty.Builder> rt = new ArrayList<EdmProperty.Builder>();

    for (String propName : model.getPropertyNames()) {
      Class<?> propType = model.getPropertyType(propName);
      EdmSimpleType<?> type = typeMapping.findEdmType(propType);
      if (type == null)
        continue;

      EdmProperty.Builder ep = EdmProperty.newBuilder(propName).setType(type).setNullable(true);
      if (decorator != null) {
        ep.setDocumentation(decorator.getDocumentationForProperty(namespace, structuralTypename, propName));
        ep.setAnnotations(decorator.getAnnotationsForProperty(namespace, structuralTypename, propName));
      }
      rt.add(ep);
    }

    return rt;
  }

}
