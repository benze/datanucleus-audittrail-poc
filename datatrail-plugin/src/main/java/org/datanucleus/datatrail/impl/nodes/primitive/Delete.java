package org.datanucleus.datatrail.impl.nodes.primitive;

import org.datanucleus.datatrail.spi.Node;
import org.datanucleus.datatrail.spi.NodeFactory;
import org.datanucleus.datatrail.spi.NodeAction;
import org.datanucleus.datatrail.spi.NodeType;
import org.datanucleus.datatrail.impl.nodes.NodeDefinition;
import org.datanucleus.datatrail.spi.Priority;
import org.datanucleus.metadata.AbstractMemberMetaData;

@NodeDefinition(type=NodeType.PRIMITIVE, action = NodeAction.DELETE)
@Priority(priority = Priority.LOWEST_PRECEDENCE)
public class Delete extends BasePrimitive {

    /**
     * Default constructor.  Should only be called via the DataTrailFactory
     *
     * @param value
     * @param mmd
     * @param parent
     */
    protected Delete(final Object value, final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(value, mmd, parent, factory);
    }
}
