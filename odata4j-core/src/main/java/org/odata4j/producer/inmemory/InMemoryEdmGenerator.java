package org.odata4j.producer.inmemory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.core4j.Predicate1;
import org.odata4j.edm.*;

public class InMemoryEdmGenerator implements EdmGenerator {

  private final Logger log = Logger.getLogger(getClass().getName());

  private final String namespace;
  private final String containerName;
  private final InMemoryTypeMapping typeMapping;
  private final Map<String, InMemoryEntityInfo<?>> eis;
  private final Map<String, InMemoryComplexTypeInfo<?>> complexTypeInfo;
  private final List<EdmComplexType.Builder> edmComplexTypes = new ArrayList<EdmComplexType.Builder>(); 
  private final Map<Class<?>, String> entityNameByClass = new HashMap<Class<?>, String>();


  
  public InMemoryEdmGenerator(String namespace, String containerName, InMemoryTypeMapping typeMapping, 
          String idPropertyName, Map<String, InMemoryEntityInfo<?>> eis,
          Map<String, InMemoryComplexTypeInfo<?>> complexTypes) {
    this.namespace = namespace;
    this.containerName = containerName;
    this.typeMapping = typeMapping;
    this.eis = eis;
    this.complexTypeInfo = complexTypes;
    
    for (Entry<String, InMemoryEntityInfo<?>> e : eis.entrySet()) {
      entityNameByClass.put(e.getValue().entityClass, e.getKey());
    }
  }

