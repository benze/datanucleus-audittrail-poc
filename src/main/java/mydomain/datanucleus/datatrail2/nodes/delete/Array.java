package mydomain.datanucleus.datatrail2.nodes.delete;

import mydomain.datanucleus.datatrail2.ContainerNode;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeFactory;
import mydomain.datanucleus.datatrail2.NodeType;
import org.datanucleus.metadata.AbstractMemberMetaData;

public class Array extends ContainerNode {
    @Override
    public NodeType getType() {
        return NodeType.ARRAY;
    }

    @Override
    public Action getAction() {
        return Action.DELETE;
    }

    public Array(Object value, AbstractMemberMetaData mmd, Node parent) {
        super(mmd, parent);

        // value might be null, in which case there is nothing left to do
        if( value == null ){
            return;
        }

        addElements((Object[])value);
    }

    /**
     * Adds all the elements in the collection
     * @param elements
     */
    private void addElements( Object[] elements ){
        // all new values, so use the raw collection values
        for(Object element : elements )
            this.removed.add(NodeFactory.getInstance().createNode(element, getAction(), null, this));
    }
}
