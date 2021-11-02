package mydomain.datanucleus.datatrail.nodes.primitive;

import mydomain.datanucleus.datatrail.AbstractNodeFactory;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.datatrail.nodes.NodePriority;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

import java.util.Optional;

@NodeDefinition(type = NodeType.PRIMITIVE, action = {NodeAction.CREATE, NodeAction.UPDATE, NodeAction.DELETE})
@NodePriority(priority = NodePriority.LOWEST_PRECEDENCE)
public class PrimitiveFactory extends AbstractNodeFactory {

    @Override
    public boolean supports(NodeAction action, Object value, MetaData md) {
        // can process any value as a primitive by using the value.toString()
        return super.supports(action, value, md);
    }

    @Override
    public Optional<Node> createNode(NodeAction action, Object value, MetaData md, Node parent) {
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