  @Override
  public EdmDataServices.Builder generateEdm(EdmDecorator decorator) {

    List<EdmSchema.Builder> schemas = new ArrayList<EdmSchema.Builder>();
    List<EdmEntityContainer.Builder> containers = new ArrayList<EdmEntityContainer.Builder>();
    List<EdmEntitySet.Builder> entitySets = new ArrayList<EdmEntitySet.Builder>();
    List<EdmEntityType.Builder> entityTypes = new ArrayList<EdmEntityType.Builder>();
    List<EdmAssociation.Builder> associations = new ArrayList<EdmAssociation.Builder>();
    List<EdmAssociationSet.Builder> associationSets = new ArrayList<EdmAssociationSet.Builder>();
    

    createComplexTypes(decorator, edmComplexTypes);
    
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

   
    createNavigationProperties(associations, associationSets,
        entityTypesByName, entitySetByName, entityNameByClass);

    EdmEntityContainer.Builder container = EdmEntityContainer.newBuilder().setName(containerName).setIsDefault(true)
        .addEntitySets(entitySets).addAssociationSets(associationSets);

    containers.add(container);

    EdmSchema.Builder schema = EdmSchema.newBuilder().setNamespace(namespace)
        .addEntityTypes(entityTypes)
        .addAssociations(associations)
        .addEntityContainers(containers)
        .addComplexTypes(edmComplexTypes);
    
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

  private void createComplexTypes(EdmDecorator decorator, List<EdmComplexType.Builder> complexTypes) {
    for (String complexTypeName : complexTypeInfo.keySet()) {
      InMemoryComplexTypeInfo<?> typeInfo = complexTypeInfo.get(complexTypeName);

      List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();

      // no keys
      properties.addAll(toEdmProperties(decorator, typeInfo.propertyModel, new String[]{}, complexTypeName));

      EdmComplexType.Builder typeBuilder = EdmComplexType.newBuilder()
          .setNamespace(namespace)
          .setName(typeInfo.typeName)
          .addProperties(properties);

      if (decorator != null) {
        typeBuilder.setDocumentation(decorator.getDocumentationForEntityType(namespace, complexTypeName));
        typeBuilder.setAnnotations(decorator.getAnnotationsForEntityType(namespace, complexTypeName));
      }

      complexTypes.add(typeBuilder);
    }
  }
  
  private void createStructuralEntities(EdmDecorator decorator, List<EdmEntitySet.Builder> entitySets,
      List<EdmEntityType.Builder> entityTypes) {

    for (String entitySetName : eis.keySet()) {
      InMemoryEntityInfo<?> entityInfo = eis.get(entitySetName);

      List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();

      properties.addAll(toEdmProperties(decorator, entityInfo.properties, entityInfo.keys, entitySetName));

      EdmEntityType.Builder eet = EdmEntityType.newBuilder()
          .setNamespace(namespace)
          .setName(entityInfo.entityTypeName)
          .addKeys(entityInfo.keys)
          .setHasStream(entityInfo.hasStream)
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
      InMemoryEntityInfo<?> ei = eis.get(entitySetName);
      Class<?> clazz1 = ei.entityClass;

      generateToOneNavProperties(associations, associationSets,
          entityTypesByName, entitySetByName, entityNameByClass,
          ei.entityTypeName, ei);

      generateToManyNavProperties(associations, associationSets,
          entityTypesByName, entitySetByName, entityNameByClass,
          ei.entityTypeName, ei, clazz1);
    }
  }

  private void generateToOneNavProperties(
      List<EdmAssociation.Builder> associations,
      List<EdmAssociationSet.Builder> associationSets,
      Map<String, EdmEntityType.Builder> entityTypesByName,
      Map<String, EdmEntitySet.Builder> entitySetByName,
      Map<Class<?>, String> entityNameByClass,
      String entityTypeName,
      InMemoryEntityInfo<?> ei) {

    for (String assocProp : ei.properties.getPropertyNames()) {

      EdmEntityType.Builder eet1 = entityTypesByName.get(entityTypeName);
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
      Map<Class<?>, String> entityNameByClass,
      String entityTypeName,
      InMemoryEntityInfo<?> ei,
      Class<?> clazz1) {

    for (String assocProp : ei.properties.getCollectionNames()) {

      final EdmEntityType.Builder eet1 = entityTypesByName.get(entityTypeName);

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
          InMemoryEntityInfo<?> class2eiInfo = eis.get(eetName2);
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

        EdmNavigationProperty.Builder np = EdmNavigationProperty.newBuilder(assocProp).setRelationship(assoc).setFromTo(fromRole, toRole);

        eet1.addNavigationProperties(np);
      } catch (Exception e) {
        log.log(Level.WARNING, "Exception building Edm associations: " + e.getMessage(), e);
      }
    }
  }

  private EdmComplexType.Builder findComplexTypeBuilder(String typeName) {
    String fqName = this.namespace + "." + typeName;
    for (EdmComplexType.Builder builder : this.edmComplexTypes) {
      if (builder.getFullyQualifiedTypeName().equals(fqName)) {
        return builder;
      }
    }
    return null;
  }
  
  private EdmComplexType.Builder findComplexTypeForClass(Class<?> clazz) {
    for (InMemoryComplexTypeInfo<?> typeInfo : this.complexTypeInfo.values()) {
      if (typeInfo.entityClass.equals(clazz)) {
        // the typeName defines the edm type name
        return findComplexTypeBuilder(typeInfo.typeName);
      }
    }
    
    return null;
  }
  
  private Collection<EdmProperty.Builder> toEdmProperties(
      EdmDecorator decorator,
      PropertyModel model,
      String[] keys,
      String structuralTypename) {

    List<EdmProperty.Builder> rt = new ArrayList<EdmProperty.Builder>();
    Set<String> keySet = Enumerable.create(keys).toSet();

    for (String propName : model.getPropertyNames()) {
      Class<?> propType = model.getPropertyType(propName);
      EdmType type = typeMapping.findEdmType(propType);
      EdmComplexType.Builder typeBuilder = null;
      if (type == null) {
         typeBuilder = findComplexTypeForClass(propType);
      }
      
      if (type == null && typeBuilder == null) {
        continue;
      }

      EdmProperty.Builder ep = EdmProperty
          .newBuilder(propName)
          .setNullable(!keySet.contains(propName));
      
      if (type != null) { 
        ep.setType(type); }
      else { 
        ep.setType(typeBuilder); 
      }
      
      if (decorator != null) {
        ep.setDocumentation(decorator.getDocumentationForProperty(namespace, structuralTypename, propName));
        ep.setAnnotations(decorator.getAnnotationsForProperty(namespace, structuralTypename, propName));
      }
      rt.add(ep);
    }
    
    // collections of primitives and complex types
    for (String collectionPropName : model.getCollectionNames()) {
      Class<?> collectionElementType = model.getCollectionElementType(collectionPropName);
      if (entityNameByClass.get(collectionElementType) != null) {
        // this will be a nav prop
        continue;
      }
      
      EdmType type = typeMapping.findEdmType(collectionElementType);
      EdmType.Builder typeBuilder = null;
      if (type == null) {
        typeBuilder = findComplexTypeForClass(collectionElementType);
      } else {
        typeBuilder = EdmSimpleType.newBuilder(type);
      }
     
      if (typeBuilder == null) {
        continue;
      }
      
      // either a simple or complex type.
      EdmProperty.Builder ep = EdmProperty.newBuilder(collectionPropName)
          .setNullable(true)
          .setCollectionKind(EdmProperty.CollectionKind.Collection)
          .setType(typeBuilder);
      
      if (decorator != null) {
        ep.setDocumentation(decorator.getDocumentationForProperty(namespace, structuralTypename, collectionPropName));
        ep.setAnnotations(decorator.getAnnotationsForProperty(namespace, structuralTypename, collectionPropName));
      }
      rt.add(ep);
    }

    return rt;
  }

}
