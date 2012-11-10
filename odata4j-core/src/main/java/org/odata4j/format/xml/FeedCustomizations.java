package org.odata4j.format.xml;

import java.util.HashMap;
import java.util.Map;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;

public class FeedCustomizations  {

  public static class PropertyCustomization {
    public final String propertyName;
    public final boolean keepInContent;

    public PropertyCustomization(String propertyName, boolean keepInContent) {
      this.propertyName = propertyName;
      this.keepInContent = keepInContent;
    }
  }

  public static class FeedCustomization {
    public static final FeedCustomization NONE = new FeedCustomization(null, null);

    public final PropertyCustomization syndicationTitle;
    public final PropertyCustomization syndicationSummary;

    private FeedCustomization(PropertyCustomization syndicationTitle, PropertyCustomization syndicationSummary) {
      this.syndicationTitle = syndicationTitle;
      this.syndicationSummary = syndicationSummary;
    }

    public static FeedCustomization create(PropertyCustomization syndicationTitle, PropertyCustomization syndicationSummary) {
      return syndicationTitle == null && syndicationSummary == null ? NONE : new FeedCustomization(syndicationTitle, syndicationSummary);
    }
  }

  private final EdmDataServices metadata;
  private Map<String, FeedCustomization> cache;

  public FeedCustomizations(EdmDataServices metadata) {
    this.metadata = metadata;
  }

  public FeedCustomization get(EdmEntityType eet) {
    if (eet == null)
      return null;
    if (cache == null)
      cache = new HashMap<String, FeedCustomization>();
    String key = eet.getFullyQualifiedTypeName();
    FeedCustomization value = cache.get(key);
    if (value == null) {
      value = computeFeedCustomization(metadata, eet);
      cache.put(key, value);
    }
    return value;
  }

  private static FeedCustomization computeFeedCustomization(EdmDataServices metadata, EdmEntityType eet) {
    PropertyCustomization syndicationTitle = null;
    PropertyCustomization syndicationSummary = null;
    for (EdmProperty ep : eet.getProperties()) {
      if ("SyndicationTitle".equals(ep.getFcTargetPath()))
        syndicationTitle = new PropertyCustomization(ep.getName(), !"false".equals(ep.getFcKeepInContent()));
      if ("SyndicationSummary".equals(ep.getFcTargetPath()))
        syndicationSummary = new PropertyCustomization(ep.getName(), !"false".equals(ep.getFcKeepInContent()));
    }
    return FeedCustomization.create(syndicationTitle, syndicationSummary);
  }

}