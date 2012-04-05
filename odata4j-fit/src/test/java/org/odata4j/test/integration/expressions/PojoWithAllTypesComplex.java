package org.odata4j.test.integration.expressions;

import java.math.BigDecimal;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.odata4j.core.Guid;
import org.odata4j.core.UnsignedByte;

/**
 *
 */
public class PojoWithAllTypesComplex extends PojoWithAllTypes {

    private final PojoWithAllTypes complexType;

    public PojoWithAllTypesComplex(byte[] binary, boolean boolean_, UnsignedByte byte_, byte sbyte, LocalDateTime dateTime, BigDecimal decimal,
            double double_, Guid guid, short int16, int int32, long int64, float single, String string, LocalTime time, DateTime dateTimeOffset,
            PojoWithAllTypes complexType) {
        
        super(binary, boolean_, byte_, sbyte, dateTime, decimal, double_, guid,
                int16, int32, int64, single, string, time, dateTimeOffset);
        this.complexType = complexType;
    }

    public PojoWithAllTypes getComplexType() {
        return this.complexType;
    }
}
