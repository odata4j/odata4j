package org.odata4j.producer.edm;

import org.odata4j.producer.PathHelper;
import org.odata4j.producer.Path;
import org.odata4j.producer.ExpressionEvaluator.VariableResolver;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.odata4j.core.IAnnotation;
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
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmAnnotationAttribute;
import org.odata4j.edm.EdmCollectionType;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmItem;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmProperty.CollectionKind;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmStructuralType;
import org.odata4j.edm.EdmType;
import org.odata4j.edm.IEdmDecorator;
import org.odata4j.format.xml.EdmxFormatWriter;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityIdResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ExpressionEvaluator;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;
import org.odata4j.producer.exceptions.NotImplementedException;

/**
 * a producer for $metadata.
 * 
 * This is somewhat brute-forceish.  There is maybe a world where an enhanced
 * InMemoryProducer and the org.odata4j.edm pojos together are sufficient to 
 * implement much of this...I'm not sure.
 * 
 * @author Tony Rozga
 */
public class MetadataProducer implements ODataProducer {

  /**
   * return this from your decorators annotation override method and the 
   * annotation will be removed.
   */
  public static final Object RemoveAnnotationOverride = new Object();

  public static class CustomOptions {

    /**
     * locale will be parsed as a locale string ala java.util.Locale.
     */
    public static final String Locale = "locale";
    /**
     * if true, a query for a structural type will return a flattened
     * representation of the type..i.e. it will contain inherited propeties
     * as well.
     */
    public static final String Flatten = "flatten";
  }

  /**
   * create
   * @param dataProducer - the data producer who defines the $metadata we will expose
   * @param edmDecorator - an optional decorator.  the decorator provides 
   *                       context for evaluating $filter expressions, custom
   *                       runtime overrides for annotation values and overrides 
   *                       for other metadata properties
   */
  public MetadataProducer(ODataProducer dataProducer, IEdmDecorator edmDecorator) {
    this.dataProducer = dataProducer;
    this.decorator = edmDecorator;
    edm = new MetadataEdmGenerator(edmDecorator).generateEdm();
  }
  private ODataProducer dataProducer = null;
  private EdmDataServices edm = null;
  private IEdmDecorator decorator = null;

  /**
   * Get the EDM model that this producer exposes.
   * @return - the model
   */
  public EdmDataServices getModel() {
    return this.dataProducer.getMetadata();
  }

  /**
   * Get the EDM that defines the queryable metadata, the meta-EDM
   * @return 
   */
  @Override
  public EdmDataServices getMetadata() {
    return edm;
  }

  // request context
  protected class Context implements VariableResolver {

    public Context(String entitySetName, QueryInfo queryInfo) {
      this(entitySetName, queryInfo, null);
    }

    public Context(String entitySetName, QueryInfo queryInfo, OEntityKey key) {
      this.entitySet = edm.findEdmEntitySet(entitySetName);
      this.queryInfo = queryInfo;
      this.entityKey = key;
      setLocale();
      pathHelper = new PathHelper(queryInfo.select, queryInfo.expand, getCustomOption(PathHelper.OptionSelectR), getCustomOption(PathHelper.OptionExpandR));
      flatten = getCustomBoolean(CustomOptions.Flatten, false);
    }

    protected final String getCustomOption(String key) {
      if (null != this.queryInfo
              && null != this.queryInfo.customOptions) {
        return this.queryInfo.customOptions.get(key);
      }
      return null;
    }

    protected final boolean getCustomBoolean(String key, boolean fallback) {
      String s = getCustomOption(key);
      return null == s ? fallback : Boolean.parseBoolean(s);
    }

    protected final void setLocale() {
      String lc = getCustomOption(CustomOptions.Locale);
      if (null != lc) {
        Locale l = parseLocale(lc);
        if (null != l) {
          locale = l;
        }
      }
    }

