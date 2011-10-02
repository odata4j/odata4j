package org.odata4j.producer.edm;

import java.util.ArrayList;
import java.util.Collections;
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
import org.odata4j.format.xml.XmlFormatParser;

/**
 * Creates an EdmDataServices instance that models the EDM in terms of the EDM,
 * call it a meta-EDM.
 *
 * This model then serves as the basis for queryable metadata.  This generator
 * can be parameterized with an optional {@link EdmDecorator}.  The decorator implements
 * aspects of the meta EDM that are application specific such as Documentation
 * and Annotations.
 *
 * The current implementation is not 100% complete.
 *
 * EntityTypes implemented include: Schema, Property, ComplexType and EntityType
 *                                  (note that NavigationProperty is *no* implemented yet)
 * EntitySets implemented include: Schemas, Properties, EntityTypes, RootEntityTypes,
 *                                 ComplexTypes and RootComplexTypes
 *
 * Documentation elements are supported
 * AnnotationAttributes are supported
 * AnnotationElements are partially supported (JSON only)
 */
public class MetadataEdmGenerator implements EdmGenerator {

  /**
   * construct
   * @param decorator - optional
   */
  public MetadataEdmGenerator(EdmDecorator decorator) {
    this.decorator = decorator;
  }

  /**
   * Get the generators decorator
   * @return the decorator
   */
  @Override
  public EdmDecorator getDecorator() {
    return decorator;
  }

  /**
   * Generate the meta EDM data serivces
   * @return - the model.
   */
  public EdmDataServices generateEdm() {

    createComplexTypes();
    createEntityTypes();

    container = new EdmEntityContainer(
            Edm.ContainerName, // name
            true, // boolean isDefault,
            Boolean.TRUE, // Boolean lazyLoadingEnabled,
            esets, // List<EdmEntitySet> entitySets,
            asets, // List<EdmAssociationSet> associationSets, a
            null);          // List<EdmFunctionImport> functionImports) {

    schema = new EdmSchema(
            Edm.namespace,
            null, // String alias,
            etypes,
            ctypes,
            assocs,
            Enumerable.create(container).toList());


    EdmDataServices services = new EdmDataServices(ODataConstants.DATA_SERVICE_VERSION,
            Enumerable.create(schema).toList());
    return services;
  }

