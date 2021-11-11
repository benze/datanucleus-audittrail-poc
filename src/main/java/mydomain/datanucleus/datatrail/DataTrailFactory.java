package mydomain.datanucleus.datatrail;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import mydomain.audit.DataTrail;
import mydomain.datanucleus.datatrail.nodes.BaseNode;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.state.ObjectProvider;

import java.util.Comparator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Factory responsible for instantiating a node type based on requirements
 */
public class DataTrailFactory {
    // get a static slf4j logger for the class
    protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataTrailFactory.class);

    static final private DataTrailFactory instance = new DataTrailFactory();

    private Set<NodeFactory> factories = ConcurrentHashMap.newKeySet();

    /**
     * Protected constructor
     */
    private DataTrailFactory() {

    }


    /**
     * Automatically scans for any non-abstract {@link NodeFactory} implementations and attempts to initialize them
     *
     * Requires the optional dependency {@code io.github.classgraph:classgraph} to be present in the classpath
     *
     * @param classPackageToScan package of class to scan for any implementations
     */
    public void registerNodeFactories(Class<?> classPackageToScan) {
        synchronized (this) {
            try (ScanResult scanResult = new ClassGraph()
                    .enableAnnotationInfo()
                    .acceptPackages(classPackageToScan.getPackage().getName())
                    .scan()) {

                scanResult.getClassesImplementing(NodeFactory.class)
                        .filter(classInfo -> !classInfo.isAbstract())
                        .stream()
                        .map(ClassInfo::loadClass)
                        .forEach(clazz -> registerFactory((Class<NodeFactory>) clazz));
            }
        }
    }

    /**
     * Uses the service loader pattern to load all implementations of the {@link NodeFactory} declared in
     * META-INF/services/mydomain.datanucleus.datatrail.NodeFactory files found within the classpath
     */
    public void registerNodeFactories() {
        ServiceLoader<NodeFactory> serviceLoader = ServiceLoader.load(NodeFactory.class);
        serviceLoader.forEach(nodeFactory -> registerFactory((Class<NodeFactory>) nodeFactory.getClass()));
    }

    /**
     * Register a {@link NodeFactory} to the DataTrail factory.  This will make all the types exposed by the factory class
     * accessible to the DataTrail factory.  The {@link NodeFactory} class must supply a no-arg public constructor
     *
     * @param factory
     */
    public void registerFactory(Class<NodeFactory> factory) {
        synchronized (this) {
            try {
                NodeFactory instance = factory.newInstance();
                instance.setDataTrailFactory(this);
                factories.add(instance);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Retrieve an instance of the factory
     * By default, use the ServiceLoader pattern to automatically add any {@link NodeFactory} declared in
     * {@code META-INF/services/mydomain.datanucleus.datatrail.NodeFactory}
     *
     * @return
     */
    static public DataTrailFactory getDataTrailFactory() {
        DataTrailFactory factory = new DataTrailFactory();
        factory.registerNodeFactories();
        return factory;
    }

    /**
     * Retrieve an instance of the factory and automatically add any {@link NodeFactory} classes found in any child package
     * of the {@code classPackageToScan} parameter.
     *
     * Requires the optional dependency {@code io.github.classgraph:classgraph} to be present in the classpath
     *
     * @param classPackageToScan package of class to scan for any implementations
     */
    static public DataTrailFactory getDataTrailFactory(Class<?> classPackageToScan) {
        DataTrailFactory factory = new DataTrailFactory();
        factory.registerNodeFactories(classPackageToScan);
        return factory;
    }

    /**
     * Retrieve a root node from the factory. Should only be used for persistable objects
     *
     * @param value
     * @param action
     * @return
     */
    public Node createNode(Object value, NodeAction action) {
        // make sure it is a persistable object
        if (!(value instanceof Persistable)) {
            return null;
        }

        MetaData md = ((ObjectProvider) ((Persistable) value).dnGetStateManager()).getClassMetaData();


        return createNode(value, action, md, null).orElse(null);
    }


    /**
     * Retrieve a node from the factory
     *
     * @param value
     * @param md
     * @param parent
     * @return
     * @throws RuntimeException if unable to create the node
     */
    public Optional<Node> createNode(Object value, NodeAction action, MetaData md, Node parent) {

        // find the factory for this type of value and use it to create the node
        return factories.stream().filter(nodeFactory -> nodeFactory.supports(action, value, md))
                .sorted(Comparator.comparingInt(NodeFactory::priority))
                .findFirst()
                .map( nodeFactory -> nodeFactory.createNode(action, value, md, parent ).orElseGet(() -> {
                    logger.debug( "Unable to find a node factory to support {}/{}", value.getClass().getCanonicalName(), action);
                    return null;
                }));
    }

}
