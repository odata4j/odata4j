package org.odata4j.test.core;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.core.OCollection;
import org.odata4j.core.OCollections;
import org.odata4j.core.OObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmSimpleType;

public class OCollectionsTest {

  private static final String VALUE = "value";
  private static final String HEX_VALUE = "0x76616c7565";

  @Test
  public void stringCollectionToStringTest() {
    OCollection<OObject> collection = OCollections.newBuilder(EdmSimpleType.STRING).add(OSimpleObjects.create(VALUE, EdmSimpleType.STRING)).build();
    String toString = collection.toString();
    Assert.assertTrue(toString.contains(VALUE));
  }

  @Test
  public void binaryCollectionToStringTest() {
    OCollection<OObject> collection = OCollections.newBuilder(EdmSimpleType.BINARY).add(OSimpleObjects.create(VALUE.getBytes(), EdmSimpleType.BINARY)).build();
    String toString = collection.toString();
    Assert.assertTrue(toString.contains(HEX_VALUE));
  }
}
