package mydomain.datanucleus.datatrail.nodes.entity;


import mydomain.datanucleus.ExtendedReferentialStateManagerImpl;
import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;

/**
 * Definition of an Entity that is being Created
 */
@NodeDefinition(type=NodeType.ENTITY, action = NodeAction.DELETE)
public class Delete extends BaseEntity {


    /**
     * Default constructor.  Should only be called via the DataTrailFactory
     *
     * @param value
     * @param md
     * @param parent
     * @param factory
     */
    protected Delete(final Persistable value, final MetaData md, final Node parent, final NodeFactory factory) {
        super(value, md, parent, factory);
    }

    @Override
    protected void setFields(final Persistable pc){
        if( pc == null )
            return;

        final ExtendedReferentialStateManagerImpl op = (ExtendedReferentialStateManagerImpl)pc.dnGetStateManager();

        // need to include all loaded fields
        for(final int position : op.getLoadedFieldNumbers()) {
            final Object field = op.provideSavedField(position);
            final AbstractMemberMetaData mmd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);
            if( mmd.isFieldToBePersisted()){
                getFactory().createNode(NodeAction.DELETE, field, mmd, this).ifPresent(node -> fields.add(node));
            }
        }
    }

}
