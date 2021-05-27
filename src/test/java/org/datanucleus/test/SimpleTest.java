package org.datanucleus.test;

import java.util.*;
import org.junit.*;
import javax.jdo.*;

import static org.junit.Assert.*;
import mydomain.model.*;
import mydomain.audit.AuditListener;
import org.datanucleus.util.NucleusLogger;

public class SimpleTest
{
    @Test
    public void testSimple()
    {
        NucleusLogger.GENERAL.info(">> test START");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("MyTest");

        // Create of object
        PersistenceManager pm = pmf.getPersistenceManager();
        AuditListener audit = new AuditListener(pm);
        pm.addInstanceLifecycleListener(audit, null);
        Transaction tx = pm.currentTransaction();
        tx.setSynchronization(audit);
        try
        {
            tx.begin();

            Person p = new Person(1, "First Person");
            pm.makePersistent(p);

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
        audit = new AuditListener(pm);
        pm.addInstanceLifecycleListener(audit, null);
        tx = pm.currentTransaction();
        tx.setSynchronization(audit);
        try
        {
            tx.begin();

            Person p = pm.getObjectById(Person.class, 1);
            p.setName("Second Person");

            tx.commit();

            NucleusLogger.GENERAL.info(">> non-tx update");
            p.setName("Third Person");
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
