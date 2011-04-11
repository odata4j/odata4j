package org.odata4j.format.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.core4j.Enumerable;
import org.core4j.Func1;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmProperty;
import org.odata4j.format.Entry;
import org.odata4j.format.Feed;
import org.odata4j.format.Settings;
import org.odata4j.format.json.JsonFeedFormatParser.JsonEntry;
import org.odata4j.format.json.JsonFeedFormatParser.JsonFeed;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader;
import org.odata4j.format.json.JsonStreamReaderFactory.JsonStreamReader.JsonEvent;

public class JsonFormatParser {
	
	static class JsonEntryMetaData {
		String uri;
		String type;
		String etag;
	}
	
	static class JsonObjectPropertyValue {
		String uri;
		OEntity entity;
		List<OEntity> entities;
	}
	
	protected static final String METADATA_PROPERTY = "__metadata";
	protected static final String DEFERRED_PROPERTY = "__deferred";
	protected static final String NEXT_PROPERTY = "__next";

	protected static final String URI_PROPERTY = "uri";
	protected static final String TYPE_PROPERTY = "type";
	protected static final String ETAG_PROPERTY = "etag";
	protected static final String RESULTS_PROPERTY = "results";
	protected static final String DATA_PROPERTY = "d";
	
	protected ODataVersion version;
	protected EdmDataServices metadata;
	protected String entitySetName;
	protected OEntityKey entityKey;
	protected boolean isResponse;
	
	protected JsonFormatParser(Settings settings) {
		this.version = settings.version;
		this.metadata = settings.metadata;
		this.entitySetName = settings.entitySetName;
		this.entityKey = settings.entityKey;
		this.isResponse = settings.isResponse;
	}

	protected JsonFeed parseFeed(EdmEntitySet ees, JsonStreamReader jsr) {
		JsonFeed feed = new JsonFeed();
		feed.entries = new ArrayList<Entry>();
		
		while (jsr.hasNext()) {
			JsonEvent event = jsr.nextEvent();
			
			if (event.isStartObject()) {
				JsonEntry entry = parseEntry(ees, jsr);
				feed.entries.add(entry);
			} else if (event.isEndArray()) {
				break;
			}
		}
		
		return feed;
	}
	
	protected JsonEntry parseEntry(JsonEntryMetaData jemd, EdmEntitySet ees, JsonStreamReader jsr) {
		JsonEntry entry = new JsonEntry();
		entry.properties = new ArrayList<OProperty<?>>();
		entry.links = new ArrayList<OLink>();
		while (jsr.hasNext()) {
			JsonEvent event = jsr.nextEvent();
			if (event.isStartProperty()) {
				addProperty(entry, ees, event.asStartProperty().getName(), jsr);
			} else if (event.isEndObject()) {
				break;
			}
		}
		List<OLink> links = Collections.emptyList();
		OEntityKey entityKey = this.entityKey!=null?this.entityKey:OEntityKey.infer(ees, entry.properties);
		entry.oentity = OEntities.create(ees, entityKey, entry.properties, links);
		
		return entry;
	}
	
	protected JsonEntry parseEntry(EdmEntitySet ees, JsonStreamReader jsr) {
		JsonEntry entry = new JsonEntry();
		entry.properties = new ArrayList<OProperty<?>>();
		entry.links = new ArrayList<OLink>();
		
		while (jsr.hasNext()) {
			JsonEvent event = jsr.nextEvent();
			
			if (event.isStartProperty()) {
				addProperty(entry, ees, event.asStartProperty().getName(), jsr);
			} else if (event.isEndObject()) {
				break;
			}
		}
		entry.oentity = OEntities.create(ees,
				entityKey!=null?entityKey:OEntityKey.infer(ees, entry.properties),
				entry.properties, entry.links);
		
		return entry;
	}

