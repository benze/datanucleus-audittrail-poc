package mydomain.datanucleus.datatrail2.nodes.delete;

import mydomain.datanucleus.datatrail2.ContainerNode;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeFactory;
import mydomain.datanucleus.datatrail2.NodeType;
import org.datanucleus.metadata.AbstractMemberMetaData;

public class Collection extends ContainerNode {
    @Override
    public NodeType getType() {
        return NodeType.COLLECTION;
    }

    @Override
    public Action getAction() {
        return Action.DELETE;
    }

    public Collection(Object value, AbstractMemberMetaData mmd, Node parent) {
        super(mmd, parent);

        // value might be null, in which case there is nothing left to do
        if( value == null ){
            return;
        }

        addElements((java.util.Collection) value);
    }

    /**
     * Adds all the elements in the collection
     * @param elements
     */
    private void addElements( java.util.Collection elements ){
        // all new values, so use the raw collection values
        for(Object element : elements )
            this.removed.add(NodeFactory.getInstance().createNode(element, getAction(), null, this));
    }
}
