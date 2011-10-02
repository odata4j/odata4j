package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.odata4j.core.Namespace;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OPredicates;
import org.odata4j.producer.exceptions.NotFoundException;

/**
 * The &lt;edmx:DataServices&gt; element contains the service metadata of a Data Service. This service metadata contains zero or more EDM conceptual schemas.
 * <p>Since this is the root of a large metadata tree, convenience methods are included to help locate child metadata elements.</p>
 * @see <a href="http://msdn.microsoft.com/en-us/library/dd541087(v=prot.10).aspx">[msdn] 2.2 &lt;edmx:DataServices&gt;</a>
 */
public class EdmDataServices {

  public static final EdmDataServices EMPTY = new EdmDataServices(null, new ArrayList<EdmSchema>());

  private final ODataVersion version;
  private final List<EdmSchema> schemas;
  private final List<Namespace> namespaces; // for Annotations

  public EdmDataServices(ODataVersion version, List<EdmSchema> schemas) {
    this(version, schemas, null);
  }

  public EdmDataServices(ODataVersion version, List<EdmSchema> schemas, List<Namespace> namespaces) {
    this.version = version;
    this.schemas = schemas;
    this.namespaces = namespaces;
  }

  public String getVersion() {
    return version != null ? version.asString : null;
  }

  public List<EdmSchema> getSchemas() {
    return schemas;
  }

  public List<Namespace> getNamespaces() {
    return this.namespaces;
  }

  public EdmEntitySet getEdmEntitySet(String entitySetName) {
    EdmEntitySet ees = findEdmEntitySet(entitySetName);
    if (ees != null) {
      return ees;
    }
    throw new NotFoundException("EdmEntitySet " + entitySetName + " is not found");
  }

  public EdmEntitySet getEdmEntitySet(final EdmEntityType type) {
    if (type == null)
      throw new IllegalArgumentException("type cannot be null");
    EdmEntitySet ees = Enumerable.create(getEntitySets())
        .firstOrNull(new Predicate1<EdmEntitySet>() {
          @Override
          public boolean apply(EdmEntitySet input) {
            return type == input.getType();
          }
        });

    if (ees != null) {
      return ees;
    }
    throw new NotFoundException("EdmEntitySet " + type.getName() + " is not found");
  }

  public EdmEntitySet findEdmEntitySet(String entitySetName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.getEntityContainers()) {
        for (EdmEntitySet ees : eec.getEntitySets()) {
          if (ees.getName().equals(entitySetName)) {
            return ees;
          }
        }
      }
    }
    return null;
  }

  public EdmFunctionImport findEdmFunctionImport(String functionImportName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.getEntityContainers()) {
        for (EdmFunctionImport efi : eec.getFunctionImports()) {
          if (efi.getName().equals(functionImportName)) {
            return efi;
          }
        }
      }
    }
    return null;
  }

  public EdmComplexType findEdmComplexType(String complexTypeFQName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmComplexType ect : schema.getComplexTypes()) {
        if (ect.getFullyQualifiedTypeName().equals(complexTypeFQName)) {
          return ect;
        }
      }
    }
    return null;
  }

  public EdmType findEdmEntityType(String fqName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityType et : schema.getEntityTypes()) {
        if (et.getFullyQualifiedTypeName().equals(fqName)) {
          return et;
        }
      }
    }
    return null;
  }

  public EdmPropertyBase findEdmProperty(String propName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.getEntityContainers()) {
        for (EdmEntitySet ees : eec.getEntitySets()) {
          for (EdmNavigationProperty ep : ees.getType().getNavigationProperties()) {
            if (ep.name.equals(propName)) {
              return ep;
            }
          }
          for (final EdmProperty ep : ees.getType().getProperties()) {
            if (ep.name.equals(propName)) {
              return ep;
            }
          }
        }
      }
    }

    return null;
  }

  public Iterable<EdmEntityType> getEntityTypes() {
    List<EdmEntityType> rt = new ArrayList<EdmEntityType>();
    for (EdmSchema schema : this.schemas) {
      rt.addAll(schema.getEntityTypes());
    }
    return rt;
  }

  public Iterable<EdmComplexType> getComplexTypes() {
    List<EdmComplexType> rt = new ArrayList<EdmComplexType>();
    for (EdmSchema schema : this.schemas) {
      rt.addAll(schema.getComplexTypes());
    }
    return rt;
  }

  public Iterable<EdmStructuralType> getStructuralTypes() {
    return Enumerable.create(getEntityTypes()).cast(EdmStructuralType.class)
        .concat(Enumerable.create(getComplexTypes()).cast(EdmStructuralType.class));
  }

  public Iterable<EdmAssociation> getAssociations() {
    List<EdmAssociation> rt = new ArrayList<EdmAssociation>();
    for (EdmSchema schema : this.schemas) {
      rt.addAll(schema.getAssociations());
    }
    return rt;
  }

  public Iterable<EdmEntitySet> getEntitySets() {
    List<EdmEntitySet> rt = new ArrayList<EdmEntitySet>();
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.getEntityContainers()) {
        rt.addAll(eec.getEntitySets());
      }
    }
    return rt;
  }

  public EdmSchema findSchema(String namespace) {
    for (EdmSchema schema : this.schemas) {
      if (schema.getNamespace().equals(namespace)) {
        return schema;
      }
    }
    return null;
  }

  public Iterable<EdmStructuralType> getSubTypes(EdmStructuralType t) {
    return Enumerable.create(getStructuralTypes()).where(OPredicates.edmSubTypeOf(t));
  }

}
