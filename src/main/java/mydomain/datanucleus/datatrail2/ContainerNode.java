package mydomain.datanucleus.datatrail2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail2.Node;
import org.datanucleus.metadata.AbstractMemberMetaData;

import java.util.ArrayList;
import java.util.Collection;

abstract public class ContainerNode extends Node {

    protected Collection<Node> added = new ArrayList<>();
    protected Collection<Node> removed = new ArrayList<>();

    protected ContainerNode(AbstractMemberMetaData mmd, Node parent) {
        super(mmd, parent);
    }

    @JsonIgnore
    @Override
    public String getValue() {
        return null;
    }

    public Collection<? extends Node> getAdded() {
        return added;
    }

    public Collection<? extends Node> getRemoved() {
        return removed;
    }

    @JsonIgnore
    @Override
    public String getClassName() {
        return super.getClassName();
    }
}
