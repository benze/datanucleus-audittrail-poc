package mydomain.datanucleus.datatrail.nodes.array;

import mydomain.datanucleus.datatrail.AbstractNodeFactory;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

import java.util.Optional;

@NodeDefinition(type = NodeType.ARRAY, action = {NodeAction.CREATE, NodeAction.UPDATE, NodeAction.DELETE})
public class ArrayFactory extends AbstractNodeFactory {

    @Override
    public boolean supports(final NodeAction action, final Object value, final MetaData md) {
        // can process any field that is identified as an array
        return super.supports(action, value, md)
                && md instanceof AbstractMemberMetaData
                && ((AbstractMemberMetaData) md).hasArray();
    }

    @Override
    public Optional<Node> createNode(final NodeAction action, final Object value, final MetaData md, final Node parent) {
        assertConfigured();
        if (!supports(action, value, md))
            return Optional.empty();

        switch(action){
            case CREATE:
                return Optional.of(new Create( value, (AbstractMemberMetaData) md, parent));
            case DELETE:
                return Optional.of(new Delete( value, (AbstractMemberMetaData) md, parent));
            case UPDATE:
                return Optional.of(new Update( value, (AbstractMemberMetaData) md, parent));
            default:
                return Optional.empty();
        }
    }
}
