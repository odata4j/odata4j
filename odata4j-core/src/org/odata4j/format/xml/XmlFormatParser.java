package org.odata4j.format.xml;

import org.odata4j.stax2.Attribute2;
import org.odata4j.stax2.QName2;
import org.odata4j.stax2.StartElement2;
import org.odata4j.stax2.XMLEvent2;

import org.core4j.Enumerable;

public class XmlFormatParser {
    
    
    public static final String NS_APP = "http://www.w3.org/2007/app";
    public static final String NS_XML = "http://www.w3.org/XML/1998/namespace";
    public static final String NS_ATOM = "http://www.w3.org/2005/Atom";

    public static final String NS_METADATA = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
    public static final String NS_DATASERVICES = "http://schemas.microsoft.com/ado/2007/08/dataservices";
    public static final String NS_EDM2006 = "http://schemas.microsoft.com/ado/2006/04/edm";
    public static final String NS_EDM2007 = "http://schemas.microsoft.com/ado/2007/05/edm";
    public static final String NS_EDM2008 = "http://schemas.microsoft.com/ado/2008/09/edm";
    public static final String NS_EDMX = "http://schemas.microsoft.com/ado/2007/06/edmx";
    public static final String NS_EDMANNOTATION = "http://schemas.microsoft.com/ado/2009/02/edm/annotation";
   
    public static final QName2 EDMX_DATASERVICES = new QName2(NS_EDMX, "DataServices");
    
    public static final QName2 EDM2006_SCHEMA = new QName2(NS_EDM2006, "Schema");
    public static final QName2 EDM2006_ENTITYTYPE = new QName2(NS_EDM2006, "EntityType");
    public static final QName2 EDM2006_ASSOCIATION = new QName2(NS_EDM2006, "Association");
    public static final QName2 EDM2006_COMPLEXTYPE = new QName2(NS_EDM2006, "ComplexType");
    public static final QName2 EDM2006_ENTITYCONTAINER = new QName2(NS_EDM2006, "EntityContainer");
    public static final QName2 EDM2006_ENTITYSET = new QName2(NS_EDM2006, "EntitySet");
    public static final QName2 EDM2006_ASSOCIATIONSET = new QName2(NS_EDM2006, "AssociationSet");
    public static final QName2 EDM2006_FUNCTIONIMPORT = new QName2(NS_EDM2006, "FunctionImport");
    public static final QName2 EDM2006_PARAMETER = new QName2(NS_EDM2006, "Parameter");
    public static final QName2 EDM2006_END = new QName2(NS_EDM2006,"End");
    public static final QName2 EDM2006_PROPERTYREF = new QName2(NS_EDM2006,"PropertyRef");
    public static final QName2 EDM2006_PROPERTY = new QName2(NS_EDM2006,"Property");
    public static final QName2 EDM2006_NAVIGATIONPROPERTY = new QName2(NS_EDM2006,"NavigationProperty");
    
    public static final QName2 EDM2007_SCHEMA = new QName2(NS_EDM2007, "Schema");
    public static final QName2 EDM2007_ENTITYTYPE = new QName2(NS_EDM2007, "EntityType");
    public static final QName2 EDM2007_ASSOCIATION = new QName2(NS_EDM2007, "Association");
    public static final QName2 EDM2007_COMPLEXTYPE = new QName2(NS_EDM2007, "ComplexType");
    public static final QName2 EDM2007_ENTITYCONTAINER = new QName2(NS_EDM2007, "EntityContainer");
    public static final QName2 EDM2007_ENTITYSET = new QName2(NS_EDM2007, "EntitySet");
    public static final QName2 EDM2007_ASSOCIATIONSET = new QName2(NS_EDM2007, "AssociationSet");
    public static final QName2 EDM2007_FUNCTIONIMPORT = new QName2(NS_EDM2007, "FunctionImport");
    public static final QName2 EDM2007_PARAMETER = new QName2(NS_EDM2007, "Parameter");
    public static final QName2 EDM2007_END = new QName2(NS_EDM2007,"End");
    public static final QName2 EDM2007_PROPERTYREF = new QName2(NS_EDM2007,"PropertyRef");
    public static final QName2 EDM2007_PROPERTY = new QName2(NS_EDM2007,"Property");
    public static final QName2 EDM2007_NAVIGATIONPROPERTY = new QName2(NS_EDM2007,"NavigationProperty");
    