    public Locale parseLocale(String lstring) {
      String[] s = lstring.split("_", 3);
      if (1 == s.length) {
        return new Locale(s[0]);
      } else if (2 == s.length) {
        return new Locale(s[0], s[1]);
      } else if (3 == s.length) {
        return new Locale(s[0], s[1], s[2]);
      } else {
        return null;
      }
    }

    public void addEntity(OEntity e) {
      entities.add(e);
    }
    EdmEntitySet entitySet;
    QueryInfo queryInfo;
    OEntityKey entityKey;
    Locale locale = Locale.ENGLISH;
    PathHelper pathHelper;
    List<OEntity> entities = new LinkedList<OEntity>();
    boolean flatten = false;    // flatten properties for structural types

    @Override
    public Object resolveVariable(String path) {
      Path p = new Path(path);
      EdmItem i = resolverContext.isEmpty() ? null : this.peekResolver();

      if (null != i) {
        if (i instanceof EdmStructuralType) {
          return resolveStructuralTypeVariable((EdmStructuralType) i, p);
        } else if (i instanceof EdmProperty) {
          return resolvePropertyVariable((EdmProperty) i, p);
        }
      }

      throw new NotImplementedException("unhandled EdmItem type in resolveVariable: " + (null == i ? "null" : i.getClass().getName()));
    }

    private Object resolveStructuralTypeVariable(EdmStructuralType et, Path path) {
      Object result = null;
      if (path.getNComponents() == 1) {
        String name = path.getLastComponent();
        if (Edm.EntityType.Abstract.equals(name)) {
          return null == et.isAbstract ? false : et.isAbstract;
        } else if (Edm.EntityType.BaseType.equals(name)) {
          return null == et.getBaseType() ? null : et.getBaseType().getFullyQualifiedTypeName();
        } else if (Edm.EntityType.Name.equals(name)) {
          return et.name;
        } else if (Edm.EntityType.Namespace.equals(name)) {
          return et.namespace;
        } else {
          // see if our decorator has an annotation that works
          try {
            return decorator.resolveStructuralTypeProperty(et, path);
          } catch (Exception ex) {
            throw new RuntimeException("EdmEntityType property " + name + " not found");
          }
        }
      } else {
        String navProp = path.getFirstComponent();
        // --to 1 props only
        // TODO: superclass maybe
        throw new RuntimeException("EdmEntityType navigation property " + navProp + " not found or not supported");
      }
      //return result;
    }

    private Object resolvePropertyVariable(EdmProperty prop, Path path) {
      Object result = null;
      if (path.getNComponents() == 1) {
        String name = path.getLastComponent();
        if (Edm.Property.DefaultValue.equals(name)) {
          return prop.defaultValue;
        } else if (Edm.Property.CollectionKind.equals(name)) {
          return prop.collectionKind.toString();
        } else if (Edm.Property.EntityTypeName.equals(name)) {
          return prop.getStructuralType().name;
        } else if (Edm.Property.FixedLength.equals(name)) {
          return null != prop.fixedLength ? prop.fixedLength.toString() : null;
        } else if (Edm.Property.MaxLength.equals(name)) {
          return null != prop.maxLength ? prop.maxLength.toString() : null;
        } else if (Edm.Property.Name.equals(name)) {
          return prop.name;
        } else if (Edm.Property.Namespace.equals(name)) {
          return prop.getStructuralType().namespace;
        } else if (Edm.Property.Nullable.equals(name)) {
          return prop.nullable ? "true" : "false";
        } else if (Edm.Property.Type.equals(name)) {
          return prop.type.getFullyQualifiedTypeName();
        } else if (Edm.Property.Precision.equals(name)) {
          return null == prop.precision ? null : prop.precision.toString();
        } else if (Edm.Property.Scale.equals(name)) {
          return null == prop.scale ? null : prop.scale.toString();
        } else {
          try {
            if (null != decorator) {
              return decorator.resolvePropertyProperty(prop, path);
            }
          } catch (Exception ex) {
          } finally {
            throw new RuntimeException("EdmProperty property " + name + " not found");
          }
        }
      } else {
        String navProp = path.getFirstComponent();
        // --to 1 props only
        // TODO: class maybe
        throw new RuntimeException("EdmProperty navigation property " + navProp + " not found or not supported");
      }
      //return result;
    }
    private Stak<EdmItem> resolverContext = new Stak<EdmItem>();

