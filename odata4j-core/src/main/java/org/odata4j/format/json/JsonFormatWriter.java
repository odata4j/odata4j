package org.odata4j.format.json;

import java.io.Writer;
import java.util.List;
import java.util.Locale;

import org.core4j.Enumerable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OPredicates;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatWriter;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

import com.sun.jersey.api.core.ExtendedUriInfo;

/** Write content to an HTTP stream in JSON format.
 * 
 * This class is abstract because it delegates the strategy pattern of writing
 * actual content elements to its (various) subclasses.
 *
 * Each element in the array to be written can be wrapped in a function call
 * on the JavaScript side by specifying the name of a function to call to the
 * constructor.
 * 
 * @param <T> the type of the content elements to be written to the stream.
 */
public abstract class JsonFormatWriter<T> implements FormatWriter<T> {

	private final String jsonpCallback;

	/** Create a new JSON writer.
	 * 
	 * @param jsonpCallback a function to call on the javascript side to act
	 * on the data provided in the content.
	 */
	public JsonFormatWriter(String jsonpCallback) {
		this.jsonpCallback = jsonpCallback;
	}

	/** A strategy method to actually write content objects
	 * @param uriInfo the base URI that indicates where in the schema we are
	 * @param jw the JSON writer object
	 * @param target the content value to be written
	 */
	abstract protected void writeContent(ExtendedUriInfo uriInfo, JsonWriter jw, T target);

	@Override
	public String getContentType() {
		return jsonpCallback == null
				? ODataConstants.APPLICATION_JAVASCRIPT_CHARSET_UTF8
				: ODataConstants.TEXT_JAVASCRIPT_CHARSET_UTF8;
	}

	protected String getJsonpCallback() {
		return jsonpCallback;
	}

	@Override
	public void write(ExtendedUriInfo uriInfo, Writer w, T target) {

		JsonWriter jw = new JsonWriter(w);
		if (getJsonpCallback() != null) {
			jw.startCallback(getJsonpCallback());
		}

		jw.startObject();
		{
			jw.writeName("d");
			writeContent(uriInfo, jw, target);
		}
		jw.endObject();

		if (getJsonpCallback() != null) {
			jw.endCallback();
		}

	}

	protected void writeProperty(JsonWriter jw, OProperty<?> prop){
		jw.writeName(prop.getName());
		Object pvalue = prop.getValue();
		if (pvalue == null) {
			jw.writeNull();
		} else if (prop.getType().equals(EdmType.BINARY)) {
			jw.writeString(Base64.encodeBase64String((byte[]) pvalue));
		} else if (prop.getType().equals(EdmType.BOOLEAN)) {
			jw.writeBoolean((Boolean) pvalue);
		} else if (prop.getType().equals(EdmType.BYTE)) {
			jw.writeString(Hex.encodeHexString(new byte[] { (Byte) pvalue }));
		} else if (prop.getType().equals(EdmType.DATETIME)) {
			LocalDateTime ldt = (LocalDateTime) pvalue;
			long millis = ldt.toDateTime(DateTimeZone.UTC).getMillis();
			String date = "\"\\/Date(" + millis + ")\\/\"";
			jw.writeRaw(date);
		} else if (prop.getType().equals(EdmType.DECIMAL)) {
			// jw.writeString("decimal'" + (BigDecimal) pvalue + "'");
			jw.writeString(String.format(Locale.ENGLISH, "%1$.4f", pvalue));
		} else if (prop.getType().equals(EdmType.DOUBLE)) {
			// jw.writeString(pvalue.toString());
			jw.writeString(String.format(Locale.ENGLISH, "%1$.4f", pvalue));
		} else if (prop.getType().equals(EdmType.GUID)) {
			jw.writeString("guid'" + (Guid) pvalue + "'");
		} else if (prop.getType().equals(EdmType.INT16)) {
			jw.writeNumber((Short) pvalue);
		} else if (prop.getType().equals(EdmType.INT32)) {
			jw.writeNumber((Integer) pvalue);
		} else if (prop.getType().equals(EdmType.INT64)) {
			jw.writeString(pvalue.toString());
		} else if (prop.getType().equals(EdmType.SINGLE)) {
			jw.writeNumber((Float) pvalue);
		} else if (prop.getType().equals(EdmType.TIME)) {
			LocalTime ldt = (LocalTime) pvalue;
			jw.writeString("time'" + ldt + "'");
		} else if (prop.getType().equals(EdmType.DATETIMEOFFSET)) {
			jw.writeString("datetimeoffset'" + InternalUtil.toString((DateTime) pvalue) + "'");
		} else {
			String value = pvalue.toString();
			jw.writeString(value);
		}
	}
	
