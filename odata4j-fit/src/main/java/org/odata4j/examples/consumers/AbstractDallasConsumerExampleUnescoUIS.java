package org.odata4j.examples.consumers;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OFuncs;
import org.odata4j.examples.AbstractCredentialsExample;
import org.odata4j.examples.ConsumerSupport;
import org.odata4j.examples.ODataEndpoints;
import org.odata4j.examples.RunSupport;

public abstract class AbstractDallasConsumerExampleUnescoUIS extends AbstractCredentialsExample implements ConsumerSupport, RunSupport {

  @Override
  public void run(String[] args) {

    String[] dallasCreds = args.length > 0 ? args : System.getenv("DALLAS").split(":");
    this.setLoginPassword(dallasCreds[0]);
    this.setLoginName(dallasCreds[1]);    

    ODataConsumer c = this.create(ODataEndpoints.DALLAS_CTP2_UNESCO_UIS);

    // Public expenditure on education as % of GDP [XGDP_FSGOV]
    for (OEntity entity : c.getEntities("UNESCO/XGDP_FSGOV").execute()
        .orderBy(OFuncs.entityPropertyValue("observationValue", Double.class)) // client-side ordering, server-side ordering not supported on dallas
    ) {
      report("Public expenditure on education as pct of GDP: %s %s, %.4f",
          entity.getProperty("referenceArea").getValue(),
          entity.getProperty("timePeriod").getValue(),
          entity.getProperty("observationValue").getValue());
    }

    // Number of national feature films produced [C_F_220006]
    for (OEntity entity : c.getEntities("UNESCO/C_F_220006").execute()
        .orderBy(OFuncs.entityPropertyValue("observationValue", Double.class))) {
      report("Number of national feature films produced: %s %s, %.0f",
          entity.getProperty("referenceArea").getValue(),
          entity.getProperty("timePeriod").getValue(),
          entity.getProperty("observationValue").getValue());
    }

  }

}

