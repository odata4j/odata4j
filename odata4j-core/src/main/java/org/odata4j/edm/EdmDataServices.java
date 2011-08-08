package org.odata4j.edm;

import java.util.ArrayList;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.odata4j.core.ODataVersion;
import org.odata4j.producer.exceptions.NotFoundException;

/**
 * The &lt;edmx:DataServices&gt; element contains the service metadata of a Data Service. This service metadata contains zero or more EDM conceptual schemas.
 * <p>Since this is the root of a large metadata tree, convenience methods are included to help locate child metadata elements.</p>
 * @see <a href="http://msdn.microsoft.com/en-us/library/dd541087(v=prot.10).aspx">[msdn] 2.2 &lt;edmx:DataServices&gt;</a>
 */
public class EdmDataServices {

  private final ODataVersion version;
  private final List<EdmSchema> schemas;

  public static final EdmDataServices EMPTY = new EdmDataServices(null, new ArrayList<EdmSchema>());

  public EdmDataServices(ODataVersion version, List<EdmSchema> schemas) {
    this.version = version;
    this.schemas = schemas;
  }

  public String getVersion() {
    return version != null ? version.asString : null;
  }

  public List<EdmSchema> getSchemas() {
    return schemas;
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
            return type == input.type;
          }
        });

    if (ees != null) {
      return ees;
    }
    throw new NotFoundException("EdmEntitySet " + type.name + " is not found");
  }

  public EdmEntitySet findEdmEntitySet(String entitySetName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.entityContainers) {
        for (EdmEntitySet ees : eec.entitySets) {
          if (ees.name.equals(entitySetName)) {
            return ees;
          }
        }
      }
    }
    return null;
  }

  public EdmFunctionImport findEdmFunctionImport(String functionImportName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.entityContainers) {
        for (EdmFunctionImport efi : eec.functionImports) {
          if (efi.name.equals(functionImportName)) {
            return efi;
          }
        }
      }
    }
    return null;
  }

  public EdmComplexType findEdmComplexType(String complexTypeFQName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmComplexType ect : schema.complexTypes) {
        if (ect.getFQName().equals(complexTypeFQName)) {
          return ect;
        }
      }
    }
    return null;
  }

  public EdmBaseType findEdmEntityType(String fqName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityType et : schema.entityTypes) {
        if (et.toTypeString().equals(fqName)) {
          return et;
        }
      }
    }
    return null;
  }

  public EdmPropertyBase findEdmProperty(String propName) {
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.entityContainers) {
        for (EdmEntitySet ees : eec.entitySets) {
          for (EdmNavigationProperty ep : ees.type.getNavigationProperties()) {
            if (ep.name.equals(propName)) {
              return ep;
            }
          }
          for (final EdmProperty ep : ees.type.getProperties()) {
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
      rt.addAll(schema.entityTypes);
    }
    return rt;
  }

  public Iterable<EdmComplexType> getComplexTypes() {
    List<EdmComplexType> rt = new ArrayList<EdmComplexType>();
    for (EdmSchema schema : this.schemas) {
      rt.addAll(schema.complexTypes);
    }
    return rt;
  }

  public Iterable<EdmAssociation> getAssociations() {
    List<EdmAssociation> rt = new ArrayList<EdmAssociation>();
    for (EdmSchema schema : this.schemas) {
      rt.addAll(schema.associations);
    }
    return rt;
  }

  public Iterable<EdmEntitySet> getEntitySets() {
    List<EdmEntitySet> rt = new ArrayList<EdmEntitySet>();
    for (EdmSchema schema : this.schemas) {
      for (EdmEntityContainer eec : schema.entityContainers) {
        rt.addAll(eec.entitySets);
      }
    }
    return rt;
  }

  public EdmSchema findSchema(String namespace) {
    for (EdmSchema schema : this.schemas) {
      if (schema.namespace.equals(namespace)) {
        return schema;
      }
    }
    return null;
  }

}
