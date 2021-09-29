package mydomain.audit;

import mydomain.datatrail.Entity;
import org.datanucleus.ExecutionContext;
import org.datanucleus.TransactionEventListener;
import org.datanucleus.enhancement.Persistable;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.api.jdo.JDOPersistenceManager;

import javax.jdo.*;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import java.util.Stack;

/**
 * Class that provides suitable hooks for auditing of a persistence process.
 *
 * This implementation simply logs the audit events.
 */
public class AuditListener implements CreateLifecycleListener,
    DeleteLifecycleListener, LoadLifecycleListener, StoreLifecycleListener, TransactionEventListener
{

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


    pub
    public void postCreate(InstanceLifecycleEvent event)
    {
        NucleusLogger.GENERAL.info("Audit : create for " +
            ((Persistable)event.getSource()).dnGetObjectId());
    }

    public void preDelete(InstanceLifecycleEvent event)
    {
        NucleusLogger.GENERAL.info("Audit : preDelete for " +
            ((Persistable)event.getSource()).dnGetObjectId());
    }

    public void postDelete(InstanceLifecycleEvent event)
    {
        NucleusLogger.GENERAL.info("Audit : postDelete for " +
            ((Persistable)event.getSource()).dnGetObjectId());
    }

    public void postLoad(InstanceLifecycleEvent event)
    {
        NucleusLogger.GENERAL.info("Audit : load for " +
            ((Persistable)event.getSource()).dnGetObjectId());
    }

    public void preStore(InstanceLifecycleEvent event)
    {
        Persistable pc = (Persistable)event.getSource();
        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();
        String[] dirtyFields = NucleusJDOHelper.getDirtyFields(pc, pm);
        NucleusLogger.GENERAL.info("Audit : preStore for " +
            pc.dnGetObjectId() + " dirtyFields=" + StringUtils.objectArrayToString(dirtyFields));
        if (dirtyFields != null && dirtyFields.length > 0)
        {
            ObjectProvider op = (ObjectProvider)pc.dnGetStateManager();
            if (op != null)
            {
                for (int i=0;i<dirtyFields.length;i++)
                {
                    int position = op.getClassMetaData().getAbsolutePositionOfMember(dirtyFields[i]);
                    Object value = op.provideField(position);
                    NucleusLogger.GENERAL.info(">> field=" + dirtyFields[i] + " position=" + position + " value=" + value);
                }
            }
        }
    }

    public void postStore(InstanceLifecycleEvent event) {
        Persistable pc = (Persistable)event.getSource();
        PersistenceManager pm = (PersistenceManager)pc.dnGetExecutionContext().getOwner();

        if (!(event.getSource() instanceof Persistable)) {
            NucleusLogger.GENERAL.debug("Nothing to do. No persistable object found. : " + event.getSource().getClass().getName());
            return;
        }
        modifications.put(pc, new Entity(pc, action));
    }

    public void transactionStarted()
    {
        NucleusLogger.GENERAL.info("Audit : TXN START");
    }
    public void transactionEnded() {}
    public void transactionPreFlush() {}
    public void transactionFlushed() {}

    public void transactionPreCommit()
    {
        NucleusLogger.GENERAL.info("Audit : TXN PRE-COMMIT");
    }

    public void transactionCommitted()
    {
        NucleusLogger.GENERAL.info("Audit : TXN COMMITTED");
        logger.info(modifications.toString());

    }

    public void transactionPreRollBack() {}
    public void transactionRolledBack() {}
    public void transactionSetSavepoint(String name) {}
    public void transactionReleaseSavepoint(String name) {}
    public void transactionRollbackToSavepoint(String name) {}

    public Collection<Entity> getModifications() {
        return modifications.values();
    }
}
