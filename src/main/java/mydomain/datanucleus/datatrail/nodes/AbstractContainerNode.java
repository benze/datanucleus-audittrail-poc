package mydomain.datanucleus.datatrail.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeFactory;
import org.datanucleus.metadata.AbstractMemberMetaData;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractContainerNode extends BaseNode implements Updatable, ContainerNode {

    protected Collection<Node> added = new ArrayList<>();
    protected Collection<Node> removed = new ArrayList<>();
    protected Collection<Node> changed = new ArrayList<>();
    protected Collection<Node> contents = new ArrayList<>();

    protected AbstractContainerNode(final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(mmd, parent, factory);
    }

    @JsonIgnore
    @Override
    public String getValue() {
        return null;
    }

    @Override
    public Collection<Node> getAdded() {
        return added;
    }

    @Override
    public Collection<Node> getRemoved() {
        return removed;
    }

    @Override
    public Collection<Node> getChanged() {
        return changed;
    }

    @Override
    public Collection<Node> getContents() {
        return contents;
    }

    @JsonIgnore
    @Override
    public String getClassName() {
        return super.getClassName();
    }

    @Override
    public void updateFields() {
        added.stream().filter(Updatable.class::isInstance).forEach( node -> ((Updatable)node).updateFields());
        removed.stream().filter(Updatable.class::isInstance).forEach( node -> ((Updatable)node).updateFields());
        changed.stream().filter(Updatable.class::isInstance).forEach( node -> ((Updatable)node).updateFields());
        contents.stream().filter(Updatable.class::isInstance).forEach( node -> ((Updatable)node).updateFields());
    }
}
