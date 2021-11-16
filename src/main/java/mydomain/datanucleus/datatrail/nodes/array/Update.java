package mydomain.datanucleus.datatrail.nodes.array;

import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.metadata.AbstractMemberMetaData;

@NodeDefinition(type=NodeType.ARRAY, action = NodeAction.UPDATE)
public class Update extends BaseArray {

    protected Update(final Object value, final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(value, mmd, parent, factory);
    }

    /**
     * Adds all the elements in the collection
     * @param elements
     */
    @Override
    protected void addElements(final Object[] elements ){
        // all new values, so use the raw collection values
        for(final Object element : elements )
            getFactory().createNode(NodeAction.UPDATE, element, null, this).ifPresent(node -> contents.add(node));
    }
}
