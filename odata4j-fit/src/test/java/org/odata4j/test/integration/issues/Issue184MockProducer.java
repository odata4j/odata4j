package org.odata4j.test.integration.issues;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.format.xml.EdmxFormatParser;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.edm.MetadataProducer;
import org.odata4j.stax2.XMLInputFactory2;
import org.odata4j.stax2.staximpl.StaxXMLFactoryProvider2;

public class Issue184MockProducer implements ODataProducer {

  private static final String SCENARIO_EDMX = "/META-INF/Issue184.edmx.xml";

  @Override
  public EdmDataServices getMetadata() {
    InputStream inputStream = Issue184MockProducer.class.getResourceAsStream(Issue184MockProducer.SCENARIO_EDMX);
    Reader reader = new InputStreamReader(inputStream);

    XMLInputFactory2 inputFactory = StaxXMLFactoryProvider2.getInstance().newXMLInputFactory2();
    EdmxFormatParser parser = new EdmxFormatParser();
    EdmDataServices edmDataService = parser.parseMetadata(inputFactory.createXMLEventReader(reader));

    return edmDataService;
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CountResponse getEntitiesCount(String entitySetName, QueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, EntityQueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BaseResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CountResponse getNavPropertyCount(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntity entity) {
    
    OEntityKey entityKey = OEntityKey.parse("1");
    List<OProperty<?>> properties = entity.getProperties();
    EdmEntitySet entitySet = this.getMetadata().findEdmEntitySet(entitySetName);
    OEntity newEntity = OEntities.create(entitySet , entityKey , properties, null);
    return Responses.entity(newEntity);
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deleteEntity(String entitySetName, OEntityKey entityKey) {
    // TODO Auto-generated method stub

  }

  @Override
  public void mergeEntity(String entitySetName, OEntity entity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateEntity(String entitySetName, OEntity entity) {
    // TODO Auto-generated method stub

  }

  @Override
  public EntityIdResponse getLinks(OEntityId sourceEntity, String targetNavProp) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void updateLink(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteLink(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {
    // TODO Auto-generated method stub

  }

  @Override
  public BaseResponse callFunction(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    // TODO Auto-generated method stub
    return null;
  }

}
