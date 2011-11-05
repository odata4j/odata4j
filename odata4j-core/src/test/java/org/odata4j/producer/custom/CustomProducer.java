
package org.odata4j.producer.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.odata4j.core.OCollection.Builder;
import org.odata4j.core.OCollections;
import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OLink;
import org.odata4j.core.OObject;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.edm.MetadataProducer;
import org.odata4j.producer.exceptions.NotFoundException;

/**
 * A custom producer for various test scenarios that aren't possible with
 * stock producers
 */
public class CustomProducer implements ODataProducer {

  private final EdmDataServices edm = new CustomEdm().generateEdm(null).build();
  private final MetadataProducer metadataProducer;

  public CustomProducer() {
    this.metadataProducer = new MetadataProducer(this, null);
  }

  @Override
  public EdmDataServices getMetadata() {
    return edm;
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    return metadataProducer;
  }

  @Override
  public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
    if (entitySetName.equals("Type1s")) {
      return Responses.entities(getType1s(), edm.findEdmEntitySet(entitySetName), null, null);
    } else {
      throw new NotFoundException("Unknown entity set: " + entitySetName);
    }
  }

  private List<OEntity> getType1s() {
    List<OEntity> l = new ArrayList<OEntity>(3);
    for (int i = 0; i < 3; i++) {
      l.add(getType1(0));
    }
    return l;
  }

  private OEntity getType1(int i) {
    List<OProperty<?>> props = new ArrayList<OProperty<?>>(3);
    String id = Integer.toString(i);
    props.add(OProperties.string("Id", id));

    Builder<OObject> builder = OCollections.newBuilder(EdmSimpleType.STRING);
    props.add(OProperties.collection("EmptyStrings", new EdmCollectionType(CollectionKind.Bag, EdmSimpleType.STRING), builder.build()));

    builder = OCollections.newBuilder(EdmSimpleType.STRING);
    for (int j = 0; j < 3; j++) {
      builder.add(OSimpleObjects.create(EdmSimpleType.STRING, "bagstring-" + j));
    }
    props.add(OProperties.collection("BagOStrings", new EdmCollectionType(CollectionKind.Bag, EdmSimpleType.STRING), builder.build()));

    builder = OCollections.newBuilder(EdmSimpleType.STRING);
    for (int j = 0; j < 5; j++) {
      builder.add(OSimpleObjects.create(EdmSimpleType.STRING, "liststring-" + j));
    }
    props.add(OProperties.collection("ListOStrings", new EdmCollectionType(CollectionKind.List, EdmSimpleType.STRING), builder.build()));

    builder = OCollections.newBuilder(EdmSimpleType.INT32);
    for (int j = 0; j < 5; j++) {
      builder.add(OSimpleObjects.create(EdmSimpleType.INT32, j));
    }
    props.add(OProperties.collection("BagOInts", new EdmCollectionType(CollectionKind.List, EdmSimpleType.INT32), builder.build()));

    EdmComplexType ct1 = this.getMetadata().findEdmComplexType("myns.ComplexType1");
    OComplexObject.Builder cb = OComplexObjects.newBuilder(ct1);
    cb.add(OProperties.string("Prop1", "Val1")).add(OProperties.string("Prop2", "Val2"));
    // hmmh, I swear I put a form of OProperties.complex that took an OComplexObject....
    props.add(OProperties.complex("Complex1", ct1, cb.build().getProperties()));

    builder = OCollections.newBuilder(ct1);
    for (int j = 0; j < 2; j++) {
      cb = OComplexObjects.newBuilder(ct1);
      cb.add(OProperties.string("Prop1", "Val" + j)).add(OProperties.string("Prop2", "Val" + j));

      builder.add(cb.build());
    }
    props.add(OProperties.collection("ListOComplex", new EdmCollectionType(CollectionKind.List, ct1), builder.build()));

    return OEntities.create(
        edm.findEdmEntitySet("Type1s"),
        OEntityKey.create("Id", id),
        props,
        new ArrayList<OLink>());
  }

  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, QueryInfo queryInfo) {
    if (entitySetName.equals("Type1s")) {
      return Responses.entity(getType1(Integer.parseInt((String)entityKey.asSingleValue())));
    } else {
      throw new NotFoundException("Unknown entity set: " + entitySetName);
    }
  }

  @Override
  public BaseResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntity entity) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteEntity(String entitySetName, OEntityKey entityKey) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void mergeEntity(String entitySetName, OEntity entity) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateEntity(String entitySetName, OEntity entity) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public EntityIdResponse getLinks(OEntityId sourceEntity, String targetNavProp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateLink(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteLink(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
