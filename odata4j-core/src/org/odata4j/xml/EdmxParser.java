package org.odata4j.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.odata4j.edm.*;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;
import org.odata4j.stax2.XMLEventReader2;

import core4j.Enumerable;
import core4j.Func1;
import core4j.Predicate1;

public class EdmxParser extends BaseParser {

    public static EdmDataServices parseMetadata(XMLEventReader2 reader) {
        List<EdmSchema> schemas = new ArrayList<EdmSchema>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();
            
            String version = null;
            
            if (isStartElement(event, EDMX_DATASERVICES))
                version = event.asStartElement().getAttributeByName(new QName2(NS_METADATA,"DataServiceVersion")).getValue();
            if (isStartElement(event, EDM2007_SCHEMA, EDM2008_SCHEMA)) 
                schemas.add(parseEdmSchema(reader, event.asStartElement()));
            
            if (isEndElement(event, EDMX_DATASERVICES))  {

                EdmDataServices rt =  new EdmDataServices(version, schemas);
                resolve(rt);
                return rt;
            }
            

        }

        throw new UnsupportedOperationException();
    }
    
    private static void resolve(EdmDataServices metadata){
        
        final Map<String,EdmEntityType> allEetsByFQName = Enumerable.create(metadata.getEntityTypes()).toMap(new Func1<EdmEntityType,String>(){  
            public String apply(EdmEntityType input) {
               return input.getFQName();
            }});
        final Map<String,EdmAssociation> allEasByFQName = Enumerable.create(metadata.getAssociations()).toMap(new Func1<EdmAssociation,String>(){  
            public String apply(EdmAssociation input) {
               return input.getFQName();
            }});
        
        for(EdmSchema edmSchema : metadata.schemas){
            
            // resolve associations
            for(int i = 0; i < edmSchema.associations.size(); i++) {
                EdmAssociation tmpAssociation = edmSchema.associations.get(i);
                
                List<EdmAssociationEnd> finalEnds = Enumerable.create(tmpAssociation.end1, tmpAssociation.end2).select(new Func1<EdmAssociationEnd, EdmAssociationEnd>() {
                    public EdmAssociationEnd apply(final EdmAssociationEnd tempEnd) {
                        EdmEntityType eet = allEetsByFQName.get(((TempEdmAssociationEnd) tempEnd).typeName);
                        return new EdmAssociationEnd(tempEnd.role, eet, tempEnd.multiplicity);
                    }
                }).toList();

                edmSchema.associations.set(i,  new EdmAssociation(tmpAssociation.namespace, tmpAssociation.name, finalEnds.get(0), finalEnds.get(1)));
            }
            
            
            
            // resolve navproperties
            for(EdmEntityType eet : edmSchema.entityTypes) {
                for(int i = 0; i < eet.navigationProperties.size(); i++) {
                    final TempEdmNavigationProperty tmp = (TempEdmNavigationProperty) eet.navigationProperties.get(i);
                    final EdmAssociation ea = allEasByFQName.get(tmp.relationshipName);
                        
                      
                    List<EdmAssociationEnd> finalEnds = Enumerable.create(tmp.fromRoleName, tmp.toRoleName).select(new Func1<String, EdmAssociationEnd>() {
                        public EdmAssociationEnd apply(String input) {
                            if (ea.end1.role.equals(input))
                                return ea.end1;
                            if (ea.end2.role.equals(input))
                                return ea.end2;
                            throw new IllegalArgumentException("Invalid role name " + input);
                        }
                    }).toList();

                    EdmNavigationProperty enp = new EdmNavigationProperty(tmp.name, ea, finalEnds.get(0), finalEnds.get(1));
                    eet.navigationProperties.set(i, enp);
                }
            }
            

            // resolve entitysets
            for(EdmEntityContainer edmEntityContainer : edmSchema.entityContainers) {
                for(int i = 0; i < edmEntityContainer.entitySets.size(); i++) {
                    final TempEdmEntitySet tmpEes = (TempEdmEntitySet) edmEntityContainer.entitySets.get(i);
                    EdmEntityType eet = allEetsByFQName.get(tmpEes.entityTypeName);
                   
                    if (eet==null)
                        throw new IllegalArgumentException("Invalid entity type " + tmpEes.entityTypeName);
                    edmEntityContainer.entitySets.set(i, new EdmEntitySet(tmpEes.name, eet));
                }
            }
            
            // resolve associationsets
            for(final EdmEntityContainer edmEntityContainer : edmSchema.entityContainers) {
                for(int i = 0; i < edmEntityContainer.associationSets.size(); i++) {
                    final TempEdmAssociationSet tmpEas = (TempEdmAssociationSet) edmEntityContainer.associationSets.get(i);
                    final EdmAssociation ea = allEasByFQName.get(tmpEas.associationName);
                  
                    
                    List<EdmAssociationSetEnd> finalEnds = Enumerable.create(tmpEas.end1, tmpEas.end2).select(new Func1<EdmAssociationSetEnd, EdmAssociationSetEnd>() {
                        public EdmAssociationSetEnd apply(EdmAssociationSetEnd input) {
                            final TempEdmAssociationSetEnd tmpEase = (TempEdmAssociationSetEnd)input;
                            
                            EdmAssociationEnd eae = ea.end1.role.equals(tmpEase.roleName)?ea.end1
                                    :ea.end2.role.equals(tmpEase.roleName)?ea.end2:null;
                          
                            if (eae==null)
                                throw new IllegalArgumentException("Invalid role name " + tmpEase.roleName);
                            
                            EdmEntitySet ees = Enumerable.create(edmEntityContainer.entitySets).first(new Predicate1<EdmEntitySet>(){
                                public boolean apply(EdmEntitySet input) {
                                    return input.name.equals(tmpEase.entitySetName);
                                }});
                            return new EdmAssociationSetEnd(eae,ees);
                        }
                    }).toList();
                    
                    edmEntityContainer.associationSets.set(i, new EdmAssociationSet(tmpEas.name,ea,finalEnds.get(0),finalEnds.get(1)));
                }
            }
            
         // resolve functionimports
            for(final EdmEntityContainer edmEntityContainer : edmSchema.entityContainers) {
                for(int i = 0; i < edmEntityContainer.functionImports.size(); i++) {
                    final TempEdmFunctionImport tmpEfi = (TempEdmFunctionImport) edmEntityContainer.functionImports.get(i);
                    EdmEntitySet ees = Enumerable.create(edmEntityContainer.entitySets).first(new Predicate1<EdmEntitySet>(){
                        public boolean apply(EdmEntitySet input) {
                            return input.name.equals(tmpEfi.entitySetName);
                        }});
                    
                    EdmEntityType eet = Enumerable.create(allEetsByFQName.values()).first(new Predicate1<EdmEntityType>() {
                        public boolean apply(EdmEntityType input) {
                            return input.getFQName().equals(tmpEfi.returnTypeName) || 
                            ("Collection("+input.getFQName()+")").equals(tmpEfi.returnTypeName);
                        }
                    });
                    
                    edmEntityContainer.functionImports.set(i, new EdmFunctionImport(tmpEfi.name,ees,eet,tmpEfi.httpMethod,tmpEfi.parameters));
                }
            }
            
            
        }
        
      
      

       

        
        
        

      
        
        
    }

    private static EdmSchema parseEdmSchema(XMLEventReader2 reader, StartElement2 schemaElement) {

        String schemaNamespace = schemaElement.getAttributeByName(new QName2("Namespace")).getValue();
        final List<EdmEntityType> edmEntityTypes = new ArrayList<EdmEntityType>();
        List<EdmComplexType> edmComplexTypes = new ArrayList<EdmComplexType>();
        List<EdmAssociation> edmAssociations = new ArrayList<EdmAssociation>();
        List<EdmEntityContainer> edmEntityContainers = new ArrayList<EdmEntityContainer>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (isStartElement(event, EDM2007_ENTITYTYPE, EDM2008_ENTITYTYPE)) {
                EdmEntityType edmEntityType = parseEdmEntityType(reader, schemaNamespace, event.asStartElement());
                edmEntityTypes.add(edmEntityType);
            }
            if (isStartElement(event, EDM2007_ASSOCIATION, EDM2008_ASSOCIATION)) {
                EdmAssociation edmAssociation = parseEdmAssociation(reader, schemaNamespace, event.asStartElement());
                edmAssociations.add(edmAssociation);
            }
            if (isStartElement(event, EDM2007_COMPLEXTYPE, EDM2008_COMPLEXTYPE)) {
                EdmComplexType edmComplexType = parseEdmComplexType(reader, schemaNamespace, event.asStartElement());
                edmComplexTypes.add(edmComplexType);
            }
            if (isStartElement(event, EDM2007_ENTITYCONTAINER, EDM2008_ENTITYCONTAINER)) {
                EdmEntityContainer edmEntityContainer = parseEdmEntityContainer(reader, schemaNamespace, event.asStartElement());
                edmEntityContainers.add(edmEntityContainer);
            }
            if (isEndElement(event, schemaElement.getName())) {
                return new EdmSchema(schemaNamespace, edmEntityTypes, edmComplexTypes, edmAssociations, edmEntityContainers);
            }
        }

        throw new UnsupportedOperationException();

    }

    private static EdmEntityContainer parseEdmEntityContainer(XMLEventReader2 reader, String schemaNamespace, StartElement2 entityContainerElement) {
        String name = entityContainerElement.getAttributeByName("Name").getValue();
        boolean isDefault = "true".equals(entityContainerElement.getAttributeByName(new QName2(NS_METADATA, "IsDefaultEntityContainer")).getValue());
        String lazyLoadingEnabledValue = getAttributeValueIfExists(entityContainerElement, new QName2(NS_EDMANNOTATION,"LazyLoadingEnabled"));
        Boolean lazyLoadingEnabled = lazyLoadingEnabledValue==null?null:lazyLoadingEnabledValue.equals("true");
        
        List<EdmEntitySet> edmEntitySets = new ArrayList<EdmEntitySet>();
        List<EdmAssociationSet> edmAssociationSets = new ArrayList<EdmAssociationSet>();
        List<EdmFunctionImport> edmFunctionImports = new ArrayList<EdmFunctionImport>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (isStartElement(event, EDM2007_ENTITYSET, EDM2008_ENTITYSET))
                edmEntitySets.add(new TempEdmEntitySet(getAttributeValueIfExists(event.asStartElement(), "Name"), getAttributeValueIfExists(event.asStartElement(), "EntityType")));

            if (isStartElement(event, EDM2007_ASSOCIATIONSET, EDM2008_ASSOCIATIONSET))
                edmAssociationSets.add(parseEdmAssociationSet(reader, schemaNamespace, event.asStartElement()));
            
            if (isStartElement(event, EDM2007_FUNCTIONIMPORT, EDM2008_FUNCTIONIMPORT))
                edmFunctionImports.add(parseEdmFunctionImport(reader, schemaNamespace, event.asStartElement()));

            if (isEndElement(event, entityContainerElement.getName())) {
                return new EdmEntityContainer(name, isDefault, lazyLoadingEnabled, edmEntitySets, edmAssociationSets, edmFunctionImports);
            }
        }
        throw new UnsupportedOperationException();

    }

    

    private static EdmFunctionImport parseEdmFunctionImport(XMLEventReader2 reader, String schemaNamespace, StartElement2 functionImportElement) {
        String name = functionImportElement.getAttributeByName("Name").getValue();
        String entitySet = functionImportElement.getAttributeByName("EntitySet").getValue();
        String returnType = functionImportElement.getAttributeByName("ReturnType").getValue();
        String httpMethod = functionImportElement.getAttributeByName(new QName2(NS_METADATA,"HttpMethod")).getValue();

        List<EdmFunctionParameter> parameters = new ArrayList<EdmFunctionParameter>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (isStartElement(event, EDM2007_PARAMETER, EDM2008_PARAMETER))
                parameters.add(new EdmFunctionParameter(
                        event.asStartElement().getAttributeByName("Name").getValue(),
                        EdmType.get( event.asStartElement().getAttributeByName("Type").getValue()), 
                        event.asStartElement().getAttributeByName("Mode").getValue()));

            if (isEndElement(event, functionImportElement.getName())) {
                return new TempEdmFunctionImport(name,entitySet,returnType,httpMethod,parameters);
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

            if (isStartElement(event, EDM2007_END, EDM2008_END))
                ends.add(new TempEdmAssociationSetEnd(event.asStartElement().getAttributeByName("Role").getValue(), event.asStartElement().getAttributeByName("EntitySet").getValue()));

            if (isEndElement(event, associationSetElement.getName())) {
                return new TempEdmAssociationSet(name, associationName, ends.get(0), ends.get(1));
            }
        }
        throw new UnsupportedOperationException();

    }

    private static EdmAssociation parseEdmAssociation(XMLEventReader2 reader, String schemaNamespace, StartElement2 associationElement) {
        String name = associationElement.getAttributeByName("Name").getValue();

        List<EdmAssociationEnd> ends = new ArrayList<EdmAssociationEnd>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (isStartElement(event, EDM2007_END, EDM2008_END))
                ends.add(new TempEdmAssociationEnd(event.asStartElement().getAttributeByName("Role").getValue(), event.asStartElement().getAttributeByName("Type").getValue(), EdmMultiplicity.fromSymbolString(event.asStartElement().getAttributeByName("Multiplicity").getValue())));

            if (isEndElement(event, associationElement.getName())) {
                return new EdmAssociation(schemaNamespace, name, ends.get(0), ends.get(1));
            }
        }
        throw new UnsupportedOperationException();

    }

    private static EdmProperty parseEdmProperty(XMLEvent2 event) {
        String propertyName = getAttributeValueIfExists(event.asStartElement(), "Name");
        String propertyType = getAttributeValueIfExists(event.asStartElement(), "Type");
        String propertyNullable = getAttributeValueIfExists(event.asStartElement(), "Nullable");
        String maxLength = getAttributeValueIfExists(event.asStartElement(), "MaxLength");
        String unicode = getAttributeValueIfExists(event.asStartElement(), "Unicode");
        String fixedLength = getAttributeValueIfExists(event.asStartElement(), "FixedLength");
        
        String storeGeneratedPattern = getAttributeValueIfExists(event.asStartElement(), new QName2(NS_EDMANNOTATION,"StoreGeneratedPattern"));

        String fcTargetPath = getAttributeValueIfExists(event.asStartElement(), M_FC_TARGETPATH);
        String fcContentKind = getAttributeValueIfExists(event.asStartElement(), M_FC_CONTENTKIND);
        String fcKeepInContent = getAttributeValueIfExists(event.asStartElement(), M_FC_KEEPINCONTENT);

        return new EdmProperty(propertyName,
                EdmType.get(propertyType), 
                "false".equals(propertyNullable), 
                maxLength == null ? null : maxLength.equals("Max")?Integer.MAX_VALUE:Integer.parseInt(maxLength), 
                "false".equals(unicode), 
                "false".equals(fixedLength), 
                storeGeneratedPattern,
                fcTargetPath, 
                fcContentKind, 
                fcKeepInContent);
    }

    private static EdmComplexType parseEdmComplexType(XMLEventReader2 reader, String schemaNamespace, StartElement2 complexTypeElement) {
        String name = complexTypeElement.getAttributeByName("Name").getValue();
        List<EdmProperty> edmProperties = new ArrayList<EdmProperty>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (isStartElement(event, EDM2007_PROPERTY, EDM2008_PROPERTY)) {
                edmProperties.add(parseEdmProperty(event));
            }

            if (isEndElement(event, complexTypeElement.getName())) {
                return new EdmComplexType(schemaNamespace, name, edmProperties);
            }
        }

        throw new UnsupportedOperationException();

    }

    private static EdmEntityType parseEdmEntityType(XMLEventReader2 reader, String schemaNamespace, StartElement2 entityTypeElement) {
        String name = entityTypeElement.getAttributeByName("Name").getValue();
        String key = null;
        List<EdmProperty> edmProperties = new ArrayList<EdmProperty>();
        List<EdmNavigationProperty> edmNavigationProperties = new ArrayList<EdmNavigationProperty>();

        while (reader.hasNext()) {
            XMLEvent2 event = reader.nextEvent();

            if (isStartElement(event, EDM2007_PROPERTYREF, EDM2008_PROPERTYREF))
                key = event.asStartElement().getAttributeByName("Name").getValue();

            if (isStartElement(event, EDM2007_PROPERTY, EDM2008_PROPERTY)) {
                edmProperties.add(parseEdmProperty(event));
            }

            if (isStartElement(event, EDM2007_NAVIGATIONPROPERTY, EDM2008_NAVIGATIONPROPERTY)) {
                String associationName = event.asStartElement().getAttributeByName("Name").getValue();
                String relationshipName = event.asStartElement().getAttributeByName("Relationship").getValue();
                String fromRoleName = event.asStartElement().getAttributeByName("FromRole").getValue();
                String toRoleName = event.asStartElement().getAttributeByName("ToRole").getValue();

                edmNavigationProperties.add(new TempEdmNavigationProperty(associationName, relationshipName, fromRoleName, toRoleName));

            }

            if (isEndElement(event, entityTypeElement.getName())) {
                return new EdmEntityType(schemaNamespace, name, key, edmProperties, edmNavigationProperties);
            }
        }


        throw new UnsupportedOperationException();
    }

    
    
    
    
    
    
    private static class TempEdmFunctionImport extends EdmFunctionImport {
        public final String entitySetName;
        public final String returnTypeName;

        public TempEdmFunctionImport(String name, String entitySetName, String returnTypeName, String httpMethod, List<EdmFunctionParameter> parameters) {
            super(name, null, null,httpMethod,parameters);
            this.entitySetName = entitySetName;
            this.returnTypeName = returnTypeName;
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