	protected JsonEntryMetaData parseMetadata(JsonStreamReader jsr) {
		JsonEntryMetaData jemd = new JsonEntryMetaData();
		ensureStartObject(jsr.nextEvent());

		while (jsr.hasNext()) {
			JsonEvent event = jsr.nextEvent();
			ensureNext(jsr);
			
			if (event.isStartProperty()
					&& URI_PROPERTY.equals(event.asStartProperty().getName())) {
				ensureEndProperty(event = jsr.nextEvent());
				jemd.uri = event.asEndProperty().getValue();
			} else if (event.isStartProperty()
					&& TYPE_PROPERTY.equals(event.asStartProperty().getName())) {
				ensureEndProperty(event = jsr.nextEvent());
				jemd.type = event.asEndProperty().getValue();
			} else if (event.isStartProperty()
					&& ETAG_PROPERTY.equals(event.asStartProperty().getName())) {
				ensureEndProperty(event = jsr.nextEvent());
				jemd.etag = event.asEndProperty().getValue();
			} else if (event.isEndObject()) {
				break;
			}
		}
		//	eat the EndProperty event
		ensureEndProperty(jsr.nextEvent());
		
		return jemd;
	}

	/**
	 * adds the property. This property can be a navigation property too. In this
	 * case a link will be added. If it's the meta data the information will be
	 * added to the entry too.
	 */
	protected void addProperty(JsonEntry entry, EdmEntitySet ees, String name, JsonStreamReader jsr) {
		
		if (METADATA_PROPERTY.equals(name)) {
			JsonEntryMetaData jemd = parseMetadata(jsr);
			entry.etag = jemd.etag;
			JsonEvent event = jsr.nextEvent();
			ensureStartProperty(event);
			name = event.asStartProperty().getName();
		}
		
		JsonEvent event = jsr.nextEvent();
		
		if (event.isEndProperty()) {
			// scalar property
			EdmProperty ep = ees.type.getProperty(name);
			if (ep == null) {
				throw new IllegalArgumentException("unknown property " + name + " for "+ ees.name);
			}
			entry.properties.add(JsonTypeConverter.parse(name, ep.type, event.asEndProperty().getValue()));
		} else if (event.isStartObject()) {
			//	reference deferred or inlined
			
			JsonObjectPropertyValue val = getValue(event, ees, name, jsr);
			
			if (val.uri != null) {
				entry.links.add(OLinks.relatedEntity(name, name, val.uri));
			} else if (val.entity != null) {
				entry.links.add(OLinks.relatedEntityInline(name, name, entry.getUri() + "/" + name, 
						val.entity));
			} else {
				entry.links.add(OLinks.relatedEntitiesInline(name, name, entry.getUri() + "/" + name, 
						val.entities));
			}
		} else if (event.isStartArray()) {
			ensureNext(jsr);
			event = jsr.nextEvent();

			if (event.isValue()) {
				throw new IllegalArgumentException("arrays of primitive types not supported! property " + ees.name + "." + name);
			} else if (event.isStartObject()) {
				EdmNavigationProperty navProp = ees.type.getNavigationProperty(name);
				if (navProp == null) {
					System.out.println("STOP");
				}
				ees = metadata.getEdmEntitySet(navProp.toRole.type);
				List<OEntity> entities = new ArrayList<OEntity>();
				do {
					entities.add(parseEntry(ees, jsr).getEntity());
					event = jsr.nextEvent();
				} while (!event.isEndArray());
				entry.links.add(OLinks.relatedEntitiesInline(name, name, entry.getUri() + "/" + name, 
						entities));
			} else {
				throw new IllegalArgumentException("What's that?");
			}
			
			ensureEndProperty(jsr.nextEvent());
		}
	}
	