  private void createComplexTypes() {
    // ----------------------------- PropertyRef --------------------------
    List<EdmProperty> props = new ArrayList<EdmProperty>();

    EdmProperty ep = new EdmProperty(
            Edm.PropertyRef.Name,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    EdmComplexType propertyRef = new EdmComplexType(Edm.namespace,
            XmlFormatParser.EDM2008_PROPERTYREF.getLocalPart(), props);
    ctypes.add(propertyRef);

    // ----------------------------- EntityKey --------------------------
    props = new ArrayList<EdmProperty>();

    ep = new EdmProperty(
            Edm.EntityKey.Keys,
            //new EdmCollectionType(getCollectionOfType(propertyRef), propertyRef), // EdmType type,
            propertyRef,
            false, // boolean nullable,
            CollectionKind.List,
            null, // documentation
            null); // annotations
    props.add(ep);

    entityKeyType = new EdmComplexType(Edm.namespace, Edm.EntityKey.name(), props);
    ctypes.add(entityKeyType);

    // ----------------------------- Documentation --------------------------

    props = new ArrayList<EdmProperty>();

    ep = new EdmProperty(
            Edm.Documentation.Summary,
            EdmSimpleType.STRING,
            true, // boolean nullable,
            CollectionKind.None,
            null, // documentation
            null); // annotations
    props.add(ep);

    ep = new EdmProperty(
            Edm.Documentation.LongDescription,
            EdmSimpleType.STRING,
            true, // boolean nullable,
            CollectionKind.None,
            null, // documentation
            null); // annotations
    props.add(ep);

    documentationType = new EdmComplexType(Edm.namespace, Edm.Documentation.name(), props);
    ctypes.add(documentationType);

  }

  private void createEntityTypes() {
    // --------------------------- Schema ------------------------------
    List<EdmProperty> props = new ArrayList<EdmProperty>();
    List<EdmNavigationProperty> navprops = new ArrayList<EdmNavigationProperty>();

    EdmProperty ep = null;

    ep = new EdmProperty(
            Edm.Schema.Namespace,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Schema.Alias,
            EdmSimpleType.STRING, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    List<String> keys = new ArrayList<String>();
    keys.add(Edm.Schema.Namespace);
    EdmEntityType schemaType = new EdmEntityType(
            Edm.namespace,
            null, // alias
            Edm.Schema.name(),
            null, // hasStream
            keys,
            null, // baseType,
            props,
            navprops,
            null, // null == decorator ? null : decorator.getDocumentationForEntityType(Edm.namespace, Edm.EntityType.name()),
            null == decorator ? null : decorator.getAnnotationsForSchema(Edm.namespace, Edm.Schema.name()));

    etypes.add(schemaType);

    EdmEntitySet schemaSet = new EdmEntitySet(Edm.EntitySets.Schemas, schemaType);
    esets.add(schemaSet);

    // --------------------------- StructuralType ------------------------------
    props = new ArrayList<EdmProperty>();
    navprops = new ArrayList<EdmNavigationProperty>();

    ep = new EdmProperty(
            Edm.StructuralType.Namespace,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.StructuralType.Name,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.StructuralType.BaseType,
            EdmSimpleType.STRING, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.StructuralType.Abstract,
            EdmSimpleType.BOOLEAN, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    keys = new ArrayList<String>();
    keys.add(Edm.StructuralType.Namespace);
    keys.add(Edm.StructuralType.Name);
    EdmEntityType structuralType = new EdmEntityType(
            Edm.namespace,
            null, // alias
            Edm.StructuralType.name(),
            null, // hasStream
            keys,
            null, // baseType,
            props,
            navprops,
            null, // null == decorator ? null : decorator.getDocumentationForEntityType(Edm.namespace, Edm.EntityType.name()),
            null); // null == decorator ? null : decorator.getAnnotationsForEntityType(Edm.namespace, Edm.EntityType.name()));

    etypes.add(structuralType);

    // maybe?
    //EdmEntitySet structuralSet = new EdmEntitySet(Edm.EntitySets.EdmStructuralTypes, structuralType);
    //esets.add(structuralSet);

    // --------------------------- ComplexType ------------------------------

    props = Collections.<EdmProperty>emptyList();
    navprops = new ArrayList<EdmNavigationProperty>();

    EdmEntityType complexType = new EdmEntityType(
            Edm.namespace,
            null, // alias
            Edm.ComplexType.name(),
            null, // hasStream
            null, // keys, defined on basetype already
            structuralType, // baseType,
            props,
            navprops,
            null, //  TODO: null == decorator ? null : decorator.getDocumentationForComplexType(Edm.namespace, Edm.ComplexType.name()),
            null); // TODO: null == decorator ? null : decorator.getAnnotationsForComplexType(Edm.namespace, Edm.ComplexType.name()));

    etypes.add(complexType);

    EdmEntitySet complexSet = new EdmEntitySet(Edm.EntitySets.ComplexTypes, complexType);
    esets.add(complexSet);

    EdmEntitySet rootComplexTypesSet = new EdmEntitySet(Edm.EntitySets.RootComplexTypes, complexType);
    esets.add(rootComplexTypesSet);


    // ---------------------------- Entity Type ----------------------------
    // adds the notion of Key
    // key is nullable because only base types specifiy the key

    props = new ArrayList<EdmProperty>();
    navprops = new ArrayList<EdmNavigationProperty>();

    ep = new EdmProperty(
            Edm.EntityType.Key,
            this.entityKeyType, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    EdmEntityType entityType = new EdmEntityType(
            Edm.namespace,
            null, // alias
            Edm.EntityType.name(),
            null, // hasStream
            null, // keys, defined on basetype already
            structuralType, // baseType,
            props,
            navprops,
            null, // null == decorator ? null : decorator.getDocumentationForEntityType(Edm.namespace, Edm.EntityType.name()),
            null); // null == decorator ? null : decorator.getAnnotationsForEntityType(Edm.namespace, Edm.EntityType.name()));

    etypes.add(entityType);

    EdmEntitySet entitySet = new EdmEntitySet(Edm.EntitySets.EntityTypes, entityType);
    esets.add(entitySet);

    EdmEntitySet rootEntitiesSet = new EdmEntitySet(Edm.EntitySets.RootEntityTypes, entityType);
    esets.add(rootEntitiesSet);


    // --------------------------- Property ------------------------------
    // model Property as an Entity so we can use the $expand mechanism to get
    // a lightweight view of the hierarchy or one that has all of the properties.
    props = new ArrayList<EdmProperty>();
    navprops = new ArrayList<EdmNavigationProperty>();

    ep = new EdmProperty(
            Edm.Property.Namespace,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.EntityTypeName,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.Name,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.Type,
            EdmSimpleType.STRING, // EdmType type,
            false); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.Nullable,
            EdmSimpleType.BOOLEAN, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.DefaultValue,
            EdmSimpleType.STRING, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.MaxLength,
            EdmSimpleType.INT32, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.FixedLength,
            EdmSimpleType.BOOLEAN, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.Precision,
            EdmSimpleType.INT16, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.Scale,
            EdmSimpleType.INT16, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.Unicode,
            EdmSimpleType.BOOLEAN, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.Collation,
            EdmSimpleType.STRING, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    ep = new EdmProperty(
            Edm.Property.ConcurrencyMode,
            EdmSimpleType.STRING, // EdmType type,
            true); // boolean nullable,
    props.add(ep);

    keys = new ArrayList<String>();
    keys.add(Edm.Property.Namespace);  // comes from the EntityType
    keys.add(Edm.Property.EntityTypeName);
    keys.add(Edm.Property.Name);
    EdmEntityType propertyType = new EdmEntityType(
            Edm.namespace,
            null, // alias
            Edm.Property.name(),
            null, // hasStream
            keys,
            null, // baseType,
            props,
            navprops,
            null == decorator ? null : decorator.getDocumentationForEntityType(Edm.namespace, Edm.Property.name()),
            null == decorator ? null : decorator.getAnnotationsForEntityType(Edm.namespace, Edm.Property.name()));

    etypes.add(propertyType);

    EdmEntitySet propertySet = new EdmEntitySet(Edm.EntitySets.Properties, propertyType);
    esets.add(propertySet);

    // Navigation Property
    // Schema ------------0..* (EntityTypes)----EntityType
    EdmAssociation assoc = defineAssociation(
            Edm.Schema.NavProps.EntityTypes,
            EdmMultiplicity.ONE,
            EdmMultiplicity.MANY,
            schemaType,
            schemaSet,
            structuralType,
            entitySet);

    EdmNavigationProperty navigationProperty = new EdmNavigationProperty(
            assoc.name,
            assoc,
            assoc.end1,
            assoc.end2);

    schemaType.addNavigationProperty(navigationProperty);

    // Schema ------------0..* (ComplexTypes)----ComplexTypes
    assoc = defineAssociation(
            Edm.Schema.NavProps.ComplexTypes,
            EdmMultiplicity.ONE,
            EdmMultiplicity.MANY,
            schemaType,
            schemaSet,
            complexType,
            complexSet);

    navigationProperty = new EdmNavigationProperty(
            assoc.name,
            assoc,
            assoc.end1,
            assoc.end2);

    schemaType.addNavigationProperty(navigationProperty);

    // EntityType ------------0..* (Properties)----Property
    assoc = defineAssociation(
            Edm.EntityType.NavProps.Properties,
            EdmMultiplicity.ONE,
            EdmMultiplicity.MANY,
            structuralType,
            entitySet,
            propertyType,
            propertySet);

    navigationProperty = new EdmNavigationProperty(
            assoc.name,
            assoc,
            assoc.end1,
            assoc.end2);

    structuralType.addNavigationProperty(navigationProperty);

    // Navigation Property
    // EntityType ------------0..* (SubTypes)----EntityType
    assoc = defineAssociation(
            Edm.EntityType.NavProps.SubTypes,
            EdmMultiplicity.ONE,
            EdmMultiplicity.MANY,
            structuralType,
            entitySet,
            structuralType,
            entitySet);

    navigationProperty = new EdmNavigationProperty(
            assoc.name,
            assoc,
            assoc.end1,
            assoc.end2);

    structuralType.addNavigationProperty(navigationProperty);

    // Navigation Property
    // EntityType ------------0..1 (SuperType)----EntityType
    assoc = defineAssociation(
            Edm.EntityType.NavProps.SuperType,
            EdmMultiplicity.ONE,
            EdmMultiplicity.ZERO_TO_ONE,
            structuralType,
            entitySet,
            structuralType,
            entitySet);

    navigationProperty = new EdmNavigationProperty(
            assoc.name,
            assoc,
            assoc.end1,
            assoc.end2);

    structuralType.addNavigationProperty(navigationProperty);
  }

  private EdmAssociation defineAssociation(
          String assocName,
          EdmMultiplicity fromMult,
          EdmMultiplicity toMult,
          EdmEntityType fromEntityType,
          EdmEntitySet fromEntitySet,
          EdmEntityType toEntityType,
          EdmEntitySet toEntitySet) {

    // add EdmAssociation
    EdmAssociationEnd fromAssociationEnd = new EdmAssociationEnd(fromEntityType.name, fromEntityType, fromMult);
    String toAssociationRole = toEntityType.name;
    if (toAssociationRole.equals(fromEntityType.name)) {
      toAssociationRole = toAssociationRole + "1";
    }
    EdmAssociationEnd toAssociationEnd = new EdmAssociationEnd(toAssociationRole, toEntityType, toMult);
    EdmAssociation association = new EdmAssociation(Edm.namespace, null, assocName, fromAssociationEnd, toAssociationEnd);

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
  private EdmComplexType entityKeyType = null;
  private EdmComplexType documentationType = null;
  private List<EdmAssociation> assocs = new LinkedList<EdmAssociation>();
  private List<EdmComplexType> ctypes = new LinkedList<EdmComplexType>();
  private List<EdmEntityType> etypes = new LinkedList<EdmEntityType>();
  private List<EdmEntitySet> esets = new LinkedList<EdmEntitySet>();
  private List<EdmAssociationSet> asets = new LinkedList<EdmAssociationSet>();
}
