package mydomain.datanucleus.datatrail.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mydomain.datanucleus.datatrail.ClassUtils;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

import java.util.Arrays;

/**
 * Base class to represent a Node in the DataTrail object graph
 * Each BaseNode will be exported as a JSON object using Jackson
 *
 * @author Eric Benzacar
 */
public abstract class BaseNode implements Node {
    private final Node parent;
    protected NodeType type;

    protected String name;
    protected String className;
    protected Object value;
    protected Object prev;
    protected MetaData md;

    // reference to the factory that created the node
    protected NodeFactory factory;


    /**
     * Constructor delegation by the subclasses
     * @param md
     * @param parent
     */
    protected BaseNode(final MetaData md, final Node parent, final NodeFactory factory) {
        this.parent = parent;
        this.md = md;
        this.factory = factory;
        if( md == null ) {
            return;
        }

        if(md instanceof AbstractClassMetaData) {
            className = ((AbstractClassMetaData) md).getFullClassName();
        } else if( md instanceof AbstractMemberMetaData){
            final AbstractMemberMetaData mmd = (AbstractMemberMetaData)md;
            // need to get the wrapped name of the class to avoid primitives
            className = ClassUtils.wrap(mmd.getType()).getTypeName();
            name = mmd.getName();
        }
    }


    /**
     * Sets the classname if not already set
     * @param value
     */
    protected void setClassName(final Object value, final boolean override ){
        // if no value, then nothing to do
        if( value == null )
            return;

        // if classname already set and not asking to override, then done
        if( !override && className != null )
            return;

        className = ClassUtils.getClass(value).getName();
    }



    /**
     * returns the type of node represented by this object
     * @return
     */
    @Override
    public NodeType getType(){
        final NodeDefinition nodeDefn = getClass().getAnnotation(NodeDefinition.class);
        return nodeDefn == null ? null : nodeDefn.type();
    }

    /**
     * Returns the action for {@link NodeType#ENTITY} objects.
     * @return action for {@link NodeType#ENTITY} objects.  Null otherwise
     */
    @Override
    public NodeAction getAction(){
        final NodeDefinition nodeDefn = getClass().getAnnotation(NodeDefinition.class);
        return nodeDefn == null ? null : Arrays.stream(nodeDefn.action()).findFirst().orElse(null);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Object getPrev() {
        return prev;
    }


    /**
     * Sets the previous value of this node
     */
    public void setPrev(final Object value){
        // by default, do nothing
    }


    /**
     * Recursive function to find a node in the tree with the a factory defined
     * @return
     */
    @JsonIgnore
    protected NodeFactory getFactory(){
        // if a factory is defined, return it
        if( factory != null ){
            return factory;
        }

        // return the parent factory if a parent exists
        return parent instanceof BaseNode ? ((BaseNode)parent).getFactory() : null;
    }
}
