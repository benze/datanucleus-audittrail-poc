package mydomain.datanucleus.datatrail2;

public class ClassUtils {


    /**
     * Helper method to return the Class for a given primitive class.
     * Works with non-primitive Classes as well - simply returns the original Class
     * @param clazz
     * @return
     */
    static public Class<?> wrap(Class<?> clazz) {
        if( clazz == null )
            return null;

        if (!clazz.isPrimitive())
            return clazz;

        if (clazz == Integer.TYPE)
            return Integer.class;
        if (clazz == Long.TYPE)
            return Long.class;
        if (clazz == Boolean.TYPE)
            return Boolean.class;
        if (clazz == Byte.TYPE)
            return Byte.class;
        if (clazz == Character.TYPE)
            return Character.class;
        if (clazz == Float.TYPE)
            return Float.class;
        if (clazz == Double.TYPE)
            return Double.class;
        if (clazz == Short.TYPE)
            return Short.class;
        if (clazz == Void.TYPE)
            return Void.class;

        return clazz;
    }



    /**
     * Null safe method to return the wrapped class of an object
     * @param object
     * @return the Class of the object.  Null if object is null
     */
    static public Class getClass(Object object){
        return object == null ? null : wrap(object.getClass());
    }
}
