package org.odata4j.core;

import org.odata4j.consumer.behaviors.AllowSelfSignedCertsBehavior;
import org.odata4j.consumer.behaviors.AzureTableBehavior;
import org.odata4j.consumer.behaviors.BasicAuthenticationBehavior;
import org.odata4j.consumer.behaviors.MethodTunnelingBehavior;

public class OClientBehaviors {

	public static OClientBehavior basicAuth(String user, String password){
		return new BasicAuthenticationBehavior(user, password);
	}
	
	public static OClientBehavior allowSelfSignedCerts(){
		return AllowSelfSignedCertsBehavior.INSTANCE;
	}
	
	public static OClientBehavior azureTables(String account, String key){
		return new AzureTableBehavior(account, key);
	}
	
	public static OClientBehavior methodTunneling(String... methodsToTunnel){
		return new MethodTunnelingBehavior(methodsToTunnel);
	}
}
