package org.odata4j.test.core;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.core.OSimpleObject;
import org.odata4j.core.OSimpleObjects;
import org.odata4j.edm.EdmSimpleType;

public class OSimpleObjectsTest {

  private static final String VALUE = "value";
  private static final String HEX_VALUE = "0x76616c7565";

  @Test
  public void stringToStringTest() {
    OSimpleObject<String> simpleObject = OSimpleObjects.create(EdmSimpleType.STRING, VALUE);
    String toString = simpleObject.toString();
    Assert.assertTrue(toString.contains(VALUE));
  }

  @Test
  public void binaryToStringTest() {
    OSimpleObject<byte[]> simpleObject = OSimpleObjects.create(EdmSimpleType.BINARY, VALUE.getBytes());
    String toString = simpleObject.toString();
    Assert.assertTrue(toString.contains(HEX_VALUE));
  }
}
