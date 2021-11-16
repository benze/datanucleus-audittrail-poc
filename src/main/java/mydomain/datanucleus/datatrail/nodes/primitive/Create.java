package mydomain.datanucleus.datatrail.nodes.primitive;

import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.datatrail.nodes.Priority;
import org.datanucleus.metadata.AbstractMemberMetaData;

@NodeDefinition(type=NodeType.PRIMITIVE, action = NodeAction.CREATE)
@Priority(priority = Priority.LOWEST_PRECEDENCE)
public class Create extends BasePrimitive{

    /**
     * Default constructor.  Should only be called via the DataTrailFactory
     *
     * @param value
     * @param mmd
     * @param parent
     */
    protected Create(final Object value, final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(value, mmd, parent, factory);
    }
}
