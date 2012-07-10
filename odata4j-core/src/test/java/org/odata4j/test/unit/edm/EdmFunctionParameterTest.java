package org.odata4j.test.unit.edm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.odata4j.edm.EdmFunctionParameter;
import org.odata4j.edm.EdmSimpleType;

public class EdmFunctionParameterTest {
  private static final Boolean NULLABLE = true;

  @Test
  public void edmFunctionParameterNullable() {
    EdmFunctionParameter.Builder builder = EdmFunctionParameter.newBuilder();
    EdmFunctionParameter parameter = builder.setNullable(NULLABLE).setType(EdmSimpleType.STRING).build();
    assertEquals(NULLABLE, parameter.isNullable());
  }
}