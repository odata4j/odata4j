
package org.odata4j.producer.custom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.core4j.Enumerable;
import org.odata4j.core.ODataConstants;
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

/**
 *
 */
public class CustomEdm implements EdmGenerator {

  @Override
  public EdmDecorator getDecorator() {
    return null;
  }

  public static final String namespace = "myns";

  @Override
  public EdmDataServices generateEdm() {
    createComplexTypes();
    createEntityTypes();

    container = new EdmEntityContainer(
            "Container1", // name
            true, // boolean isDefault,
            Boolean.TRUE, // Boolean lazyLoadingEnabled,
            esets, // List<EdmEntitySet> entitySets,
            asets, // List<EdmAssociationSet> associationSets, a
            null);          // List<EdmFunctionImport> functionImports) {

    schema = new EdmSchema(
            namespace,
            null, // String alias,
            etypes,
            ctypes,
            assocs,
            Enumerable.create(container).toList());


    EdmDataServices services = new EdmDataServices(ODataConstants.DATA_SERVICE_VERSION,
            Enumerable.create(schema).toList());
    return services;
  }

  private EdmComplexType ct1 = null;

  private void createComplexTypes() {
    // ----------------------------- ComplexType1 --------------------------
    List<EdmProperty.Builder> props = new ArrayList<EdmProperty.Builder>();

    EdmProperty.Builder ep = EdmProperty.newBuilder("Prop1").setType(EdmSimpleType.STRING);
    props.add(ep);

    ep = EdmProperty.newBuilder("Prop2").setType(EdmSimpleType.STRING);
    props.add(ep);


    ct1 = new EdmComplexType(namespace, "ComplexType1", props);
    ctypes.add(ct1);
  }

  private void createEntityTypes() {
    // --------------------------- Type1 ------------------------------
    List<EdmProperty.Builder> props = new ArrayList<EdmProperty.Builder>();
    List<EdmNavigationProperty> navprops = new ArrayList<EdmNavigationProperty>();

    EdmProperty.Builder ep = null;

    ep = EdmProperty.newBuilder("EmptyStrings").setType(EdmSimpleType.STRING).setCollectionKind(CollectionKind.LIST);
    props.add(ep);

    ep = EdmProperty.newBuilder("ListOStrings").setType(EdmSimpleType.STRING).setCollectionKind(CollectionKind.LIST);
    props.add(ep);

    ep = EdmProperty.newBuilder("BagOStrings").setType(EdmSimpleType.STRING).setCollectionKind(CollectionKind.BAG);
    props.add(ep);

    ep = EdmProperty.newBuilder("BagOInts").setType(EdmSimpleType.INT32).setCollectionKind(CollectionKind.BAG);
    props.add(ep);

    ep = EdmProperty.newBuilder("Complex1").setType(ct1);
    props.add(ep);

    ep = EdmProperty.newBuilder("ListOComplex").setType(ct1).setCollectionKind(CollectionKind.LIST);
    props.add(ep);

    ep = EdmProperty.newBuilder("Id").setType(EdmSimpleType.STRING).setNullable(false);
    props.add(ep);

    List<String> keys = new ArrayList<String>();
    keys.add("Id");
    EdmEntityType type1Type = new EdmEntityType(
            namespace,
            null, // alias
            "Type1",
            null, // hasStream
            keys,
            null, // baseType,
            props,
            navprops,
            null, // null == decorator ? null : decorator.getDocumentationForEntityType(Edm.namespace, Edm.EntityType.name()),
            null == decorator ? null : decorator.getAnnotationsForEntityType(namespace, "Type1"));

    etypes.add(type1Type);

    EdmEntitySet type1Set = new EdmEntitySet("Type1s", type1Type);
    esets.add(type1Set);

  }

  @SuppressWarnings("unused")
  private EdmAssociation defineAssociation(
          String assocName,
          EdmMultiplicity fromMult,
          EdmMultiplicity toMult,
          EdmEntityType fromEntityType,
          EdmEntitySet fromEntitySet,
          EdmEntityType toEntityType,
          EdmEntitySet toEntitySet) {

    // add EdmAssociation
    EdmAssociationEnd fromAssociationEnd = new EdmAssociationEnd(fromEntityType.getName(), fromEntityType, fromMult);
    String toAssociationRole = toEntityType.getName();
    if (toAssociationRole.equals(fromEntityType.getName())) {
      toAssociationRole = toAssociationRole + "1";
    }
    EdmAssociationEnd toAssociationEnd = new EdmAssociationEnd(toAssociationRole, toEntityType, toMult);
    EdmAssociation association = new EdmAssociation(namespace, null, assocName, fromAssociationEnd, toAssociationEnd);

    // add EdmAssociationSet
    EdmAssociationSet associationSet = new EdmAssociationSet(
            assocName,
            association,
            new EdmAssociationSetEnd(fromAssociationEnd, fromEntitySet),
            new EdmAssociationSetEnd(toAssociationEnd, toEntitySet));
    asets.add(associationSet);
    assocs.add(association);
    return association;
  }
  private EdmDecorator decorator = null;
  private EdmSchema schema = null;
  private EdmEntityContainer container = null;
  private List<EdmAssociation> assocs = new LinkedList<EdmAssociation>();
  private List<EdmComplexType> ctypes = new LinkedList<EdmComplexType>();
  private List<EdmEntityType> etypes = new LinkedList<EdmEntityType>();
  private List<EdmEntitySet> esets = new LinkedList<EdmEntitySet>();
  private List<EdmAssociationSet> asets = new LinkedList<EdmAssociationSet>();

}
