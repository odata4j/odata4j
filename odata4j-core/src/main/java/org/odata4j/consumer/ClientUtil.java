package org.odata4j.consumer;

import java.lang.reflect.Field;
import java.util.Set;

import javax.ws.rs.ext.RuntimeDelegate;

import org.odata4j.core.OClientBehavior;
import org.odata4j.internal.PlatformUtil;
import org.odata4j.internal.StringProvider2;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.impl.provider.header.MediaTypeProvider;
import com.sun.jersey.core.spi.factory.AbstractRuntimeDelegate;
import com.sun.jersey.spi.HeaderDelegateProvider;

class ClientUtil {

  static {
    if (PlatformUtil.runningOnAndroid())
      androidJerseyClientHack();
  }

  @SuppressWarnings("unchecked")
  private static void androidJerseyClientHack() {
    try {
      RuntimeDelegate rd = RuntimeDelegate.getInstance();
      Field f = AbstractRuntimeDelegate.class.getDeclaredField("hps");
      f.setAccessible(true);
      Set<HeaderDelegateProvider<?>> hps = (Set<HeaderDelegateProvider<?>>) f.get(rd);
      hps.clear();
      hps.add(new MediaTypeProvider());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Client newClient(OClientBehavior[] behaviors) {
    DefaultClientConfig cc = new DefaultClientConfig();
    cc.getSingletons().add(new StringProvider2());
    if (behaviors != null)
      for (OClientBehavior behavior : behaviors)
        behavior.modify(cc);
    Client client = Client.create(cc);
    return client;
  }

}
