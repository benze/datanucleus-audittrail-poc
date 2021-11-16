package mydomain.datanucleus.datatrail.nodes.map;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.nodes.BaseNode;
import mydomain.datanucleus.datatrail.nodes.Updatable;

import java.util.Arrays;

/**
 * Class to represent the key/value information found in the map.  Must not implement a Map.MapEntry as Jackson will automatically serialize those
 * differently
 */
public class MapEntry extends BaseNode implements Updatable {
    Node key;

    public MapEntry(final Node key, final Node value) {
        super(null, null, null);
        this.key = key;
        this.value = value;
    }

    @JsonProperty("key")
    public Node getKey() {
        return key;
    }

    @JsonProperty("value")
    @Override
    public Node getValue() {
        return (Node) value;
    }

    @Override
    public void updateFields() {
        Arrays.asList(key, value).stream().filter(Updatable.class::isInstance).forEach( node -> ((Updatable)node).updateFields());
    }
}
