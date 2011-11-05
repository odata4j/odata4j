package org.odata4j.producer.resources;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.odata4j.core.ODataConstants;
import org.odata4j.core.ODataVersion;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OFunctionParameters;
import org.odata4j.edm.EdmFunctionImport;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.format.FormatWriter;
import org.odata4j.format.FormatWriterFactory;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.CollectionResponse;
import org.odata4j.producer.ComplexObjectResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.exceptions.NotImplementedException;

/**
 * Handles function calls.  Unfortunately the OData URI scheme makes it
 * impossible to differentiate a function call "resource" from an EntitySet.
 * So, we hack:  EntitiesRequestResource delegates to this class if it determines
 * that a function is being referenced.
 *
 * TODO:
 *  - function parameter facets (required, value ranges, etc).  For now, all
 *    validation is up to the function handler in the producer.
 *  - non-simple function parameter types
 *  - make sure this works for GET and POST
 */
public class FunctionResource extends BaseResource {

  /**
   * Handles function call resource access by gathering function call info from
   * the request and delegating to the producer.
   */
  @SuppressWarnings("rawtypes")
  public static Response callFunction(HttpHeaders httpHeaders,
      UriInfo uriInfo,
      ODataProducer producer,
      String functionName,
      String format,
      String callback,
      String skipToken) throws Exception {

    Map<String, String> opts = OptionsQueryParser.parseCustomOptions(uriInfo);

    // do we have this function?
    EdmFunctionImport function = producer.getMetadata().findEdmFunctionImport(functionName);
    if (null == function) {
      return Response.status(Status.NOT_FOUND).build();
    }

    final BaseResponse response = producer.callFunction(
        function, getFunctionParameters(function, opts), null);

    if (response == null) {
      return Response.status(Status.NOT_FOUND).build();
    }

    ODataVersion version = ODataConstants.DATA_SERVICE_VERSION;

    StringWriter sw = new StringWriter();
    FormatWriter<?> fwBase;

    // hmmh...we are missing an abstraction somewhere..
    if (response instanceof ComplexObjectResponse) {
      FormatWriter<ComplexObjectResponse> fw =
          FormatWriterFactory.getFormatWriter(
              ComplexObjectResponse.class,
              httpHeaders.getAcceptableMediaTypes(),
              format,
              callback);

      fw.write(uriInfo, sw, (ComplexObjectResponse) response);
      fwBase = fw;
    } else if (response instanceof CollectionResponse) {
      FormatWriter<CollectionResponse> fw =
          FormatWriterFactory.getFormatWriter(
              CollectionResponse.class,
              httpHeaders.getAcceptableMediaTypes(),
              format,
              callback);

      fw.write(uriInfo, sw, (CollectionResponse<?>) response);
      fwBase = fw;
    } else {
      // TODO add in other response types.
      throw new NotImplementedException("Unknown BaseResponse type: " + response.getClass().getName());
    }

    String entity = sw.toString();
    return Response.ok(entity, fwBase.getContentType())
        .header(ODataConstants.Headers.DATA_SERVICE_VERSION, version.asString)
        .build();
  }

  /**
   * Takes a Map<String,String> filled with the request URIs custom parameters and
   * turns them into a map of strongly-typed OFunctionParameter objects.
   *
   * @param function  the function being called
   * @param opts  request URI custom parameters
   */
  private static Map<String, OFunctionParameter> getFunctionParameters(EdmFunctionImport function, Map<String, String> opts) {
    Map<String, OFunctionParameter> m = new HashMap<String, OFunctionParameter>();
    for (EdmFunctionParameter p : function.getParameters()) {
      String val = opts.get(p.getName());
      m.put(p.getName(), null == val ? null : OFunctionParameters.parse(p.getName(), p.getType(), val));
    }
    return m;
  }
}