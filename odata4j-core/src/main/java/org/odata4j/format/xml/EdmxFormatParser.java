package org.odata4j.format.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.core4j.Predicate1;
import org.odata4j.core.Namespace;
import org.odata4j.core.ODataVersion;
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
import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;

public class EdmxFormatParser extends XmlFormatParser {

  public static EdmDataServices parseMetadata(XMLEventReader2 reader) {
    List<EdmSchema> schemas = new ArrayList<EdmSchema>();
    List<Namespace> namespaces = null;

    ODataVersion version = null;
    boolean foundDataServices = false;
    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      boolean shouldReturn = false;

      if (isStartElement(event, XmlFormatParser.EDMX_EDMX)) {
        // should extract the declared namespaces here...
      }

      if (isStartElement(event, EDMX_DATASERVICES)) {
        foundDataServices = true;
        String str = getAttributeValueIfExists(event.asStartElement(), new QName2(NS_METADATA, "DataServiceVersion"));
        version = str != null
            ? ODataVersion.parse(str)
            : null;
      }

      if (isStartElement(event, EDM2006_SCHEMA, EDM2007_SCHEMA, EDM2008_SCHEMA, EDM2009_SCHEMA)) {
        schemas.add(parseEdmSchema(reader, event.asStartElement()));
        if (!foundDataServices) // some dallas services have Schema as the document element!
          shouldReturn = true;
      }

      if (isEndElement(event, EDMX_DATASERVICES))
        shouldReturn = true;

      if (shouldReturn) {
        EdmDataServices rt = new EdmDataServices(version, schemas, namespaces);
        resolve(rt);
        return rt;
      }
    }

