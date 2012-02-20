package org.odata4j.producer.jdbc.commandproducer;

import org.odata4j.core.OEntityKey;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.QueryInfo;

public interface GetEntityCommandContext extends ProducerCommandContext<EntityResponse> {

  String getEntitySetName();

  OEntityKey getEntityKey();

  QueryInfo getQueryInfo();

}
