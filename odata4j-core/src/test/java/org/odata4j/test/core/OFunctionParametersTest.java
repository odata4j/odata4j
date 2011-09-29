package org.odata4j.test.core;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.core.OFunctionParameter;
import org.odata4j.core.OFunctionParameters;

public class OFunctionParametersTest {

  private static final String NAME = "name";
  private static final byte[] VALUE = "value".getBytes();
  private static final String HEX_VALUE = "0x76616c7565";

  @Test
  public void toStringTest() {
    OFunctionParameter functionParameter = OFunctionParameters.create(NAME, VALUE);
    String toString = functionParameter.toString();
    Assert.assertTrue(toString.contains(NAME));
    Assert.assertTrue(toString.contains(HEX_VALUE));
  }
}
