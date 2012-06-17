package org.odata4j.format.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.core4j.Predicate1;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OPredicates;
import org.odata4j.core.PrefixedNamespace;
import org.odata4j.edm.EdmAnnotation;
import org.odata4j.edm.EdmAssociation;
import org.odata4j.edm.EdmAssociationEnd;
import org.odata4j.edm.EdmAssociationSet;
import org.odata4j.edm.EdmAssociationSetEnd;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmFunctionParameter.Mode;
import org.odata4j.edm.EdmMultiplicity;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmType;
import org.odata4j.internal.AndroidCompat;
import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.Namespace2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;

public class EdmxFormatParser extends XmlFormatParser {

  private final EdmDataServices.Builder dataServices = EdmDataServices.newBuilder();

  public EdmxFormatParser() {}

  public EdmDataServices parseMetadata(XMLEventReader2 reader) {
    List<EdmSchema.Builder> schemas = new ArrayList<EdmSchema.Builder>();
    List<PrefixedNamespace> namespaces = null;

    ODataVersion version = null;
    boolean foundDataServices = false;
    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      boolean shouldReturn = false;

      if (isStartElement(event, XmlFormatParser.EDMX_EDMX)) {
        namespaces = getExtensionNamespaces(event.asStartElement());
      }

      if (isStartElement(event, EDMX_DATASERVICES)) {
        foundDataServices = true;
        String str = getAttributeValueIfExists(event.asStartElement(), new QName2(NS_METADATA, "DataServiceVersion"));
        version = str != null
            ? ODataVersion.parse(str)
            : null;
      }

      if (isStartElement(event, EDM2006_SCHEMA, EDM2007_SCHEMA, EDM2008_1_SCHEMA, EDM2008_9_SCHEMA, EDM2009_SCHEMA)) {
        schemas.add(parseEdmSchema(reader, event.asStartElement()));
        if (!foundDataServices) // some dallas services have Schema as the document element!
          shouldReturn = true;
      }

      if (isEndElement(event, EDMX_DATASERVICES))
        shouldReturn = true;

      if (shouldReturn) {
        dataServices.setVersion(version).addSchemas(schemas).addNamespaces(namespaces);
        resolve();
        return dataServices.build();
      }
    }

