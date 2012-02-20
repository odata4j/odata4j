package org.odata4j.producer.jdbc.commandproducer;

import org.odata4j.core.OEntityKey;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.QueryInfo;

public interface GetNavPropertyCountCommandContext extends ProducerCommandContext<CountResponse> {

  String getEntitySetName();

  OEntityKey getEntityKey();

  String getNavProp();

  QueryInfo getQueryInfo();

}
