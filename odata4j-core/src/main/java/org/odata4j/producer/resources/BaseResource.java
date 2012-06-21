package org.odata4j.producer.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

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

public abstract class BaseResource {

  protected OEntity getRequestEntity(HttpHeaders httpHeaders, UriInfo uriInfo, String payload, EdmDataServices metadata, String entitySetName, OEntityKey entityKey) throws ODataException {
    // TODO validation of MaxDataServiceVersion against DataServiceVersion
    // see spec [ms-odata] section 1.7

    ODataVersion version = InternalUtil.getDataServiceVersion(httpHeaders.getRequestHeaders().getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));
    return convertFromString(payload, httpHeaders.getMediaType(), version, metadata, entitySetName, entityKey);
  }

  private static OEntity convertFromString(String requestEntity, MediaType type, ODataVersion version, EdmDataServices metadata, String entitySetName, OEntityKey entityKey) throws NotAcceptableException {
    FormatParser<Entry> parser = FormatParserFactory.getParser(Entry.class, type,
        new Settings(version, metadata, entitySetName, entityKey, null, false));
    Entry entry = parser.parse(new StringReader(requestEntity));
    return entry.getEntity();
  }
  
  protected OEntity getRequestEntity(HttpHeaders httpHeaders, UriInfo uriInfo, InputStream payload, EdmDataServices metadata, String entitySetName, OEntityKey entityKey) throws ODataException, UnsupportedEncodingException {
    // TODO validation of MaxDataServiceVersion against DataServiceVersion
    // see spec [ms-odata] section 1.7

    ODataVersion version = InternalUtil.getDataServiceVersion(httpHeaders.getRequestHeaders().getFirst(ODataConstants.Headers.DATA_SERVICE_VERSION));
    FormatParser<Entry> parser = FormatParserFactory.getParser(Entry.class, httpHeaders.getMediaType(),
        new Settings(version, metadata, entitySetName, entityKey, null, false));
    
    String charset = httpHeaders.getMediaType().getParameters().get("charset");
    if (null == charset) {
      charset = "ISO-8859-1"; // from HTTP 1.1
    }
    
    Entry entry = parser.parse(new BufferedReader(
            new InputStreamReader(payload, charset)));
    
    return entry.getEntity();
  }

}