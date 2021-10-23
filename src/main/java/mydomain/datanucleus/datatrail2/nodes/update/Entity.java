package mydomain.datanucleus.datatrail2.nodes.update;


import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.ExtendedReferentialStateManagerImpl;
import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeFactory;
import mydomain.datanucleus.datatrail2.NodeType;
import mydomain.datanucleus.datatrail2.ReferenceNode;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

import javax.jdo.PersistenceManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Definition of an Entity that is being Created
 */
public class Entity extends ReferenceNode {

    protected Instant dateModified;
    protected String username;
    private Set<Node> fields = new HashSet<Node>();
    /**
     * Default constructor.  Should only be called via the NodeFactory
     *
     * @param value
     * @param md
     * @param parent
     */
    public Entity(Persistable value, MetaData md, Node parent) {
        // an entity is the root node in the tree
        super(value, md, null);
        setFields(value);
        dateModified = Instant.now();
    }

    @Override
    public NodeType getType() {
        return NodeType.ENTITY;
    }

    @Override
    public Action getAction() {
        return Action.UPDATE;
    }

    @JsonProperty
    public Set<Node> getFields() {
        return fields;
    }

    private void setFields(Persistable pc) {
        if (pc == null)
            return;

        PersistenceManager pm = (PersistenceManager) pc.dnGetExecutionContext().getOwner();
        ExtendedReferentialStateManagerImpl op = (ExtendedReferentialStateManagerImpl) pc.dnGetStateManager();

        // need to include all dirty fields
        int[] absoluteFieldPositions = op.getDirtyFieldNumbers() != null ? op.getDirtyFieldNumbers() : new int[]{};
        for (int position : absoluteFieldPositions) {
            Object field = op.provideField(position);
            Object prevField = op.provideSavedField(position);
            AbstractMemberMetaData mmd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);

            if (mmd.isFieldToBePersisted()) {
                Node current = NodeFactory.getInstance().createNode(field, getAction(), mmd, this);
                current.setPrev(NodeFactory.getInstance().createNode(prevField, getAction(), mmd, this));
                fields.add(current);
            }
        }
    }

    @JsonProperty
    public Instant getDateModified() {
        return dateModified;
    }

    @JsonProperty("user")
    public String getUsername() {
        return username;
    }

    @Override
    public void updateFields() {
        super.updateFields();
        fields.stream().forEach(node -> node.updateFields());
    }
}
