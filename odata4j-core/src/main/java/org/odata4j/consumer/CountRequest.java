package org.odata4j.consumer;

/**
 * Client request type for $count.  
 * 
 */
public interface CountRequest {

  int execute();

  CountRequest top(int top);

  void setEntitySetName(String entitySetName);

}