    private void pushResolver(EdmItem item) {
      resolverContext.push(item);
    }

    private EdmItem peekResolver() {
      return resolverContext.peek();
    }

    private void popResolver() {
      resolverContext.pop();
    }

    private class Stak<T> implements Iterable<T> {
      // sugar!

      public Stak() {
      }

      public int size() {
        return l.size();
      }

      public boolean isEmpty() {
        return l.isEmpty();
      }

      public T pop() {
        return l.remove(l.size() - 1);
      }

      public T peek() {
        return l.get(l.size() - 1);
      }

      public void push(T value) {
        l.add(value);
      }

      public T get(int i) {
        return l.get(i);
      }

      public void clear() {
        l.clear();
      }
      private List<T> l = new ArrayList<T>();

      @Override
      public Iterator<T> iterator() {
        return l.iterator();
      }
    }
  }

  @Override
  public EntitiesResponse getEntities(String entitySetName, QueryInfo queryInfo) {

    Context c = new Context(entitySetName, queryInfo);
    if (entitySetName.equals(Edm.EntitySets.Schemas)) {
      getSchemas(c);
    } else if (entitySetName.equals(Edm.EntitySets.EntityTypes)) {
      getEntityTypes(c, false);
    } else if (entitySetName.equals(Edm.EntitySets.RootEntityTypes)) {
      getEntityTypes(c, true);
    } else if (entitySetName.equals(Edm.EntitySets.ComplexTypes)) {
      getComplexTypes(c, false);
    } else if (entitySetName.equals(Edm.EntitySets.RootComplexTypes)) {
      getComplexTypes(c, true);
    } else if (entitySetName.equals(Edm.EntitySets.Properties)) {
      getProperties(c);
    } else {
      // TODO: how does one return a 404?
      throw new RuntimeException("EntitySet " + entitySetName + " not found");
    }

    return Responses.entities(c.entities, c.entitySet,
            null, // inline count
            null);      // skip token.
  }

  protected void getSchemas(Context c) {
    EdmDataServices ds = dataProducer.getMetadata();
    ExpressionEvaluator f = null;
    if (null != c.queryInfo && null != c.queryInfo.filter) {
      f = new ExpressionEvaluator(c); // , c.queryInfo.filter); // TODO add resolver
    }

    for (EdmSchema schema : ds.getSchemas()) {
      boolean add = true;
      if (null != f) {
        c.pushResolver(schema);
        add = f.evaluate(c.queryInfo.filter);
      }
      if (add) {
        c.addEntity(getSchema(c, schema));
      }
      if (null != f) {
        c.popResolver();
      }
    }
  }

