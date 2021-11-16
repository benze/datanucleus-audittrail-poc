package mydomain.datanucleus.datatrail.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;

import java.time.Instant;
import java.util.Set;

public interface EntityNode extends ReferenceNode{
    @JsonProperty
    Set<Node> getFields();

    @JsonProperty
    Instant getDateModified();

    @JsonProperty("user")
    String getUsername();

    @JsonProperty("txId")
    String getTransactionId();

    @JsonIgnore(false)
    @JsonProperty("action")
    NodeAction getAction();
}
