package mydomain.datanucleus.datatrail.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail.Node;

public interface ReferenceNode extends Node {
    @JsonProperty("id")
    Object getValue();

    @JsonProperty("version")
    String getVersion();

    @JsonProperty("desc")
    String getDescription();
}
