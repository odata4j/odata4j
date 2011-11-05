
package org.odata4j.producer.custom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationEnd;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmAssociationSetEnd;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmDecorator;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmGenerator;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.edm.Edm;

/**
 *
 */
public class CustomEdm implements EdmGenerator {


  private EdmDecorator decorator = null;
  private EdmSchema.Builder schema = null;
  private EdmEntityContainer.Builder container = null;
  private List<EdmAssociation.Builder> assocs = new LinkedList<EdmAssociation.Builder>();
  private List<EdmComplexType.Builder> ctypes = new LinkedList<EdmComplexType.Builder>();
  private List<EdmEntityType.Builder> etypes = new LinkedList<EdmEntityType.Builder>();
  private List<EdmEntitySet.Builder> esets = new LinkedList<EdmEntitySet.Builder>();
  private List<EdmAssociationSet.Builder> asets = new LinkedList<EdmAssociationSet.Builder>();

  public static final String namespace = "myns";

  @Override
  public EdmDataServices generateEdm(EdmDecorator decorator) {
    createComplexTypes();
    createEntityTypes();

    container = EdmEntityContainer.newBuilder()
            .setName("Container1")
            .setIsDefault(true)
            .setLazyLoadingEnabled(Boolean.TRUE)
            .addEntitySets(esets)
            .addAssociationSets(asets);

    schema = EdmSchema.newBuilder()
        .setNamespace(namespace)
        .addEntityTypes(etypes)
        .addComplexTypes(ctypes)
        .addAssociations(assocs)
        .addEntityContainers(container);

    return EdmDataServices.newBuilder().addSchemas(schema).build();
  }

  private EdmComplexType.Builder ct1 = null;

  private void createComplexTypes() {
    // ----------------------------- ComplexType1 --------------------------
    List<EdmProperty.Builder> props = new ArrayList<EdmProperty.Builder>();

    EdmProperty.Builder ep = EdmProperty.newBuilder("Prop1").setType(EdmSimpleType.STRING);
    props.add(ep);

    ep = EdmProperty.newBuilder("Prop2").setType(EdmSimpleType.STRING);
    props.add(ep);


    ct1 = EdmComplexType.newBuilder().setNamespace(namespace).setName("ComplexType1").addProperties(props);
    ctypes.add(ct1);
  }

  private void createEntityTypes() {
    // --------------------------- Type1 ------------------------------
    List<EdmProperty.Builder> props = new ArrayList<EdmProperty.Builder>();
    List<EdmNavigationProperty.Builder> navprops = new ArrayList<EdmNavigationProperty.Builder>();

    EdmProperty.Builder ep = null;

    ep = EdmProperty.newBuilder("EmptyStrings").setType(EdmSimpleType.STRING).setCollectionKind(CollectionKind.List);
    props.add(ep);

    ep = EdmProperty.newBuilder("ListOStrings").setType(EdmSimpleType.STRING).setCollectionKind(CollectionKind.List);
    props.add(ep);

    ep = EdmProperty.newBuilder("BagOStrings").setType(EdmSimpleType.STRING).setCollectionKind(CollectionKind.Bag);
    props.add(ep);

    ep = EdmProperty.newBuilder("BagOInts").setType(EdmSimpleType.INT32).setCollectionKind(CollectionKind.Bag);
    props.add(ep);

    ep = EdmProperty.newBuilder("Complex1").setType(ct1);
    props.add(ep);

    ep = EdmProperty.newBuilder("ListOComplex").setType(ct1).setCollectionKind(CollectionKind.List);
    props.add(ep);

    ep = EdmProperty.newBuilder("Id").setType(EdmSimpleType.STRING).setNullable(false);
    props.add(ep);

    List<String> keys = new ArrayList<String>();
    keys.add("Id");
    EdmEntityType.Builder type1Type = EdmEntityType.newBuilder()
            .setNamespace(namespace)
            .setName("Type1")
            .addKeys(keys)
            .addProperties(props)
            .addNavigationProperties(navprops);
    if (decorator != null) {
      type1Type.setDocumentation(decorator.getDocumentationForEntityType(Edm.namespace, Edm.EntityType.name()));
      type1Type.setAnnotations(decorator.getAnnotationsForEntityType(namespace, "Type1"));
    }

    etypes.add(type1Type);

    EdmEntitySet.Builder type1Set = EdmEntitySet.newBuilder().setName("Type1s").setEntityType(type1Type);
    esets.add(type1Set);

  }

  @SuppressWarnings("unused")
  private EdmAssociation.Builder defineAssociation(
          String assocName,
          EdmMultiplicity fromMult,
          EdmMultiplicity toMult,
          EdmEntityType.Builder fromEntityType,
          EdmEntitySet.Builder fromEntitySet,
          EdmEntityType.Builder toEntityType,
          EdmEntitySet.Builder toEntitySet) {

    // add EdmAssociation
    EdmAssociationEnd.Builder fromAssociationEnd = EdmAssociationEnd.newBuilder().setRole(fromEntityType.getName()).setType(fromEntityType).setMultiplicity(fromMult);
    String toAssociationRole = toEntityType.getName();
    if (toAssociationRole.equals(fromEntityType.getName())) {
      toAssociationRole = toAssociationRole + "1";
    }
    EdmAssociationEnd.Builder toAssociationEnd = EdmAssociationEnd.newBuilder().setRole(toAssociationRole).setType(toEntityType).setMultiplicity(toMult);
    EdmAssociation.Builder association = EdmAssociation.newBuilder().setNamespace(namespace).setName( assocName).setEnds(fromAssociationEnd, toAssociationEnd);

    // add EdmAssociationSet
    EdmAssociationSet.Builder associationSet = EdmAssociationSet.newBuilder()
            .setName(assocName)
            .setAssociation(association).setEnds(
            EdmAssociationSetEnd.newBuilder().setRole(fromAssociationEnd).setEntitySet(fromEntitySet),
            EdmAssociationSetEnd.newBuilder().setRole(toAssociationEnd).setEntitySet(toEntitySet));
    asets.add(associationSet);
    assocs.add(association);
    return association;
  }

}
