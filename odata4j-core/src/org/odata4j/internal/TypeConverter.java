package org.odata4j.internal;

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

        throw new UnsupportedOperationException(String.format("Unable to convert %s into %s", objClass.getName(), desiredClass.getName()));
    }
}