  protected OEntity getSchema(Context c, EdmSchema schema) {
    List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    if (c.pathHelper.isSelected(Edm.Schema.Namespace)) {
      props.add(OProperties.string(Edm.Schema.Namespace, schema.namespace));
    }
    if (null != schema.alias && c.pathHelper.isSelected(Edm.Schema.Alias)) {
      props.add(OProperties.string(Edm.Schema.Alias, schema.alias));
    }

    // links
    List<OLink> links = new LinkedList<OLink>();
    // --------------- ComplexTypes -------------------------------------
    if (c.pathHelper.isSelected(Edm.Schema.NavProps.ComplexTypes)) {
      if (c.pathHelper.isExpanded(Edm.Schema.NavProps.ComplexTypes)) {
        c.pathHelper.navigate(Edm.Schema.NavProps.ComplexTypes);
        List<OEntity> complexTypes = new ArrayList<OEntity>(schema.complexTypes.size());
        for (EdmComplexType ct : schema.complexTypes) {
          complexTypes.add(this.getStructuralType(c, ct));
        }
        c.pathHelper.popPath();
        links.add(OLinks.relatedEntitiesInline(null, Edm.Schema.NavProps.ComplexTypes, null, complexTypes));
      } else {
        // deferred
        links.add(OLinks.relatedEntities(null, Edm.Schema.NavProps.ComplexTypes, null));
      }
    } // else not selected

    // --------------- EntityTypes -------------------------------------
    if (c.pathHelper.isSelected(Edm.Schema.NavProps.EntityTypes)) {
      if (c.pathHelper.isExpanded(Edm.Schema.NavProps.EntityTypes)) {
        c.pathHelper.navigate(Edm.Schema.NavProps.EntityTypes);
        List<OEntity> etypes = new ArrayList<OEntity>(schema.entityTypes.size());
        for (EdmEntityType et : schema.entityTypes) {
          etypes.add(this.getStructuralType(c, et));
        }
        c.pathHelper.popPath();
        links.add(OLinks.relatedEntitiesInline(null, Edm.Schema.NavProps.EntityTypes, null, etypes));
      } else {
        // deferred
        links.add(OLinks.relatedEntities(null, Edm.Schema.NavProps.EntityTypes, null));
      }
    } // else not selected

    // not sure why CSDL doesn't have documentation on a schema element
    //addDocumenation(c, schema, props);

    addAnnotationProperties(c, schema, props);

    return OEntities.create(c.entitySet,
            OEntityKey.create(Edm.Schema.Namespace, schema.namespace), // OEntityKey entityKey, 
            props,
            links);
  }

  protected void getEntityTypes(Context c, boolean isRoot) {
    EdmDataServices ds = dataProducer.getMetadata();
    ExpressionEvaluator f = null;
    if (null != c.queryInfo && null != c.queryInfo.filter) {
      f = new ExpressionEvaluator(c); // , c.queryInfo.filter); // TODO add resolver
    }

    for (EdmEntityType et : ds.getEntityTypes()) {
      if ((isRoot && et.isRootType()) || (!isRoot)) {
        boolean add = true;
        if (null != f) {
          c.pushResolver(et);
          add = f.evaluate(c.queryInfo.filter);
        }
        if (add) {
          c.addEntity(getStructuralType(c, et));
        }
        if (null != f) {
          c.popResolver();
        }
      }
    }
  }

