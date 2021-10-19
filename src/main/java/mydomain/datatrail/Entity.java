package mydomain.datatrail;

import com.fasterxml.jackson.annotation.JsonProperty;
import mydomain.datanucleus.ExtendedReferentialStateManagerImpl;
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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Entity extends Node {

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
    protected WeakReference<Persistable> source = new WeakReference<>(null);


    /**
     * Default delegator to a create/update Entity
     * @param pc
     */
    public Entity(Persistable pc) {
        this(pc, null);
    }

    public Entity(Persistable pc, Action action){
        ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();
        this.source = new WeakReference<>(pc);

        setId(pc);
        this.action = action;
        this.className = op.getClassMetaData().getFullClassName();
        setVersion(pc);
        this.dateModified = Instant.now();

        if( pc instanceof ITrailDesc){
            description = ((ITrailDesc)pc).minimalTxtDesc();
        }

        // set the fields for the entity
        switch(action){
            case CREATE:
                setCreateFields(pc);
                break;
            case DELETE:
                setDeleteFields(pc);
                break;
            case UPDATE:
                setUpdateFields(pc);
        }
    }


//    /**
//     *
//     * @param pc
//     * @param delete true if pc is being deleted.  False otherwise (ie: saved/updated/etc).
//     */
//    public Entity(Persistable pc, boolean delete) {
//        ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();
//
//        // if the object is being deleted, the constructor must be called before the object is actually deleted from the
//        // datastore, and before the object's state has registered it as being "in deletion", or will be impossible to retrieve
//        if( delete ){
//            action = Action.DELETE;
//        } else {
//            // use the lifecycle to identify if the object is being created or updated
//            // TODO use JDOHelper.getObjectState() instead of DN LifecycleState
//            setAction(op.getLifecycleState());
//        }
//        setId(pc);
//
//        this.className = op.getClassMetaData().getFullClassName();
//        this.version = pc.dnGetVersion() != null ? pc.dnGetVersion().toString() : null;
//        this.dateModified = Instant.now();
//
//        if( pc instanceof ITrailDesc){
//            description = ((ITrailDesc)pc).minimalTxtDesc();
//        }
//
//        // set the fields for the entity
//        switch(action){
//            case CREATE:
//                setCreateFields(pc);
//                break;
//            case DELETE:
//                setDeleteFields(pc);
//                break;
//            case UPDATE:
//                throw new UnsupportedOperationException("UPDATE not yet supported");
//        }
//    }


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

        if( objectId == null ) {
            id = null;
        } else if(IdentityUtils.isDatastoreIdentity( objectId ) ) {
            id = ((DatastoreId) objectId).getKeyAsObject().toString();
        } else {
            id = objectId.toString();
        }
    }


    private void setVersion(Persistable pc){
        if( JDOHelper.getVersion(pc) == null ){
            this.version = null;
        } else {
            this.version = JDOHelper.getVersion(pc).toString();
        }
    }


    /**
     * Identifies which fields need to be set
     * @param pc
     */
    private void setCreateFields(Persistable pc){
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
                    fields.add(Field.newField(field, null, (FieldMetaData) ammd));
                } else {
                    NucleusLogger.GENERAL.debug("No FieldMetaData found for " + ammd.getFullFieldName() + ".  Was " + ammd.getClass().getName() + ".  IsToBePersisted: " + ammd.isFieldToBePersisted() + ". Skipping field");
                }
            }
        }
    }


    /**
     * Identifies which fields need to be set when an object is being deleted
     * @param pc
     */
    private void setDeleteFields(Persistable pc){
        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();
        ExtendedReferentialStateManagerImpl op = (ExtendedReferentialStateManagerImpl)pc.dnGetStateManager();

        if( action == Action.DELETE){
            // need to include all loaded fields
            int[] absoluteFieldPositions = op.getClassMetaData().getAllMemberPositions();
            for(int position : absoluteFieldPositions) {
                Object field = op.provideSavedField(position);
                AbstractMemberMetaData ammd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);

                if(ammd instanceof FieldMetaData && ammd.isFieldToBePersisted()) {
                    // only add persistable fields to the list of fields
                    fields.add(Field.newField(field, null, (FieldMetaData) ammd));
                } else {
                    NucleusLogger.GENERAL.debug("No FieldMetaData found for " + ammd.getFullFieldName() + ".  Was " + ammd.getClass().getName() + ".  IsToBePersisted: " + ammd.isFieldToBePersisted() + ". Skipping field");
                }
            }
        }
    }



    /**
     * Identifies which fields need to be set when an object is being deleted
     * @param pc
     */
    private void setUpdateFields(Persistable pc){
        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();
        ExtendedReferentialStateManagerImpl op = (ExtendedReferentialStateManagerImpl)pc.dnGetStateManager();

        if( action == Action.UPDATE){
            // need to include all dirty fields
            int[] absoluteFieldPositions = op.getDirtyFieldNumbers();
            if( absoluteFieldPositions != null ) {
                for (int position : absoluteFieldPositions) {
                    Object field = op.provideField(position);
                    Object prevField = op.provideSavedField(position);
                    AbstractMemberMetaData ammd = op.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(position);

                    if (ammd instanceof FieldMetaData && ammd.isFieldToBePersisted()) {
                        // only add persistable fields to the list of fields
                        fields.add(Field.newField(field, prevField, (FieldMetaData) ammd));
                    } else {
                        NucleusLogger.GENERAL.debug("No FieldMetaData found for " + ammd.getFullFieldName() + ".  Was " + ammd.getClass().getName() + ".  IsToBePersisted: " + ammd.isFieldToBePersisted() + ". Skipping field");
                    }
                }
            }
        }
    }


    /**
     * Method is used to scan through each field to ensure that they have the correct data after the store process (ex: missing ObjectId, other preStore handlers, etc)
     */
    public void updateFieldsPostStore(){
        setId(source.get());
        setVersion(source.get());
        fields.stream().forEach(field -> field.updateValue());
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
