package mydomain.datanucleus.datatrail2.nodes.create;

import mydomain.datanucleus.datatrail2.ClassUtils;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeType;
import org.datanucleus.metadata.AbstractMemberMetaData;

public class Primitive extends Node {

    @Override
    public NodeType getType() {
        return NodeType.PRIMITIVE;
    }

    @Override
    public Action getAction() {
        return Action.CREATE;
    }



    /**
     * Default constructor.  Should only be called via the NodeFactory
     * @param value
     * @param mmd
     * @param parent
     */
    public Primitive(Object value, AbstractMemberMetaData mmd, Node parent){
        // an entity is the root node in the tree
        super(mmd, parent);
        this.value = value == null ? null : value.toString();
        setClassName(value, false);
    }
}
