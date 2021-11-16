package mydomain.datanucleus.datatrail;

import mydomain.datanucleus.datatrail.annotation.DataTrailAnnotationHandler;
import mydomain.datanucleus.datatrail.nodes.NodeDefinition;
import mydomain.datanucleus.datatrail.nodes.StringConverter;
import org.datanucleus.metadata.MetaData;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static mydomain.datanucleus.datatrail.nodes.NodeDefinition.Helper.isSupported;

public abstract class AbstractNodeFactory implements NodeFactory {

    protected DataTrailFactory dataTrailFactory;

    public DataTrailFactory getDataTrailFactory() {
        return dataTrailFactory;
    }

    /**
     * Sets the {@link DataTrailFactory} used to create this factory.  In order to protect the structural integrity of the generated Nodes,
     * the dataTrailFactory cannot be changed once it is set.  A new factory needs to be instantiated instead.
     *
     * @param dataTrailFactory
     */
    @Override
    public void setDataTrailFactory(final DataTrailFactory dataTrailFactory) {
        if (this.dataTrailFactory != null) {
            throw new IllegalStateException("Cannot change the DataTrailFactory once it has already been set.  Create a new instance instead");
        }

        this.dataTrailFactory = dataTrailFactory;
    }

    /**
     * Checks to see if this factory can produce a node for the given parameters
     *
     * @param action
     * @param value  the object to be represented by a DataTrail node
     * @param md     the metadata relating to the given object
     * @return true if the value is not read-only and the factory supports this type of object
     */
    public boolean supports(final NodeAction action, final Object value, final MetaData md) {
        assertConfigured();
        final NodeDefinition nodeDefn = getClass().getAnnotation(NodeDefinition.class);
        return isObjectIncluded(md) && isObjectUpdatable(md) && isSupported(nodeDefn, action);
    }


    /**
     * Ensures that the {@link DataTrailFactory} is set
     */
    protected void assertConfigured() {
        if (dataTrailFactory == null) {
            throw new IllegalStateException("Factory cannot be used before the DataTrailFactory is set");
        }
    }


    /**
     * Checks to see if the object is updateable
     *
     * @param md
     * @return false if the DN Read-Only extension is enabled on either the class object or the field
     */
    protected boolean isObjectUpdatable(final MetaData md) {
        // by default, object is updatable, unless there is metadata to specify otherwise
        if (md == null) {
            return true;
        }

        final boolean classReadOnly = "true".equals(md.getValueForExtension(MetaData.EXTENSION_CLASS_READ_ONLY));
        final boolean fieldReadOnly = "false".equals(md.getValueForExtension(MetaData.EXTENSION_MEMBER_INSERTABLE)) || "false".equals(md.getValueForExtension(MetaData.EXTENSION_MEMBER_UPDATEABLE));

        // either the class or the field can be identified as read only
        return !classReadOnly && !fieldReadOnly;
    }


    /**
     * Checks to see if the object is included in the datatrail.  By default, all fields and classes are part of the data trail
     *
     * @param md
     * @return
     */
    protected boolean isObjectIncluded(final MetaData md) {
        // by default, everything is included
        if (md == null) {
            return true;
        }

        final boolean classExcluded = "true".equals(md.getValueForExtension(DataTrailAnnotationHandler.EXTENSION_CLASS_DATATRAIL_EXCLUDE));
        final boolean fieldExcluded = "true".equals(md.getValueForExtension(DataTrailAnnotationHandler.EXTENSION_MEMBER_DATATRAIL_EXCLUDE));

        return !classExcluded && !fieldExcluded;
    }

}