    throw new UnsupportedOperationException();
  }

  private static void resolve(EdmDataServices metadata) {

    final Map<String, EdmEntityType> allEetsByFQName = Enumerable.create(metadata.getEntityTypes()).toMap(new Func1<EdmEntityType, String>() {
      public String apply(EdmEntityType input) {
        return input.getFQAliasName() != null ? input.getFQAliasName() : input.getFullyQualifiedTypeName();
      }
    });
    final Map<String, EdmAssociation> allEasByFQName = Enumerable.create(metadata.getAssociations()).toMap(new Func1<EdmAssociation, String>() {
      public String apply(EdmAssociation input) {
        return input.getFQAliasName() != null ? input.getFQAliasName() : input.getFQNamespaceName();
      }
    });

    for (EdmSchema edmSchema : metadata.getSchemas()) {

      // resolve associations
      for (int i = 0; i < edmSchema.getAssociations().size(); i++) {
        EdmAssociation tmpAssociation = edmSchema.getAssociations().get(i);

        List<EdmAssociationEnd> finalEnds = Enumerable.create(tmpAssociation.getEnd1(), tmpAssociation.getEnd2()).select(new Func1<EdmAssociationEnd, EdmAssociationEnd>() {
          public EdmAssociationEnd apply(final EdmAssociationEnd tempEnd) {
            EdmEntityType eet = allEetsByFQName.get(((TempEdmAssociationEnd) tempEnd).typeName);
            return new EdmAssociationEnd(tempEnd.getRole(), eet, tempEnd.getMultiplicity());
          }
        }).toList();
        EdmAssociation ea = new EdmAssociation(tmpAssociation.getNamespace(), tmpAssociation.getAlias(), tmpAssociation.getName(), finalEnds.get(0), finalEnds.get(1));
        edmSchema.getAssociations().set(i, ea);
        allEasByFQName.put(ea.getFQAliasName() != null ? ea.getFQAliasName() : ea.getFQNamespaceName(), ea);

      }

      // resolve navproperties
      for (EdmEntityType eet : edmSchema.getEntityTypes()) {
        List<EdmNavigationProperty> navProps = eet.getDeclaredNavigationProperties().toList();
        for (int i = 0; i < navProps.size(); i++) {
          final TempEdmNavigationProperty tmp = (TempEdmNavigationProperty) navProps.get(i);
          final EdmAssociation ea = allEasByFQName.get(tmp.relationshipName);

          List<EdmAssociationEnd> finalEnds = Enumerable.create(tmp.fromRoleName, tmp.toRoleName).select(new Func1<String, EdmAssociationEnd>() {
            public EdmAssociationEnd apply(String input) {
              if (ea.getEnd1().getRole().equals(input))
                return ea.getEnd1();
              if (ea.getEnd2().getRole().equals(input))
                return ea.getEnd2();
              throw new IllegalArgumentException("Invalid role name " + input);
            }
          }).toList();

          EdmNavigationProperty enp = new EdmNavigationProperty(tmp.getName(), ea, finalEnds.get(0), finalEnds.get(1));
          navProps.set(i, enp);
        }
        eet.setDeclaredNavigationProperties(Enumerable.create(navProps));

      }

      // resolve entitysets
      for (EdmEntityContainer edmEntityContainer : edmSchema.getEntityContainers()) {
        for (int i = 0; i < edmEntityContainer.getEntitySets().size(); i++) {
          final TempEdmEntitySet tmpEes = (TempEdmEntitySet) edmEntityContainer.getEntitySets().get(i);
          EdmEntityType eet = allEetsByFQName.get(tmpEes.entityTypeName);

          if (eet == null)
                        throw new IllegalArgumentException("Invalid entity type " + tmpEes.entityTypeName);
          edmEntityContainer.getEntitySets().set(i, new EdmEntitySet(tmpEes.getName(), eet));
        }
      }

      // resolve associationsets
      for (final EdmEntityContainer edmEntityContainer : edmSchema.getEntityContainers()) {
        for (int i = 0; i < edmEntityContainer.getAssociationSets().size(); i++) {
          final TempEdmAssociationSet tmpEas = (TempEdmAssociationSet) edmEntityContainer.getAssociationSets().get(i);
          final EdmAssociation ea = allEasByFQName.get(tmpEas.associationName);

          List<EdmAssociationSetEnd> finalEnds = Enumerable.create(tmpEas.getEnd1(), tmpEas.getEnd2()).select(new Func1<EdmAssociationSetEnd, EdmAssociationSetEnd>() {
            public EdmAssociationSetEnd apply(EdmAssociationSetEnd input) {
              final TempEdmAssociationSetEnd tmpEase = (TempEdmAssociationSetEnd) input;

              EdmAssociationEnd eae = ea.getEnd1().getRole().equals(tmpEase.roleName) ? ea.getEnd1()
                                    : ea.getEnd2().getRole().equals(tmpEase.roleName) ? ea.getEnd2() : null;

              if (eae == null)
                                throw new IllegalArgumentException("Invalid role name " + tmpEase.roleName);

              EdmEntitySet ees = Enumerable.create(edmEntityContainer.getEntitySets()).first(new Predicate1<EdmEntitySet>() {
                public boolean apply(EdmEntitySet input) {
                  return input.getName().equals(tmpEase.entitySetName);
                }
              });
              return new EdmAssociationSetEnd(eae, ees);
            }
          }).toList();

          edmEntityContainer.getAssociationSets().set(i, new EdmAssociationSet(tmpEas.getName(), ea, finalEnds.get(0), finalEnds.get(1)));
        }
      }

      // resolve functionimports
      for (final EdmEntityContainer edmEntityContainer : edmSchema.getEntityContainers()) {
        for (int i = 0; i < edmEntityContainer.getFunctionImports().size(); i++) {
          final TempEdmFunctionImport tmpEfi = (TempEdmFunctionImport) edmEntityContainer.getFunctionImports().get(i);
          EdmEntitySet ees = Enumerable.create(edmEntityContainer.getEntitySets()).firstOrNull(new Predicate1<EdmEntitySet>() {
            public boolean apply(EdmEntitySet input) {
              return input.getName().equals(tmpEfi.entitySetName);
            }
          });

          EdmType type = null;

          // type resolution:
          // NOTE: this will likely change if RowType is ever implemented. I'm
          //       guessing that in that case, the TempEdmFunctionImport will already
          //       have a EdmRowType instance it built during parsing.
          // first, try to resolve the type name as a simple or complex type
          type = EdmType.get(tmpEfi.returnTypeName);
          if (!type.isSimple()) {
            type = metadata.findEdmEntityType(tmpEfi.returnTypeName);
            if (type == null)
              type = metadata.findEdmComplexType(tmpEfi.returnTypeName);
          }
          if (type == null)
            throw new RuntimeException("Edm-type not found: " + tmpEfi.returnTypeName);

          if (tmpEfi.isCollection) {
            type = new EdmCollectionType(tmpEfi.returnTypeName, type);
          }

          edmEntityContainer.getFunctionImports().set(i,
            new EdmFunctionImport(tmpEfi.getName(), ees, type, tmpEfi.getHttpMethod(), tmpEfi.getParameters()));
        }
      }

      // resolve type hierarchy
      for (Entry<String, EdmEntityType> entry : allEetsByFQName.entrySet()) {
        String baseTypeName = entry.getValue().getFQBaseTypeName();
        if (null != baseTypeName) {
          EdmEntityType baseType = allEetsByFQName.get(baseTypeName);
          if (null == baseType) {
            throw new IllegalArgumentException("Invalid baseType: " + baseTypeName);
          }
          entry.getValue().setBaseType(baseType);
        }
      }

    }

  }

  private static EdmSchema parseEdmSchema(XMLEventReader2 reader, StartElement2 schemaElement) {

    String schemaNamespace = schemaElement.getAttributeByName(new QName2("Namespace")).getValue();
    String schemaAlias = getAttributeValueIfExists(schemaElement, new QName2("Alias"));
    final List<EdmEntityType> edmEntityTypes = new ArrayList<EdmEntityType>();
    List<EdmComplexType> edmComplexTypes = new ArrayList<EdmComplexType>();
    List<EdmAssociation> edmAssociations = new ArrayList<EdmAssociation>();
    List<EdmEntityContainer> edmEntityContainers = new ArrayList<EdmEntityContainer>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_ENTITYTYPE, EDM2007_ENTITYTYPE, EDM2008_ENTITYTYPE, EDM2009_ENTITYTYPE)) {
        EdmEntityType edmEntityType = parseEdmEntityType(reader, schemaNamespace, schemaAlias, event.asStartElement());
        edmEntityTypes.add(edmEntityType);
      }
      if (isStartElement(event, EDM2006_ASSOCIATION, EDM2007_ASSOCIATION, EDM2008_ASSOCIATION, EDM2009_ASSOCIATION)) {
        EdmAssociation edmAssociation = parseEdmAssociation(reader, schemaNamespace, schemaAlias, event.asStartElement());
        edmAssociations.add(edmAssociation);
      }
      if (isStartElement(event, EDM2006_COMPLEXTYPE, EDM2007_COMPLEXTYPE, EDM2008_COMPLEXTYPE, EDM2009_COMPLEXTYPE)) {
        EdmComplexType edmComplexType = parseEdmComplexType(reader, schemaNamespace, event.asStartElement());
        edmComplexTypes.add(edmComplexType);
      }
      if (isStartElement(event, EDM2006_ENTITYCONTAINER, EDM2007_ENTITYCONTAINER, EDM2008_ENTITYCONTAINER, EDM2009_ENTITYCONTAINER)) {
        EdmEntityContainer edmEntityContainer = parseEdmEntityContainer(reader, schemaNamespace, event.asStartElement());
        edmEntityContainers.add(edmEntityContainer);
      }
      if (isEndElement(event, schemaElement.getName())) {
        return new EdmSchema(schemaNamespace, schemaAlias, edmEntityTypes, edmComplexTypes, edmAssociations, edmEntityContainers);
      }
    }

    throw new UnsupportedOperationException();

  }

  private static EdmEntityContainer parseEdmEntityContainer(XMLEventReader2 reader, String schemaNamespace, StartElement2 entityContainerElement) {
    String name = entityContainerElement.getAttributeByName("Name").getValue();
    boolean isDefault = "true".equals(getAttributeValueIfExists(entityContainerElement, new QName2(NS_METADATA, "IsDefaultEntityContainer")));
    String lazyLoadingEnabledValue = getAttributeValueIfExists(entityContainerElement, new QName2(NS_EDMANNOTATION, "LazyLoadingEnabled"));
    Boolean lazyLoadingEnabled = lazyLoadingEnabledValue == null ? null : lazyLoadingEnabledValue.equals("true");

    List<EdmEntitySet> edmEntitySets = new ArrayList<EdmEntitySet>();
    List<EdmAssociationSet> edmAssociationSets = new ArrayList<EdmAssociationSet>();
    List<EdmFunctionImport> edmFunctionImports = new ArrayList<EdmFunctionImport>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_ENTITYSET, EDM2007_ENTITYSET, EDM2008_ENTITYSET, EDM2009_ENTITYSET))
                edmEntitySets.add(new TempEdmEntitySet(getAttributeValueIfExists(event.asStartElement(), "Name"), getAttributeValueIfExists(event.asStartElement(), "EntityType")));

      if (isStartElement(event, EDM2006_ASSOCIATIONSET, EDM2007_ASSOCIATIONSET, EDM2008_ASSOCIATIONSET, EDM2009_ASSOCIATIONSET))
                edmAssociationSets.add(parseEdmAssociationSet(reader, schemaNamespace, event.asStartElement()));

      if (isStartElement(event, EDM2006_FUNCTIONIMPORT, EDM2007_FUNCTIONIMPORT, EDM2008_FUNCTIONIMPORT, EDM2009_FUNCTIONIMPORT))
                edmFunctionImports.add(parseEdmFunctionImport(reader, schemaNamespace, event.asStartElement()));

      if (isEndElement(event, entityContainerElement.getName())) {
        return new EdmEntityContainer(name, isDefault, lazyLoadingEnabled, edmEntitySets, edmAssociationSets, edmFunctionImports);
      }
    }
    throw new UnsupportedOperationException();

  }

  private static EdmFunctionImport parseEdmFunctionImport(XMLEventReader2 reader, String schemaNamespace, StartElement2 functionImportElement) {
    String name = functionImportElement.getAttributeByName("Name").getValue();
    String entitySet = getAttributeValueIfExists(functionImportElement, "EntitySet");
    Attribute2 returnTypeAttr = functionImportElement.getAttributeByName("ReturnType");
    String returnType = returnTypeAttr != null ? returnTypeAttr.getValue() : null;

    // strict parsing
    boolean isCollection = null != returnType && returnType.matches("^Collection\\(.*\\)$");
    if (isCollection) {
        returnType = returnType.substring(11, returnType.length() - 1);
    }
    String httpMethod = getAttributeValueIfExists(functionImportElement, new QName2(NS_METADATA, "HttpMethod"));

    List<EdmFunctionParameter> parameters = new ArrayList<EdmFunctionParameter>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_PARAMETER, EDM2007_PARAMETER, EDM2008_PARAMETER, EDM2009_PARAMETER)) {
        // Mode attribute is optional and thus can be null
        Attribute2 modeAttribute = event.asStartElement().getAttributeByName("Mode");
        parameters.add(new EdmFunctionParameter(
            event.asStartElement().getAttributeByName("Name").getValue(),
            EdmType.get(event.asStartElement().getAttributeByName("Type").getValue()),
            modeAttribute != null ? Mode.valueOf(modeAttribute.getValue()) : null));
      }

      if (isEndElement(event, functionImportElement.getName())) {
        return new TempEdmFunctionImport(name, entitySet, returnType, isCollection, httpMethod, parameters);
      }
    }
    throw new UnsupportedOperationException();

  }

  private static EdmAssociationSet parseEdmAssociationSet(XMLEventReader2 reader, String schemaNamespace, StartElement2 associationSetElement) {
    String name = associationSetElement.getAttributeByName("Name").getValue();
    String associationName = associationSetElement.getAttributeByName("Association").getValue();

    List<EdmAssociationSetEnd> ends = new ArrayList<EdmAssociationSetEnd>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_END, EDM2007_END, EDM2008_END, EDM2009_END))
                ends.add(new TempEdmAssociationSetEnd(event.asStartElement().getAttributeByName("Role").getValue(), event.asStartElement().getAttributeByName("EntitySet").getValue()));

      if (isEndElement(event, associationSetElement.getName())) {
        return new TempEdmAssociationSet(name, associationName, ends.get(0), ends.get(1));
      }
    }
    throw new UnsupportedOperationException();

  }

  private static EdmAssociation parseEdmAssociation(XMLEventReader2 reader, String schemaNamespace, String schemaAlias, StartElement2 associationElement) {
    String name = associationElement.getAttributeByName("Name").getValue();

    List<EdmAssociationEnd> ends = new ArrayList<EdmAssociationEnd>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_END, EDM2007_END, EDM2008_END, EDM2009_END))
                ends.add(new TempEdmAssociationEnd(event.asStartElement().getAttributeByName("Role").getValue(), event.asStartElement().getAttributeByName("Type").getValue(), EdmMultiplicity.fromSymbolString(event.asStartElement().getAttributeByName("Multiplicity").getValue())));

      if (isEndElement(event, associationElement.getName())) {
        return new EdmAssociation(schemaNamespace, schemaAlias, name, ends.get(0), ends.get(1));
      }
    }
    throw new UnsupportedOperationException();

  }

  private static EdmProperty.Builder parseEdmProperty(XMLEvent2 event) {
    String propertyName = getAttributeValueIfExists(event.asStartElement(), "Name");
    String propertyType = getAttributeValueIfExists(event.asStartElement(), "Type");
    String propertyNullable = getAttributeValueIfExists(event.asStartElement(), "Nullable");
    String maxLength = getAttributeValueIfExists(event.asStartElement(), "MaxLength");
    String unicode = getAttributeValueIfExists(event.asStartElement(), "Unicode");
    String fixedLength = getAttributeValueIfExists(event.asStartElement(), "FixedLength");
    String collectionKindS = getAttributeValueIfExists(event.asStartElement(), "CollectionKind");
    CollectionKind ckind = CollectionKind.NONE;
    if (null != collectionKindS) {
      ckind = Enum.valueOf(CollectionKind.class, collectionKindS);
    }
    String defaultValue = getAttributeValueIfExists(event.asStartElement(), "DefaultValue");
    String precision = getAttributeValueIfExists(event.asStartElement(), "Precision");
    String scale = getAttributeValueIfExists(event.asStartElement(), "Scale");

    String storeGeneratedPattern = getAttributeValueIfExists(event.asStartElement(), new QName2(NS_EDMANNOTATION, "StoreGeneratedPattern"));

    String fcTargetPath = getAttributeValueIfExists(event.asStartElement(), M_FC_TARGETPATH);
    String fcContentKind = getAttributeValueIfExists(event.asStartElement(), M_FC_CONTENTKIND);
    String fcKeepInContent = getAttributeValueIfExists(event.asStartElement(), M_FC_KEEPINCONTENT);
    String fcEpmContentKind = getAttributeValueIfExists(event.asStartElement(), M_FC_EPMCONTENTKIND);
    String fcEpmKeepInContent = getAttributeValueIfExists(event.asStartElement(), M_FC_EPMKEEPINCONTENT);

    return EdmProperty.newBuilder(propertyName)
        .setType(EdmType.get(propertyType))
        .setNullable("false".equals(propertyNullable))
        .setMaxLength(maxLength == null ? null : maxLength.equals("Max") ? Integer.MAX_VALUE : Integer.parseInt(maxLength))
        .setUnicode("false".equals(unicode))
        .setFixedLength("false".equals(fixedLength))
        .setStoreGeneratedPattern(storeGeneratedPattern)
        .setFcTargetPath(fcTargetPath)
        .setFcContentKind(fcContentKind)
        .setFcKeepInContent(fcKeepInContent)
        .setFcEpmContentKind(fcEpmContentKind)
        .setFcEpmKeepInContent(fcEpmKeepInContent)
        .setCollectionKind(ckind)
        .setDefaultValue(defaultValue)
        .setPrecision(precision == null ? null : Integer.parseInt(precision))
        .setScale(scale == null ? null : Integer.parseInt(scale));
  }

  private static EdmComplexType parseEdmComplexType(XMLEventReader2 reader, String schemaNamespace, StartElement2 complexTypeElement) {
    String name = complexTypeElement.getAttributeByName("Name").getValue();
    String isAbstractS = getAttributeValueIfExists(complexTypeElement, "Abstract");
    List<EdmProperty.Builder> edmProperties = new ArrayList<EdmProperty.Builder>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_PROPERTY, EDM2007_PROPERTY, EDM2008_PROPERTY, EDM2009_PROPERTY)) {
        edmProperties.add(parseEdmProperty(event));
      }

      if (isEndElement(event, complexTypeElement.getName())) {
        return new EdmComplexType(schemaNamespace, name, edmProperties,
                null, // doc
                null, // annotations
                null == isAbstractS ? null : "true".equals(isAbstractS));
      }
    }

    throw new UnsupportedOperationException();

  }

  private static EdmEntityType parseEdmEntityType(XMLEventReader2 reader, String schemaNamespace, String schemaAlias, StartElement2 entityTypeElement) {
    String name = entityTypeElement.getAttributeByName("Name").getValue();
    String hasStreamValue = getAttributeValueIfExists(entityTypeElement, new QName2(NS_METADATA, "HasStream"));
    Boolean hasStream = hasStreamValue == null ? null : hasStreamValue.equals("true");
    String baseType = getAttributeValueIfExists(entityTypeElement, "BaseType");
    String isAbstractS = getAttributeValueIfExists(entityTypeElement, "Abstract");

    List<String> keys = new ArrayList<String>();
    List<EdmProperty.Builder> edmProperties = new ArrayList<EdmProperty.Builder>();
    List<EdmNavigationProperty> edmNavigationProperties = new ArrayList<EdmNavigationProperty>();

    while (reader.hasNext()) {
      XMLEvent2 event = reader.nextEvent();

      if (isStartElement(event, EDM2006_PROPERTYREF, EDM2007_PROPERTYREF, EDM2008_PROPERTYREF, EDM2009_PROPERTYREF))
                keys.add(event.asStartElement().getAttributeByName("Name").getValue());

      if (isStartElement(event, EDM2006_PROPERTY, EDM2007_PROPERTY, EDM2008_PROPERTY, EDM2009_PROPERTY)) {
        edmProperties.add(parseEdmProperty(event));
      }

      if (isStartElement(event, EDM2006_NAVIGATIONPROPERTY, EDM2007_NAVIGATIONPROPERTY, EDM2008_NAVIGATIONPROPERTY, EDM2009_NAVIGATIONPROPERTY)) {
        String associationName = event.asStartElement().getAttributeByName("Name").getValue();
        String relationshipName = event.asStartElement().getAttributeByName("Relationship").getValue();
        String fromRoleName = event.asStartElement().getAttributeByName("FromRole").getValue();
        String toRoleName = event.asStartElement().getAttributeByName("ToRole").getValue();

        edmNavigationProperties.add(new TempEdmNavigationProperty(associationName, relationshipName, fromRoleName, toRoleName));

      }

      if (isEndElement(event, entityTypeElement.getName())) {
        return new EdmEntityType(
                schemaNamespace,
                schemaAlias,
                name,
                hasStream,
                keys,
                edmProperties,
                edmNavigationProperties,
                baseType,
                null, // documentation, TODO
                null, // annotations, TODO
                null == isAbstractS ? null : "true".equals(isAbstractS));
      }
    }

    throw new UnsupportedOperationException();
  }

  private static class TempEdmFunctionImport extends EdmFunctionImport {
    public final String entitySetName;
    public final String returnTypeName;
    public final boolean isCollection;

    public TempEdmFunctionImport(String name, String entitySetName, String returnTypeName,
        boolean isCollection, String httpMethod, List<EdmFunctionParameter> parameters) {
      super(name, null, null, httpMethod, parameters);
      this.entitySetName = entitySetName;
      this.returnTypeName = returnTypeName;
      this.isCollection = isCollection;
    }
  }

  private static class TempEdmAssociationSet extends EdmAssociationSet {
    public final String associationName;

    public TempEdmAssociationSet(String name, String associationName, EdmAssociationSetEnd end1, EdmAssociationSetEnd end2) {
      super(name, null, end1, end2);
      this.associationName = associationName;
    }
  }

  private static class TempEdmAssociationSetEnd extends EdmAssociationSetEnd {
    public final String roleName;
    public final String entitySetName;

    public TempEdmAssociationSetEnd(String roleName, String entitySetName) {
      super(null, null);
      this.roleName = roleName;
      this.entitySetName = entitySetName;
    }
  }

  private static class TempEdmEntitySet extends EdmEntitySet {
    public final String entityTypeName;

    public TempEdmEntitySet(String name, String entityTypeName) {
      super(name, null);
      this.entityTypeName = entityTypeName;
    }
  }

  private static class TempEdmAssociationEnd extends EdmAssociationEnd {
    public final String typeName;

    public TempEdmAssociationEnd(String role, String typeName, EdmMultiplicity multiplicity) {
      super(role, null, multiplicity);
      this.typeName = typeName;
    }
  }

  private static class TempEdmNavigationProperty extends EdmNavigationProperty {

    public final String relationshipName;
    public final String fromRoleName;
    public final String toRoleName;

    public TempEdmNavigationProperty(String name, String relationshipName, String fromRoleName, String toRoleName) {
      super(name, null, null, null);
      this.relationshipName = relationshipName;
      this.fromRoleName = fromRoleName;
      this.toRoleName = toRoleName;
    }
  }

}
