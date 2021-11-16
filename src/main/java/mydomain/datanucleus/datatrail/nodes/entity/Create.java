package mydomain.datanucleus.datatrail.nodes.entity;


import mydomain.datanucleus.datatrail.Node;
import mydomain.datanucleus.datatrail.NodeAction;
import mydomain.datanucleus.datatrail.NodeFactory;
import mydomain.datanucleus.datatrail.NodeType;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.state.ObjectProvider;

/**
 * Definition of an Entity that is being Created
 */
@NodeDefinition(type=NodeType.ENTITY, action = NodeAction.CREATE)
public class Create extends BaseEntity {


    /**
     * Default constructor.  Should only be called via the DataTrailFactory
     *
     * @param value
     * @param md
     * @param parent
     * @param factory
     */
    protected Create(final Persistable value, final MetaData md, final Node parent, final NodeFactory factory) {
        super(value, md, parent, factory);
    }

    @Override
    protected void setFields(final Persistable pc){
        if( pc == null )
            return;

        final ObjectProvider<Persistable> op = (ObjectProvider)pc.dnGetStateManager();

        // need to include all loaded fields
        for(final int position : op.getLoadedFieldNumbers()) {
            final Object field = op.provideField(position);
            final AbstractMemberMetaData mmd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);
            if( mmd.isFieldToBePersisted()){
                getFactory().createNode(NodeAction.CREATE, field, mmd, this).ifPresent( node -> fields.add(node));
            }
        }
    }
}