  private OEntity getStructuralType(Context c, EdmStructuralType st) {
    List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    if (c.pathHelper.isSelected(Edm.StructuralType.Name)) {
      props.add(OProperties.string(Edm.StructuralType.Name, st.name));
    }
    if (c.pathHelper.isSelected(Edm.StructuralType.Namespace)) {
      props.add(OProperties.string(Edm.StructuralType.Namespace, st.namespace));
    }
    if (null != st.isAbstract && c.pathHelper.isSelected(Edm.StructuralType.Abstract)) {
      props.add(OProperties.boolean_(Edm.StructuralType.Abstract, st.isAbstract));
    }
    if (null != st.getBaseType()) {
      if (c.pathHelper.isSelected(Edm.StructuralType.BaseType)) {
        props.add(OProperties.string(Edm.StructuralType.BaseType, st.getBaseType().getFullyQualifiedTypeName()));
      }
    } else if (st instanceof EdmEntityType && c.pathHelper.isSelected(Edm.EntityType.Key)) {
      // all root types must specify a key
                /*
       * Entity.Key isA EntityKey
       * EntityKey.Keys isA Collection(PropertyRef)
       * PropertyRef.Name isA String
       */
      EdmComplexType propRefType = edm.findEdmComplexType(Edm.PropertyRef.fqName());
      EdmComplexType entityKeyType = edm.findEdmComplexType(Edm.EntityKey.fqName());
      Builder<OComplexObject> builder = OCollections.newBuilder(propRefType);
      for (String key : ((EdmEntityType) st).getKeys()) {
        List<OProperty<?>> refProps = new ArrayList<OProperty<?>>();
        refProps.add(OProperties.string(Edm.PropertyRef.Name, key));
        builder.add(OComplexObjects.create(propRefType, refProps));
      }


      List<OProperty<?>> keyProps = new ArrayList<OProperty<?>>();
      EdmType collectionItemType = entityKeyType.findProperty(Edm.EntityKey.Keys).type;
      keyProps.add(OProperties.collection(Edm.EntityKey.Keys, new EdmCollectionType(collectionItemType.getFullyQualifiedTypeName(), collectionItemType), builder.build()));

      OComplexObject key = OComplexObjects.create(entityKeyType, keyProps);

      props.add(OProperties.complex(Edm.EntityType.Key, entityKeyType, key.getProperties()));
    }


    // links
    List<OLink> links = new LinkedList<OLink>();
    // --------------- Properties -------------------------------------
    if (c.pathHelper.isSelected(Edm.StructuralType.NavProps.Properties)) {
      if (c.pathHelper.isExpanded(Edm.StructuralType.NavProps.Properties)) {
        c.pathHelper.navigate(Edm.StructuralType.NavProps.Properties);
        List<OEntity> properties = new ArrayList<OEntity>(st.getDeclaredProperties().count());
        addProperties(st, st, properties, c);
        c.pathHelper.popPath();
        links.add(OLinks.relatedEntitiesInline(null, Edm.StructuralType.NavProps.Properties, null, properties));
      } else {
        // deferred
        links.add(OLinks.relatedEntities(null, Edm.StructuralType.NavProps.Properties, null));
      }
    } // else not selected

    // --------------- SuperType-------------------------------------
    if (c.pathHelper.isSelected(Edm.StructuralType.NavProps.SuperType)) {
      if (c.pathHelper.isExpanded(Edm.StructuralType.NavProps.SuperType)) {

        OEntity superType = null;
        if (null != st.getBaseType()) {
          c.pathHelper.navigate(Edm.StructuralType.NavProps.SuperType);
          superType = this.getStructuralType(c, st.getBaseType());
          c.pathHelper.popPath();
        }

        links.add(OLinks.relatedEntityInline(null, Edm.StructuralType.NavProps.SuperType, null, superType));
      } else {
        // deferred
        links.add(OLinks.relatedEntities(null, Edm.StructuralType.NavProps.SuperType, null));
      }
    }  // else not selected

    // --------------- SubTypes-------------------------------------
    if (c.pathHelper.isSelected(Edm.StructuralType.NavProps.SubTypes)) {
      if (c.pathHelper.isExpanded(Edm.StructuralType.NavProps.SubTypes)) {
        List<EdmStructuralType> stypes = getSubTypes(st);
        List<OEntity> subtypes = new ArrayList<OEntity>(stypes.size());
        // these are not root types...
        EdmEntitySet baseSet = c.entitySet;
        if (baseSet.name.equals(Edm.EntitySets.RootEntityTypes)) {
          c.entitySet = edm.findEdmEntitySet(Edm.EntitySets.EntityTypes);
        } else if (baseSet.name.equals(Edm.EntitySets.RootComplexTypes)) {
          c.entitySet = edm.findEdmEntitySet(Edm.EntitySets.ComplexTypes);
        }
        c.pathHelper.navigate(Edm.StructuralType.NavProps.SubTypes);
        for (EdmStructuralType subtype : stypes) {
          subtypes.add(this.getStructuralType(c, subtype));
        }
        c.pathHelper.popPath();
        links.add(OLinks.relatedEntitiesInline(null, Edm.StructuralType.NavProps.SubTypes, null, subtypes));
        c.entitySet = baseSet;
      } else {
        // deferred
        links.add(OLinks.relatedEntities(null, Edm.StructuralType.NavProps.SubTypes, null));
      }
    }  // else not selected

    addDocumenation(c, st, props);

    addAnnotationProperties(c, st, props);

    return OEntities.create(c.entitySet,
            OEntityKey.create(Edm.StructuralType.Namespace, st.namespace, Edm.StructuralType.Name, st.name), // OEntityKey entityKey, 
            props,
            links);
  }

