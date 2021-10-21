package mydomain.datanucleus.datatrail2.nodes.delete;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail2.ContainerNode;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeFactory;
import mydomain.datanucleus.datatrail2.NodeType;
import mydomain.datanucleus.datatrail2.nodes.MapEntry;
import org.datanucleus.metadata.AbstractMemberMetaData;

import java.util.Set;

public class Map extends ContainerNode {

    @Override
    public NodeType getType() {
        return NodeType.MAP;
    }

    @Override
    public Action getAction() {
        return Action.DELETE;
    }

    public Map(java.util.Map value, AbstractMemberMetaData mmd, Node parent) {
        super(mmd, parent);

        // value might be null, in which case there is nothing left to do
        if( value == null ){
            return;
        }

        addElements(value.entrySet());
    }

    /**
     * Adds all the elements in the collection
     * @param elements
     */
    private void addElements( Set<java.util.Map.Entry> elements ){
        // all new values, so use the raw collection values
        for( java.util.Map.Entry element : elements){
            Node key = NodeFactory.getInstance().createNode(element.getKey(), getAction(), null, this);
            Node value = NodeFactory.getInstance().createNode(element.getValue(), getAction(), null, this);

            this.removed.add(new MapEntry(key, value));
        }
    }
}
