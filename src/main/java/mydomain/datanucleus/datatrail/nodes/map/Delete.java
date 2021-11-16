package mydomain.datanucleus.datatrail.nodes.map;

import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.metadata.AbstractMemberMetaData;

import java.util.Map;
import java.util.Optional;

@NodeDefinition(type=NodeType.MAP, action = NodeAction.DELETE)
public class Delete extends BaseMap {
    protected Delete(final Map value, final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(value, mmd, parent, factory);
    }

    /**
     * Adds all the elements in the collection
     * @param map
     */
    @Override
    protected void addElements(final Map map ){
        // all new values, so use the raw collection values
        map.entrySet().stream().forEach(element -> {
            final Optional<Node> key = getFactory().createNode(NodeAction.DELETE, ((Map.Entry)element).getKey(), null, this);
            final Optional<Node> value = getFactory().createNode(NodeAction.DELETE, ((Map.Entry)element).getValue(), null, this);

            if( key.isPresent() && value.isPresent() ){
                removed.add(new MapEntry(key.get(), value.get()));
            }
        });
    }
}
