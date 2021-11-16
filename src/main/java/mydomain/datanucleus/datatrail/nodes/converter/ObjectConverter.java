package mydomain.datanucleus.datatrail.nodes.converter;

import mydomain.datanucleus.datatrail.nodes.Priority;
import mydomain.datanucleus.datatrail.nodes.StringConverter;

/**
 * Default converter.  Can convert any object by using the toString() method of the object
 */
@Priority(priority = Priority.LOWEST_PRECEDENCE)
public class ObjectConverter implements StringConverter {
    @Override
    public boolean supports(Class<?> clazz) {
        return Object.class.isAssignableFrom(clazz);
    }

    @Override
    public String getAsString(Object value) {
        return value == null ? null : value.toString();
    }
}
