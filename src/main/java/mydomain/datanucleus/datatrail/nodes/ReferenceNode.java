package mydomain.datanucleus.datatrail.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.datatrail.BaseNode;
import mydomain.model.ITrailDesc;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.identity.DatastoreId;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.MetaData;

import javax.jdo.JDOHelper;
import java.lang.ref.WeakReference;

abstract public class ReferenceNode extends BaseNode implements Updatable {

    final protected WeakReference<Persistable> source;
    protected String version;
    protected String description;

    public ReferenceNode(Persistable source, MetaData mmd, BaseNode parent) {
        super(mmd, parent);
        this.source = new WeakReference(source);
        setId(source);
        setVersion(source);
        setDescription(source);
        setClassName(source, false);
//        if( className == null && source != null ){
//            className = ClassUtils.getClass(source).getName();
//        }
    }

    /**
     * Helper method to set the Id based on the the type of identity of the persistable object.
     * Supports application-id and datastore identity
     * @param pc
     */
    protected void setId(Persistable pc){
        if( pc == null )
            return;

        Object objectId = pc.dnGetObjectId();

        if( objectId == null ) {
            value = null;
        } else if(IdentityUtils.isDatastoreIdentity( objectId ) ) {
            value = ((DatastoreId) objectId).getKeyAsObject().toString();
        } else {
            value = objectId.toString();
        }
    }

    protected void setVersion(Persistable pc){
        if( JDOHelper.getVersion(pc) == null ){
            this.version = null;
        } else {
            this.version = JDOHelper.getVersion(pc).toString();
        }
    }

    protected void setDescription(Object field){
        if( field instanceof ITrailDesc){
            description = ((ITrailDesc)field).minimalTxtDesc();
        }
    }

    @Override
    public void updateFields() {
        setId(getSource());
        setVersion(getSource());
    }

    @JsonProperty("id")
    @Override
    public Object getValue() {
        return super.getValue();
    }

    @Override
    public Object getPrev() {
        return super.getPrev();
    }

    @JsonIgnore
    public Persistable getSource() {
        return source.get();
    }

    @JsonProperty
    public String getVersion() {
        return version;
    }

    @JsonProperty("desc")
    public String getDescription() {
        return description;
    }


}
