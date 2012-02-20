package org.odata4j.producer.jdbc.commandproducer;

import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.QueryInfo;

public interface GetEntitiesCommandContext extends ProducerCommandContext<EntitiesResponse> {

  String getEntitySetName();

  QueryInfo getQueryInfo();

}
