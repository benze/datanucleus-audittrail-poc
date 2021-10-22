package mydomain.datanucleus.datatrail2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.state.ObjectProvider;

abstract public class Node {

    public enum Action{
        CREATE,
        UPDATE,
        DELETE
    };

    final private Node parent;
    protected NodeType type;

    protected String name;
    protected String className;
    protected Object value;
    protected Object prev;
    protected MetaData md;


    /**
     * Constructor delegation by the subclasses
     * @param md
     * @param parent
     */
    protected Node(MetaData md, Node parent) {
        this.parent = parent;
        this.md = md;
        if( md == null ) {
            return;
        }

        if(md instanceof AbstractClassMetaData) {
            className = ((AbstractClassMetaData) md).getFullClassName();
        } else if( md instanceof AbstractMemberMetaData){
            AbstractMemberMetaData mmd = (AbstractMemberMetaData)md;
            // need to get the wrapped name of the class to avoid primitives
            className = ClassUtils.wrap(mmd.getType()).getTypeName();
            name = mmd.getName();
        }
    }


    /**
     * Recursive function to find the closest ObjectProvider in the tree
     * @param node
     * @return
     */
    private ObjectProvider getObjectProvider(Node node){
        if( node == null ){
            return null;
        } else if( ReferenceNode.class.isAssignableFrom(node.getClass()) && ((ReferenceNode) node).getSource() != null){
            ReferenceNode refNode = (ReferenceNode) node;
            return (ObjectProvider)refNode.getSource().dnGetStateManager();
        } else {
            return getObjectProvider(node.getParent());
        }
    }


    /**
     * Sets the classname if not already set
     * @param value
     */
    protected void setClassName( Object value, boolean override ){
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
    abstract public NodeType getType();

    /**
     * returns the type of action supported by this object
     * @return
     */
    abstract public Node.Action getAction();


    public String getName() {
        return name;
    }

    @JsonProperty("class")
    public String getClassName() {
        return className;
    }

    @JsonProperty("value")
    public Object getValue() {
        return value;
    }

    @JsonProperty("prevValue")
    public Object getPrev() {
        return prev;
    }

    @JsonIgnore
    public Node getParent() {
        return parent;
    }


    public void updateFields(){

    }
}
