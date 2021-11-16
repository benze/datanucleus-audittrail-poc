package mydomain.datanucleus.datatrail.nodes.map;

import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.metadata.AbstractMemberMetaData;

import java.util.Map;
import java.util.Optional;

@NodeDefinition(type=NodeType.MAP, action = NodeAction.CREATE)
public class Create extends BaseMap {

    protected Create(final Map value, final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(value, mmd, parent, factory);
    }

    /**
     * Adds all the key/value pairs in the map
     * @param map
     */
    @Override
    protected void addElements(final Map map ){
        // all new values, so use the raw collection values
        map.entrySet().stream().forEach(element -> {
            // only add the entry to the set if a node can be created for both the key and the value
            final Optional<Node> key = getFactory().createNode(NodeAction.CREATE, ((Map.Entry)element).getKey(), null, this);
            final Optional<Node> value = getFactory().createNode(NodeAction.CREATE, ((Map.Entry)element).getValue(), null, this);

            if( key.isPresent() && value.isPresent() ){
                added.add(new MapEntry(key.get(), value.get()));
            }
        });
    }
}
