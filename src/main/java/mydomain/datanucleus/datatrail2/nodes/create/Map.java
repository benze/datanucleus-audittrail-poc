package mydomain.datanucleus.datatrail2.nodes.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail2.ContainerNode;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeFactory;
import mydomain.datanucleus.datatrail2.NodeType;
import org.datanucleus.metadata.AbstractMemberMetaData;

import java.util.Set;

public class Map extends ContainerNode {

    /**
     * Class to represent the key/value information found in the map.  Must not implement a Map.Entry as Jackson will automatically serialize those
     * differently
     */
    class Entry extends Node{
        Node mapKey;
        Node mapValue;

        public Entry(Node mapKey, Node mapValue) {
            super(null, null);
            this.mapKey = mapKey;
            this.mapValue = mapValue;
        }

        @JsonProperty("key")
        public Node getMapKey() {
            return mapKey;
        }

        @Override
        public NodeType getType() {
            return null;
        }

        @Override
        public Action getAction() {
            return null;
        }

        @JsonProperty("value")
        public Node getMapValue() {
            return mapValue;
        }
    }


    @Override
    public NodeType getType() {
        return NodeType.MAP;
    }

    @Override
    public Action getAction() {
        return Action.CREATE;
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

            this.added.add(new Entry(key, value));
        }
    }
}
