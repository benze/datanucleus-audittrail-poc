package mydomain.audit;

import mydomain.datatrail.Entity;
import org.datanucleus.TransactionEventListener;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Class that provides suitable hooks for auditing of a persistence process.
 * <p>
 * This implementation simply logs the audit events.
 */
public class AuditListener implements CreateLifecycleListener,
        DeleteLifecycleListener, LoadLifecycleListener, StoreLifecycleListener, TransactionEventListener {

    NucleusLogger logger = NucleusLogger.getLoggerInstance("Console");

    //    Stack<Entity> modifications = new Stack<>();
    Map<Object, Entity> modifications = new HashMap<Object, Entity>() {
        @Override
        public Entity put(Object key, Entity value) {
            if(key instanceof Persistable){
                key = ((Persistable)key).dnGetObjectId();
            }
            return super.put(key, value);
        }

        @Override
        public Entity get(Object key) {
            if(key instanceof Persistable){
                key = ((Persistable)key).dnGetObjectId();
            }
            return super.get(key);
        }
    };


    public AuditListener() {
    }

    public void postCreate(InstanceLifecycleEvent event) {
        NucleusLogger.GENERAL.info("Audit : create for " +
                ((Persistable) event.getSource()).dnGetObjectId());
    }

    public void preDelete(InstanceLifecycleEvent event) {
        Persistable pc = (Persistable) event.getSource();
        NucleusLogger.GENERAL.info("Audit : preDelete for " + pc.dnGetObjectId());
        NucleusLogger.GENERAL.info("Audit : preDelete for " + JDOHelper.getObjectState(pc));

        if (!(event.getSource() instanceof Persistable) || JDOHelper.isDetached(event.getPersistentInstance())) {
            NucleusLogger.GENERAL.debug("Nothing to do. No persistable object found. : " + event.getSource().getClass().getName());
            return;
        }

        // force all fields to be loaded before deletion
        ObjectProvider op = (ObjectProvider) ((Persistable) event.getPersistentInstance()).dnGetStateManager();


        // in Optimistic locking mode, the listener is called 2x.
        // event listener called for objects in lifecycle states:
        // - persistent-deleted (just before the object is permanently deleted)
        // - other depending on the state of the object


        // if the object isn't dirty, then need to force the OP to save the initial state of the object
        if (!JDOHelper.isDirty(pc)) {
            op.saveFields();
        }

        // load any extra fields before deletion
        op.loadUnloadedFields();

        modifications.put(event.getSource(), new Entity(pc, Entity.Action.DELETE));


//        if( !JDOHelper.isDirty(pc)){
//            ObjectProvider op = (ObjectProvider)((Persistable)event.getPersistentInstance()).dnGetStateManager();
//            // ensure the original object is loaded in memory and stored internally before being triggered for deletion
//            op.refresh();
//            op.loadUnloadedFields();
//            op.saveFields();
//        }

//        ObjectProvider op = (ObjectProvider)((Persistable)event.getPersistentInstance()).dnGetStateManager();
//        // ensure the original object is loa
//        op.saveFields();

    }

    public void postDelete(InstanceLifecycleEvent event) {
//        Persistable pc = (Persistable)event.getSource();
//        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();
//        ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();
//        int[] absoluteFieldPositions = op.getClassMetaData().getAllMemberPositions();
//
//        for( int fieldPosition : absoluteFieldPositions ){
//            Object obj = op.provideField(fieldPosition);
//        }
//
//        boolean[] loadedFields = op.getLoadedFields();
//        for( int i = 0; i < loadedFields.length; i++){
//            op.getClassMetaData().getAbsoluAbsolutePositionOfMember(i);
//        }
//

        NucleusLogger.GENERAL.info("Audit : postDelete for " +
                ((Persistable) event.getSource()).dnGetObjectId());
//        modifications.push(new Entity((Persistable)event.getSource()));

    }

    public void postLoad(InstanceLifecycleEvent event) {
        NucleusLogger.GENERAL.info("Audit : load for " +
                ((Persistable) event.getSource()).dnGetObjectId());
    }

    public void preStore(InstanceLifecycleEvent event) {
        Persistable pc = (Persistable) event.getSource();
        PersistenceManager pm = (PersistenceManager) pc.dnGetExecutionContext().getOwner();
        String[] dirtyFields = NucleusJDOHelper.getDirtyFields(pc, pm);
        NucleusLogger.GENERAL.info("Audit : preStore for " +
                pc.dnGetObjectId() + " dirtyFields=" + StringUtils.objectArrayToString(dirtyFields));
        if (dirtyFields != null && dirtyFields.length > 0) {
            ObjectProvider op = (ObjectProvider) pc.dnGetStateManager();
            if (op != null) {
                for (int i = 0; i < dirtyFields.length; i++) {
                    int position = op.getClassMetaData().getAbsolutePositionOfMember(dirtyFields[i]);
                    Object value = op.provideField(position);
                    NucleusLogger.GENERAL.info(">> field=" + dirtyFields[i] + " position=" + position + " value=" + value);
                }
            }
        }

        // postStore called for both new objects and updating objects, so need to determine which is the state of the object
        Entity.Action action = pc.dnGetStateManager().isNew(pc) ? Entity.Action.CREATE : Entity.Action.UPDATE;
        if( !JDOHelper.isNew(pc) && JDOHelper.isDirty(pc)) {
            modifications.put(pc, new Entity(pc, Entity.Action.UPDATE));
        }

    }

    public void postStore(InstanceLifecycleEvent event) {
        Persistable pc = (Persistable) event.getSource();
        PersistenceManager pm = (PersistenceManager) pc.dnGetExecutionContext().getOwner();

        if (!(event.getSource() instanceof Persistable)) {
            NucleusLogger.GENERAL.debug("Nothing to do. No persistable object found. : " + event.getSource().getClass().getName());
            return;
        }

        // postStore called for both new objects and updating objects, so need to determine which is the state of the object
        Entity.Action action = pc.dnGetStateManager().isNew(pc) ? Entity.Action.CREATE : Entity.Action.UPDATE;

        if( JDOHelper.isNew(pc) ) {
            modifications.put(pc, new Entity(pc, action));
        }
    }

    public void transactionStarted() {
        NucleusLogger.GENERAL.info("Audit : TXN START");
    }

    public void transactionEnded() {
    }

    public void transactionPreFlush() {
    }

    public void transactionFlushed() {
    }

    public void transactionPreCommit() {
        NucleusLogger.GENERAL.info("Audit : TXN PRE-COMMIT");
    }

    public void transactionCommitted() {
        NucleusLogger.GENERAL.info("Audit : TXN COMMITTED");
        logger.info(modifications.toString());

    }

    public void transactionPreRollBack() {
    }

    public void transactionRolledBack() {
    }

    public void transactionSetSavepoint(String name) {
    }

    public void transactionReleaseSavepoint(String name) {
    }

    public void transactionRollbackToSavepoint(String name) {
    }

    public Collection<Entity> getModifications() {
        return modifications.values();
    }
}
