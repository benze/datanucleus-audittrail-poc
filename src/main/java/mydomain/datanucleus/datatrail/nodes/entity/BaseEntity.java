package mydomain.datanucleus.datatrail.nodes.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.ReferenceNode;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.datatrail.nodes.NodeFactory;
import mydomain.datanucleus.datatrail.nodes.Updatable;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.state.ObjectProvider;

import javax.jdo.PersistenceManager;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Definition of an Entity that is being Created
 */
abstract public class BaseEntity extends ReferenceNode {
    protected Set<Node> fields = new HashSet<Node>();
    protected Instant dateModified;
    protected String username;

    /**
     * Default constructor.  Should only be called via the DataTrailFactory
     * @param value
     * @param md
     * @param parent
     */
    protected BaseEntity(Persistable value, MetaData md, Node parent, NodeFactory factory){
        // an entity is the root node in the tree
        super(value, md,null);
        this.factory = factory;
        setFields(value);
        dateModified = Instant.now();
    }


    abstract protected void setFields(Persistable pc);

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

    @Override
    public boolean canProcess(Object value, MetaData md) {
        // can process any Persitable object that is passed as a class
        return value instanceof Persistable && md instanceof AbstractClassMetaData;
    }


    /**
     * Returns the action for {@link NodeType#ENTITY} objects.
     * @return action for {@link NodeType#ENTITY} objects.  Null otherwise
     */
    public Action getAction(){
        NodeDefinition nodeDefn = this.getClass().getAnnotation(NodeDefinition.class);
        return nodeDefn == null ? null : Arrays.stream(nodeDefn.action()).findFirst().orElse(null);
    }
}