    throw new UnsupportedOperationException();
  }

  private void resolve() {

    final Map<String, EdmEntityType.Builder> allEetsByFQName = Enumerable
        .create(dataServices.getEntityTypes())
        .toMap(EdmEntityType.Builder.func1_getFullyQualifiedTypeName());

    final Map<String, EdmAssociation.Builder> allEasByFQName = Enumerable
        .create(dataServices.getAssociations())
        .toMap(EdmAssociation.Builder.func1_getFQNamespaceName());

    for (EdmSchema.Builder edmSchema : dataServices.getSchemas()) {

      // resolve associations
      for (int i = 0; i < edmSchema.getAssociations().size(); i++) {
        EdmAssociation.Builder tmpAssociation = edmSchema.getAssociations().get(i);

        tmpAssociation.getEnd1().setType(allEetsByFQName.get(tmpAssociation.getEnd1().getTypeName()));
        tmpAssociation.getEnd2().setType(allEetsByFQName.get(tmpAssociation.getEnd2().getTypeName()));
      }

      // resolve navproperties
      for (EdmEntityType.Builder eet : edmSchema.getEntityTypes()) {
        List<EdmNavigationProperty.Builder> navProps = eet.getNavigationProperties();
        for (int i = 0; i < navProps.size(); i++) {
          final EdmNavigationProperty.Builder tmp = navProps.get(i);
          final EdmAssociation.Builder ea = allEasByFQName.get(tmp.getRelationshipName());
          if (ea == null)
            throw new IllegalArgumentException("Invalid relationship name " + tmp.getRelationshipName());

          List<EdmAssociationEnd.Builder> finalEnds = Enumerable.create(tmp.getFromRoleName(), tmp.getToRoleName()).select(new Func1<String, EdmAssociationEnd.Builder>() {
            public EdmAssociationEnd.Builder apply(String input) {
              if (ea.getEnd1().getRole().equals(input))
                return ea.getEnd1();
              if (ea.getEnd2().getRole().equals(input))
                return ea.getEnd2();
              throw new IllegalArgumentException("Invalid role name " + input);
            }
          }).toList();

          tmp.setRelationship(ea).setFromTo(finalEnds.get(0), finalEnds.get(1));
        }
      }

      // resolve entitysets
      for (EdmEntityContainer.Builder edmEntityContainer : edmSchema.getEntityContainers()) {
        for (int i = 0; i < edmEntityContainer.getEntitySets().size(); i++) {
          final EdmEntitySet.Builder tmpEes = edmEntityContainer.getEntitySets().get(i);
          EdmEntityType.Builder eet = allEetsByFQName.get(tmpEes.getEntityTypeName());
          if (eet == null)
            throw new IllegalArgumentException("Invalid entity type " + tmpEes.getEntityTypeName());
          edmEntityContainer.getEntitySets().set(i, EdmEntitySet.newBuilder().setName(tmpEes.getName()).setEntityType(eet));
        }
      }

      // resolve associationsets
      for (final EdmEntityContainer.Builder edmEntityContainer : edmSchema.getEntityContainers()) {
        for (int i = 0; i < edmEntityContainer.getAssociationSets().size(); i++) {
          final EdmAssociationSet.Builder tmpEas = edmEntityContainer.getAssociationSets().get(i);
          final EdmAssociation.Builder ea = allEasByFQName.get(tmpEas.getAssociationName());

          List<EdmAssociationSetEnd.Builder> finalEnds = Enumerable.create(tmpEas.getEnd1(), tmpEas.getEnd2())
              .select(new Func1<EdmAssociationSetEnd.Builder, EdmAssociationSetEnd.Builder>() {
                public EdmAssociationSetEnd.Builder apply(final EdmAssociationSetEnd.Builder input) {

                  EdmAssociationEnd.Builder eae =
                      ea.getEnd1().getRole().equals(input.getRoleName()) ? ea.getEnd1()
                          : ea.getEnd2().getRole().equals(input.getRoleName()) ? ea.getEnd2() : null;

                  if (eae == null)
                    throw new IllegalArgumentException("Invalid role name " + input.getRoleName());

                  EdmEntitySet.Builder ees = Enumerable.create(edmEntityContainer.getEntitySets()).first(OPredicates.nameEquals(EdmEntitySet.Builder.class, input.getEntitySetName()));
                  return EdmAssociationSetEnd.newBuilder().setRole(eae).setEntitySet(ees);
                }
              }).toList();

          tmpEas.setAssociation(ea).setEnds(finalEnds.get(0), finalEnds.get(1));
        }
      }

      // resolve functionimports
      for (final EdmEntityContainer.Builder edmEntityContainer : edmSchema.getEntityContainers()) {
        for (int i = 0; i < edmEntityContainer.getFunctionImports().size(); i++) {
          final EdmFunctionImport.Builder tmpEfi = edmEntityContainer.getFunctionImports().get(i);
          EdmEntitySet.Builder ees = Enumerable.create(edmEntityContainer.getEntitySets()).firstOrNull(new Predicate1<EdmEntitySet.Builder>() {
            public boolean apply(EdmEntitySet.Builder input) {
              return input.getName().equals(tmpEfi.getEntitySetName());
            }
          });

          EdmType.Builder<?, ?> typeBuilder = null;
          if (tmpEfi.getReturnTypeName() != null) {
            typeBuilder = dataServices.resolveType(tmpEfi.getReturnTypeName());
            if (typeBuilder == null)
              throw new RuntimeException("Edm-type not found: " + tmpEfi.getReturnTypeName());

            if (tmpEfi.isCollection()) {
              typeBuilder = EdmCollectionType.newBuilder().setKind(CollectionKind.Collection).setCollectionType(typeBuilder);
            }
          }

          edmEntityContainer.getFunctionImports().set(i,
              EdmFunctionImport.newBuilder()
                  .setName(tmpEfi.getName())
                  .setEntitySet(ees)
                  .setReturnType(typeBuilder)
                  .setHttpMethod(tmpEfi.getHttpMethod())
                  .addParameters(tmpEfi.getParameters()));
        }
      }

      // resolve type hierarchy
      for (Entry<String, EdmEntityType.Builder> entry : allEetsByFQName.entrySet()) {
        String baseTypeName = entry.getValue().getFQBaseTypeName();
        if (baseTypeName != null) {
          EdmEntityType.Builder baseType = allEetsByFQName.get(baseTypeName);
          if (baseType == null) {
            throw new IllegalArgumentException("Invalid baseType: " + baseTypeName);
          }
          entry.getValue().setBaseType(baseType);
        }
      }

    }

  }

  private EdmSchema.Builder parseEdmSchema(XMLEventReader2 reader, StartElement2 schemaElement) {

    String schemaNamespace = schemaElement.getAttributeByName(new QName2("Namespace")).getValue();
    String schemaAlias = getAttributeValueIfExists(schemaElement, new QName2("Alias"));
    final List<EdmEntityType.Builder> edmEntityTypes = new ArrayList<EdmEntityType.Builder>();
    List<EdmComplexType.Builder> edmComplexTypes = new ArrayList<EdmComplexType.Builder>();
    List<EdmAssociation.Builder> edmAssociations = new ArrayList<EdmAssociation.Builder>();
    List<EdmEntityContainer.Builder> edmEntityContainers = new ArrayList<EdmEntityContainer.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_ENTITYTYPE, EDM2007_ENTITYTYPE, EDM2008_1_ENTITYTYPE, EDM2008_9_ENTITYTYPE, EDM2009_ENTITYTYPE)) {
        EdmEntityType.Builder edmEntityType = parseEdmEntityType(reader, schemaNamespace, schemaAlias, event.asStartElement());
        edmEntityTypes.add(edmEntityType);
      }
      if (isStartElement(event, EDM2006_ASSOCIATION, EDM2007_ASSOCIATION, EDM2008_1_ASSOCIATION, EDM2008_9_ASSOCIATION, EDM2009_ASSOCIATION)) {
        EdmAssociation.Builder edmAssociation = parseEdmAssociation(reader, schemaNamespace, schemaAlias, event.asStartElement());
        edmAssociations.add(edmAssociation);
      }
      if (isStartElement(event, EDM2006_COMPLEXTYPE, EDM2007_COMPLEXTYPE, EDM2008_1_COMPLEXTYPE, EDM2008_9_COMPLEXTYPE, EDM2009_COMPLEXTYPE)) {
        EdmComplexType.Builder edmComplexType = parseEdmComplexType(reader, schemaNamespace, event.asStartElement());
        edmComplexTypes.add(edmComplexType);
      }
      if (isStartElement(event, EDM2006_ENTITYCONTAINER, EDM2007_ENTITYCONTAINER, EDM2008_1_ENTITYCONTAINER, EDM2008_9_ENTITYCONTAINER, EDM2009_ENTITYCONTAINER)) {
        EdmEntityContainer.Builder edmEntityContainer = parseEdmEntityContainer(reader, schemaNamespace, event.asStartElement());
        edmEntityContainers.add(edmEntityContainer);
      }
      if (isEndElement(event, schemaElement.getName())) {
        return EdmSchema.newBuilder().setNamespace(schemaNamespace).setAlias(schemaAlias)
            .addEntityTypes(edmEntityTypes)
            .addComplexTypes(edmComplexTypes)
            .addAssociations(edmAssociations)
            .addEntityContainers(edmEntityContainers)
            .setAnnotations(getAnnotations(schemaElement));
      }
    }

    throw new UnsupportedOperationException();

  }

  private EdmEntityContainer.Builder parseEdmEntityContainer(XMLEventReader2 reader, String schemaNamespace, StartElement2 entityContainerElement) {
    String name = entityContainerElement.getAttributeByName("Name").getValue();
    boolean isDefault = "true".equals(getAttributeValueIfExists(entityContainerElement, new QName2(NS_METADATA, "IsDefaultEntityContainer")));
    String lazyLoadingEnabledValue = getAttributeValueIfExists(entityContainerElement, new QName2(NS_EDMANNOTATION, "LazyLoadingEnabled"));
    Boolean lazyLoadingEnabled = lazyLoadingEnabledValue == null ? null : lazyLoadingEnabledValue.equals("true");

    List<EdmEntitySet.Builder> edmEntitySets = new ArrayList<EdmEntitySet.Builder>();
    List<EdmAssociationSet.Builder> edmAssociationSets = new ArrayList<EdmAssociationSet.Builder>();
    List<EdmFunctionImport.Builder> edmFunctionImports = new ArrayList<EdmFunctionImport.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_ENTITYSET, EDM2007_ENTITYSET, EDM2008_1_ENTITYSET, EDM2008_9_ENTITYSET, EDM2009_ENTITYSET)) {
        StartElement2 entitySetStartElement = event.asStartElement();
        edmEntitySets.add(EdmEntitySet.newBuilder()
                .setName(getAttributeValueIfExists(entitySetStartElement, "Name"))
                .setEntityTypeName(getAttributeValueIfExists(entitySetStartElement, "EntityType"))
                .setAnnotations(getAnnotations(entitySetStartElement)));
      }

      if (isStartElement(event, EDM2006_ASSOCIATIONSET, EDM2007_ASSOCIATIONSET, EDM2008_1_ASSOCIATIONSET, EDM2008_9_ASSOCIATIONSET, EDM2009_ASSOCIATIONSET))
        edmAssociationSets.add(parseEdmAssociationSet(reader, schemaNamespace, event.asStartElement()));

      if (isStartElement(event, EDM2006_FUNCTIONIMPORT, EDM2007_FUNCTIONIMPORT, EDM2008_1_FUNCTIONIMPORT, EDM2008_9_FUNCTIONIMPORT, EDM2009_FUNCTIONIMPORT))
        edmFunctionImports.add(parseEdmFunctionImport(reader, schemaNamespace, event.asStartElement()));

      if (isEndElement(event, entityContainerElement.getName())) {
        return EdmEntityContainer.newBuilder().setName(name).setIsDefault(isDefault).setLazyLoadingEnabled(lazyLoadingEnabled)
            .addEntitySets(edmEntitySets).addAssociationSets(edmAssociationSets).addFunctionImports(edmFunctionImports)
                .setAnnotations(getAnnotations(entityContainerElement));
      }
    }
    throw new UnsupportedOperationException();

  }

  private EdmFunctionImport.Builder parseEdmFunctionImport(XMLEventReader2 reader, String schemaNamespace, StartElement2 functionImportElement) {
    String name = functionImportElement.getAttributeByName("Name").getValue();
    String entitySet = getAttributeValueIfExists(functionImportElement, "EntitySet");
    Attribute2 returnTypeAttr = functionImportElement.getAttributeByName("ReturnType");
    String returnType = returnTypeAttr != null ? returnTypeAttr.getValue() : null;

    // strict parsing
    boolean isCollection = returnType != null && returnType.matches("^Collection\\(.*\\)$");
    if (isCollection) {
      returnType = returnType.substring(11, returnType.length() - 1);
    }
    String httpMethod = getAttributeValueIfExists(functionImportElement, new QName2(NS_METADATA, "HttpMethod"));

    List<EdmFunctionParameter.Builder> parameters = new ArrayList<EdmFunctionParameter.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_PARAMETER, EDM2007_PARAMETER, EDM2008_1_PARAMETER, EDM2008_9_PARAMETER, EDM2009_PARAMETER)) {
        // Mode attribute is optional and thus can be null
        StartElement2 paramStartElement = event.asStartElement();
        Attribute2 modeAttribute = paramStartElement.getAttributeByName("Mode");
        parameters.add(EdmFunctionParameter.newBuilder()
            .setName(paramStartElement.getAttributeByName("Name").getValue())
            //.setType(EdmType.get(event.asStartElement().getAttributeByName("Type").getValue()))
            .setType(EdmType.newDeferredBuilder(paramStartElement.getAttributeByName("Type").getValue(), dataServices))
            .setMode(modeAttribute != null ? Mode.valueOf(modeAttribute.getValue()) : null)
            .setAnnotations(getAnnotations(paramStartElement)));
      }

      if (isEndElement(event, functionImportElement.getName())) {
        return EdmFunctionImport.newBuilder().setName(name).setEntitySetName(entitySet).setReturnTypeName(returnType).setIsCollection(isCollection).setHttpMethod(httpMethod)
            .addParameters(parameters).setAnnotations(getAnnotations(functionImportElement));
      }
    }
    throw new UnsupportedOperationException();

  }

  private EdmAssociationSet.Builder parseEdmAssociationSet(XMLEventReader2 reader, String schemaNamespace, StartElement2 associationSetElement) {
    String name = associationSetElement.getAttributeByName("Name").getValue();
    String associationName = associationSetElement.getAttributeByName("Association").getValue();

    List<EdmAssociationSetEnd.Builder> ends = new ArrayList<EdmAssociationSetEnd.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_END, EDM2007_END, EDM2008_1_END, EDM2008_9_END, EDM2009_END)) {
        StartElement2 endStartElement = event.asStartElement();
        ends.add(EdmAssociationSetEnd.newBuilder()
            .setRoleName(endStartElement.getAttributeByName("Role").getValue())
            .setEntitySetName(endStartElement.getAttributeByName("EntitySet").getValue())
            .setAnnotations(getAnnotations(endStartElement)));
      }

      if (isEndElement(event, associationSetElement.getName())) {
        return EdmAssociationSet.newBuilder().setName(name).setAssociationName(associationName)
                .setEnds(ends.get(0), ends.get(1))
                .setAnnotations(getAnnotations(associationSetElement));
      }
    }
    throw new UnsupportedOperationException();

  }

  private EdmAssociation.Builder parseEdmAssociation(XMLEventReader2 reader, String schemaNamespace, String schemaAlias, StartElement2 associationElement) {
    String name = associationElement.getAttributeByName("Name").getValue();

    List<EdmAssociationEnd.Builder> ends = new ArrayList<EdmAssociationEnd.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_END, EDM2007_END, EDM2008_1_END, EDM2008_9_END, EDM2009_END)) {
        StartElement2 endStartElement = event.asStartElement();
        ends.add(EdmAssociationEnd.newBuilder()
            .setRole(endStartElement.getAttributeByName("Role").getValue())
            .setTypeName(endStartElement.getAttributeByName("Type").getValue())
            .setMultiplicity(EdmMultiplicity.fromSymbolString(endStartElement.getAttributeByName("Multiplicity").getValue()))
            .setAnnotations(getAnnotations(endStartElement)));
      }

      if (isEndElement(event, associationElement.getName())) {
        return EdmAssociation.newBuilder().setNamespace(schemaNamespace)
                .setAlias(schemaAlias).setName(name)
                .setEnds(ends.get(0), ends.get(1))
                .setAnnotations(getAnnotations(associationElement));
      }
    }
    throw new UnsupportedOperationException();

  }

  private EdmProperty.Builder parseEdmProperty(XMLEvent2 event) {
    StartElement2 startElement = event.asStartElement();
    String propertyName = getAttributeValueIfExists(startElement, "Name");
    String propertyType = getAttributeValueIfExists(startElement, "Type");
    String propertyNullable = getAttributeValueIfExists(startElement, "Nullable");
    String maxLength = getAttributeValueIfExists(startElement, "MaxLength");
    String unicode = getAttributeValueIfExists(startElement, "Unicode");
    String fixedLength = getAttributeValueIfExists(startElement, "FixedLength");
    String collectionKindS = getAttributeValueIfExists(startElement, "CollectionKind");
    CollectionKind ckind = CollectionKind.NONE;
    if (collectionKindS != null) {
      ckind = Enum.valueOf(CollectionKind.class, collectionKindS);
    }
    String defaultValue = getAttributeValueIfExists(startElement, "DefaultValue");
    String precision = getAttributeValueIfExists(startElement, "Precision");
    String scale = getAttributeValueIfExists(startElement, "Scale");

    String storeGeneratedPattern = getAttributeValueIfExists(startElement, new QName2(NS_EDMANNOTATION, "StoreGeneratedPattern"));

    String fcTargetPath = getAttributeValueIfExists(startElement, M_FC_TARGETPATH);
    String fcContentKind = getAttributeValueIfExists(startElement, M_FC_CONTENTKIND);
    String fcKeepInContent = getAttributeValueIfExists(startElement, M_FC_KEEPINCONTENT);
    String fcEpmContentKind = getAttributeValueIfExists(startElement, M_FC_EPMCONTENTKIND);
    String fcEpmKeepInContent = getAttributeValueIfExists(startElement, M_FC_EPMKEEPINCONTENT);

    return EdmProperty.newBuilder(propertyName)
        .setType(EdmType.newDeferredBuilder(propertyType, dataServices))
        .setNullable("true".equalsIgnoreCase(propertyNullable))
        .setMaxLength(maxLength == null ? null : maxLength.equals("Max") ? Integer.MAX_VALUE : Integer.parseInt(maxLength))
        .setUnicode("true".equalsIgnoreCase(unicode))
        .setFixedLength("true".equalsIgnoreCase(fixedLength))
        .setStoreGeneratedPattern(storeGeneratedPattern)
        .setFcTargetPath(fcTargetPath)
        .setFcContentKind(fcContentKind)
        .setFcKeepInContent(fcKeepInContent)
        .setFcEpmContentKind(fcEpmContentKind)
        .setFcEpmKeepInContent(fcEpmKeepInContent)
        .setCollectionKind(ckind)
        .setDefaultValue(defaultValue)
        .setPrecision(precision == null ? null : Integer.parseInt(precision))
        .setScale(scale == null ? null : Integer.parseInt(scale))
        .setAnnotations(getAnnotations(startElement));
  }

  private EdmComplexType.Builder parseEdmComplexType(XMLEventReader2 reader, String schemaNamespace, StartElement2 complexTypeElement) {
    String name = complexTypeElement.getAttributeByName("Name").getValue();
    String isAbstractS = getAttributeValueIfExists(complexTypeElement, "Abstract");
    List<EdmProperty.Builder> edmProperties = new ArrayList<EdmProperty.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_PROPERTY, EDM2007_PROPERTY, EDM2008_1_PROPERTY, EDM2008_9_PROPERTY, EDM2009_PROPERTY)) {
        edmProperties.add(parseEdmProperty(event));
      }

      if (isEndElement(event, complexTypeElement.getName())) {
        EdmComplexType.Builder complexType = EdmComplexType.newBuilder()
                .setNamespace(schemaNamespace)
                .setName(name)
                .addProperties(edmProperties)
                .setAnnotations(getAnnotations(complexTypeElement));
        if (isAbstractS != null)
          complexType.setIsAbstract("true".equals(isAbstractS));
        return complexType;
      }
    }

    throw new UnsupportedOperationException();

  }

  private EdmEntityType.Builder parseEdmEntityType(XMLEventReader2 reader, String schemaNamespace, String schemaAlias, StartElement2 entityTypeElement) {
    String name = entityTypeElement.getAttributeByName("Name").getValue();
    String hasStreamValue = getAttributeValueIfExists(entityTypeElement, new QName2(NS_METADATA, "HasStream"));
    Boolean hasStream = hasStreamValue == null ? null : hasStreamValue.equals("true");
    String baseType = getAttributeValueIfExists(entityTypeElement, "BaseType");
    String isAbstractS = getAttributeValueIfExists(entityTypeElement, "Abstract");


    List<String> keys = new ArrayList<String>();
    List<EdmProperty.Builder> edmProperties = new ArrayList<EdmProperty.Builder>();
    List<EdmNavigationProperty.Builder> edmNavigationProperties = new ArrayList<EdmNavigationProperty.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_PROPERTYREF, EDM2007_PROPERTYREF, EDM2008_1_PROPERTYREF, EDM2008_9_PROPERTYREF, EDM2009_PROPERTYREF))
        keys.add(event.asStartElement().getAttributeByName("Name").getValue());

      if (isStartElement(event, EDM2006_PROPERTY, EDM2007_PROPERTY, EDM2008_1_PROPERTY, EDM2008_9_PROPERTY, EDM2009_PROPERTY)) {
        edmProperties.add(parseEdmProperty(event));
      }

      if (isStartElement(event, EDM2006_NAVIGATIONPROPERTY, EDM2007_NAVIGATIONPROPERTY, EDM2008_1_NAVIGATIONPROPERTY, EDM2008_9_NAVIGATIONPROPERTY, EDM2009_NAVIGATIONPROPERTY)) {
        StartElement2 navPropStartElement = event.asStartElement();
        String associationName = navPropStartElement.getAttributeByName("Name").getValue();
        String relationshipName = navPropStartElement.getAttributeByName("Relationship").getValue();
        String fromRoleName = navPropStartElement.getAttributeByName("FromRole").getValue();
        String toRoleName = navPropStartElement.getAttributeByName("ToRole").getValue();

        edmNavigationProperties.add(EdmNavigationProperty.newBuilder(associationName)
                .setRelationshipName(relationshipName)
                .setFromToName(fromRoleName, toRoleName)
                .setAnnotations(getAnnotations(navPropStartElement)));
      }

      if (isEndElement(event, entityTypeElement.getName())) {
        return EdmEntityType.newBuilder()
            .setNamespace(schemaNamespace)
            .setAlias(schemaAlias)
            .setName(name)
            .setHasStream(hasStream)
            .addKeys(keys)
            .addProperties(edmProperties)
            .addNavigationProperties(edmNavigationProperties)
            .setBaseType(baseType)
            .setIsAbstract(isAbstractS == null ? null : "true".equals(isAbstractS))
            .setAnnotations(getAnnotations(entityTypeElement));
      }
    }

    throw new UnsupportedOperationException();
  }

  protected boolean isExtensionNamespace(String namespaceUri) {
    return namespaceUri != null &&
           !AndroidCompat.String_isEmpty(namespaceUri.trim()) &&
           !namespaceUri.contains("schemas.microsoft.com");
  }

  protected List<EdmAnnotation<?>> getAnnotations(StartElement2 element) {
     // extract Annotation attributes
    try {
      Enumerable<Attribute2> atts = element.getAttributes();
      List<EdmAnnotation<?>> annots = new ArrayList<EdmAnnotation<?>>();
      for (Attribute2 att : atts) {
        QName2 q = att.getName();
        if (isExtensionNamespace(q.getNamespaceUri())) {
          // a user extension
          annots.add(EdmAnnotation.attribute(q.getNamespaceUri(), q.getPrefix(), q.getLocalPart(), att.getValue()));
        }
      }
      return annots;
    } catch (Exception ex) {
      // not all of the xml parsing implementations implement getAttributes() yet.
      return null;
    }
  }

  protected List<PrefixedNamespace> getExtensionNamespaces(StartElement2 startElement) {

    try {
      Enumerable<Namespace2> nse = startElement.getNamespaces();
      List<PrefixedNamespace> nsl = new ArrayList<PrefixedNamespace>();
      for (Namespace2 ns : nse) {
        if (this.isExtensionNamespace(ns.getNamespaceURI())) {
          nsl.add(new PrefixedNamespace(ns.getNamespaceURI(), ns.getPrefix()));
        }
      }
      return nsl;
    } catch (Exception ex) {
      // not all of the xml parsing implementations implement getNamespaces() yet.
      return null;
    }
  }

}
