package org.odata4j.consumer.behaviors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OClientBehavior;
import org.odata4j.core.ODataConstants;
import org.odata4j.repack.org.apache.commons.codec.binary.Base64;

public class AzureTableBehavior implements OClientBehavior {

    private final String account;
    private final String key;

    public AzureTableBehavior(String account, String key) {
        this.account = account;
        this.key = key;
    }

    @Override
    public ODataClientRequest transform(ODataClientRequest request) {
        try {
            String utc = new DateTime(DateTimeZone.UTC).toString("EEE, dd MMM yyyy HH:mm:ss zzz");
            String date = utc.substring(0, utc.lastIndexOf(' ') + 1) + "GMT";

            // VERB + "\n" +
            // Content-MD5 + "\n" +
            // Content-Type + "\n" +
            // Date + "\n" +
            // CanonicalizedResource;

            String path = request.getUrl().substring(request.getUrl().indexOf('/', 8) + 1);
            String contentType = request.getHeaders().get("Content-Type");
            contentType = contentType == null ? "" : contentType;
            boolean isPut = request.getMethod().equals("PUT");
            boolean isPost = request.getMethod().equals("POST");
            boolean isDelete = request.getMethod().equals("DELETE");
            if (isPut || isPost || isDelete) {
                contentType = MediaType.APPLICATION_ATOM_XML;
            }

            String canonicalizedResource = "/" + account + "/" + path;
            String stringToSign = request.getMethod() + "\n\n" + contentType + "\n" + date + "\n" + canonicalizedResource;

            if (ODataConsumer.DUMP_REQUEST_HEADERS)
                System.out.println("stringToSign: " + stringToSign);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(base64Decode(key), mac.getAlgorithm()));
            mac.update(stringToSign.getBytes("utf8"));
            byte[] sigBytes = mac.doFinal();

            String sig = base64Encode(sigBytes);
            String auth = "SharedKey " + account + ":" + sig;

            if (ODataConsumer.DUMP_REQUEST_HEADERS)
                System.out.println("auth: " + auth);

            request = request.header("x-ms-version", "2009-09-19").header("x-ms-date", date).header("Authorization", auth).header("DataServiceVersion", "1.0;NetFx").header("MaxDataServiceVersion", "1.0;NetFx");

            if (isPut || isDelete || (isPost && request.getHeaders().containsKey(ODataConstants.Headers.X_HTTP_METHOD)))
                request = request.header("If-Match", "*"); // azure tables require for put,delete,merge

            if (isDelete) {
                request = request.header("Content-Type", MediaType.APPLICATION_ATOM_XML);
            }

            return request;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String base64Encode(byte[] value) {
        return Base64.encodeBase64String(value).trim();
    }

    private static byte[] base64Decode(String value) {
        return Base64.decodeBase64(value);
    }
}