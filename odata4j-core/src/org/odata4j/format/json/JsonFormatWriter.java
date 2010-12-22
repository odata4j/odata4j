package org.odata4j.format.json;

import java.io.Writer;
import java.util.Locale;

import org.core4j.Enumerable;
import org.core4j.Predicate1;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.core.ODataConstants;
import org.odata4j.core.OEntity;
import org.odata4j.core.OLink;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLink;
import org.odata4j.core.ORelatedEntityLink;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmNavigationProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.format.FormatWriter;
import org.odata4j.internal.InternalUtil;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;
import org.odata4j.repack.org.apache.commons.codec.binary.Hex;

import com.sun.jersey.api.core.ExtendedUriInfo;

public abstract class JsonFormatWriter<T> implements FormatWriter<T> {

	private final String jsonpCallback;

	public JsonFormatWriter(String jsonpCallback) {
		this.jsonpCallback = jsonpCallback;
	}

	abstract protected void writeContent(ExtendedUriInfo uriInfo,
			JsonWriter jw, T target);

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

	protected void writeOEntity(ExtendedUriInfo uriInfo, JsonWriter jw,
			OEntity oe, EdmEntitySet ees) {

		String baseUri = uriInfo.getBaseUri().toString();

		jw.startObject();
		{
			if (ees != null) {
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

			boolean isFirst = true;
			for (OProperty<?> prop : oe.getProperties()) {
				if (isFirst) {
					isFirst = false;
				} else {
					jw.writeSeparator();
				}

				jw.writeName(prop.getName());
				Object pvalue = prop.getValue();
				if (pvalue == null) {
					jw.writeNull();
				} else if (prop.getType().equals(EdmType.BINARY)) {
					jw.writeString(Base64.encodeBase64String((byte[]) pvalue));
				} else if (prop.getType().equals(EdmType.BOOLEAN)) {
					jw.writeBoolean((Boolean) pvalue);
				} else if (prop.getType().equals(EdmType.BYTE)) {
					jw.writeString(Hex
							.encodeHexString(new byte[] { (Byte) pvalue }));
				} else if (prop.getType().equals(EdmType.DATETIME)) {
					LocalDateTime ldt = (LocalDateTime) pvalue;
					long millis = ldt.toDateTime(DateTimeZone.UTC).getMillis();
					String date = "\"\\/Date(" + millis + ")\\/\"";
					jw.writeRaw(date);
				} else if (prop.getType().equals(EdmType.DECIMAL)) {
					// jw.writeString("decimal'" + (BigDecimal) pvalue + "'");
					jw.writeString(String.format(Locale.ENGLISH, "%1$.4f",
							pvalue));
				} else if (prop.getType().equals(EdmType.DOUBLE)) {
					// jw.writeString(pvalue.toString());
					jw.writeString(String.format(Locale.ENGLISH, "%1$.4f",
							pvalue));
				} else if (prop.getType().equals(EdmType.GUID)) {
					jw.writeString("guid'" + (Guid) pvalue + "'");
				} else if (prop.getType().equals(EdmType.INT16)) {
					jw.writeNumber((Short) pvalue);
				} else if (prop.getType().equals(EdmType.INT32)) {
					jw.writeNumber((Integer) pvalue);
				} else if (prop.getType().equals(EdmType.INT64)) {
					jw.writeString(pvalue.toString());
				} else if (prop.getType().equals(EdmType.SINGLE)) {
					jw.writeString(pvalue.toString() + "f");
				} else if (prop.getType().equals(EdmType.TIME)) {
					LocalTime ldt = (LocalTime) pvalue;
					jw.writeString("time'" + ldt + "'");
				} else if (prop.getType().equals(EdmType.DATETIMEOFFSET)) {
					jw.writeString("datetimeoffset'"
							+ InternalUtil.toString((DateTime) pvalue) + "'");
				} else {
					String value = pvalue.toString();
					jw.writeString(value);
				}
			}

			if (ees != null) {
				for (final EdmNavigationProperty np : ees.type.navigationProperties) {
					if (!np.selected) {
						continue;
					}

					jw.writeSeparator();

					// check whether we have to write inlined entities
					OLink linkToInline = oe.getLinks() != null
							? Enumerable.create(oe.getLinks()).firstOrNull(
									new Predicate1<OLink>() {
										@Override
										public boolean apply(OLink input) {
											return np.name.equals(input
													.getTitle());
										}
									})
							: null;

					jw.writeName(np.name);
					if (linkToInline == null) {
						jw.startObject();
						{
							jw.writeName("__deferred");
							jw.startObject();
							{
								String absId = baseUri
										+ InternalUtil.getEntityRelId(oe);
								jw.writeName("uri");
								jw.writeString(absId + "/" + np.name);
							}
							jw.endObject();
						}
						jw.endObject();
					} else {
						if (linkToInline instanceof ORelatedEntitiesLink) {
							jw.startObject();
							{
								jw.writeName("results");

								jw.startArray();
								{
									boolean isFirstInlinedEntity = true;
									for (OEntity re : ((ORelatedEntitiesLink) linkToInline)
											.getRelatedEntities()) {

										if (isFirstInlinedEntity) {
											isFirstInlinedEntity = false;
										} else {
											jw.writeSeparator();
										}

										writeOEntity(uriInfo, jw, re,
												re.getEntitySet());
									}

								}
								jw.endArray();
							}
							jw.endObject();
						} else if (linkToInline instanceof ORelatedEntityLink) {
							OEntity re = ((ORelatedEntityLink) linkToInline)
									.getRelatedEntity();
							writeOEntity(uriInfo, jw, re, re.getEntitySet());
						} else
							throw new RuntimeException("Unknown OLink type "
									+ linkToInline.getClass());
					}
				}
			}
		}

		jw.endObject();
	}
}
