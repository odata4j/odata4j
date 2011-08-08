package org.odata4j.core;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.LocalDateTime;

/**
 * A query that is a function call request.
 */
public interface OFunctionRequest<T> extends OQueryRequest<T> {

    /**
     * Add a parameter to a function call request
     * @param name - parameter name
     * @param value - parameter value
     * @return - this
     */
    public OFunctionRequest<T> parameter(String name, OObject value);
    
    // type-specific parameters:
    public OFunctionRequest<T> pBoolean(String name, boolean value);
    public OFunctionRequest<T> pByte(String name, byte value);
    public OFunctionRequest<T> pDateTime(String name, Calendar value);
    public OFunctionRequest<T> pDateTime(String name, Date value);
    public OFunctionRequest<T> pDateTime(String name, LocalDateTime value);
    public OFunctionRequest<T> pDecimal(String name, BigDecimal value);
    public OFunctionRequest<T> pDouble(String name, double value);
    public OFunctionRequest<T> pGuid(String name, Guid value);
    public OFunctionRequest<T> pInt16(String name, short value);
    public OFunctionRequest<T> pInt32(String name, int value);
    public OFunctionRequest<T> pInt64(String name, long value);
    public OFunctionRequest<T> pSingle(String name, float value);
    public OFunctionRequest<T> pTime(String name, Calendar value);
    public OFunctionRequest<T> pTime(String name, Date value);
    public OFunctionRequest<T> pTime(String name, LocalDateTime value);
}