    public static final QName2 EDM2008_SCHEMA = new QName2(NS_EDM2008, "Schema");
    public static final QName2 EDM2008_ENTITYTYPE = new QName2(NS_EDM2008, "EntityType");
    public static final QName2 EDM2008_ASSOCIATION = new QName2(NS_EDM2008, "Association");
    public static final QName2 EDM2008_COMPLEXTYPE = new QName2(NS_EDM2008, "ComplexType");
    public static final QName2 EDM2008_ENTITYCONTAINER = new QName2(NS_EDM2008, "EntityContainer");
    public static final QName2 EDM2008_ENTITYSET = new QName2(NS_EDM2008, "EntitySet");
    public static final QName2 EDM2008_ASSOCIATIONSET = new QName2(NS_EDM2008, "AssociationSet");
    public static final QName2 EDM2008_FUNCTIONIMPORT = new QName2(NS_EDM2008, "FunctionImport");
    public static final QName2 EDM2008_PARAMETER = new QName2(NS_EDM2008, "Parameter");
    public static final QName2 EDM2008_END = new QName2(NS_EDM2008,"End");
    public static final QName2 EDM2008_PROPERTYREF = new QName2(NS_EDM2008,"PropertyRef");
    public static final QName2 EDM2008_PROPERTY = new QName2(NS_EDM2008,"Property");
    public static final QName2 EDM2008_NAVIGATIONPROPERTY = new QName2(NS_EDM2008,"NavigationProperty");
    
    
    public static final QName2 ATOM_ENTRY = new QName2(NS_ATOM, "entry");
    public static final QName2 ATOM_ID = new QName2(NS_ATOM, "id");
    public static final QName2 ATOM_TITLE = new QName2(NS_ATOM, "title");
    public static final QName2 ATOM_SUMMARY = new QName2(NS_ATOM, "summary");
    public static final QName2 ATOM_UPDATED = new QName2(NS_ATOM, "updated");
    public static final QName2 ATOM_CATEGORY = new QName2(NS_ATOM, "category");
    public static final QName2 ATOM_CONTENT = new QName2(NS_ATOM, "content");
    public static final QName2 ATOM_LINK = new QName2(NS_ATOM, "link");

    public static final QName2 APP_WORKSPACE = new QName2(NS_APP, "workspace");
    public static final QName2 APP_SERVICE = new QName2(NS_APP, "service");
    public static final QName2 APP_COLLECTION = new QName2(NS_APP, "collection");
    public static final QName2 APP_ACCEPT = new QName2(NS_APP, "accept");

    public static final QName2 M_ETAG = new QName2(NS_METADATA, "etag");
    public static final QName2 M_PROPERTIES = new QName2(NS_METADATA, "properties");
    public static final QName2 M_TYPE = new QName2(NS_METADATA, "type");
    public static final QName2 M_NULL = new QName2(NS_METADATA, "null");
    public static final QName2 M_FC_TARGETPATH = new QName2(NS_METADATA,"FC_TargetPath");
    public static final QName2 M_FC_CONTENTKIND = new QName2(NS_METADATA,"FC_ContentKind");
    public static final QName2 M_FC_KEEPINCONTENT = new QName2(NS_METADATA,"FC_KeepInContent");
    public static final QName2 M_FC_EPMCONTENTKIND = new QName2(NS_METADATA,"FC_EpmContentKind");
    public static final QName2 M_FC_EPMKEEPINCONTENT = new QName2(NS_METADATA,"FC_EpmKeepInContent");

    public static final QName2 XML_BASE = new QName2(NS_XML, "base");
    
    protected static boolean isStartElement(XMLEvent2 event, QName2... names) {
        return event.isStartElement() && Enumerable.create(names).contains(event.asStartElement().getName());
    }
    protected static boolean isEndElement(XMLEvent2 event, QName2 qname) {
        return event.isEndElement() && event.asEndElement().getName().equals(qname);
    }

    protected static String urlCombine(String base, String rel) {
        if (!base.endsWith("/") && !rel.startsWith("/"))
            base = base + "/";
        return base + rel;
    }
    
    protected static String getAttributeValueIfExists(StartElement2 element, String localName) {
        return getAttributeValueIfExists(element, new QName2(null, localName));
    }

    protected static String getAttributeValueIfExists(StartElement2 element, QName2 attName) {
        Attribute2 rt = element.getAttributeByName(attName);
        return rt == null ? null : rt.getValue();
    }
}