  private void addProperties(EdmStructuralType queryType, EdmStructuralType st, List<OEntity> props, Context c) {

    for (EdmProperty p : st.getDeclaredProperties()) {
      props.add(getProperty(queryType, st, p, c));
    }

    if (c.flatten && st.getBaseType() != null) {
      addProperties(queryType, st.getBaseType(), props, c);
    }
  }

  private void addDocumenation(Context c, EdmItem item, List<OProperty<?>> props) {
    if (null != item.getDocumentation() && (null != item.getDocumentation().getSummary()
            || null != item.getDocumentation().getLongDescription()) && c.pathHelper.isSelected(Edm.Documentation.name())) {
      List<OProperty<?>> docProps = new ArrayList<OProperty<?>>();
      EdmComplexType docType = edm.findEdmComplexType(Edm.Documentation.fqName());
      if (null != item.getDocumentation().getSummary()) {
        docProps.add(OProperties.string(Edm.Documentation.Summary, item.getDocumentation().getSummary()));
      }
      if (null != item.getDocumentation().getLongDescription()) {
        docProps.add(OProperties.string(Edm.Documentation.LongDescription, item.getDocumentation().getLongDescription()));
      }
      OComplexObject doc = OComplexObjects.create(docType, docProps);
      props.add(OProperties.complex(Edm.Documentation.class.getSimpleName(), docType, docProps));
    }
  }

  private void addAnnotationProperties(Context c, EdmItem item, List<OProperty<?>> props) {
    if (null != item.getAnnotations()) {
      for (IAnnotation a : item.getAnnotations()) {
        if (null != a.getValue()) {
          /*
           * property naming: so...annotations live in a namespace.  JSON doesn't have the concept of namespaces,
           * I think <prefix>_<propname> makes the most sense.  We *could* use <prefix>:<propname> if we quoted the
           * json key..that isn't a universally supported thing though.
           * The issue gets weird with Atom.  The OData spec says that each sub-element of <m:properties> must live
           * in the data service namespace....I guess I'll just use the same JSON name....this of course makes
           * the queryable metadata property names different than the names one would see from $metadata...not
           * sure we can do anything about that.
           */
          String propName = a.getNamespacePrefix() + "_" + a.getLocalName();
          if (c.pathHelper.isSelected(propName)) {
            Object override = null != this.decorator ? this.decorator.getAnnotationValueOverride(item, a, c.flatten, c.locale,
                    null == c.queryInfo ? null : c.queryInfo.customOptions) : null;

            if (override != MetadataProducer.RemoveAnnotationOverride) {
              if (a instanceof EdmAnnotationAttribute) {
                props.add(OProperties.string(propName, null == override ? a.getValue().toString() : override.toString()));
              } else {
                OComplexObject co = (OComplexObject) (null == override ? a.getValue() : override);
                props.add(OProperties.complex(propName, (EdmComplexType) co.getType(), co.getProperties()));
              }
            }
          }
        }
      }
    }
  }

  private List<EdmStructuralType> getSubTypes(EdmStructuralType t) {
    List<EdmStructuralType> l = new LinkedList<EdmStructuralType>();
    EdmDataServices ds = dataProducer.getMetadata();
    // EdmDataServices api weakness..
    Iterable candidates = t instanceof EdmEntityType ? ds.getEntityTypes() : ds.getComplexTypes();
    for (Object eto : candidates) {
      EdmStructuralType st = (EdmStructuralType) eto;
      if ((!t.equals(st)) && t.equals(st.getBaseType())) {
        l.add(st);
      }
    }
    return l;
  }

