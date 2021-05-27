package mydomain.audit;

import org.datanucleus.ExecutionContext;
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

public class AuditListener implements CreateLifecycleListener,
    DeleteLifecycleListener, LoadLifecycleListener, StoreLifecycleListener
{
    PersistenceManager pm = null;

    public AuditListener(PersistenceManager pm)
    {
        this.pm = pm;
    }

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
        String[] dirtyFields = NucleusJDOHelper.getDirtyFields(pc, pm);
        NucleusLogger.GENERAL.info("Audit : preStore for " +
            pc.dnGetObjectId() + " dirtyFields=" + StringUtils.objectArrayToString(dirtyFields));
        if (dirtyFields != null && dirtyFields.length > 0)
        {
            ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
            ObjectProvider op = ec.findObjectProvider(pc);
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

    public void postStore(InstanceLifecycleEvent event)
    {
        NucleusLogger.GENERAL.info("Audit : postStore for " +
            ((Persistable)event.getSource()).dnGetObjectId());
    }
}
