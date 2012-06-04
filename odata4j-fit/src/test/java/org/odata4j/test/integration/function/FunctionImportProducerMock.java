package org.odata4j.test.integration.function;

import java.util.ArrayList;
import java.util.Map;

import org.odata4j.core.OComplexObject;
import org.odata4j.core.OComplexObjects;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmSimpleType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionImportProducerMock implements ODataProducer {

  public static final String COMPLEY_TYPE_NAME_LOCATION = "RefScenario.c_Location";
  public static final String COMPLEY_TYPE_NAME_CITY = "RefScenario.c_City";
  public static final String COUNTRY = "Bavaria";
  public static final String CITY = "Munic";
  public static final String POSTAL_CODE = "12345";
  public static final String EMPLOYEE_NAME = "Hugo Hurtig";
  public static final String EMPLOYEE_ID = "abc123";

  public static final boolean BOOLEAN_VALUE = true;

  public static final String SOME_TEXT = "some text";

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionImportProducerMock.class);

  private Map<String, OFunctionParameter> queryParameter;

  private QueryInfo queryInfo;

  private EdmDataServices metadata;

  @Override
  public EdmDataServices getMetadata() {
    if (this.metadata == null) {
      this.metadata = MetadataUtil.readMetadataServiceFromFile();
    }
    return this.metadata;
  }

  @Override
  public MetadataProducer getMetadataProducer() {
    return null;
  }

  @Override
  public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public CountResponse getEntitiesCount(String entitySetName, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, EntityQueryInfo queryInfo) {
    return null;
  }

  @Override
  public BaseResponse getNavProperty(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public CountResponse getNavPropertyCount(String entitySetName, OEntityKey entityKey, String navProp, QueryInfo queryInfo) {
    return null;
  }

  @Override
  public void close() {}

  @Override
  public EntityResponse createEntity(String entitySetName, OEntity entity) {
    return null;
  }

  @Override
  public EntityResponse createEntity(String entitySetName, OEntityKey entityKey, String navProp, OEntity entity) {
    return null;
  }

  @Override
  public void deleteEntity(String entitySetName, OEntityKey entityKey) {}

  @Override
  public void mergeEntity(String entitySetName, OEntity entity) {}

  @Override
  public void updateEntity(String entitySetName, OEntity entity) {}

  @Override
  public EntityIdResponse getLinks(OEntityId sourceEntity, String targetNavProp) {
    return null;
  }

  @Override
  public void createLink(OEntityId sourceEntity, String targetNavProp, OEntityId targetEntity) {}

  @Override
  public void updateLink(OEntityId sourceEntity, String targetNavProp, OEntityKey oldTargetEntityKey, OEntityId newTargetEntity) {}

  @Override
  public void deleteLink(OEntityId sourceEntity, String targetNavProp, OEntityKey targetEntityKey) {}

  @Override
  public BaseResponse callFunction(EdmFunctionImport name, Map<String, OFunctionParameter> params, QueryInfo queryInfo) {
    BaseResponse response;

    FunctionImportProducerMock.LOGGER.debug("EdmFunctionImport Object:    " + name.getName());
    FunctionImportProducerMock.LOGGER.debug("EdmFunctionImport Parameter: " + params);
    FunctionImportProducerMock.LOGGER.debug("EdmFunctionImport QueryInfo: " + queryInfo);

    this.queryParameter = params;
    this.queryInfo = queryInfo;

    if (MetadataUtil.TEST_FUNCTION_RETURN_STRING.equals(name.getName())) {
      response = Responses.simple(EdmSimpleType.STRING, name.getName(), FunctionImportProducerMock.SOME_TEXT);
    } else if (MetadataUtil.TEST_FUNCTION_RETURN_BOOLEAN.equals(name.getName())) {
      response = Responses.simple(EdmSimpleType.BOOLEAN, name.getName(), FunctionImportProducerMock.BOOLEAN_VALUE);
    } else if (MetadataUtil.TEST_FUNCTION_RETURN_EMPLOYEE.equals(name.getName())) {
      OEntity entity = this.createEmployeeEntity();
      response = Responses.entity(entity);
    } else if (MetadataUtil.TEST_FUNCTION_RETURN_COMPLEX_TYPE.equals(name.getName())) {
      OComplexObject complexObject = this.createCompleyTypeLocation();
      response = Responses.complexObject(complexObject);
    }
    else {
      throw new RuntimeException("Unsupported Test Case for FunctionImport: " + name.getName());
    }

    return response;
  }

  private OComplexObject createCompleyTypeLocation() {
    ArrayList<OProperty<?>> propertiesCity = new ArrayList<OProperty<?>>();
    propertiesCity.add(OProperties.string("PostalCode", FunctionImportProducerMock.POSTAL_CODE));
    propertiesCity.add(OProperties.string("CityName", FunctionImportProducerMock.CITY));
    OComplexObject cityType = OComplexObjects.create(this.getMetadata().findEdmComplexType(FunctionImportProducerMock.COMPLEY_TYPE_NAME_CITY), propertiesCity);

    ArrayList<OProperty<?>> propertiesLocation = new ArrayList<OProperty<?>>();
    propertiesLocation.add(OProperties.complex("City", this.getMetadata().findEdmComplexType(FunctionImportProducerMock.COMPLEY_TYPE_NAME_CITY), propertiesCity));
    propertiesLocation.add(OProperties.string("Country", FunctionImportProducerMock.COUNTRY));
    return OComplexObjects.create(this.getMetadata().findEdmComplexType(FunctionImportProducerMock.COMPLEY_TYPE_NAME_LOCATION), propertiesLocation);
  }

  private OEntity createEmployeeEntity() {
    EdmEntitySet entitySet = this.getMetadata().findEdmEntitySet("Employees");
    OEntityKey entityKey = OEntityKey.parse("EmployeeId='" + FunctionImportProducerMock.EMPLOYEE_ID + "'");
    ArrayList<OProperty<?>> properties = new ArrayList<OProperty<?>>();
    properties.add(OProperties.string("EmployeeName", FunctionImportProducerMock.EMPLOYEE_NAME));
    properties.add(OProperties.string("EmployeeId", FunctionImportProducerMock.EMPLOYEE_ID));
    OEntity entity = OEntities.create(entitySet, entityKey, properties, null);
    return entity;
  }

  public Map<String, OFunctionParameter> getQueryParameter() {
    return this.queryParameter;
  }

  public QueryInfo getQueryInfo() {
    return this.queryInfo;
  }
}
