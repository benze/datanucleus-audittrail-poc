package org.datanucleus.test;

import org.datanucleus.identity.DatastoreIdImplKodo;
import org.junit.*;
import javax.jdo.*;

import static org.junit.Assert.*;
import mydomain.model.*;
import mydomain.audit.AuditListener;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.api.jdo.JDOTransaction;

public class SimpleTest
{
    @Test
    public void testSimple()
    {
        NucleusLogger.GENERAL.info(">> test START");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("MyTest");

        // Create of object
        PersistenceManager pm = pmf.getPersistenceManager();
        AuditListener audit = new AuditListener();
        pm.addInstanceLifecycleListener(audit, null);
        Transaction tx = pm.currentTransaction();
        ((JDOTransaction)tx).registerEventListener(audit);
        try
        {
            tx.begin();

            Student p = new Student("First Student");
            p.setAddress(new Address(new Street[]{new Street("Regina")}));
            pm.makePersistent(p);
            p.setAddress( new Address(new Street[]{new Street("Victoria")}));

            tx.commit();
        }
        catch (Throwable thr)
        {
            NucleusLogger.GENERAL.error(">> Exception in test", thr);
            fail("Failed test : " + thr.getMessage());
        }
        finally 
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            pm.close();
        }
        pmf.getDataStoreCache().evictAll();

        // Update of field
        pm = pmf.getPersistenceManager();
        audit = new AuditListener();
        pm.addInstanceLifecycleListener(audit, null);
        tx = pm.currentTransaction();
        ((JDOTransaction)tx).registerEventListener(audit);
        try
        {
            tx.begin();

            Object id = new DatastoreIdImplKodo( Student.class.getName(), 1);

            Student p = pm.getObjectById(Student.class, id);
            p.getName();
            p.setName("Second Student");

            pm.flush();
            pm.deletePersistent(p);
            pm.flush();

            tx.commit();

            NucleusLogger.GENERAL.info(">> non-tx update");
            p.setName("Third Student");
            NucleusLogger.GENERAL.info(">> non-tx update done");
        }
        catch (Throwable thr)
        {
            NucleusLogger.GENERAL.error(">> Exception in test", thr);
            fail("Failed test : " + thr.getMessage());
        }
        finally 
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            pm.close();
        }

        pmf.close();
        NucleusLogger.GENERAL.info(">> test END");
    }
}
