package mydomain.datanucleus.datatrail.nodes.create;


import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.ReferenceNode;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.datatrail.nodes.Updatable;
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
@NodeDefinition(type=NodeType.ENTITY, action = Node.Action.CREATE)
public class Entity extends ReferenceNode {


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
        ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();

        // need to include all loaded fields
        String[] fieldNames = NucleusJDOHelper.getLoadedFields( pc, pm);
        for(String fieldName : fieldNames) {
            int position = op.getClassMetaData().getAbsolutePositionOfMember(fieldName);
            Object field = op.provideField(position);
            AbstractMemberMetaData mmd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);
            if( mmd.isFieldToBePersisted()){
                fields.add(NodeFactory.getInstance().createNode( field, Action.CREATE, mmd, this));
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
        fields.stream().filter(node -> node instanceof Updatable).forEach(node -> ((Updatable)node).updateFields());
    }
}