package org.odata4j.consumer;

import org.odata4j.core.OClientBehavior;
import org.odata4j.core.OClientBehaviors;

/**
 * A static factory to create {@link ODataConsumer} instances preconfigured for specific services.
 */
public class ODataConsumers {

  private ODataConsumers() {}

  /**
   * Creates a new OData consumer for the Azure Table Storage service.
   * 
   * @param account  azure account key
   * @param key  azure secret key
   * @return a new OData consumer for the Azure Table Storage service
   * @see <a href="http://msdn.microsoft.com/en-us/library/dd179423.aspx">[msdn] Table Service API</a>
   */
  public static ODataConsumer azureTables(String account, String key) {
    String url = "http://" + account + ".table.core.windows.net/";

    return ODataConsumer.newBuilder(url).setClientBehaviors(OClientBehaviors.azureTables(account, key)).build();

  }

  /**
   * Creates a new OData consumer for the (now obsolete?) "dallas" service.
   * 
   * @param serviceRootUri  the service uri
   * @param accountKey  dallas account key
   * @param uniqueUserId  dallas user id
   * @return a new OData consumer for the (now obsolete?) "dallas" service
   */
  public static ODataConsumer dallas(String serviceRootUri, String accountKey, String uniqueUserId) {

    // CTP2
    //OClientBehavior dallasAuth = new DallasCtp2AuthenticationBehavior(accountKey, uniqueUserId);
    //OClientBehavior paging = new OldStylePagingBehavior(50, 1);
    //return ODataConsumer.create(serviceRootUri, dallasAuth, paging);

    // CTP3
    OClientBehavior basicAuth = OClientBehaviors.basicAuth("accountKey", accountKey);
    return ODataConsumer.newBuilder(serviceRootUri).setClientBehaviors(basicAuth).build();
  }

  /**
   * Creates a new OData consumer for the Windows Azure DataMarket service.
   * 
   * @param serviceRootUri  the service uri
   * @param accountKey  account key for basic authentication
   * @return a new OData consumer for the Windows Azure DataMarket service
   */
  public static ODataConsumer dataMarket(String serviceRootUri, String accountKey) {
    OClientBehavior basicAuth = OClientBehaviors.basicAuth("accountKey", accountKey);
    return ODataConsumer.newBuilder(serviceRootUri).setClientBehaviors(basicAuth).build();
  }

}
