package mydomain.datanucleus.datatrail.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail.Node;

import java.util.Collection;

public interface ContainerNode extends Node{
    @JsonProperty("added")
    Collection<Node> getAdded();

    @JsonProperty("removed")
    Collection<Node> getRemoved();

    @JsonProperty("changed")
    Collection<Node> getChanged();

    @JsonProperty("contents")
    Collection<Node> getContents();
}