  private OEntity getProperty(EdmStructuralType queryType, EdmStructuralType et, EdmProperty p, Context c) {
    List<OProperty<?>> props = new ArrayList<OProperty<?>>();
    if (c.pathHelper.isSelected(Edm.Property.Namespace)) {
      props.add(OProperties.string(Edm.Property.Namespace, et.namespace));
    }
    if (c.pathHelper.isSelected(Edm.Property.EntityTypeName)) {
      props.add(OProperties.string(Edm.Property.EntityTypeName, et.name));
    }
    if (c.pathHelper.isSelected(Edm.Property.Name)) {
      props.add(OProperties.string(Edm.Property.Name, p.name));
    }
    if (c.pathHelper.isSelected(Edm.Property.Type)) {
      props.add(OProperties.string(Edm.Property.Type, p.type.getFullyQualifiedTypeName()));
    }
    if (c.pathHelper.isSelected(Edm.Property.Nullable)) {
      props.add(OProperties.boolean_(Edm.Property.Nullable, p.nullable));
    }
    if (null != p.defaultValue && c.pathHelper.isSelected(Edm.Property.DefaultValue)) {
      props.add(OProperties.string(Edm.Property.DefaultValue, p.defaultValue));
    }
    if (null != p.maxLength && c.pathHelper.isSelected(Edm.Property.MaxLength)) {
      props.add(OProperties.int32(Edm.Property.MaxLength, p.maxLength));
    }
    if (null != p.fixedLength && c.pathHelper.isSelected(Edm.Property.FixedLength)) {
      props.add(OProperties.boolean_(Edm.Property.FixedLength, p.fixedLength));
    }
    if (null != p.precision && c.pathHelper.isSelected(Edm.Property.Precision)) {
      props.add(OProperties.int32(Edm.Property.Precision, p.precision));
    }
    if (null != p.scale && c.pathHelper.isSelected(Edm.Property.Scale)) {
      props.add(OProperties.int32(Edm.Property.Scale, p.scale));
    }
    if (null != p.unicode && c.pathHelper.isSelected(Edm.Property.Unicode)) {
      props.add(OProperties.boolean_(Edm.Property.Unicode, p.unicode));
    }
    // TODO: collation
    // TODO: ConcurrencyMode
    if (p.collectionKind != CollectionKind.None) {
      props.add(OProperties.string(Edm.Property.CollectionKind, p.collectionKind.toString()));
    }

    this.addDocumenation(c, p, props);
    addAnnotationProperties(c, p, props);

    EdmEntitySet entitySet = edm.findEdmEntitySet(Edm.EntitySets.Properties);

    if (null != this.decorator) {
      this.decorator.decorateEntity(entitySet, p, queryType, props, c.flatten, c.locale, null != c.queryInfo ? c.queryInfo.customOptions : null);
    }

    return OEntities.create(entitySet,
            OEntityKey.create(Edm.Property.Namespace, et.namespace, Edm.Property.EntityTypeName, et.name, Edm.Property.Name, p.name),
            props,
            Collections.<OLink>emptyList());
  }

  protected void getComplexTypes(Context c, boolean isRoot) {
    EdmDataServices ds = dataProducer.getMetadata();

    ExpressionEvaluator f = null;
    if (null != c.queryInfo && null != c.queryInfo.filter) {
      f = new ExpressionEvaluator(c); // , c.queryInfo.filter); // TODO add resolver
    }

    for (EdmComplexType ct : ds.getComplexTypes()) {
      if ((isRoot && ct.isRootType()) || (!isRoot)) {
        boolean add = true;
        if (null != f) {
          c.pushResolver(ct);
          add = f.evaluate(c.queryInfo.filter);
        }
        if (add) {
          c.addEntity(getStructuralType(c, ct));
        }
        if (null != f) {
          c.popResolver();
        }
      }
    }
  }

  protected void getProperties(Context c) {
    EdmDataServices ds = dataProducer.getMetadata();

    ExpressionEvaluator f = null;
    if (null != c.queryInfo && null != c.queryInfo.filter) {
      f = new ExpressionEvaluator(c);
    }

    for (EdmComplexType ct : ds.getComplexTypes()) {
      if (ct.isRootType()) {
        addStructuralTypeProperties(c, ct, f);
      }
    }

    for (EdmEntityType ct : ds.getEntityTypes()) {
      if (ct.isRootType()) {
        addStructuralTypeProperties(c, ct, f);
      }
    }
  }

