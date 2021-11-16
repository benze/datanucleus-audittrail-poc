package mydomain.datanucleus.datatrail.nodes.reference;

import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;

@NodeDefinition(type=NodeType.REF, action = NodeAction.UPDATE)
public class Update extends BaseReference {

    /**
     * Default constructor.  Should only be called via the DataTrailFactory
     *
     * @param value
     * @param mmd
     * @param parent
     */
    protected Update(final Persistable value, final AbstractMemberMetaData mmd, final Node parent, final NodeFactory factory) {
        super(value, mmd, parent, factory);
    }

    @Override
    public void setPrev(final Object value) {
        // previous must be of same type
        if( value != null && value.getClass() != getClass()){
            throw new IllegalArgumentException( "Previous value is not of the same type: " + value.getClass().getName() + " !=" + getClass().getName());
        }

        prev = value;
    }

}
