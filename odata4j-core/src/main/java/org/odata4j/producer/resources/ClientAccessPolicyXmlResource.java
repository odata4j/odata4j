package org.odata4j.producer.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("clientaccesspolicy.xml")
public class ClientAccessPolicyXmlResource {

    @GET
    @Produces("text/xml")
    public String getClientAccessPolicyXml() {

        String content = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + "<access-policy>" + "  <cross-domain-access>" + "    <policy>" + "      <allow-from http-request-headers=\"*\">" + "        <domain uri=\"*\" /> " + "      </allow-from>" + "      <grant-to>" + "        <resource path=\"/\" include-subpaths=\"true\" /> " + "      </grant-to>" + "    </policy>" + "  </cross-domain-access>" + "</access-policy>";
        return content;
    }
}
