package org.odata4j.internal;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

public class TypeConverter {

    @SuppressWarnings("unchecked")
    public static <T> T convert(Object obj, Class<T> desiredClass) {
        if (obj == null) {
            return null;
        }
        Class<?> objClass = obj.getClass();
        if (objClass.equals(desiredClass)) {
            return (T) obj;
        }

        if ((desiredClass.equals(Byte.TYPE) || desiredClass.equals(Byte.class)) && Number.class.isAssignableFrom(objClass)) {
            return (T) (Object) ((Number) obj).byteValue();
        }
        if ((desiredClass.equals(Integer.TYPE) || desiredClass.equals(Integer.class)) && Number.class.isAssignableFrom(objClass)) {
            return (T) (Object) ((Number) obj).intValue();
        }
        if ((desiredClass.equals(Long.TYPE) || desiredClass.equals(Long.class)) && Number.class.isAssignableFrom(objClass)) {
            return (T) (Object) ((Number) obj).longValue();
        }
        if ((desiredClass.equals(Float.TYPE) || desiredClass.equals(Float.class)) && Number.class.isAssignableFrom(objClass)) {
            return (T) (Object) ((Number) obj).floatValue();
        }
        if ((desiredClass.equals(Double.TYPE) || desiredClass.equals(Double.class)) && Number.class.isAssignableFrom(objClass)) {
            return (T) (Object) ((Number) obj).doubleValue();
        }
        if ((desiredClass.equals(Short.TYPE) || desiredClass.equals(Short.class)) && Number.class.isAssignableFrom(objClass)) {
            return (T) (Object) ((Number) obj).shortValue();
        }
		if (desiredClass.equals(Date.class)) {
			if (objClass.equals(LocalTime.class)) {
				return (T) new Date(getMillis((LocalTime) obj));
			} else if (objClass.equals(LocalDateTime.class)) {
				return (T) ((LocalDateTime) obj).toDateTime().toDate();
			}
		} else if (desiredClass.equals(Calendar.class)) {
			if (objClass.equals(LocalTime.class)) {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(getMillis((LocalTime) obj));
				return (T) cal;
			} else if (objClass.equals(LocalDateTime.class)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(((LocalDateTime) obj).toDateTime().toDate());
				return (T) cal;
			}
		} else if (desiredClass.equals(Time.class)) {
			if (objClass.equals(LocalTime.class)) {
				return (T) new Time(getMillis((LocalTime) obj));
			} else if (objClass.equals(LocalDateTime.class)) {
				return (T)new Time(((LocalDateTime) obj).toDateTime().getMillis());
			}
		} else if (desiredClass.equals(java.sql.Date.class)) {
			if (objClass.equals(LocalTime.class)) {
				return (T) new java.sql.Date(getMillis((LocalTime) obj));
			} else if (objClass.equals(LocalDateTime.class)) {
				return (T) new java.sql.Date(((LocalDateTime) obj).toDateTime().getMillis());
			}
		} else if (desiredClass.equals(java.sql.Timestamp.class)) {
			if (objClass.equals(LocalTime.class)) {
				return (T) new Timestamp(getMillis((LocalTime) obj));
			} else if (objClass.equals(LocalDateTime.class)) {
				return (T) new Timestamp(((LocalDateTime) obj).toDateTime().getMillis());
			}
		}        
        
        throw new UnsupportedOperationException(String.format("Unable to convert %s into %s", objClass.getName(), desiredClass.getName()));
    }
        
     private static long getMillis(LocalTime localTime) {
    	 return new LocalDateTime(localTime.getMillisOfDay(), 
    			 	DateTimeZone.UTC).toDateTime().getMillis();
     }
}
