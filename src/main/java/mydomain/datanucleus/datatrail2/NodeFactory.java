package mydomain.datanucleus.datatrail2;

import mydomain.datanucleus.datatrail2.nodes.create.Array;
import mydomain.datanucleus.datatrail2.nodes.create.Collection;
import mydomain.datanucleus.datatrail2.nodes.create.Entity;
import mydomain.datanucleus.datatrail2.nodes.create.Primitive;
import mydomain.datanucleus.datatrail2.nodes.create.Reference;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.state.ObjectProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.datanucleus.util.ClassUtils.getConstructorWithArguments;


/**
 * Singleton factory responsible for instantiating a node type based on requirements
 */
public class NodeFactory {
    // get a static slf4j logger for the class
    protected static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NodeFactory.class);

    static final private NodeFactory instance = new NodeFactory();

    private Map<Node.Action,Map<NodeType, Class<? extends Node>>> nodeTypes = new HashMap<>();

    private NodeFactory() {
        // initialize the map
        Arrays.stream(Node.Action.values()).forEach(action -> nodeTypes.put(action, new HashMap<>()));

        // scan through all availabe nodes and add them to the list
        registerNodes();
    }

    /**
     * Helper function to register all types of nodes that are accessible to this factory
     */
    private void registerNodes(){
        nodeTypes.get(Node.Action.CREATE).put(NodeType.ENTITY, Entity.class);
        nodeTypes.get(Node.Action.CREATE).put(NodeType.PRIMITIVE, Primitive.class);
        nodeTypes.get(Node.Action.CREATE).put(NodeType.REF, Reference.class);
        nodeTypes.get(Node.Action.CREATE).put(NodeType.COLLECTION, Collection.class);
        nodeTypes.get(Node.Action.CREATE).put(NodeType.ARRAY, Array.class);
    }


    /**
     * Retrieve the singleton
     * @return
     */
    static public NodeFactory getInstance() {
        return instance;
    }


    /**
     * Retrieve a root node from the factory. Should only be used for persistable objects
     * @param value
     * @param action
     * @return
     */
    public Node createRootNode(Object value, Node.Action action){
        if( !( value instanceof Persistable) ) {
            return null;
        }

        MetaData md = ((ObjectProvider)((Persistable)value).dnGetStateManager()).getClassMetaData();

        return createNode(value, action, md, null);
    }


        // TODO Fix the exception handling when calling the constructor
    /**
     * Retrieve a node from the factory
     * @param value
     * @param md
     * @param parent
     * @return
     * @throws RuntimeException if unable to create the node
     */
    public Node createNode(Object value, Node.Action action, MetaData md, Node parent){
        // get the map by action
        Map<NodeType, Class<? extends Node>> nodes = nodeTypes.get(action);
        NodeType type = getType(value, md, parent);
        Class clazz = nodes.get(type);
        if( clazz == null ){
            throw new IllegalArgumentException("No such type/action supported: " + type + " / " + action);
        }

        Constructor ctor = getConstructorWithArguments(clazz, new Class[]{ClassUtils.getClass(value), ClassUtils.getClass(md), ClassUtils.getClass(parent)});
        try {
            return (Node)ctor.newInstance(value, md, parent);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    // TODO move this logic to the individual Node implementation to determine if they can handle it or not
    /**
     * Determine the type of node needed for the given object
     * @param value
     * @return
     */
    private NodeType getType(Object value, MetaData md, Node parent){
        if( parent == null ){
            // by definition, the root object MUST be an Entity type
            return NodeType.ENTITY;
        }

        // anything other than the root must have field metadata
        AbstractMemberMetaData mmd = (AbstractMemberMetaData)md;

        // if the object is a peristable object, it must be a REF
        if( value instanceof Persistable){
            return NodeType.REF;
        }

        // if the object is not a persistable object, need to find if it is a special container (ie: collection / Map)
        if(mmd.hasArray() ){
            logger.warn("Unable to track changes to objects with arrays. {}.{}", mmd.getClassName(), mmd.getName());
            return NodeType.ARRAY;
        }

        if( mmd.hasCollection() ){
            return NodeType.COLLECTION;
        }

        if( mmd.hasMap() ){
            return NodeType.MAP;
        }

        // default case, treat as primitive field
        return NodeType.PRIMITIVE;
    }

}
