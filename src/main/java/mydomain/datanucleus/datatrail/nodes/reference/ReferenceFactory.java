package mydomain.datanucleus.datatrail.nodes.reference;

import mydomain.datanucleus.datatrail.AbstractNodeFactory;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

import java.util.Optional;

@NodeDefinition(type = NodeType.PRIMITIVE, action = {NodeAction.CREATE, NodeAction.UPDATE, NodeAction.DELETE})
public class ReferenceFactory extends AbstractNodeFactory {

    @Override
    public boolean supports(final NodeAction action, final Object value, final MetaData md) {
        // either the is persistent, or the field is supposed to be persistable (ex: if the value is null)
        return super.supports(action, value, md) &&
                (value instanceof Persistable ||
                        md instanceof AbstractMemberMetaData && Persistable.class.isAssignableFrom(((AbstractMemberMetaData) md).getType())
                );
    }

    @Override
    public Optional<Node> createNode(final NodeAction action, final Object value, final MetaData md, final Node parent) {
        assertConfigured();
        if (!supports(action, value, md))
            return dataTrailFactory.createNode(value, action, md, parent );

        final Persistable pc = (Persistable) value;

        switch (action) {
            case CREATE:
                return Optional.of(new Create(pc, (AbstractMemberMetaData) md, parent, this));
            case DELETE:
                return Optional.of(new Delete(pc, (AbstractMemberMetaData) md, parent, this));
            case UPDATE:
                return Optional.of(new Update(pc, (AbstractMemberMetaData) md, parent, this));
            default:
                return Optional.empty();
        }
    }
}
