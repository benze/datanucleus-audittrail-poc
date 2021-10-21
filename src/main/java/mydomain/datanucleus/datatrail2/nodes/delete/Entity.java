package mydomain.datanucleus.datatrail2.nodes.delete;


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
import org.datanucleus.state.ObjectProvider;

import javax.jdo.PersistenceManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Definition of an Entity that is being Created
 */
public class Entity extends ReferenceNode {

    @Override
    public NodeType getType() {
        return NodeType.ENTITY;
    }

    @Override
    public Action getAction() {
        return Action.DELETE;
    }


    private Set<Node> fields = new HashSet<Node>();
    protected Instant dateModified;
    protected String username;

    /**
     * Default constructor.  Should only be called via the NodeFactory
     * @param value
     * @param md
     * @param parent
     */
    public Entity(Persistable value, MetaData md, Node parent){
        // an entity is the root node in the tree
        super(value, md,null);
        setFields(value);
        dateModified = Instant.now();
    }


    private void setFields(Persistable pc){
        if( pc == null )
            return;

        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();
        ExtendedReferentialStateManagerImpl op = (ExtendedReferentialStateManagerImpl)pc.dnGetStateManager();

        // need to include all loaded fields
        String[] fieldNames = NucleusJDOHelper.getLoadedFields( pc, pm);
        for(String fieldName : fieldNames) {
            int position = op.getClassMetaData().getAbsolutePositionOfMember(fieldName);
            Object field = op.provideSavedField(position);
            AbstractMemberMetaData mmd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);
            if( mmd.isFieldToBePersisted()){
                fields.add(NodeFactory.getInstance().createNode( field, getAction(), mmd, this));
            }
        }
    }


    @JsonProperty
    public Set<Node> getFields() {
        return fields;
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
        fields.stream().forEach( node -> node.updateFields());
    }
}
