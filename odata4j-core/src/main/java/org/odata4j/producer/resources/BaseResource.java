package org.odata4j.producer.resources;

import java.io.StringReader;

import javax.ws.rs.core.MediaType;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.format.Entry;
import org.odata4j.format.FormatParser;
import org.odata4j.format.FormatParserFactory;
import org.odata4j.format.Settings;
import org.odata4j.internal.InternalUtil;
import org.odata4j.producer.exceptions.NotAcceptableException;
import org.odata4j.producer.exceptions.ODataException;

import com.sun.jersey.api.core.HttpRequestContext;

public abstract class BaseResource {

  protected OEntity getRequestEntity(HttpRequestContext request, EdmDataServices metadata, String entitySetName, OEntityKey entityKey) throws ODataException {
    String requestEntity = request.getEntity(String.class);

    // TODO validation of MaxDataServiceVersion against DataServiceVersion
    // see spec [ms-odata] section 1.7

    ODataVersion version = InternalUtil.getDataServiceVersion(request
        .getHeaderValue(ODataConstants.Headers.DATA_SERVICE_VERSION));
    return convertFromString(requestEntity, request.getMediaType(), version, metadata, entitySetName, entityKey);
  }

  private static OEntity convertFromString(String requestEntity, MediaType type, ODataVersion version, EdmDataServices metadata, String entitySetName, OEntityKey entityKey) throws NotAcceptableException {
    FormatParser<Entry> parser = FormatParserFactory.getParser(Entry.class, type,
        new Settings(version, metadata, entitySetName, entityKey, null, false));
    Entry entry = parser.parse(new StringReader(requestEntity));
    return entry.getEntity();
  }

}
