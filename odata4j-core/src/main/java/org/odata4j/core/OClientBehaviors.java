package org.odata4j.core;

import org.odata4j.consumer.behaviors.AllowSelfSignedCertsBehavior;
import org.odata4j.consumer.behaviors.AzureTableBehavior;
import org.odata4j.consumer.behaviors.BasicAuthenticationBehavior;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;

/**
 * A static factory to create built-in {@link OClientBehavior} instances.
 */
public class OClientBehaviors {

	private OClientBehaviors() {}
	
	/**
	 * Creates a behavior that does http basic authentication.
	 * 
	 * @param user  the basic auth user
	 * @param password  the basic auth password
	 * @return a behavior that does http basic authentication
	 */
	public static OClientBehavior basicAuth(String user, String password){
		return new BasicAuthenticationBehavior(user, password);
	}
	
	/**
	 * Creates a behavior that allows for https services with self-signed certificates.
	 * 
	 * @return a behavior that allows for https services with self-signed certificates
	 */
	public static OClientBehavior allowSelfSignedCerts(){
		return AllowSelfSignedCertsBehavior.INSTANCE;
	}
	
	/**
	 * Creates a behavior that signs requests properly for the Azure Table Storage service.
	 * 
	 * @param account  azure account key
	 * @param key  azure secret key
	 * @return a behavior that signs requests properly for the Azure Table Storage service
	 */
	public static OClientBehavior azureTables(String account, String key){
		return new AzureTableBehavior(account, key);
	}
	
	/**
	 * Creates a behavior that tunnels specific http request methods through POST.
	 * 
	 * @param methodsToTunnel  the methods to tunnel.  e.g. <code>MERGE</code>
	 * @return a behavior that tunnels specific http request methods through POST
	 */
	public static OClientBehavior methodTunneling(String... methodsToTunnel){
		return new MethodTunnelingBehavior(methodsToTunnel);
	}
}
