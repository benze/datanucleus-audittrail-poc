package mydomain.datanucleus.datatrail.nodes.primitive;

import mydomain.datanucleus.datatrail.AbstractNodeFactory;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.datatrail.nodes.Priority;
import mydomain.datanucleus.datatrail.nodes.StringConverter;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@NodeDefinition(type = NodeType.PRIMITIVE, action = {NodeAction.CREATE, NodeAction.UPDATE, NodeAction.DELETE})
@Priority(priority = Priority.LOWEST_PRECEDENCE)
public class PrimitiveFactory extends AbstractNodeFactory {

    private Set<StringConverter> stringConverters = ConcurrentHashMap.newKeySet();
    private Map<Class<?>, StringConverter> stringConvertersCache = new ConcurrentHashMap<>();

    /**
     * Loads any String converters it finds in the classpath
     */
    public PrimitiveFactory() {
        // load any string converters from the classpath
        ServiceLoader<StringConverter> serviceLoader = ServiceLoader.load(StringConverter.class);
        serviceLoader.forEach( stringConverters::add);
    }

    @Override
    public Optional<Node> createNode(final NodeAction action, final Object value, final MetaData md, final Node parent) {
        assertConfigured();
        if (!supports(action, value, md))
            return dataTrailFactory.createNode(value, action, md, parent );

        switch(action){
            case CREATE:
                return Optional.of(new Create( value, (AbstractMemberMetaData) md, parent, this));
            case DELETE:
                return Optional.of(new Delete( value, (AbstractMemberMetaData) md, parent, this));
            case UPDATE:
                return Optional.of(new Update( value, (AbstractMemberMetaData) md, parent, this));
            default:
                return Optional.empty();
        }
    }


    /**
     * Gets the converter for the object's class
     * @param value
     * @return
     */
    private Optional<StringConverter> getConverter(Object value){
        final Class<?> clazz = value == null ? void.class : value.getClass();
        StringConverter converter;
        try {
            converter = stringConvertersCache.computeIfAbsent(clazz, aClass -> stringConverters.stream()
                    .sorted(Comparator.comparingLong(StringConverter::priority).reversed())
                    .filter(stringConverter -> stringConverter.supports(clazz))
                    .findFirst()
                    .get());
        } catch( NullPointerException e){
            converter = null;
        }

        return Optional.ofNullable(converter);
    }


    /**
     * Uses a {@link StringConverter} to convert the given object to a String
     * @param value
     * @return
     */
    public String getAsString(Object value){
        return getConverter(value).map( stringConverter -> stringConverter.getAsString(value) ).orElse(null);
    }
}
