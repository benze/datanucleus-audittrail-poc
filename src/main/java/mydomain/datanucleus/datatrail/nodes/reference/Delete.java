package mydomain.datanucleus.datatrail.nodes.reference;

import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;

@NodeDefinition(type=NodeType.REF, action = NodeAction.DELETE)
public class Delete extends BaseReference {
    /**
     * Default constructor.  Should only be called via the DataTrailFactory
     *
     * @param value
     * @param mmd
     * @param parent
     */
    protected Delete(final Persistable value, final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(value, mmd, parent, factory);
    }
}
