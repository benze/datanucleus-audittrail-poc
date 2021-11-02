package mydomain.datanucleus.datatrail.nodes.collection;

import mydomain.datanucleus.datatrail.AbstractNodeFactory;
import mydomain.datanucleus.datatrail.DataTrailFactory;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

import java.util.Optional;

@NodeDefinition(type = NodeType.COLLECTION, action = {Node.Action.CREATE, Node.Action.UPDATE, Node.Action.DELETE})
public class CollectionFactory extends AbstractNodeFactory {
    public CollectionFactory(DataTrailFactory dataTrailFactory) {
        super(dataTrailFactory);
    }

    @Override
    public boolean supports(Node.Action action, Object value, MetaData md) {
        // can process any field that is identified as a collection
        return super.supports(action, value, md)
                && md instanceof AbstractMemberMetaData
                && ((AbstractMemberMetaData) md).hasCollection();

    }

    @Override
    public Optional<Node> create(Node.Action action, Object value, MetaData md, Node parent) {
        if (!supports(action, value, md))
            return Optional.empty();

        switch (action) {
            case CREATE:
                return Optional.of(new Create(value, (AbstractMemberMetaData) md, parent));
            case DELETE:
                return Optional.of(new Delete(value, (AbstractMemberMetaData) md, parent));
            case UPDATE:
                return Optional.of(new Update(value, (AbstractMemberMetaData) md, parent));
            default:
                return Optional.empty();
        }
    }
}
