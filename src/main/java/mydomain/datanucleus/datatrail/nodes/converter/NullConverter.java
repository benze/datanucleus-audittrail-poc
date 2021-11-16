package mydomain.datanucleus.datatrail.nodes.converter;

import mydomain.datanucleus.datatrail.nodes.Priority;
import mydomain.datanucleus.datatrail.nodes.StringConverter;

/**
 * Null converter.  Returns a null for a null object
 */
@Priority(priority = Priority.LOWEST_PRECEDENCE)
public class NullConverter implements StringConverter {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == null;
    }

    @Override
    public String getAsString(Object value) {
        return null;
    }
}
