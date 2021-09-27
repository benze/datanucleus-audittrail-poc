package mydomain.datatrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datatrail.field.Field;
import mydomain.model.ITrailDesc;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.identity.DatastoreId;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.util.NucleusLogger;

import javax.jdo.PersistenceManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Entity {

    public enum Action{
        CREATE,
        UPDATE,
        DELETE
    }

    protected Action action;
    protected String className;
    protected String id;
    protected String version;
    protected List<Field> fields = new ArrayList<>();
    protected String username;
    protected Instant dateModified;
    protected String description;



    public Entity(Persistable pc) {
        ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();
        setAction( op.getLifecycleState() );
        setId(pc);

        this.className = op.getClassMetaData().getFullClassName();
        this.version = pc.dnGetVersion() != null ? pc.dnGetVersion().toString() : null;
        this.dateModified = Instant.now();

        if( pc instanceof ITrailDesc){
            description = ((ITrailDesc)pc).minimalTxtDesc();
        }

        setFields(pc);
    }


    /**
     * Sets the action based on the LifeCycleState of the persistable object
     * @param lc
     */
    private void setAction(LifeCycleState lc){
        // set the action
        if( lc.isNew() ){
            action = Action.CREATE;
        } else if (lc.isDeleted()){
            action = Action.DELETE;
        } else if( lc.isDirty()){
            action = Action.UPDATE;
        }
    }

    /**
     * Helper method to set the Id based on the the type of identity of the persistable object.
     * Supports application-id and datastore identity
     * @param pc
     */
    private void setId( Persistable pc){
        Object objectId = pc.dnGetObjectId();

        if(IdentityUtils.isDatastoreIdentity( objectId ) ) {
            id = ((DatastoreId) objectId).getKeyAsObject().toString();
        } else {
            id = objectId.toString();
        }
    }



    /**
     * Identifies which fields need to be set
     * @param pc
     */
    private void setFields( Persistable pc){
        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();
        ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();

        if( action == Action.CREATE){
            // need to include all loaded fields
            String[] fieldNames = NucleusJDOHelper.getLoadedFields( pc, pm);
            for(String fieldName : fieldNames) {
                int position = op.getClassMetaData().getAbsolutePositionOfMember(fieldName);
                Object field = op.provideField(position);
                AbstractMemberMetaData ammd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);

                if(ammd instanceof FieldMetaData && ammd.isFieldToBePersisted()) {
                    // only add persistable fields to the list of fields
                    fields.add(Field.newField(field, (FieldMetaData) ammd));
                } else {
                    NucleusLogger.GENERAL.debug("No FieldMetaData found for " + ammd.getFullFieldName() + ".  Was " + ammd.getClass().getName() + ".  IsToBePersisted: " + ammd.isFieldToBePersisted() + ". Skipping field");
                }
            }
        }
    }


    @Override
    public String toString() {
        return "Entity{" +
                "action=" + action +
                ", classname='" + className + '\'' +
                ", id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", fields=" + fields +
                ", username='" + username + '\'' +
                ", dateModified=" + dateModified +
                '}';
    }


    @JsonProperty
    public Action getAction() {
        return action;
    }

    @JsonProperty("class")
    public String getClassName() {
        return className;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    @JsonProperty
    public String getVersion() {
        return version;
    }

    @JsonProperty
    public List<Field> getFields() {
        return fields;
    }

    @JsonProperty("user")
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public Instant getDateModified() {
        return dateModified;
    }

    @JsonProperty("desc")
    public String getDescription() {
        return description;
    }
}
