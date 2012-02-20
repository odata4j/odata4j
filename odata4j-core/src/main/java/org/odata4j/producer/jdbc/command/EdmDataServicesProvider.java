package org.odata4j.producer.jdbc.command;

import org.odata4j.edm.EdmDataServices;

public interface EdmDataServicesProvider {

  EdmDataServices getMetadata();

}
