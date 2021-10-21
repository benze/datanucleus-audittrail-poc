package mydomain.datanucleus.datatrail2.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeType;

/**
 * Class to represent the key/value information found in the map.  Must not implement a Map.MapEntry as Jackson will automatically serialize those
 * differently
 */
public class MapEntry extends Node {
    Node key;
    Node value;

    public MapEntry(Node key, Node value) {
        super(null, null);
        this.key = key;
        this.value = value;
    }

    @JsonProperty("key")
    public Node getKey() {
        return key;
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
    @Override
    public Node getValue() {
        return value;
    }
}
