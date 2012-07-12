package org.odata4j.core;

public interface OCountRequest {

  OCountRequest setEntitySetName(String entitySetName);

  OCountRequest top(int top);

  int execute();
}
