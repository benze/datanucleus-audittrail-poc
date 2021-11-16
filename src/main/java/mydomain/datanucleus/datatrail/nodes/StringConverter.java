package mydomain.datanucleus.datatrail.nodes;

/**
 * Identifies a converter for converting a given Class to a String
 * Each converter can specify a {@link Priority} if multiple converters are found for the same class
 *
 */
public interface StringConverter {

    /**
     * Identifies if the converter can convert the given class
     * @param clazz the class to be converted.  A null value is represented by a {@code null} clazz value
     * @return
     */
    boolean supports(Class<?> clazz);

    /**
     * Returns the object as a String
     * @param value
     * @return
     */
    String getAsString( Object value );


    /**
     * The priority of the converter of the class.  By default, the priority is '0'.
     *
     * @return
     */
    default int priority() {
        final Priority nodePriority = getClass().getAnnotation(Priority.class);
        return nodePriority == null ? 0 : nodePriority.priority();
    }

}
