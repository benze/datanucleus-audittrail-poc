package mydomain.datanucleus.datatrail.nodes.collection;

import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.types.wrappers.tracker.ChangeTrackable;
import mydomain.datanucleus.types.wrappers.tracker.ChangeTracker;
import org.datanucleus.metadata.AbstractMemberMetaData;

import java.util.stream.Collectors;

@NodeDefinition(type=NodeType.COLLECTION, action = NodeAction.UPDATE)
public class Update extends BaseCollection {
    protected Update(Object value, AbstractMemberMetaData mmd, Node parent) {
        super(value, mmd, parent);
    }

    /**
     * Adds all the elements in the collection
     * @param collection
     */
    @Override
    protected void addElements( java.util.Collection collection ){
        if( collection instanceof ChangeTrackable && ((ChangeTrackable)collection).getChangeTracker().isTracking()){
            ChangeTracker changeTracker = ((ChangeTrackable)collection).getChangeTracker();
            added = (java.util.Collection<Node>) changeTracker.getAdded().stream().map(o -> getFactory().createNode(NodeAction.UPDATE, o, null, this).get()).collect(Collectors.toList());
            removed = (java.util.Collection<Node>) changeTracker.getRemoved().stream().map(o -> getFactory().createNode(NodeAction.UPDATE, o, null, this).get()).collect(Collectors.toList());
        } else {
            for (Object element : collection) {
                getFactory().createNode(NodeAction.UPDATE, element, null, this).ifPresent(node -> contents.add(node));
            }
        }
    }

}
