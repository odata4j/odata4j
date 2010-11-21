package org.odata4j.test.expression;

import junit.framework.Assert;

import org.junit.Test;
import org.odata4j.internal.TypeConverter;

public class TypeConverterTest {

    @Test
    public void testTypeConverter(){
        
        Assert.assertNull(TypeConverter.convert(null, Object.class));
        Assert.assertEquals((byte)16,(Object)TypeConverter.convert(16, Byte.class));
        Assert.assertEquals(16,(Object)TypeConverter.convert(16, Integer.class));
        
        
    }
}