	protected void writeOEntity(ExtendedUriInfo uriInfo, JsonWriter jw, OEntity oe, EdmEntitySet ees, boolean isResponse) {

		jw.startObject();
		{
			String baseUri = null;

			if (isResponse && ees != null) {
				baseUri = uriInfo.getBaseUri().toString();

				jw.writeName("__metadata");
				jw.startObject();
				{
					String absId = baseUri + InternalUtil.getEntityRelId(oe);
					jw.writeName("uri");
					jw.writeString(absId);
					jw.writeSeparator();
					jw.writeName("type");
					jw.writeString(ees.type.getFQNamespaceName());
				}
				jw.endObject();
				jw.writeSeparator();
			}

			writeOProperties(jw, oe.getProperties());

			if (isResponse) {
    			if (ees != null) {
    				for (final EdmNavigationProperty np : ees.type.navigationProperties) {
    					if (!np.selected) {
    						continue;
    					}
    
    					jw.writeSeparator();
    
    					// check whether we have to write inlined entities
    					OLink linkToInline = oe.getLinks() != null
    							? Enumerable.create(oe.getLinks()).firstOrNull(OPredicates.linkTitleEquals(np.name))
    							: null;
    
    					jw.writeName(np.name);
    					if (linkToInline == null 
    							|| ( (linkToInline instanceof ORelatedEntitiesLinkInline) && 
    									((ORelatedEntitiesLinkInline) linkToInline).getRelatedEntities() == null ) 
    							|| ( (linkToInline instanceof ORelatedEntityLinkInline) &&
    									((ORelatedEntityLinkInline) linkToInline).getRelatedEntity() == null )
    						)  {
    						jw.startObject();
    						{
    							jw.writeName("__deferred");
    							jw.startObject();
    							{
    								String absId = baseUri + InternalUtil.getEntityRelId(oe);
    								jw.writeName("uri");
    								jw.writeString(absId + "/" + np.name);
    							}
    							jw.endObject();
    						}
    						jw.endObject();
    					} else {
    						if (linkToInline instanceof ORelatedEntitiesLinkInline) {
    							jw.startObject();
    							{
    								jw.writeName("results");
    
    								jw.startArray();
    								{
    									boolean isFirstInlinedEntity = true;
    									for (OEntity re : ((ORelatedEntitiesLinkInline) linkToInline).getRelatedEntities()) {
    
    										if (isFirstInlinedEntity) {
    											isFirstInlinedEntity = false;
    										} else {
    											jw.writeSeparator();
    										}
    
    										writeOEntity(uriInfo, jw, re, re.getEntitySet(), isResponse);
    									}
    
    								}
    								jw.endArray();
    							}
    							jw.endObject();
    						} else if (linkToInline instanceof ORelatedEntityLinkInline) {
    							OEntity re = ((ORelatedEntityLinkInline) linkToInline).getRelatedEntity();
    							writeOEntity(uriInfo, jw, re, re.getEntitySet(), isResponse);
    						} else
    							throw new RuntimeException("Unknown OLink type " + linkToInline.getClass());
    					}
    				}
    			}
			} else {
				for (OLink link : oe.getLinks()) {
					jw.writeSeparator();

					jw.writeName(link.getTitle());

					if (link instanceof ORelatedEntitiesLink) {

						jw.startArray();
						
						if (link instanceof ORelatedEntitiesLinkInline) {
							List<OEntity> relEntities = ((ORelatedEntitiesLinkInline)link).getRelatedEntities();
							for (int i = 0, size = relEntities.size(); i < size; i++) {
								OEntity relEntity = relEntities.get(i);
								writeOEntity(uriInfo, jw, relEntity, relEntity.getEntitySet(), isResponse);
								if (i < size - 1) {
									jw.writeSeparator();
								}
							}
						} else {
							writeLinkInline(jw, link);
						}
						
						jw.endArray();
					} else {
						if (link instanceof ORelatedEntityLinkInline) {
							OEntity relEntity = ((ORelatedEntityLinkInline)link).getRelatedEntity();
							writeOEntity(uriInfo, jw, relEntity, relEntity.getEntitySet(), isResponse);
						} else {
							writeLinkInline(jw, link);
						}
					}
				}
			}
		}

		jw.endObject();
	}

	private void writeLinkInline(JsonWriter jw, OLink link) {
		jw.startObject();							
		jw.writeName("__metadata");
		jw.startObject();
		{
			jw.writeName("uri");
			jw.writeString(link.getHref());
		}
		jw.endObject();
		jw.endObject();
	}

	protected void writeOProperties(JsonWriter jw, List<OProperty<?>> properties) {
		boolean isFirst = true;
		for (OProperty<?> prop : properties) {
			if (isFirst) {
				isFirst = false;
			} else {
				jw.writeSeparator();
			}

			writeProperty(jw,prop);
		}
	}
}
