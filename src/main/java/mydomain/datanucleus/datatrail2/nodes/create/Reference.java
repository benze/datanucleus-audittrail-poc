package mydomain.datanucleus.datatrail2.nodes.create;

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
        return Action.CREATE;
    }


    /**
     * Default constructor.  Should only be called via the NodeFactory
     * @param value
     * @param mmd
     * @param parent
     */
    public Reference(Object value, AbstractMemberMetaData mmd, Node parent){
        super((Persistable) value, mmd, parent);
        if(!(value instanceof Persistable)){
            throw new IllegalArgumentException("This class only accepts " + Persistable.class.getName());
        }

        if( value != null ) {
            this.value = value.toString();
            setDescription(value);
        }

        if( mmd != null )
            this.name = mmd.getName();
    }


}
