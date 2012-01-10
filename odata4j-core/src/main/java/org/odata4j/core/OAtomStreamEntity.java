package org.odata4j.core;

public interface OAtomStreamEntity extends OExtension<OEntity> {

  String getAtomEntityType();

  String getAtomEntitySource();

}