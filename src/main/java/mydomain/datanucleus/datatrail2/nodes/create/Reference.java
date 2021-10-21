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
    public Reference(Persistable value, AbstractMemberMetaData mmd, Node parent){
        super(value, mmd, parent);

        if( mmd != null )
            this.name = mmd.getName();
    }


}