	protected JsonObjectPropertyValue getValue(JsonEvent event, EdmEntitySet ees, String name, JsonStreamReader jsr) {
		JsonObjectPropertyValue rt = new JsonObjectPropertyValue();
		
		ensureStartObject(event);

		event = jsr.nextEvent();
		ensureStartProperty(event);
		
		// "__deferred": 
		if (DEFERRED_PROPERTY.equals(event.asStartProperty().getName())) {	
			// deferred feed or entity
			
			// {
			ensureStartObject(jsr.nextEvent());
			
			// "uri" :
			ensureStartProperty(jsr.nextEvent(), URI_PROPERTY);
			// "uri" property value
			String uri = jsr.nextEvent().asEndProperty().getValue();
			
			rt.uri = uri;

			// }
			ensureEndObject(jsr.nextEvent());
			
			// eat EndObject event and EndProperty event for "__deferred"
			// }
			ensureEndProperty(jsr.nextEvent());
			ensureEndObject(jsr.nextEvent());

		// "results" :
		} else if (RESULTS_PROPERTY.equals(event.asStartProperty().getName())) {
			if (version == ODataVersion.V1) {
				throw new IllegalArgumentException("no valid OData JSON format results not expected");
			}
			
			// inlined feed
			EdmNavigationProperty navProp = ees.type.getNavigationProperty(name);
			
			// [
			ensureStartArray(jsr.nextEvent());
			
			Feed feed = parseFeed(metadata.getEdmEntitySet(navProp.toRole.type), jsr);

			rt.entities = Enumerable.create(feed.getEntries())
					.cast(JsonEntry.class)
					.select(new Func1<JsonEntry, OEntity>() {
						@Override
						public OEntity apply(JsonEntry input) {
							return input.getEntity();
						}
					}).toList();
			
			ensureEndProperty(jsr.nextEvent());
			ensureEndObject(jsr.nextEvent());

		} else if (METADATA_PROPERTY.equals(event.asStartProperty().getName())) {
			//	inlined entity or link starting with meta data
			EdmNavigationProperty navProp = ees.type.getNavigationProperty(name);
			JsonEntryMetaData jemd = parseMetadata(jsr);
			JsonEntry refentry = parseEntry(jemd, metadata.getEdmEntitySet(navProp.toRole.type), jsr);
			
			//	if we are parsing a request, the links to existing 
			//  entities are represented as the inline representation 
			//  of an entity with only the __metadata and no properties
			if (isResponse) {
				rt.entity = refentry.getEntity();
			} else {
				boolean isInlined = !refentry.properties.isEmpty() || !refentry.links.isEmpty();
				if (isInlined) {
					rt.entity = refentry.getEntity();
				} else {
					rt.uri = jemd.uri;
				}
			}				
		} else if (event.isStartProperty()){
			//	inlined entity
			
			EdmNavigationProperty navProp = ees.type.getNavigationProperty(name);
			ees = metadata.getEdmEntitySet(navProp.toRole.type);
			
			JsonEntry refentry = new JsonEntry();
			refentry.properties = new ArrayList<OProperty<?>>();
			refentry.links = new ArrayList<OLink>();
			do {
				addProperty(refentry, ees, event.asStartProperty().getName(), jsr);
				event = jsr.nextEvent();
			} while (!event.isEndObject());
			rt.entity = refentry.getEntity();
			
		} else {
			throw new IllegalArgumentException("What's that?");
		}

		ensureEndProperty(jsr.nextEvent());

		return rt;
	}

	protected void ensureNext(JsonStreamReader jsr) {
		if (!jsr.hasNext()) {
			throw new IllegalArgumentException("no valid JSON format exepected at least one more event");
		}
	}

	protected void ensureStartProperty(JsonEvent event) {
		if (!event.isStartProperty()) {
			throw new IllegalArgumentException("no valid OData JSON format (expected StartProperty got " + event+ ")");
		}
	}
	
	protected void ensureStartProperty(JsonEvent event, String name) {
		if (!event.isStartProperty()
			&& !name.equals(event.asStartProperty().getName())) {
			throw new IllegalArgumentException("no valid OData JSON format (expected StartProperty " + name + " got " + event + ")");
		}
	}

	protected void ensureEndProperty(JsonEvent event) {
		if (!event.isEndProperty()) {
			throw new IllegalArgumentException("no valid OData JSON format (expected EndProperty got " + event+ ")");
		}
	}

	protected void ensureStartObject(JsonEvent event) {
		if (!event.isStartObject()) {
			throw new IllegalArgumentException("no valid OData JSON format expected StartObject got " + event+ ")");
		}
	}

	protected void ensureEndObject(JsonEvent event) {
		if (!event.isEndObject()) {
			throw new IllegalArgumentException("no valid OData JSON format expected EndObject got " + event+ ")");
		}
	}

	protected void ensureStartArray(JsonEvent event) {
		if (!event.isStartArray()) {
			throw new IllegalArgumentException("no valid OData JSON format expected StartArray got " + event+ ")");
		}
	}


}
