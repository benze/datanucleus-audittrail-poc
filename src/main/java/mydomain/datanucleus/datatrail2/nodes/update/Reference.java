package mydomain.datanucleus.datatrail2.nodes.update;

import mydomain.datanucleus.datatrail2.Node;
import mydomain.datanucleus.datatrail2.NodeType;
import mydomain.datanucleus.datatrail2.ReferenceNode;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;

public class Reference extends ReferenceNode {

    @Override
    public NodeType getType() {
        return NodeType.REF;
    }

    @Override
    public Action getAction() {
        return Action.UPDATE;
    }


    /**
     * Default constructor.  Should only be called via the NodeFactory
     * @param value
     * @param mmd
     * @param parent
     */
    public Reference(Persistable value, AbstractMemberMetaData mmd, Node parent){
        super(value, mmd, parent);

        if( mmd != null )
            this.name = mmd.getName();
    }

    @Override
    public void setPrev(Object value) {
        // previous must be of same type
        if( value != null && value.getClass() != this.getClass()){
            throw new IllegalArgumentException( "Previous value is not of the same type: " + value.getClass().getName() + " !=" + this.getClass().getName());
        }

        this.prev = this.getClass().cast(value).getValue();
    }
}