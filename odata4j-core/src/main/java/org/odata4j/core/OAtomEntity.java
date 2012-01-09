package org.odata4j.core;

/**
 * Interface providing information for ATOM serialization.
 * 
 * <p>Enables feed customization of ATOM entries for certain properties instead of using &lt;m:Properties&gt;
 */
public interface OAtomEntity {

  String getAtomEntityTitle();

  String getAtomEntitySummary();

  String getAtomEntityAuthor();

  String getAtomEntityUpdated();
  
}