  protected void addStructuralTypeProperties(Context c, EdmStructuralType st, ExpressionEvaluator ev) {
    for (EdmProperty prop : st.properties) {
      boolean add = true;
      if (null != ev) {
        c.pushResolver(prop);
        add = ev.evaluate(c.queryInfo.filter);
      }
      if (add) {
        c.addEntity(this.getProperty(st, st, prop, c));
      }
      if (null != ev) {
        c.popResolver();
      }
    }

    EdmDataServices ds = dataProducer.getMetadata();
    Iterator candidates = (st instanceof EdmComplexType) ? ds.getComplexTypes().iterator() : ds.getEntityTypes().iterator();
    // down the subtypes hole...
    while (candidates.hasNext()) {
      EdmStructuralType item = (EdmStructuralType) candidates.next();
      if (null != item.getBaseType() && item.getBaseType().equals(st)) {
        addStructuralTypeProperties(c, item, ev);
      }
    }
  }

  @Override
  public EntityResponse getEntity(String entitySetName, OEntityKey entityKey, QueryInfo queryInfo) {
    Context c = new Context(entitySetName, queryInfo, entityKey);

    if (entitySetName.equals(Edm.EntitySets.Schemas)) {
      findSchema(c);
    } else if (entitySetName.equals(Edm.EntitySets.EntityTypes)
            || entitySetName.equals(Edm.EntitySets.RootEntityTypes)) {
      findStructuralType(c, true, entitySetName.equals(Edm.EntitySets.RootEntityTypes));
    } else if (entitySetName.equals(Edm.EntitySets.ComplexTypes)
            || entitySetName.equals(Edm.EntitySets.RootComplexTypes)) {
      findStructuralType(c, false, entitySetName.equals(Edm.EntitySets.RootComplexTypes));
    } else {
      // TODO: how does one return a 404?
      throw new RuntimeException("EntitySet " + entitySetName + " not found");
    }

    // TODO: how does one return a 404?
    if (c.entities.isEmpty()) {
      throw new RuntimeException(entitySetName + entityKey.toKeyString() + " not found");
    }

    return Responses.entity(c.entities.get(0));
  }

  protected void findSchema(Context c) {
    EdmDataServices ds = dataProducer.getMetadata();
    String nm = (String) c.entityKey.asSingleValue();
    for (EdmSchema s : ds.getSchemas()) {
      if (nm.equals(s.namespace)) {
        c.entities.add(this.getSchema(c, s));
      }
    }
  }

  protected void findStructuralType(Context c, boolean isEntity, boolean root) {
    EdmDataServices ds = dataProducer.getMetadata();
    Iterable candidates = isEntity ? ds.getEntityTypes() : ds.getComplexTypes();
    for (Object eto : candidates) {
      EdmStructuralType st = (EdmStructuralType) eto;

      if (root && st.getBaseType() != null) {
        continue;
      }
      boolean matchedAll = true;
      for (OProperty<?> keyprop : c.entityKey.asComplexProperties()) {
        String val = null;
        if (keyprop.getName().equals(Edm.EntityType.Namespace)) {
          val = st.namespace;
        } else if (keyprop.getName().equals(Edm.EntityType.Name)) {
          val = st.name;
        } else {
          throw new RuntimeException(keyprop.getName() + " is not a key property of " + c.entitySet.name);
        }
        if (!keyprop.getValue().toString().equals(val)) {
          matchedAll = false;
          break;
        }
      }
      if (matchedAll) {
        c.entities.add(this.getStructuralType(c, st));
      }
    }
    // didn't find it...
  }

  public void log() {
    StringWriter sw = new StringWriter();
    EdmxFormatWriter.write(edm, sw);
    //log.debug(sw.toString());
    System.out.println(sw.toString());
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

  @Override
  public MetadataProducer getMetadataProducer() {
    return null; // stop the brutal recursion :)
  }
}
