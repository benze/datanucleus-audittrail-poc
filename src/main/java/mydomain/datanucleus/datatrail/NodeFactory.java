package mydomain.datanucleus.datatrail;

import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.datatrail.nodes.NodePriority;
import org.datanucleus.metadata.MetaData;

import java.util.Optional;

public interface NodeFactory {
    /**
     * Identifies if this factory supports / can produce a node with the given parameters
     *
     * @param action
     * @param value the object to be represented by a DataTrail node
     * @param md the metadata relating to the given object
     * @return
     */
    boolean supports(NodeAction action, Object value, MetaData md);

    /**
     * Factory method to create a node with the given parameters.
     * @param value the object to be represented by a DataTrail node
     * @param md the metadata relating to the given object
     * @param parent the parent node for this node.  Null if this is supposed to be the root of the tree
     * @return Only produces a node if the factory can create it
     */
    Optional<Node> create(NodeAction action, Object value, MetaData md, Node parent);

    default Node createNode(Object value, NodeAction action, MetaData md, Node parent){
        return create(action, value,  md, parent).get();
    };

    /**
     * Type of node implemented by the class
     */
    default NodeType type() {
        NodeDefinition nodeDefn = this.getClass().getAnnotation(NodeDefinition.class);
        return nodeDefn == null ? null : nodeDefn.type();
    }


    /**
     * Type of action implemented by the class
     * @return
     */
    default NodeAction[] action(){
        NodeDefinition nodeDefn = this.getClass().getAnnotation(NodeDefinition.class);
        return nodeDefn == null ? null : nodeDefn.action();
    }


    /**
     * The priority of the node generated by the class.  By default, the priority is '0'.
     * @return
     */
    default int priority(){
        NodePriority nodePriority = this.getClass().getAnnotation(NodePriority.class);
        return nodePriority == null ? 0 : nodePriority.priority();
    }
}
